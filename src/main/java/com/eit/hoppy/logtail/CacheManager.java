package com.eit.hoppy.logtail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * description: 日志控制器，缓存文件和reader状态
 *
 * @author Hlingoes
 * @date 2022/6/11 22:12
 */
public class CacheManager {
    private static Logger logger = LoggerFactory.getLogger(CacheManager.class);
    /**
     * 缓存由DirFilePolling线程定时遍历用户配置的目录生成的符合条件的元数据
     * key: sourcePath
     * value: LogMeta
     */
    private static final Map<String, LogMeta> FILE_CACHE_MAP = new ConcurrentHashMap<>(10000);
    /**
     * 缓存由FileModifyPolling线程生成的Create/Modify/Delete事件
     * key: sourcePath
     * value: LogMeta
     */
    private static final BlockingQueue<LogMeta> POLLING_EVENT_QUEUE = new ArrayBlockingQueue<>(10000);

    public static boolean isFileCached(String filePath) {
        synchronized (FILE_CACHE_MAP) {
            return FILE_CACHE_MAP.containsKey(filePath);
        }
    }

    public static boolean isFileCacheEmpty() {
        synchronized (FILE_CACHE_MAP) {
            return FILE_CACHE_MAP.isEmpty();
        }
    }

    public static void addFileCache(LogMeta logMeta) {
        synchronized (FILE_CACHE_MAP) {
            FILE_CACHE_MAP.put(logMeta.getSourcePath(), logMeta);
        }
    }

    public static void removeFileCache(String filePath) {
        synchronized (FILE_CACHE_MAP) {
            FILE_CACHE_MAP.remove(filePath);
        }
    }

    public static List<LogMeta> getSortedCacheLogMeta() {
        synchronized (FILE_CACHE_MAP) {
            return FILE_CACHE_MAP.values().stream().sorted(Comparator.comparing(LogMeta::getLastUpdateTime)).collect(Collectors.toList());
        }
    }

    public static void addEventQueue(LogMeta logMeta) {
        try {
            POLLING_EVENT_QUEUE.put(logMeta);
        } catch (InterruptedException e) {
            logger.error("addEventQueue error", e);
        }
    }

}
