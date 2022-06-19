package com.eit.hoppy.logtail.polling;

import com.eit.hoppy.logtail.CacheManager;
import com.eit.hoppy.logtail.LogMeta;
import com.eit.hoppy.util.FileHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

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

    public DirFilePollingThread(String dirPath) {
        super(DirFilePollingThread.class.getSimpleName(), 1000L);
        this.dirPath = dirPath;
    }

    public DirFilePollingThread(String dirPath, long period) {
        super(DirFilePollingThread.class.getSimpleName(), period);
        this.dirPath = dirPath;
    }

    @Override
    void polling() {
        loopFiles();
    }

    private void loopFiles() {
        List<File> files = FileHelper.getFileSort(dirPath);
        files.forEach(file -> {
            if (!CacheManager.isFileCached(file.getAbsolutePath())) {
                CacheManager.addFileCache(new LogMeta(file));
            }
        });
    }

}
