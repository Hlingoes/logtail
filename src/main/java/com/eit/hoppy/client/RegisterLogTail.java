package com.eit.hoppy.client;

import com.eit.hoppy.logtail.polling.*;

import java.util.ArrayList;
import java.util.List;

/**
 * description: 读取配置，注册LogTail
 *
 * @author Hlingoes
 * @date 2022/6/19 21:45
 */
public class RegisterLogTail {

    private static List<AbstractPollingThread> pollingThreads = new ArrayList<>();

    private static void registerTask() {
        AbstractPollingThread dirThread = new DirFilePollingThread("E:\\hulin_workspace\\file-message-server\\logs");
        AbstractPollingThread fileThread = new FileModifyPollingThread();
        AbstractPollingThread eventHandleThread = new FileEventPollingThread();
        AbstractPollingThread rotateThread = new RotateReaderPollingThread();
        pollingThreads.add(dirThread);
        pollingThreads.add(fileThread);
        pollingThreads.add(eventHandleThread);
        pollingThreads.add(rotateThread);
    }

    public static void startTask() {
        registerTask();
        pollingThreads.forEach(pollingThread -> pollingThread.start());
    }


    public static void stopTask() {
        pollingThreads.forEach(pollingThread -> pollingThread.setStopped(true));
    }

}
