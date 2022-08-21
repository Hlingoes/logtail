package com.eit.hoppy.logtail.thread;

import com.eit.hoppy.logtail.CacheManager;
import com.eit.hoppy.logtail.FileEventEnum;
import com.eit.hoppy.logtail.LogEvent;
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
public class FileModifyThread extends AbstractPollingThread {
    private static Logger logger = LoggerFactory.getLogger(FileModifyThread.class);

    public FileModifyThread() {
        super(FileModifyThread.class.getSimpleName(), 1000L);
    }

    public FileModifyThread(long period) {
        super(FileModifyThread.class.getSimpleName(), period);
    }

    @Override
    void polling() {
        List<LogMeta> logMetas = CacheManager.getSortedCacheLogMeta();
        logMetas.forEach(logMeta -> {
            String devInode = FileHelper.getFileInode(logMeta.getSourcePath());
            File reGetFile = new File(logMeta.getSourcePath());
            if (!reGetFile.exists()) {
                CacheManager.offerLogEvent(new LogEvent(FileEventEnum.DELETE, logMeta.getSourcePath(), devInode));
            } else {
                // 如果devInode不一样，说明文件是rotate
                if (!devInode.equals(logMeta.getDevInode())) {
                    logMeta.setDevInode(devInode);
                    logMeta.setLastModified(reGetFile.lastModified());
                    CacheManager.offerLogEvent(new LogEvent(FileEventEnum.CREATE_ROTATE, logMeta.getSourcePath(), devInode));
                } else {
                    logMeta.setLastModified(reGetFile.lastModified());
                    CacheManager.offerLogEvent(new LogEvent(FileEventEnum.MODIFY, logMeta.getSourcePath(), devInode));
                }
            }
        });
    }


}
