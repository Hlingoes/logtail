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
            doModifyEvent(logEvent);
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

    /**
     * description: 首先根据dev+inode查找devInodeReaderMap，找到该Reader所在的ReaderQueue，获取ReaderQueue的队列首部的Reader进行日志读取操作；
     * 若该日志文件读取完毕(readOffsetfileSize)且ReaderQueue的size > 1，则从ReaderQueue中移除该Reader
     * （日志已经发生了轮转，且轮转后的文件已经读取完毕，所以可以从ReaderQueue中移除），此时继续把Modify Event push到Event队列中，触发队列后续文件的读取，进入下一循环；
     * 若日志文件读取完毕且ReaderQueue的size1（size为1说明该文件并没有轮转，极有可能后续还有写入，所以不能从ReaderQueue中移除），则完成次轮Modify Event处理，进入下一循环
     * 若日志文件没有读取完成，则把Modify Event push到Event队列中，进入下一循环（避免所有时间都被同一文件占用，保证日志文件读取公平性）
     *
     * @param logEvent
     * @return void
     * @author Hlingoes 2022/8/21
     */
    private void doModifyEvent(LogEvent logEvent) {
        try {
            Queue<LogFileReader> readerQueue = CacheManager.getReader(logEvent.getDevInode()).getReaderQueue();
            LogFileReader curReader = readerQueue.element();
            curReader.readLog(super.getPeriod());
            if (curReader.finishReading()) {
                // 在Windows环境下，不释放句柄，则logback日志不能rotate
                curReader.closeAccessFile();
                if (readerQueue.size() > 1) {
                    // 检查日志是否在一定时间内没有被读取，只有在过期后才能将reader移除
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
