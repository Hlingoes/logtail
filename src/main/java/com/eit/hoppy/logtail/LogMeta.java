package com.eit.hoppy.logtail;

import com.eit.hoppy.util.FileHelper;

import java.io.File;
import java.util.StringJoiner;

/**
 * description: 日志文件的元数据
 *
 * @author Hlingoes
 * @date 2022/6/11 20:41
 */
public class LogMeta {
    /**
     * 文件路径
     */
    private String sourcePath;
    /**
     * 用作文件签名的字节数，默认为1024
     */
    private int signBytes = 1024;
    /**
     * 文件的签名,使用日志文件的前1024字节的hash
     */
    private int signature;
    /**
     * 文件的dev + Inode组合,在windows系统中取不到值
     */
    private String devInode;
    /**
     * 录最后一次进行读取的时间
     */
    private long lastUpdateTime;
    /**
     * 日志文件
     */
    private File file;
    /**
     * 文件事件
     */
    private FileEventEnum eventEnum;

    /**
     * description: 默认是CREATE事件，lastUpdateTime = 0L
     *
     * @param file
     * @author Hlingoes 2022/6/12
     */
    public LogMeta(File file) {
        this.file = file;
        devInode = FileHelper.getFileInode(file.getAbsolutePath());
        lastUpdateTime = 0L;
        sourcePath = file.getAbsolutePath();
        signBytes = file.length() > 1024 ? 1024 : (int) file.length();
        signature = calSignature();
        eventEnum = FileEventEnum.CREATE;
    }

    /**
     * description: 文件的签名,使用日志文件的前bytes字节的hash
     *
     * @return int
     * @author Hlingoes 2022/6/19
     */
    public int calSignature() {
        String firstBytes = FileHelper.readFirstBytes(sourcePath, signBytes);
        return firstBytes.hashCode();
    }

    public String getSourcePath() {
        return sourcePath;
    }

    public void setSourcePath(String sourcePath) {
        this.sourcePath = sourcePath;
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

    public String getDevInode() {
        return devInode;
    }

    public void setDevInode(String devInode) {
        this.devInode = devInode;
    }

    public long getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(long lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public FileEventEnum getEventEnum() {
        return eventEnum;
    }

    public void setEventEnum(FileEventEnum eventEnum) {
        this.eventEnum = eventEnum;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", LogMeta.class.getSimpleName() + "[", "]")
                .add("sourcePath='" + sourcePath + "'")
                .add("signBytes=" + signBytes)
                .add("signature=" + signature)
                .add("devInode='" + devInode + "'")
                .add("lastUpdateTime=" + lastUpdateTime)
                .add("file=" + file)
                .add("eventEnum=" + eventEnum)
                .toString();
    }
}
