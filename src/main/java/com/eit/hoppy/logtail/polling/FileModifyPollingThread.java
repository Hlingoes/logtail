package com.eit.hoppy.logtail.polling;

import com.eit.hoppy.core.FileHelper;
import com.eit.hoppy.core.FileMeta;
import com.eit.hoppy.core.FileState;
import com.eit.hoppy.logtail.CacheManager;
import com.eit.hoppy.logtail.FileEventEnum;
import com.eit.hoppy.logtail.LogMeta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Map;

/**
 * description: 定期扫描FILE_CACHE_MAP中文件状态，对比上一次状态（DevInode、Modify Time、Size），若发现更新则生成modify event
 *
 * @author Hlingoes
 * @date 2022/6/11 23:07
 */
public class FileModifyPollingThread extends AbstractPollingThread {
    private static Logger logger = LoggerFactory.getLogger(FileModifyPollingThread.class);

    /**
     * 轮询间隔，默认是3000毫秒
     */
    private long period = 3000L;

    public FileModifyPollingThread() {
        super(FileModifyPollingThread.class.getSimpleName());
    }

    public FileModifyPollingThread(long period) {
        super(FileModifyPollingThread.class.getSimpleName());
        this.period = period;
    }

    @Override
    void polling() {
        try {
            if (CacheManager.getFileCacheMap().size() > 0) {
                for (Map.Entry<String, LogMeta> entry : CacheManager.getFileCacheMap().entrySet()) {
                    LogMeta logMeta = entry.getValue();
                    // file be rooling
                    String devInode = FileHelper.getFileInode(entry.getValue().getSourcePath());
                    if (!logMeta.getDevInode().equals(devInode)) {
                        logMeta.setDevInode(devInode);
                        logMeta.setEventEnum(FileEventEnum.CREATE);
                    } else {
                        File reGetFile = new File(entry.getValue().getSourcePath());
                        if (reGetFile.lastModified() != logMeta.getLastUpdateTime()) {
                            logMeta.setEventEnum(FileEventEnum.MODIFY);
                            logMeta.setLastUpdateTime(reGetFile.lastModified());
                            logger.info("file:{} fire changed", entry.getKey());
                        } else {
                            logMeta.setEventEnum(FileEventEnum.NULL);
                        }
                    }
                    CacheManager.getPollingEventQueue().add(logMeta);
                    //fileEventHandle.handleFile(fileMeta);
                }
            }
            Thread.sleep(period);
        } catch (Exception ex) {
            logger.error("polling error", ex);
        }
    }
}
