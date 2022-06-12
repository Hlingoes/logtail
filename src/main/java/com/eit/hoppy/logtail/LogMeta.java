package com.eit.hoppy.logtail;

import com.eit.hoppy.core.FileHelper;
import com.eit.hoppy.core.UdpReceive;

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
     * 文件的签名,使用日志文件的前1024字节的hash
     */
    private String signature;
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

    public LogMeta() {

    }

    public LogMeta(File file) {
        this.file = file;
        devInode = FileHelper.getFileInode(file.getAbsolutePath());
        lastUpdateTime = file.lastModified();
        sourcePath = file.getAbsolutePath();
    }

    public String getSourcePath() {
        return sourcePath;
    }

    public void setSourcePath(String sourcePath) {
        this.sourcePath = sourcePath;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
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
                .add("signature='" + signature + "'")
                .add("devInode='" + devInode + "'")
                .add("lastUpdateTime=" + lastUpdateTime)
                .add("file=" + file)
                .add("eventEnum=" + eventEnum)
                .toString();
    }
}
