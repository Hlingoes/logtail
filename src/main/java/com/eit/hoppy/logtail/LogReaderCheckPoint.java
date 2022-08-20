package com.eit.hoppy.logtail;

/**
 * description:
 *
 * @author Hlingoes
 * @date 2022/8/20 23:24
 */
public class LogReaderCheckPoint {
    /**
     * 文件路径
     */
    private String sourcePath;
    /**
     * 文件的dev + Inode组合
     */
    private String devInode;
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

    public LogReaderCheckPoint(String sourcePath, String devInode, int signBytes, int signature, long readOffset, long lastUpdateTime) {
        this.sourcePath = sourcePath;
        this.devInode = devInode;
        this.signBytes = signBytes;
        this.signature = signature;
        this.readOffset = readOffset;
        this.lastUpdateTime = lastUpdateTime;
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
}
