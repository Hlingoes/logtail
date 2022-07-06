package com.eit.hoppy.util;

import com.eit.hoppy.command.ExecuteResult;
import com.eit.hoppy.command.WinCommandExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * description:
 *
 * @author Hlingoes 2022/6/5
 * @citation Created by jaren.han on 2020-01-10.
 */
public class FileHelper {

    private static Logger logger = LoggerFactory.getLogger(FileHelper.class);

    public static String getFileInode(String filePath) {
        // windows系统需要调用命令行获取fileId, linux则通过java api获取
        if (OsHelper.isWindows()) {
            return queryWindowsFileIdOrDefault(filePath);
        }
        return getDevInodeOrDefault(filePath);
    }

    /**
     * description: 通过java api获取devInode，如果没找到，则默认返回filePath
     *
     * @param filePath
     * @return java.lang.String
     * @author Hlingoes 2022/6/12
     */
    public static String getDevInodeOrDefault(String filePath) {
        Path path = Paths.get(filePath);
        BasicFileAttributes bfa = null;
        try {
            bfa = Files.readAttributes(path, BasicFileAttributes.class);
            Object devInode = bfa.fileKey();
            if (Objects.isNull(devInode)) {
                return filePath;
            }
            return devInode.toString();
        } catch (IOException e) {
            logger.error("getFileInode error ", e);
        }
        return filePath;
    }

    /**
     * description: 在windows系统获取文件id，如果没找到，则默认返回filePath
     * 适用于：Windows Server 2022、Windows Server 2019、Windows Server 2016、Windows 10、Windows Server 2012 R2、Windows 8.1、Windows Server 2012、Windows 8
     * fsutil file queryfileid .\pom.xml
     *
     * @param filePath 文件路径
     * @return java.lang.String
     * @author Hlingoes 2022/6/12
     */
    public static String queryWindowsFileIdOrDefault(String filePath) {
        List<String> command = new ArrayList<>();
        command.add("cmd");
        command.add("/c");
        command.add("fsutil");
        command.add("file");
        command.add("queryfileid");
        command.add(filePath);
        WinCommandExecutor commandExecutor = new WinCommandExecutor(command, 1000);
        ExecuteResult result = commandExecutor.executeCommand();
        if (result.getExitCode() == -1) {
            return filePath;
        }
        return result.getContent();
    }

    public static List<File> getFileSort(String path) {
        List<File> list = new ArrayList<>();
        getFiles(path, list);
        return list.stream().sorted(Comparator.comparing(File::lastModified)).collect(Collectors.toList());
    }

    public static List<File> getFiles(String realpath, List<File> files) {
        File realFile = new File(realpath);
        if (realFile.isDirectory()) {
            File[] subFiles = realFile.listFiles();
            for (File file : subFiles) {
                if (file.isDirectory()) {
                    getFiles(file.getAbsolutePath(), files);
                } else {
                    files.add(file);
                }
            }
        }
        return files;
    }

    /**
     * description: 读取文件的前bytes字节
     *
     * @param filePath
     * @param bytes
     * @return java.lang.String
     * @author Hlingoes 2022/6/12
     */
    public static String readFirstBytes(String filePath, int bytes) {
        Path fPath = Paths.get(filePath);
        char[] buff = new char[bytes];
        try (BufferedReader bfr = Files.newBufferedReader(fPath, Charset.forName("UTF-8"))) {
            bfr.read(buff, 0, bytes);
            return String.valueOf(buff);
        } catch (IOException e) {
            logger.warn("readFirstBytes fail", e);
        }
        return "";
    }

}
