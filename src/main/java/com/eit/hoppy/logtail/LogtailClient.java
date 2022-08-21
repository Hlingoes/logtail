package com.eit.hoppy.logtail;

import com.eit.hoppy.logtail.thread.*;
import com.eit.hoppy.util.IniFileReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * description: 读取配置，注册LogTail
 *
 * @author Hlingoes
 * @date 2022/6/19 21:45
 */
public class LogtailClient {

    private static Logger logger = LoggerFactory.getLogger(LogtailClient.class);

    private static List<AbstractPollingThread> pollingThreads = new ArrayList<>();

    private static String dir;
    private static int dirScanInterval;
    private static int eventDealInterval;
    private static int rotateInterval;
    private static int consumeInterval;
    private static Consumer dataConsumer;

    public static void start(Consumer consumer) throws IOException {
        dataConsumer = consumer;
        initProperties();
        initThread();
        initFileCache();
        startThread();
    }

    private static void initProperties() {
        IniFileReader iniReader;
        try {
            iniReader = new IniFileReader("logtail.properties");
            dir = iniReader.getStrValue("scan_dir");
            dirScanInterval = iniReader.getIntValue("dir_scan_interval", 1000);
            eventDealInterval = iniReader.getIntValue("event_deal_interval", 5000);
            rotateInterval = iniReader.getIntValue("rotate_interval", 5 * 60 * 1000);
            consumeInterval = iniReader.getIntValue("consume_interval", 1000);
        } catch (IOException e) {
            logger.warn("can not find file: logtail.properties, us", e);
        }
    }

    private static void initThread() {
        AbstractPollingThread dirThread = new DirScanThread(dir, dirScanInterval);
        AbstractPollingThread fileThread = new FileModifyThread(eventDealInterval);
        AbstractPollingThread rotateThread = new EventHandlerThread(rotateInterval);
        AbstractPollingThread consumeThread = new LogConsumerThread(consumeInterval, dataConsumer);
        pollingThreads.add(dirThread);
        pollingThreads.add(fileThread);
        pollingThreads.add(rotateThread);
        pollingThreads.add(consumeThread);
    }

    private static void initFileCache() throws IOException {
        CacheManager.addFileCache(dir);
        CacheManager.recoverCheckPointFile();
    }

    private static void startThread() {
        pollingThreads.forEach(pollingThread -> pollingThread.start());
    }

    public static void stop() {
        // 通知线程停止任务
        pollingThreads.forEach(pollingThread -> pollingThread.setStopped(true));
        // 消费已读取的日志信息
        List<String> remainContents = CacheManager.remainContents();
        dataConsumer.consumer(remainContents);
    }

}
