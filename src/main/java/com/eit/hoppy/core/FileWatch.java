package com.eit.hoppy.core;

import com.eit.hoppy.util.FileHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * description:
 *
 * @author Hlingoes 2022/6/5
 * @citation Created by jaren.han on 2020-01-10.
 */
public class FileWatch {

    Logger logger = LoggerFactory.getLogger(FileWatch.class);

    private String dirPath;
    private Map<String, FileMeta> fileMap = new HashMap<String, FileMeta>();
    private boolean needRunning = false;
    private Thread dirScanWork;
    private Thread cacheCheckWork;
    private Thread eventProcess;
    private BlockingQueue<FileMeta> fileMetas = new ArrayBlockingQueue<FileMeta>(1000);
    static final ThreadGroup ELEMENTS_THREAD_GROUP = new ThreadGroup("FileWatch");


    public FileWatch(String dirPath) {
        this.dirPath = dirPath;
        //  eventHandle = new FileEventHandle();
    }

    public synchronized void start() {
        try {
            if (needRunning) {
                return;
            }
            needRunning = true;
            dirScanWork = new Thread(new DirScanWork("dir-scan"));
            dirScanWork.start();

            cacheCheckWork = new Thread(new FileMofifyScanWork("check-modify"));
            cacheCheckWork.start();

            eventProcess = new Thread(new EventProcess("event-process"));
            eventProcess.start();

        } catch (Exception ex) {
            logger.error("start", ex);
        }
    }

    public synchronized void stop() {
        try {
            needRunning = false;
            if (dirScanWork != null) {
                dirScanWork.interrupt();
            }
            if (cacheCheckWork != null) {
                cacheCheckWork.interrupt();
            }
            if (eventProcess != null) {
                eventProcess.interrupt();
            }
        } catch (Exception ex) {
            logger.error("stop", ex);
        }
    }

    /**
     * 事件处理
     **/
    private class EventProcess extends StageThread {

        EventProcess(String name) {
            super(name);
        }

        @Override
        void work() {
            try {
                FileMeta fileMeta = fileMetas.take();
                if (fileMeta != null) {
                    //eventHandle.handleFile(fileMeta);
                }
            } catch (InterruptedException e) {
                logger.error("event-process", e);
            }
        }
    }

    /***
     *  scan directory
     * */
    private class DirScanWork extends StageThread {

        private DirScanWork(String name) {
            super(name);
        }

        @Override
        void work() {
            try {
                getFiles(dirPath);
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                logger.error("dir work", e);
            }
        }

        private void getFiles(String dirPath) {
            List<File> files = FileHelper.getFileSort(dirPath);
            FileMeta fileMeta;
            for (File file : files) {
                if (!fileMap.containsKey(file.getAbsolutePath())) {
                    fileMeta = new FileMeta();
                    fileMeta.setFile(file);
                    fileMeta.setLastModifyTime(file.lastModified());
                    fileMeta.setFileInode(FileHelper.getFileInode(file.getAbsolutePath()));
                    fileMeta.setSrcFilepath(file.getAbsolutePath());
                    fileMap.put(file.getAbsolutePath(), fileMeta);
                }
            }
        }
    }

    /**
     * check file modify
     */
    private class FileMofifyScanWork extends StageThread {

        FileMofifyScanWork(String name) {
            super(name);
        }

        @Override
        void work() {
            try {
                if (fileMap.size() > 0) {
                    for (Map.Entry<String, FileMeta> file : fileMap.entrySet()) {
                        FileMeta fileMeta = file.getValue();
                        //file be rooling
                        String fileInode = FileHelper.getFileInode(file.getValue().getSrcFilepath());
                        if (!fileMeta.getFileInode().equals(fileInode)) {
                            fileMeta.setFileInode(fileInode);
                            fileMeta.setFileState(FileState.CREATE);
                        } else {
                            File reGetFile = new File(file.getValue().getSrcFilepath());
                            if (reGetFile.lastModified() != fileMeta.getLastModifyTime()) {
                                fileMeta.setFileState(FileState.MODIFY);
                                fileMeta.setLastModifyTime(reGetFile.lastModified());
                                logger.info("file:{} fire changed", file.getKey());
                            } else {
                                fileMeta.setFileState(FileState.NOCHANGE);
                            }
                        }
                        fileMetas.add(fileMeta);
                        //fileEventHandle.handleFile(fileMeta);
                    }
                }
                Thread.sleep(3000);
            } catch (Exception ex) {
                logger.error("FileMofifyScanWork.work", ex);
            }
        }
    }

    private abstract class StageThread extends Thread {

        public StageThread(String name) {
            super(ELEMENTS_THREAD_GROUP, name);
            setDaemon(true);
        }

        @Override
        public void run() {
            while (true) {
                try {
                    work();
                    if (!needRunning) {
                        logger.warn("the application is stop");
                        break;
                    }
                } catch (Exception ex) {
                    logger.error("work error", ex);
                }
            }
        }

        abstract void work();
    }

}
