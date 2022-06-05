package com.eit.hoppy.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * description:
 *
 * @author Hlingoes 2022/6/5
 * @citation Created by jaren.han on 2020-01-13.
 */
public class FileEventHandle {

    static Logger logger = LoggerFactory.getLogger(FileEventHandle.class);

    /**
     * key:srcPath
     * value: fileReader
     */
    private static Map<String, List<LogFileReader>> logFileReaderQueue = new HashMap<String, List<LogFileReader>>();
    /**
     * 用于存储当前正在读取的所有Reader
     * key:inode
     */
    private static Map<String, LogFileReader> devInodeLogFileReaderMap = new HashMap<String, LogFileReader>();

    private static Map<String, LogFileReader> ronateLogfileReader = new HashMap<String, LogFileReader>();

    /**
     * 接收来自 filewatch  的列表
     */
    private LinkedList<FileMeta> watchList = new LinkedList<FileMeta>();

    /**
     * 接收来自 notify 系统通知文件变化  的列表
     */
    private LinkedList<FileMeta> notifyList = new LinkedList<FileMeta>();


    public void handleFile(FileMeta fileMeta) {
        //let reader read log
        if (fileMeta.getFileState() == FileState.MODIFY) {
            List<LogFileReader> logFileReaders = logFileReaderQueue.get(fileMeta.getSrcFilepath());
            if (logFileReaders != null && logFileReaders.size() > 0) {
                for (LogFileReader logFileReader : logFileReaders) {
                    if (logFileReader.getFile().length() == logFileReader.getReadOffset() && logFileReaders.size() > 1) {
                        ronateLogfileReader.put(fileMeta.getFileInode(), logFileReader);
                        logFileReaders.remove(logFileReader);
                        logger.info("file read finish:{}", fileMeta.toString());
                    } else {
                        logger.info("file start read:{}", fileMeta.toString());
                        logFileReader.readLog();
                    }
                }
            }
        } else if (fileMeta.getFileState() == FileState.CREATE) {
            if (!devInodeLogFileReaderMap.containsKey(fileMeta.getFileInode())) {
                LogFileReader logFileReader = new LogFileReader(fileMeta);
                devInodeLogFileReaderMap.put(fileMeta.getFileInode(), logFileReader);
                if (!logFileReaderQueue.containsKey(fileMeta.getSrcFilepath())) {
                    LinkedList<LogFileReader> logFileReaders = new LinkedList<LogFileReader>();
                    logFileReaders.add(logFileReader);
                    logFileReaderQueue.put(fileMeta.getSrcFilepath(), logFileReaders);
                } else {
                    List<LogFileReader> logFileReaders = logFileReaderQueue.get(fileMeta.getSrcFilepath());
                    logFileReaders.add(logFileReader);
                }
            }
        } else if (fileMeta.getFileState() == FileState.DELETE) {

        }
    }

//    public void run() {
//        LinkedList<FileMeta> l1 = new LinkedList<>();
//        LinkedList<FileMeta> l2 = new LinkedList<>();
//        for (int i = 0; i < watchList.size(); i++) {
//            l1.add(watchList.remove(i));
//        }
//        for (int i = 0; i < notifyList.size(); i++) {
//            l2.add(notifyList.remove(i));
//        }
//        LinkedList<FileMeta> mergeList = merge(l1, l2);
//
//    }
//
//    /***
//      *
//     * **/
//    private LinkedList<FileMeta> merge(LinkedList<FileMeta> a, LinkedList<FileMeta> b) {
//        LinkedList<FileMeta> fileMetas = new LinkedList<>();
//        if (a.size() > 0 && b.size() == 0) {
//            fileMetas.addAll(a);
//        } else if (a.size() == 0 && b.size() > 0) {
//            fileMetas.addAll(b);
//        } else if (a.size() > 0 && b.size() > 0) {
//            fileMetas.addAll(a);
//            for (FileMeta fileMeta : b) {
//                boolean match = fileMetas.
//                        stream().
//                        anyMatch(
//                                c ->
//                                        c.getFileState() == fileMeta.getFileState() && c.getFileInode().equals(fileMeta.getFileInode())
//                        );
//                if (!match) {
//                    fileMetas.add(fileMeta);
//                }
//            }
//        }
//        return fileMetas;
//    }

//    public class fileEventReceive implements FileEventReceive {
//
//        public void receiveWatch(FileMeta fileMeta) {
//            watchList.add(fileMeta);
//        }
//
//        public void receiveInotify(FileMeta fileMeta) {
//
//        }
//    }
}
