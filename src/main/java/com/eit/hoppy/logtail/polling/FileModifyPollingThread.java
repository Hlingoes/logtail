package com.eit.hoppy.logtail.polling;

import com.eit.hoppy.logtail.CacheManager;
import com.eit.hoppy.logtail.FileEventEnum;
import com.eit.hoppy.logtail.LogFileReader;
import com.eit.hoppy.logtail.LogMeta;
import com.eit.hoppy.util.FileHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;
import java.util.Queue;

/**
 * description: 定期扫描FILE_CACHE_MAP中文件状态，对比上一次状态（DevInode、Modify Time、Size），若发现更新则生成modify event
 *
 * @author Hlingoes
 * @date 2022/6/11 23:07
 */
public class FileModifyPollingThread extends AbstractPollingThread {
    private static Logger logger = LoggerFactory.getLogger(FileModifyPollingThread.class);

    public FileModifyPollingThread() {
        super(FileModifyPollingThread.class.getSimpleName(), 1000L);
    }

    public FileModifyPollingThread(long period) {
        super(FileModifyPollingThread.class.getSimpleName(), period);
    }

    @Override
    void polling() {
        try {
            if (!CacheManager.isFileCacheEmpty()) {
                pollingEvent();
            }
        } catch (Exception ex) {
            logger.error("polling error", ex);
        }
    }

    private void pollingEvent() {
        // 按照lastUpdateTime排序，asc
        List<LogMeta> logMetas = CacheManager.getSortedCacheLogMeta();
        for (LogMeta logMeta : logMetas) {
            // 判断文件是否存在
            if (!logMeta.getFile().exists()) {
                fileDelete(logMeta);
            } else {
                File reGetFile = new File(logMeta.getSourcePath());
                long lastModified = reGetFile.lastModified();
                String devInode = FileHelper.getFileInode(logMeta.getSourcePath());
                // 一般场景devInode可认为是识别该文件的唯一标识，若变化，说明是新增
                if (!logMeta.getDevInode().equals(devInode)) {
                    fileCreate(logMeta, lastModified, devInode);
                } else {
                    /**
                     * 文件系统对inode有回收重用的机制，在文件被删除之后，原来的inode可以被分配给新创建的文件
                     * @citation https://blog.csdn.net/wangyiyungw/article/details/85004859
                     */
                    fileModify(logMeta, lastModified);
                }
            }
            if (logger.isDebugEnabled()) {
                logger.debug("file created: {}", logMeta);
            }
        }
    }

    /**
     * description: 文件删除
     *
     * @param logMeta
     * @return void
     * @author Hlingoes 2022/6/26
     */
    private void fileDelete(LogMeta logMeta) {
        logMeta.setEventEnum(FileEventEnum.DELETE);
        Queue<LogFileReader> logFileReaders = CacheManager.getNamedLogFileReaderQueue(logMeta.getSourcePath());
        LogFileReader logFileReader = logFileReaders.poll();
        /**
         * 对于日志文件的Delete Event，若当前解析进度落后，文件虽被删除但日志未采集完成，则忽略此Delete事件；
         * 日志未采集完成，且一定时间内该Reader没有处理过Modify事件，则删除该Reader
         */
        if (logFileReader.getDeleteFlag() && logFileReader.isExpired(super.getPeriod())) {
            CacheManager.removeFileCache(logMeta.getSourcePath());
            logger.info("file delete: {}", logMeta);
        } else {
            logFileReader.setDeleteFlag(true);
            logFileReader.readLog(super.getPeriod());
            recycleModifyEvent(logMeta, logFileReader);
        }
    }

    /**
     * description: 文件变化
     *
     * @param logMeta
     * @param lastModified
     * @return void
     * @author Hlingoes 2022/6/26
     */
    private void fileModify(LogMeta logMeta, long lastModified) {
        logMeta.setEventEnum(FileEventEnum.MODIFY);
        logMeta.setLastUpdateTime(lastModified);
        fileModify(logMeta);
    }

    /**
     * description: 文件变化
     *
     * @param logMeta
     * @return void
     * @author Hlingoes 2022/6/26
     */
    private void fileModify(LogMeta logMeta) {
        // 首先根据sourcePath查找NamedLogFileReaderMap，找到该Reader所在的ReaderQueue，获取ReaderQueue的队列首部的Reader进行日志读取操作
        Queue<LogFileReader> logFileReaders = CacheManager.getLogReaderQueue(logMeta.getSourcePath());
        LogFileReader logFileReader = logFileReaders.poll();
        int signature = logMeta.calSignature();
        if (signature != logMeta.getSignature()) {
            // 日志读取时首先检查signature是否改变，若改变则认为日志被truncate写，从文件头开始读取；若signature未改变，则从readOffset处开始读取并更新readOffset
            logMeta.setSignature(signature);
            logFileReader.setReadOffset(0L);
            logFileReader.readLog(super.getPeriod());
            recycleModifyEvent(logMeta, logFileReader);
        } else if (logFileReader.finishReading() && logFileReaders.size() == 1) {
            // 若该日志文件读取完毕(readOffset == fileSize)且ReaderQueue的size == 1，则将当前ReaderQueue加入到rotateReaderMap中
            CacheManager.addRotateLogFileReader(logMeta.getDevInode(), logFileReader);
            // 继续把Modify Event push到Event队列中，触发队列后续文件的读取，进入下一循环
            if (logger.isDebugEnabled()) {
                logger.info("file read finish:{}", logMeta);
            }
        } else {
            logFileReader.readLog(super.getPeriod());
            recycleModifyEvent(logMeta, logFileReader);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("file modify: {}", logMeta);
        }
    }

    /**
     * description: 文件新增
     *
     * @param logMeta
     * @param lastModified
     * @param devInode
     * @return void
     * @author Hlingoes 2022/6/26
     */
    private void fileCreate(LogMeta logMeta, long lastModified, String devInode) {
        logMeta.setDevInode(devInode);
        logMeta.setEventEnum(FileEventEnum.CREATE);
        logMeta.setLastUpdateTime(lastModified);
        CacheManager.addCreateEvent(logMeta);
    }

    /**
     * description: 若日志文件没有读取完成，则把Modify Event push到Event队列中，进入下一循环（避免所有时间都被同一文件占用，保证日志文件读取公平性）
     *
     * @param logMeta
     * @param logFileReader
     * @return void
     * @author Hlingoes 2022/6/19
     */
    private void recycleModifyEvent(LogMeta logMeta, LogFileReader logFileReader) {
        CacheManager.addNamedLogFileReader(logMeta.getSourcePath(), logFileReader);
    }

}
