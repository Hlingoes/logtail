package com.eit.hoppy.logtail.thread;

import com.eit.hoppy.logtail.CacheManager;
import com.eit.hoppy.logtail.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.List;

/**
 * description: 读取采集的日志
 *
 * @author Hlingoes
 * @date 2022/6/21 21:02
 */
public class ConsumerPollingThread extends AbstractPollingThread {

    private static final Logger logger = LoggerFactory.getLogger(ConsumerPollingThread.class);

    private Consumer consumer;

    public ConsumerPollingThread(long period, Consumer consumer) {
        super(ConsumerPollingThread.class.getSimpleName(), period);
        this.consumer = consumer;
    }

    @Override
    void polling() {
        List<String> batchReadContents = CacheManager.batchReadContents(1000);
        if (!batchReadContents.isEmpty()) {
            consumer.consumer(batchReadContents);
        }
    }
}
