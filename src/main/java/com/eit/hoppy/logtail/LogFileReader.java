package com.eit.hoppy.logtail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Objects;

/**
 * description: 读取日志
 *
 * @author Hlingoes
 * @date 2022/6/11 21:58
 */
public class LogFileReader {
    private static Logger logger = LoggerFactory.getLogger(LogFileReader.class);
    /**
     * 读取的文件
     */
    private LogMeta logMeta;
    /**
     * 用于标识该文件是否被删除
     */
    private boolean deleteFlag;
    /**
     * 文件指针
     */
    private RandomAccessFile randomAccessFile;
    /**
     * 当前日志解析进度
     */
    private long readOffset = 0L;
    /**
     * 单次读取的结束时间
     */
    private long readEndTime;
    /**
     * 按行读取的日志
     */
    String lineContent;

    public LogFileReader(LogMeta logMeta) {
        this.logMeta = logMeta;
        this.readOffset = logMeta.getFile().length();
    }

    /**
     * description: 单次读取日志的时长，超时则停止，进入下一个循环
     *
     * @param readingPeriod
     * @return void
     * @author Hlingoes 2022/6/26
     */
    public void readLog(long readingPeriod) {
        if (finishReading()) {
            if (logger.isDebugEnabled()) {
                logger.debug("no new content to read: {}", logMeta);
            }
            return;
        }
        long startTime = System.currentTimeMillis();
        this.readEndTime = startTime + readingPeriod;
        if (logger.isDebugEnabled()) {
            logger.debug("reading: {}, during: [{}, {}]", logMeta, startTime, readEndTime);
        }
        try {
            if (Objects.isNull(randomAccessFile)) {
                // 重新获取句柄
                randomAccessFile = new RandomAccessFile(logMeta.getFile(), "r");
            }
            randomAccessFile.seek(readOffset);
            while (System.currentTimeMillis() < readEndTime && (lineContent = randomAccessFile.readLine()) != null) {
                CacheManager.addLogContent(lineContent);
            }
            readOffset = randomAccessFile.getFilePointer();
            // 释放句柄，否则在Windows下，文件无法操作
            releasePoinerIfFinished();
        } catch (IOException e) {
            logger.error("get current log error", e);
        }
    }

    public boolean finishReading() {
        if (logMeta.getFile().exists()) {
            return logMeta.getFile().length() == readOffset;
        }
        return true;
    }

    private void releasePoinerIfFinished() {
        if (finishReading()) {
            try {
                randomAccessFile.close();
            } catch (IOException e) {
                logger.error("get current log error", e);
            } finally {
                randomAccessFile = null;
            }
        }
    }

    public boolean isExpired(long period) {
        return System.currentTimeMillis() > (logMeta.getLastUpdateTime() + period);
    }

    public LogMeta getLogMeta() {
        return logMeta;
    }

    public void setLogMeta(LogMeta logMeta) {
        this.logMeta = logMeta;
    }

    public boolean getDeleteFlag() {
        return deleteFlag;
    }

    public void setDeleteFlag(boolean deleteFlag) {
        this.deleteFlag = deleteFlag;
    }

    public long getReadOffset() {
        return readOffset;
    }

    public void setReadOffset(long readOffset) {
        this.readOffset = readOffset;
    }

}
