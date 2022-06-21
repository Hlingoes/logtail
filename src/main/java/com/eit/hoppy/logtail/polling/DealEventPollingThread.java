package com.eit.hoppy.logtail.polling;

import com.eit.hoppy.logtail.CacheManager;
import com.eit.hoppy.logtail.FileEventEnum;
import com.eit.hoppy.logtail.LogFileReader;
import com.eit.hoppy.logtail.LogMeta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Queue;

/**
 * description: 轮询处理事件
 *
 * @author Hlingoes
 * @date 2022/6/19 16:27
 */
public class DealEventPollingThread extends AbstractPollingThread {
    private static Logger logger = LoggerFactory.getLogger(DealEventPollingThread.class);

    public DealEventPollingThread() {
        super(DealEventPollingThread.class.getSimpleName(), 1000L);
    }

    public DealEventPollingThread(long period) {
        super(DealEventPollingThread.class.getSimpleName(), period);
    }

    @Override
    void polling() {
        LogMeta logMeta = CacheManager.getEventLogMeta();
        if (null == logMeta) {
            return;
        }
        if (logMeta.getEventEnum() == FileEventEnum.CREATE) {
            LogFileReader logFileReader = new LogFileReader(logMeta, getPeriod() / 3);
            CacheManager.addNamedLogFileReader(logMeta.getSourcePath(), logFileReader);
        } else if (logMeta.getEventEnum() == FileEventEnum.MODIFY) {
            // 首先根据sourcePath查找NamedLogFileReaderMap，找到该Reader所在的ReaderQueue，获取ReaderQueue的队列首部的Reader进行日志读取操作
            Queue<LogFileReader> logFileReaders = CacheManager.getLogReaderQueue(logMeta.getSourcePath());
            if (null == logFileReaders) {
                return;
            }
            Iterator<LogFileReader> iterator = logFileReaders.iterator();
            while (iterator.hasNext()) {
                LogFileReader logFileReader = iterator.next();
                int signature = logMeta.calSignature();
                if (signature != logMeta.getSignature()) {
                    // 日志读取时首先检查signature是否改变，若改变则认为日志被truncate写，从文件头开始读取；若signature未改变，则从readOffset处开始读取并更新readOffset
                    logMeta.setSignature(signature);
                    logFileReader.setReadOffset(0L);
                    logFileReader.readLog();
                    recycleIfUnfinished(logMeta, logFileReader);
                } else if (logFileReader.finishReading() && logFileReaders.size() > 1) {
                    // 若该日志文件读取完毕(readOffset == fileSize)且ReaderQueue的size > 1，则从ReaderQueue中移除该Reader并加入到rotateReaderMap中
                    CacheManager.addRotateLogFileReader(logMeta.getDevInode(), logFileReader);
                    iterator.remove();
                    // 继续把Modify Event push到Event队列中，触发队列后续文件的读取，进入下一循环
                    CacheManager.addEventQueue(logMeta);
                    logger.info("file read finish:{}", logMeta);
                } else {
                    logger.info("file start read:{}", logMeta);
                    logFileReader.readLog();
                    recycleIfUnfinished(logMeta, logFileReader);
                }
            }
        } else if (logMeta.getEventEnum() == FileEventEnum.DELETE) {
            Queue<LogFileReader> logFileReaders = CacheManager.getNamedLogFileReaderQueue(logMeta.getSourcePath());
            /**
             * 对于日志文件的Delete Event，若该Reader所在队列长度大于1（当前解析进度落后，文件虽被删除但日志未采集完成），则忽略此Delete事件；
             * 若Reader所在队列长度为1，设置该Reader的deleteFlag，若一定时间内该Reader没有处理过Modify事件且日志解析完毕则删除该Reader
             */
            if (null == logFileReaders) {
                return;
            }
            if (logFileReaders.size() > 1) {
                logger.info("file is deleted but log still reading: {}", logMeta);
                return;
            } else if (logFileReaders.size() == 1) {
                LogFileReader logFileReader = logFileReaders.element();
                if (logFileReader.getDeleteFlag() && logFileReader.finishReading()
                        && System.currentTimeMillis() > (logFileReader.getLogMeta().getLastUpdateTime() + getPeriod())) {
                    logFileReader.close();
                    logFileReaders.clear();
                } else {
                    logFileReader.setDeleteFlag(true);
                }
            }
        }
    }

    /**
     * description: 若日志文件没有读取完成，则把Modify Event push到Event队列中，进入下一循环（避免所有时间都被同一文件占用，保证日志文件读取公平性）
     *
     * @param logMeta
     * @param logFileReader
     * @return void
     * @author Hlingoes 2022/6/19
     */
    private void recycleIfUnfinished(LogMeta logMeta, LogFileReader logFileReader) {
        if (logFileReader.finishReading()) {
            logger.info("read file to the end: {}", logMeta);
        } else {
            CacheManager.addEventQueue(logMeta);
        }
    }

}
