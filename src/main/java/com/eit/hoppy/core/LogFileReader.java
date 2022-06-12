package com.eit.hoppy.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.RandomAccessFile;

/**
 * description:
 *
 * @author Hlingoes 2022/6/5
 * @citation Created by jaren.han on 2020-01-10.
 */
public class LogFileReader {

    public static final Logger logger = LoggerFactory.getLogger(LogFileReader.class);

    private long readOffset = 0L;
    private File file;
    private String lastFileInode;
    private long lastModifyTime;
    private String srcFilePath;
    //文件签名
    private String signature;
    private ReceiveDataChannel receiveDataChannel;

    public LogFileReader(File file) {
        this.file = file;
        lastFileInode = FileHelper.getFileInode(file.getAbsolutePath());
        lastModifyTime = file.lastModified();
        srcFilePath = file.getAbsolutePath();
        receiveDataChannel = new UdpReceive();
    }

    public LogFileReader(FileMeta fileMeta) {
        this.file = fileMeta.getFile();
        receiveDataChannel = new UdpReceive();
    }

    public void readLog() {
        RandomAccessFile randomAccessFile = null;
        try {
            randomAccessFile = new RandomAccessFile(file, "rw");
            long fileLength = randomAccessFile.length();
            if (fileLength > readOffset) {
                randomAccessFile.seek(readOffset);
                String content = null;
                while ((content = randomAccessFile.readLine()) != null) {
                    receiveDataChannel.receiveData(content);
                }
                readOffset = randomAccessFile.getFilePointer();
                randomAccessFile.close();
            }
        } catch (Exception e) {
            logger.error("GetCurrentLog", e);
        }
    }

    public File getFile() {
        return this.file;
    }

    public long getReadOffset() {
        return readOffset;
    }

    public String getLastFileInode() {
        return lastFileInode;
    }

    public long getLastModifyTime() {
        return lastModifyTime;
    }

    public String getSrcFilePath() {
        return srcFilePath;
    }
}
