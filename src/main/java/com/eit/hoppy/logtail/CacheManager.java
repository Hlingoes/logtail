package com.eit.hoppy.logtail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
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
    private static final Map<String, LogMeta> FILE_CACHE_MAP = new HashMap<>(10000);
    /**
     * 缓存由FileModifyPolling线程生成的Create/Modify/Delete事件
     * key: sourcePath
     * value: LogMeta
     */
    private static final Queue<LogMeta> POLLING_EVENT_QUEUE = new LinkedList<>();
    /**
     * 以sourcePath为key/LogFileReaderQueue为value的map，用于存储当前正在读取的所有ReaderQueue
     * key: sourcePath
     * value: LogFileReaderQueue
     * LogFileReaderQueue 存储sourcePath相同且未采集完毕的reader列表，reader按照日志文件创建顺序进行排列
     */
    private static final Map<String, Queue<LogFileReader>> NAMED_LOG_FILE_READER_QUEUE_MAP = new HashMap<>(10000);
    /**
     * 以devInode为key/LogFileReader为value的map，用于存储处于轮转状态且已经读取完毕的Reader
     * key: devInode
     * value: LogFileReader
     */
    private static final Map<String, LogFileReader> ROTATE_LOG_FILE_READER_MAP = new HashMap<>(10000);
    /**
     * 缓存日志内容
     */
    private static final Queue<String> LOG_CONTENT_QUEUE = new ArrayBlockingQueue<>(10000);

    public static boolean isFileCached(String filePath) {
        return FILE_CACHE_MAP.containsKey(filePath);
    }

    public static boolean isFileCacheEmpty() {
        return FILE_CACHE_MAP.isEmpty();
    }

    public static void addFileCache(LogMeta logMeta) {
        FILE_CACHE_MAP.put(logMeta.getSourcePath(), logMeta);
    }

    public static void removeFileCache(String filePath) {
        FILE_CACHE_MAP.remove(filePath);
    }

    public static List<LogMeta> getSortedCacheLogMeta() {
        return FILE_CACHE_MAP.values().stream().sorted(Comparator.comparing(LogMeta::getLastUpdateTime)).collect(Collectors.toList());
    }

    public static void addEventQueue(LogMeta logMeta) {
        POLLING_EVENT_QUEUE.add(logMeta);
    }

    public static LogMeta getEventLogMeta() {
        return POLLING_EVENT_QUEUE.poll();
    }

    public static Queue<LogFileReader> getLogReaderQueue(String sourcePath) {
        return NAMED_LOG_FILE_READER_QUEUE_MAP.get(sourcePath);
    }

    public static void addLogContent(String content) {
        LOG_CONTENT_QUEUE.add(content);
    }

    public static void addNamedLogFileReader(String sourcePath, LogFileReader logFileReader) {
        Queue<LogFileReader> queue = NAMED_LOG_FILE_READER_QUEUE_MAP.get(sourcePath);
        if (null != queue) {
            queue.add(logFileReader);
        } else {
            queue = new LinkedList<>();
            queue.add(logFileReader);
            NAMED_LOG_FILE_READER_QUEUE_MAP.put(sourcePath, queue);
        }
    }

    public static Queue<LogFileReader> getNamedLogFileReaderQueue(String sourcePath) {
        return NAMED_LOG_FILE_READER_QUEUE_MAP.get(sourcePath);
    }

    public static void addRotateLogFileReader(String sourcePath, LogFileReader logFileReader) {
        ROTATE_LOG_FILE_READER_MAP.put(sourcePath, logFileReader);
    }

    public static Map<String, LogFileReader> getRotateLogFileReaderMap() {
        return ROTATE_LOG_FILE_READER_MAP;
    }

}
