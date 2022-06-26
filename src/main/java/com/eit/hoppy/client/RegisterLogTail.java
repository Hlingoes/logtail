package com.eit.hoppy.client;

import com.eit.hoppy.logtail.CacheManager;
import com.eit.hoppy.logtail.LogMeta;
import com.eit.hoppy.logtail.polling.*;
import com.eit.hoppy.util.FileHelper;
import com.eit.hoppy.util.IniFileReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * description: 读取配置，注册LogTail
 *
 * @author Hlingoes
 * @date 2022/6/19 21:45
 */
public class RegisterLogTail {

    private static Logger logger = LoggerFactory.getLogger(RegisterLogTail.class);

    private static List<AbstractPollingThread> pollingThreads = new ArrayList<>();

    public static void startTask() {
        registerTask();
        pollingThreads.forEach(pollingThread -> pollingThread.start());
    }

    private static void registerTask() {
        IniFileReader iniReader;
        try {
            iniReader = new IniFileReader("logtail.properties");
            String dir = iniReader.getStrValue("scan_dir");
            int dirScanInterval = iniReader.getIntValue("dir_scan_interval", 1000);
            int eventDealInterval = iniReader.getIntValue("event_deal_interval", 5000);
            int rotateInterval = iniReader.getIntValue("rotate_interval", 5 * 60 * 1000);
            int consumeInterval = iniReader.getIntValue("consume_interval", 1000);
            initFileCache(dir, eventDealInterval);
            AbstractPollingThread dirThread = new DirFilePollingThread(dir, dirScanInterval);
            AbstractPollingThread fileThread = new FileModifyPollingThread(eventDealInterval);
            AbstractPollingThread rotateThread = new RotateReaderPollingThread(rotateInterval);
            AbstractPollingThread consumeThread = new ConsumerPollingThread(consumeInterval);
            pollingThreads.add(dirThread);
            pollingThreads.add(fileThread);
            pollingThreads.add(rotateThread);
            pollingThreads.add(consumeThread);
        } catch (IOException e) {
            logger.warn("can not find file: logtail.properties, us", e);
        }
    }

    private static void initFileCache(String dir, long period) {
        List<File> files = FileHelper.getFileSort(dir);
        files.forEach(file -> {
            LogMeta logMeta = new LogMeta(file);
            CacheManager.addFileCreateCache(logMeta);
        });
    }

    public static void stopTask() {
        pollingThreads.forEach(pollingThread -> pollingThread.setStopped(true));
    }

}
