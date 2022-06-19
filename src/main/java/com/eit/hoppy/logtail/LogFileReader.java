package com.eit.hoppy.logtail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

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
    private Boolean deleteFlag;
    /**
     * 文件指针
     */
    private RandomAccessFile randomAccessFile;
    /**
     * 当前日志解析进度
     */
    private long readOffset;
    /**
     * 单次读取日志的时长，超时则停止，进入下一个循环
     */
    private long readingTime = 5000L;

    public LogFileReader(LogMeta logMeta) {
        this.logMeta = logMeta;
        try {
            randomAccessFile = new RandomAccessFile(logMeta.getFile(), "rw");
        } catch (FileNotFoundException e) {
            logger.error("open file fail: {}", logMeta, e);
        }
    }

    public void readLog() {
        long startTime = System.currentTimeMillis();
        long endTime = startTime + readingTime;
        try {
            long fileLength = randomAccessFile.length();
            if (fileLength > readOffset) {
                randomAccessFile.seek(readOffset);
                String content;
                while (System.currentTimeMillis() < endTime && (content = randomAccessFile.readLine()) != null) {
                    CacheManager.addLogContent(content);
                }
                readOffset = randomAccessFile.getFilePointer();
                randomAccessFile.close();
            } else {
                logger.info("no new content to read: {}", logMeta);
            }
        } catch (Exception e) {
            logger.error("get current log", e);
        } finally {
            if (null != randomAccessFile) {
                try {
                    randomAccessFile.close();
                } catch (IOException e) {
                    logger.warn("read error", e);
                }
            }
        }
    }

    public boolean finishReading() {
        return logMeta.getFile().length() == readOffset;
    }

    public void close() {
        try {
            randomAccessFile.close();
        } catch (IOException e) {
            logger.warn("close reader error: {}", logMeta, e);
        }
    }

    public LogMeta getLogMeta() {
        return logMeta;
    }

    public void setLogMeta(LogMeta logMeta) {
        this.logMeta = logMeta;
    }

    public Boolean getDeleteFlag() {
        return deleteFlag;
    }

    public void setDeleteFlag(Boolean deleteFlag) {
        this.deleteFlag = deleteFlag;
    }

    public long getReadOffset() {
        return readOffset;
    }

    public void setReadOffset(long readOffset) {
        this.readOffset = readOffset;
    }

}
