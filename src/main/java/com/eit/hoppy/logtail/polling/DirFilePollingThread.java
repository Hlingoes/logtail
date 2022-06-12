package com.eit.hoppy.logtail.polling;

import com.eit.hoppy.logtail.CacheManager;
import com.eit.hoppy.logtail.LogMeta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;

/**
 * description: DirFilePolling负责根据用户配置定期遍历文件夹，将符合日志采集配置的文件加入到FILE_CACHE_MAP中
 *
 * @author Hlingoes
 * @date 2022/6/11 22:30
 */
public class DirFilePollingThread extends AbstractPollingThread {
    private static Logger logger = LoggerFactory.getLogger(DirFilePollingThread.class);

    /**
     * 需要监控的日志目录
     */
    private String dirPath;
    /**
     * 轮询间隔，默认是5000毫秒
     */
    private long period = 5000L;

    public DirFilePollingThread(String dirPath) {
        super(DirFilePollingThread.class.getSimpleName());
        this.dirPath = dirPath;
    }

    public DirFilePollingThread(String dirPath, long period) {
        super(DirFilePollingThread.class.getSimpleName());
        this.dirPath = dirPath;
        this.period = period;
    }

    @Override
    void polling() {
        try {
            loopFiles();
            Thread.sleep(period);
        } catch (InterruptedException e) {
            logger.error("dir polling", e);
        }
    }

    private void loopFiles() {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(dirPath))) {
            for (Path path : stream) {
                File file = path.toFile();
                if (!CacheManager.getFileCacheMap().containsKey(file.getAbsolutePath())) {
                    CacheManager.addFileCache(new LogMeta(file));
                }
            }
        } catch (IOException | DirectoryIteratorException ex) {
            logger.warn("loop files error: {}", dirPath, ex);
        }
    }

}
