package com.eit.hoppy.core;

import java.io.File;

/**
 * description:
 *
 * @author Hlingoes 2022/6/5
 * @citation Created by jaren.han on 2020-01-09.
 */
public class FileMeta {

    private long lastModifyTime;
    private String fileInode;
    private File file;
    private FileState fileState;
    private String srcFilepath;

    public String getSrcFilepath() {
        return srcFilepath;
    }

    public void setSrcFilepath(String srcFilepath) {
        this.srcFilepath = srcFilepath;
    }

    public long getLastModifyTime() {
        return lastModifyTime;
    }

    public void setLastModifyTime(long lastModifyTime) {
        this.lastModifyTime = lastModifyTime;
    }

    public String getFileInode() {
        return fileInode;
    }

    public void setFileInode(String fileInode) {
        this.fileInode = fileInode;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public FileState getFileState() {
        return fileState;
    }

    public void setFileState(FileState fileState) {
        this.fileState = fileState;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(String.format("file name:%s |", file.getName()));
        stringBuilder.append(String.format("file srcFilepath:%s |", srcFilepath));
        stringBuilder.append(String.format("file fileInode:%s |", fileInode));
        stringBuilder.append(String.format("file last modify time:%s |", lastModifyTime));
        stringBuilder.append(String.format("file state:%s |", fileState));
        return stringBuilder.toString();
    }
}
