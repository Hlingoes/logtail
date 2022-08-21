package com.eit.hoppy.logtail;

import com.eit.hoppy.util.FileHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Queue;
import java.util.StringJoiner;

/**
 * description: 读取日志
 *
 * @author Hlingoes
 * @date 2022/6/11 21:58
 */
public class LogFileReader {
    public static final long EXPIRE_TIME = 5 * 1000L;
    private static Logger logger = LoggerFactory.getLogger(LogFileReader.class);

    /**
     * 文件路径
     */
    private String sourcePath;
    /**
     * 文件的dev + Inode组合
     */
    private String devInode;
    /**
     * 文件指针
     */
    private RandomAccessFile randomAccessFile;
    /**
     * 用作文件签名的字节数，默认为1024
     */
    private int signBytes = 1024;
    /**
     * 文件的签名,使用日志文件的前1024字节的hash
     */
    private int signature;
    /**
     * 已读取的日志位置
     */
    private long readOffset;
    /**
     * 单次读取的结束时间
     */
    private long lastUpdateTime;
    /**
     * 删除标记
     */
    private boolean deleteFlag;
    /**
     * 相同sourcePath的在读队列
     */
    private Queue<LogFileReader> readerQueue;

    public LogFileReader(String sourcePath, String devInode) throws IOException {
        this.sourcePath = sourcePath;
        this.devInode = devInode;
        this.randomAccessFile = new RandomAccessFile(new File(sourcePath), "r");
        this.readOffset = randomAccessFile.length();
        this.signBytes = this.readOffset > signBytes ? signBytes : (int) this.readOffset;
        this.signature = FileHelper.calSignature(sourcePath, signBytes);
    }

    public LogFileReader(String sourcePath, String devInode, long readOffset) throws IOException {
        this.sourcePath = sourcePath;
        this.devInode = devInode;
        this.randomAccessFile = new RandomAccessFile(new File(sourcePath), "r");
        this.readOffset = readOffset;
        this.signBytes = this.readOffset > signBytes ? signBytes : (int) this.readOffset;
        this.signature = FileHelper.calSignature(sourcePath, signBytes);
    }

    public LogFileReader(String sourcePath, String devInode, int signBytes, int signature, long readOffset, long lastUpdateTime) throws IOException {
        this.sourcePath = sourcePath;
        this.devInode = devInode;
        this.randomAccessFile = new RandomAccessFile(new File(sourcePath), "r");
        this.signBytes = signBytes;
        this.signature = signature;
        this.readOffset = readOffset;
        this.lastUpdateTime = lastUpdateTime;
    }

    public LogReaderCheckPoint toCheckPoint() {
        return new LogReaderCheckPoint(sourcePath, devInode, signBytes, signature, readOffset, lastUpdateTime);
    }

    /**
     * description: 单次读取日志的时长，超时则停止，进入下一个循环
     *
     * @param readingPeriod
     * @return void
     * @author Hlingoes 2022/6/26
     */
    public void readLog(long readingPeriod) {
        try {
            if (Objects.isNull(randomAccessFile)) {
                String curDevInode = FileHelper.getFileInode(sourcePath);
                if (!curDevInode.equalsIgnoreCase(devInode) && CacheManager.getSourcePath(devInode).equals(sourcePath)) {
                    // 说明文件发生rotate，但是create事件还未被消费
                    return;
                }
                if (curDevInode.equalsIgnoreCase(devInode)) {
                    randomAccessFile = new RandomAccessFile(new File(sourcePath), "r");
                } else {
                    // 文件发生了轮转，需要用新的路径打开文件
                    randomAccessFile = new RandomAccessFile(new File(CacheManager.getSourcePath(devInode)), "r");
                }
            }
            long startTime = System.currentTimeMillis();
            long readEndTime = startTime + readingPeriod;
            randomAccessFile.seek(readOffset);
            String lineContent;
            while (System.currentTimeMillis() < readEndTime && (lineContent = randomAccessFile.readLine()) != null) {
                /**
                 * 使用 RandomAccessFile对象方法的 readLine() 都会将编码格式转换成 ISO-8859-1
                 * 所以要把"ISO-8859-1"编码的字节数组再次转换成系统默认的编码才可以显示正常
                 */
                CacheManager.addLogContent(new String(lineContent.getBytes("ISO-8859-1"), StandardCharsets.UTF_8));
                readOffset = randomAccessFile.getFilePointer();
                lastUpdateTime = System.currentTimeMillis();
                CacheManager.writeCheckPointFile();
            }
        } catch (IOException e) {
            logger.error("get current log error: {}", this, e);
        }
    }

    public void closeAccessFile() {
        if (Objects.isNull(randomAccessFile)) {
            return;
        }
        try {
            randomAccessFile.close();
        } catch (IOException e) {
            logger.error("release randomAccessFile fail: {}", this, e);
        } finally {
            randomAccessFile = null;
        }
    }

    public boolean finishReading() {
        if (Objects.isNull(randomAccessFile)) {
            return isExpired();
        }
        try {
            return randomAccessFile.length() == readOffset;
        } catch (IOException e) {
            logger.error("RandonAccessFile error: {}", this, e);
        }
        return false;
    }

    /**
     * description: 判断日志读取时间是否过期
     *
     * @param
     * @return boolean
     * @author Hlingoes 2022/8/21
     */
    public boolean isExpired() {
        return System.currentTimeMillis() > (lastUpdateTime + EXPIRE_TIME);
    }

    public String getSourcePath() {
        return sourcePath;
    }

    public void setSourcePath(String sourcePath) {
        this.sourcePath = sourcePath;
    }

    public String getDevInode() {
        return devInode;
    }

    public void setDevInode(String devInode) {
        this.devInode = devInode;
    }

    public Queue<LogFileReader> getReaderQueue() {
        return readerQueue;
    }

    public void setReaderQueue(Queue<LogFileReader> readerQueue) {
        this.readerQueue = readerQueue;
    }

    public int getSignBytes() {
        return signBytes;
    }

    public void setSignBytes(int signBytes) {
        this.signBytes = signBytes;
    }

    public int getSignature() {
        return signature;
    }

    public void setSignature(int signature) {
        this.signature = signature;
    }

    public long getReadOffset() {
        return readOffset;
    }

    public void setReadOffset(long readOffset) {
        this.readOffset = readOffset;
    }

    public long getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(long lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public boolean getDeleteFlag() {
        return deleteFlag;
    }

    public void setDeleteFlag(boolean deleteFlag) {
        this.deleteFlag = deleteFlag;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", LogFileReader.class.getSimpleName() + "[", "]")
                .add("sourcePath='" + sourcePath + "'")
                .add("devInode='" + devInode + "'")
                .add("signBytes=" + signBytes)
                .add("signature=" + signature)
                .add("readOffset=" + readOffset)
                .add("lastUpdateTime=" + lastUpdateTime)
                .add("deleteFlag=" + deleteFlag)
                .toString();
    }
}
