package com.eit.hoppy.logtail;

import com.eit.hoppy.util.FileHelper;

import java.io.File;

/**
 * description: 点位文件生成工厂
 *
 * @author Hlingoes
 * @date 2022/7/6 22:37
 */
public class LogMetaFactory {

    /**
     * description: 创建点位文件
     *
     * @param file
     * @return com.eit.hoppy.logtail.LogMeta
     * @author Hlingoes 2022/7/6
     */
    public static LogMeta createLogMeta(File file) {
        LogMeta logMeta = new LogMeta();
        logMeta.setReadOffset(file.length());
        String devInode = FileHelper.getFileInode(file.getAbsolutePath());
        logMeta.setDevInode(devInode);
        logMeta.setLastUpdateTime(file.lastModified());
        logMeta.setSourcePath(file.getAbsolutePath());
        int signBytes = file.length() > 1024 ? 1024 : (int) file.length();
        logMeta.setSignBytes(signBytes);
        int signature = calSignature(file.getAbsolutePath(), signBytes);
        logMeta.setSignature(signature);
        return logMeta;
    }

    /**
     * description: 文件的签名,使用日志文件的前bytes字节的hash
     *
     * @param sourcePath
     * @param signBytes
     * @return int
     * @author Hlingoes 2022/7/6
     */
    public static int calSignature(String sourcePath, int signBytes) {
        String firstBytes = FileHelper.readFirstBytes(sourcePath, signBytes);
        return firstBytes.hashCode();
    }

}
