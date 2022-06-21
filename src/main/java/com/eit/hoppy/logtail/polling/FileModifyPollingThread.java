package com.eit.hoppy.logtail.polling;

import com.eit.hoppy.logtail.CacheManager;
import com.eit.hoppy.logtail.FileEventEnum;
import com.eit.hoppy.logtail.LogMeta;
import com.eit.hoppy.util.FileHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

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
                logMeta.setEventEnum(FileEventEnum.DELETE);
                CacheManager.removeFileCache(logMeta.getSourcePath());
                CacheManager.addEventQueue(logMeta);
                logger.info("file delete: {}", logMeta);
            } else {
                File reGetFile = new File(logMeta.getSourcePath());
                String devInode = FileHelper.getFileInode(logMeta.getSourcePath());
                // 一般场景devInode可认为是识别该文件的唯一标识，若变化，说明是新增
                if (!logMeta.getDevInode().equals(devInode)) {
                    logMeta.setDevInode(devInode);
                    logMeta.setEventEnum(FileEventEnum.CREATE);
                    logMeta.setLastUpdateTime(reGetFile.lastModified());
                    CacheManager.addEventQueue(logMeta);
                    logger.info("file create: {}", logMeta);
                } else {
                    /**
                     * 文件系统对inode有回收重用的机制，在文件被删除之后，原来的inode可以被分配给新创建的文件
                     * @citation https://blog.csdn.net/wangyiyungw/article/details/85004859
                     */
                    int signature = logMeta.calSignature();
                    if (signature != logMeta.getSignature()) {
                        logMeta.setSignature(signature);
                        logMeta.setEventEnum(FileEventEnum.CREATE);
                        logMeta.setLastUpdateTime(reGetFile.lastModified());
                        CacheManager.addEventQueue(logMeta);
                        logger.info("file create: {}", logMeta);
                    } else if (reGetFile.lastModified() != logMeta.getLastUpdateTime()) {
                        logMeta.setEventEnum(FileEventEnum.MODIFY);
                        logMeta.setLastUpdateTime(reGetFile.lastModified());
                        CacheManager.addEventQueue(logMeta);
                        logger.info("file modify: {}", logMeta);
                    }
                }
            }
            if (logger.isDebugEnabled()) {
                logger.debug("file created: {}", logMeta);
            }
        }
    }

}
