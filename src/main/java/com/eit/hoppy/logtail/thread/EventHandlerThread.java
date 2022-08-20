package com.eit.hoppy.logtail.thread;

import com.eit.hoppy.logtail.CacheManager;
import com.eit.hoppy.logtail.FileEventEnum;
import com.eit.hoppy.logtail.LogEvent;
import com.eit.hoppy.logtail.LogFileReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Objects;
import java.util.Queue;

/**
 * description:
 *
 * @author Hlingoes
 * @date 2022/6/19 19:18
 */
public class EventHandlerThread extends AbstractPollingThread {
    public static final long EXPIRE_TIME = 5000L;
    private static Logger logger = LoggerFactory.getLogger(EventHandlerThread.class);

    public EventHandlerThread(long period) {
        super(EventHandlerThread.class.getSimpleName(), period);
    }

    @Override
    void polling() {
        LogEvent logEvent = CacheManager.pollLogEvent();
        if (Objects.isNull(logEvent)) {
            return;
        }
        if (logEvent.getFileEventEnum() == FileEventEnum.DELETE) {
            doDeleteEvent(logEvent);
        } else if (logEvent.getFileEventEnum() == FileEventEnum.CREATE) {
            doCreateEvent(logEvent);
        } else if (logEvent.getFileEventEnum() == FileEventEnum.CREATE_ROTATE) {
            doCreateRotateEvent(logEvent);
        } else {

        }

    }

    /**
     * description: 文件删除
     *
     * @param logEvent
     * @return void
     * @author Hlingoes 2022/6/26
     */
    private void doDeleteEvent(LogEvent logEvent) {
        Queue<LogFileReader> readers = CacheManager.getReader(logEvent.getDevInode()).getReaderQueue();
        /**
         * 对于日志文件的Delete Event，若当前解析进度落后，文件虽被删除但日志未采集完成，则忽略此Delete事件；
         * 日志未采集完成，且一定时间内该Reader没有处理过Modify事件，则删除该Reader
         */
        if (readers.size() == 1) {
            LogFileReader reader = readers.element();
            reader.setDeleteFlag(true);
            long expireTime = reader.getLastUpdateTime() + EXPIRE_TIME;
            if (System.currentTimeMillis() > expireTime && reader.finishReading()) {
                readers.poll();
                CacheManager.removeFileCache(logEvent.getSourcePath(), logEvent.getDevInode());
            }
        }
    }

    private void doCreateEvent(LogEvent logEvent) {
        try {
            LogFileReader reader = new LogFileReader(logEvent.getSourcePath(), logEvent.getDevInode());
            CacheManager.addReader(reader);
        } catch (IOException e) {
            logger.error("create logFileReader error: {}", logEvent, e);
        }
    }

    private void doCreateRotateEvent(LogEvent logEvent) {
        try {
            LogFileReader reader = new LogFileReader(logEvent.getSourcePath(), logEvent.getDevInode(), 0L);
            CacheManager.addReader(reader);
        } catch (IOException e) {
            logger.error("create logFileReader error: {}", logEvent, e);
        }
    }

    private void doModifyEvent(LogEvent logEvent) {
        try {
            Queue<LogFileReader> readerQueue = CacheManager.getReader(logEvent.getDevInode()).getReaderQueue();
            LogFileReader curReader = readerQueue.element();
            curReader.readLog(super.getPeriod());
            if (curReader.finishReading()) {
                curReader.closeAccessFile();
                if (readerQueue.size() > 1) {
                    // 检查日志是否在一定时间内没有被读取
                    if (curReader.isExpired()) {
                        readerQueue.poll();
                        CacheManager.offerLogEvent(logEvent);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("do modify event error: {}", logEvent, e);
        }
    }

}
