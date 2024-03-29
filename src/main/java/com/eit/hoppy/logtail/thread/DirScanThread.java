package com.eit.hoppy.logtail.thread;

import com.eit.hoppy.logtail.CacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * description: 负责根据用户配置定期遍历文件夹，将符合日志采集配置的文件加入到FILE_CACHE_MAP中
 *
 * @author Hlingoes
 * @date 2022/6/11 22:30
 */
public class DirScanThread extends AbstractPollingThread {
    private static Logger logger = LoggerFactory.getLogger(DirScanThread.class);

    /**
     * 需要监控的日志目录
     */
    private String dirPath;

    public DirScanThread(String dirPath, long period) {
        super(DirScanThread.class.getSimpleName(), period);
        this.dirPath = dirPath;
    }

    @Override
    void polling() {
        CacheManager.addFileCache(dirPath);
    }

}
