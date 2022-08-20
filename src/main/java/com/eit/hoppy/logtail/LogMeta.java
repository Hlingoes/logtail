package com.eit.hoppy.logtail;

import java.util.StringJoiner;

/**
 * description: 日志文件的标识属性
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
     * 文件的dev + Inode组合
     */
    private String devInode;
    /**
     * 文件更新时间
     */
    private long lastModified;

    public LogMeta(String sourcePath, String devInode, long lastModified) {
        this.sourcePath = sourcePath;
        this.devInode = devInode;
        this.lastModified = lastModified;
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

    public long getLastModified() {
        return lastModified;
    }

    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", LogMeta.class.getSimpleName() + "[", "]")
                .add("sourcePath='" + sourcePath + "'")
                .add("devInode='" + devInode + "'")
                .add("lastModified=" + lastModified)
                .toString();
    }

}
