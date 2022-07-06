package com.eit.hoppy.logtail;

import com.eit.hoppy.util.FileHelper;

import java.io.File;
import java.io.Serializable;
import java.util.StringJoiner;

/**
 * description: 日志文件的点位文件数据
 *
 * @author Hlingoes
 * @date 2022/6/11 20:41
 */
public class LogMeta implements Serializable {

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
     * 当前日志解析进度
     */
    private long readOffset = 0L;

    public LogMeta() {

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

    public long getReadOffset() {
        return readOffset;
    }

    public void setReadOffset(long readOffset) {
        this.readOffset = readOffset;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", LogMeta.class.getSimpleName() + "[", "]")
                .add("sourcePath='" + sourcePath + "'")
                .add("signBytes=" + signBytes)
                .add("signature=" + signature)
                .add("devInode='" + devInode + "'")
                .add("lastUpdateTime=" + lastUpdateTime)
                .add("readOffset=" + readOffset)
                .toString();
    }
}
