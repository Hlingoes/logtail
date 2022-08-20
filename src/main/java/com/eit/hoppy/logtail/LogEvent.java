package com.eit.hoppy.logtail;

import java.util.Objects;
import java.util.StringJoiner;

/**
 * description: 日志事件
 *
 * @author Hlingoes
 * @date 2022/8/20 15:56
 */
public class LogEvent {
    
    private FileEventEnum fileEventEnum;

    private String sourcePath;

    private String devInode;

    public LogEvent(FileEventEnum fileEventEnum, String sourcePath, String devInode) {
        this.fileEventEnum = fileEventEnum;
        this.sourcePath = sourcePath;
        this.devInode = devInode;
    }

    public FileEventEnum getFileEventEnum() {
        return fileEventEnum;
    }

    public void setFileEventEnum(FileEventEnum fileEventEnum) {
        this.fileEventEnum = fileEventEnum;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LogEvent logEvent = (LogEvent) o;
        return fileEventEnum == logEvent.fileEventEnum &&
                Objects.equals(sourcePath, logEvent.sourcePath) &&
                Objects.equals(devInode, logEvent.devInode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fileEventEnum, sourcePath, devInode);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", LogEvent.class.getSimpleName() + "[", "]")
                .add("fileEventEnum=" + fileEventEnum)
                .add("sourcePath='" + sourcePath + "'")
                .add("devInode='" + devInode + "'")
                .toString();
    }
}
