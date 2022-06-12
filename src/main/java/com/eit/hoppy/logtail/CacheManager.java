package com.eit.hoppy.logtail;

import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

/**
 * description: 日志控制器，缓存文件和reader状态
 *
 * @author Hlingoes
 * @date 2022/6/11 22:12
 */
public class CacheManager {
    /**
     * 缓存由DirFilePolling线程定时遍历用户配置的目录生成的符合条件的元数据
     * key: sourcePath
     * value: LogMeta
     */
    private static final Map<String, LogMeta> FILE_CACHE_MAP = new ConcurrentHashMap<>(1000);
    /**
     * 缓存由FileModifyPolling线程生成的Create/Modify/Delete事件
     * key: sourcePath
     * value: LogMeta
     */
    private static final BlockingQueue<LogMeta> POLLING_EVENT_QUEUE = new ArrayBlockingQueue<>(1000);

    public static Map<String, LogMeta> getFileCacheMap() {
        return FILE_CACHE_MAP;
    }

    public static BlockingQueue<LogMeta> getPollingEventQueue() {
        return POLLING_EVENT_QUEUE;
    }

}
