package com.eit.hoppy.core;

import java.util.LinkedList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * description:
 *
 * @author Hlingoes 2022/6/5
 * @citation Created by jaren.han on 2020-01-20.
 */
public class FileEventsReceive implements FileEventReceive {

    /**
     * 接收来自 filewatch  的列表
     */
    private LinkedList<FileMeta> watchList = new LinkedList<FileMeta>();

    /**
     * 接收来自 notify 系统通知文件变化  的列表
     */
    private LinkedList<FileMeta> notifyList = new LinkedList<FileMeta>();

    private FileEventHandle fileEventHandle;

    private ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

    public FileEventsReceive() {

    }

    @Override
    public void receiveWatch(FileMeta fileMeta) {
        this.watchList.add(fileMeta);
    }

    @Override
    public void receiveInotify(FileMeta fileMeta) {
        this.notifyList.add(fileMeta);
    }

    public void setFileEventHandle(FileEventHandle fileEventHandle) {
        this.fileEventHandle = fileEventHandle;
    }

    /***
     *
     * **/
    private LinkedList<FileMeta> merge(LinkedList<FileMeta> a, LinkedList<FileMeta> b) {
        LinkedList<FileMeta> fileMetas = new LinkedList<>();
        if (a.size() > 0 && b.size() == 0) {
            fileMetas.addAll(a);
        } else if (a.size() == 0 && b.size() > 0) {
            fileMetas.addAll(b);
        } else if (a.size() > 0 && b.size() > 0) {
            fileMetas.addAll(a);
            for (FileMeta fileMeta : b) {
                boolean match = fileMetas.stream().anyMatch(
                        c -> c.getFileState() == fileMeta.getFileState() && c.getFileInode().equals(fileMeta.getFileInode())
                );
                if (!match) {
                    fileMetas.add(fileMeta);
                }
            }
        }
        return fileMetas;
    }

    class FileProcess implements Runnable {

        @Override
        public void run() {
            LinkedList<FileMeta> l1 = new LinkedList<>();
            LinkedList<FileMeta> l2 = new LinkedList<>();
            for (int i = 0; i < watchList.size(); i++) {
                l1.add(watchList.remove(i));
            }
            for (int i = 0; i < notifyList.size(); i++) {
                l2.add(notifyList.remove(i));
            }
            LinkedList<FileMeta> mergeList = merge(l1, l2);

        }
    }

}
