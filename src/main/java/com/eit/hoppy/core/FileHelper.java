package com.eit.hoppy.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
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
        Path path = Paths.get(filePath);
        BasicFileAttributes bfa = null;
        try {
            bfa = Files.readAttributes(path, BasicFileAttributes.class);
            Object innode = bfa.fileKey();
            if (Objects.isNull(innode)) {
                return "";
            }
            return innode.toString();
        } catch (IOException e) {
            logger.error("getFileInode error ", e);
            return "";
        }
    }

    public static List<File> getFileSort(String dir) {
        List<File> list = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(dir))) {
            for (Path path : stream) {
                list.add(path.toFile());
            }
            return list.stream().sorted(Comparator.comparing(File::lastModified)).collect(Collectors.toList());
        } catch (IOException | DirectoryIteratorException ex) {
            logger.warn("get sub files error: {}", dir, ex);
        }
        return list;
    }

}
