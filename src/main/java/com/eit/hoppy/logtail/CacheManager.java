package com.eit.hoppy.logtail;

import com.eit.hoppy.util.FileHelper;
import com.eit.hoppy.util.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.stream.Collectors;

/**
 * description: 日志控制器，缓存文件和reader状态
 *
 * @author Hlingoes
 * @date 2022/6/11 22:12
 */
public class CacheManager {
    private static Logger logger = LoggerFactory.getLogger(CacheManager.class);

    public static final int MAX_CAPACITY = 10000;

    /**
     * 记录点位信息的序列化问价
     */
    private static final File lOG_META_CACHE_FILE = new File("data/log_meta_cache.dat");
    /**
     * 缓存由DirFilePolling线程定时遍历用户配置的目录生成的符合条件的元数据
     * key: sourcePath
     * value: LogMeta
     */
    private static final Map<String, LogMeta> FILE_CACHE_MAP = new HashMap<>();
    /**
     * 以sourcePath为key/LogFileReaderQueue为value的map，用于存储当前正在读取的所有ReaderQueue
     * key: sourcePath
     * value: LogFileReaderQueue
     * LogFileReaderQueue 存储sourcePath相同且未采集完毕的reader列表，reader按照日志文件创建顺序进行排列
     */
    private static final Map<String, Queue<LogFileReader>> NAMED_LOG_FILE_READER_QUEUE_MAP = new HashMap<>();
    /**
     * 以devInode为key/LogFileReader为value的map，用于存储当前正在读取的所有ReaderQueue
     * key: devInode
     * value: LogFileReader
     */
    private static final Map<String, LogFileReader> DEVINODE_READER_MAP = new HashMap<>();
    /**
     * 日志事件
     */
    private static final BlockingQueue<LogEvent> LOG_EVENT_QUEUE = new ArrayBlockingQueue<>(MAX_CAPACITY);
    /**
     * 缓存日志内容
     */
    private static final BlockingQueue<String> LOG_CONTENT_QUEUE = new ArrayBlockingQueue<>(MAX_CAPACITY);

    /**
     * description: 遍历文件目录，生成文件缓存
     *
     * @param dirPath
     * @return void
     * @author Hlingoes 2022/8/20
     */
    public static void addFileCache(String dirPath) {
        List<File> files = FileHelper.getFileSort(dirPath, File::isFile);
        files.forEach(file -> {
            LogMeta logMeta = FILE_CACHE_MAP.get(file.getAbsolutePath());
            if (Objects.isNull(logMeta)) {
                String devInode = FileHelper.getFileInode(file.getAbsolutePath());
                logMeta = new LogMeta(file.getAbsolutePath(), devInode, file.lastModified());
                FILE_CACHE_MAP.put(file.getAbsolutePath(), logMeta);
                offerLogEvent(new LogEvent(FileEventEnum.CREATE, file.getAbsolutePath(), devInode));
            } else {
                logMeta.setLastModified(file.lastModified());
            }
        });
    }

    public static List<LogMeta> getSortedCacheLogMeta() {
        if (FILE_CACHE_MAP.isEmpty()) {
            return new ArrayList<>();
        }
        return FILE_CACHE_MAP.values().stream().sorted(Comparator.comparing(LogMeta::getLastModified)).collect(Collectors.toList());
    }

    /**
     * description: 添加reader，先判断devInodeMap中是否存在，若存在说明是rotate，只需更新reader的sourcePath
     * 若不存在，则先添加到devInodeMap中，再添加到sourcePath相同的namedReaderMap的队尾，同时加到reader queue的队尾
     *
     * @param reader
     * @return void
     * @author Hlingoes 2022/8/20
     */
    public static void addReader(LogFileReader reader) {
        LogFileReader preReader = DEVINODE_READER_MAP.get(reader.getDevInode());
        if (!Objects.isNull(preReader)) {
            preReader.setSourcePath(reader.getSourcePath());
            return;
        }
        DEVINODE_READER_MAP.put(reader.getDevInode(), reader);
        Queue<LogFileReader> readerQueue = NAMED_LOG_FILE_READER_QUEUE_MAP.get(reader.getSourcePath());
        if (Objects.isNull(readerQueue)) {
            readerQueue = new ArrayBlockingQueue<>(MAX_CAPACITY);
            readerQueue.offer(reader);
            NAMED_LOG_FILE_READER_QUEUE_MAP.put(reader.getSourcePath(), readerQueue);
        } else {
            readerQueue.add(reader);
        }
        reader.setReaderQueue(readerQueue);
    }

    /**
     * description: 添加事件
     *
     * @param logEvent
     * @return void
     * @author Hlingoes 2022/6/26
     */
    public static void offerLogEvent(LogEvent logEvent) {
        if (!LOG_EVENT_QUEUE.contains(logEvent)) {
            LOG_EVENT_QUEUE.offer(logEvent);
        }
    }

    public static LogEvent pollLogEvent() {
        return LOG_EVENT_QUEUE.poll();
    }

    public static LogFileReader getReader(String devInode) {
        return DEVINODE_READER_MAP.get(devInode);
    }

    public static String getSourcePath(String devInode) {
        return DEVINODE_READER_MAP.get(devInode).getSourcePath();
    }

    public static void removeFileCache(String sourcePath, String devInode) {
        DEVINODE_READER_MAP.remove(devInode);
        FILE_CACHE_MAP.remove(sourcePath);
    }


    public static void addLogContent(String content) {
        LOG_CONTENT_QUEUE.offer(content);
    }

    public static List<String> batchReadContents(int batch) {
        List<String> batchContents = new ArrayList<>(batch);
        LOG_CONTENT_QUEUE.drainTo(batchContents, batch);
        return batchContents;
    }

    public static List<String> remainContents() {
        List<String> batchContents = new ArrayList<>(LOG_CONTENT_QUEUE.size());
        LOG_CONTENT_QUEUE.drainTo(batchContents, LOG_CONTENT_QUEUE.size());
        return batchContents;
    }

    public static void writeCheckPointFile() throws IOException {
        Map<String, List<LogReaderCheckPoint>> checkPointMap = new HashMap<>(NAMED_LOG_FILE_READER_QUEUE_MAP.size());
        NAMED_LOG_FILE_READER_QUEUE_MAP.forEach((sourcePath, logFileReaders) -> {
            List<LogReaderCheckPoint> checkPoints = new ArrayList<>();
            logFileReaders.forEach(reader -> checkPoints.add(reader.toCheckPoint()));
            checkPointMap.put(sourcePath, checkPoints);
        });
        SerializationUtils.writeSerializeObject(lOG_META_CACHE_FILE, checkPointMap);
    }

    public static void recoverCheckPointFile() throws IOException {
        if (!lOG_META_CACHE_FILE.exists()) {
            lOG_META_CACHE_FILE.createNewFile();
            return;
        }
        if (lOG_META_CACHE_FILE.length() == 0) {
            logger.warn("no cache data: {}", lOG_META_CACHE_FILE.getAbsolutePath());
            return;
        }
        Map<String, List<LogReaderCheckPoint>> checkPointMap = (Map<String, List<LogReaderCheckPoint>>) SerializationUtils.readDeserialize(lOG_META_CACHE_FILE);
        Map<String, String> devInodePointMap = new HashMap<>(FILE_CACHE_MAP.size());
        FILE_CACHE_MAP.forEach((sourcePath, logMeta) -> devInodePointMap.put(logMeta.getDevInode(), sourcePath));
        // 恢复reader状态
        for (Map.Entry<String, List<LogReaderCheckPoint>> entry : checkPointMap.entrySet()) {
            List<LogReaderCheckPoint> logReaderCheckPoints = entry.getValue();
            for (LogReaderCheckPoint checkPoint : logReaderCheckPoints) {
                String effPath = devInodePointMap.get(checkPoint.getDevInode());
                // 检查devInode在当前的缓存文件中是否存在，不存在则放弃创建
                if (!Objects.isNull(effPath)) {
                    long signature = FileHelper.calSignature(effPath, checkPoint.getSignBytes());
                    // 如果signature没变，则创建reader，否则认为该文件被删除重建，忽略该check point
                    if (signature == checkPoint.getSignature()) {
                        addReader(new LogFileReader(effPath, checkPoint.getDevInode(), checkPoint.getSignBytes(), checkPoint.getSignature(), checkPoint.getReadOffset(), checkPoint.getLastUpdateTime()));
                    }
                }
            }
        }
    }

}
