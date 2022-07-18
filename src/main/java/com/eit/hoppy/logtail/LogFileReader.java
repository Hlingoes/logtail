package com.eit.hoppy.logtail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
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
     * 点位文件信息
     */
    private LogMeta logMeta;
    /**
     * 读取的文件
     */
    private File file;
    /**
     * 用于标识该文件是否被删除
     */
    private boolean deleteFlag;
    /**
     * 文件指针
     */
    private RandomAccessFile randomAccessFile;
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
        this.file = new File(logMeta.getSourcePath());
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
                randomAccessFile = new RandomAccessFile(file, "r");
            }
            randomAccessFile.seek(logMeta.getReadOffset());
            while (System.currentTimeMillis() < readEndTime && (lineContent = randomAccessFile.readLine()) != null) {
                /**
                 * 使用 RandomAccessFile对象方法的 readLine() 都会将编码格式转换成 ISO-8859-1
                 * 所以要把"ISO-8859-1"编码的字节数组再次转换成系统默认的编码才可以显示正常
                 */
                CacheManager.addLogContent(new String(lineContent.getBytes("ISO-8859-1"), StandardCharsets.UTF_8));
                logMeta.setReadOffset(randomAccessFile.getFilePointer());
                CacheManager.writeCacheMapFile();
            }
            // 释放句柄，否则在Windows下，文件无法操作
            releasePoinerIfFinished();
        } catch (IOException e) {
            logger.error("get current log error", e);
        }
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

    /**
     * description: 文件是否读取完成
     *
     * @param
     * @return boolean
     * @author Hlingoes 2022/7/6
     */
    public boolean finishReading() {
        if (file.exists()) {
            return file.length() == logMeta.getReadOffset();
        }
        return true;
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

}
