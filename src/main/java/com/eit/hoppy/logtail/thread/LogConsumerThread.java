package com.eit.hoppy.logtail.thread;

import com.eit.hoppy.logtail.CacheManager;
import com.eit.hoppy.logtail.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * description: 读取采集的日志
 *
 * @author Hlingoes
 * @date 2022/6/21 21:02
 */
public class LogConsumerThread extends AbstractPollingThread {

    private static final Logger logger = LoggerFactory.getLogger(LogConsumerThread.class);

    private Consumer consumer;

    public LogConsumerThread(long period, Consumer consumer) {
        super(LogConsumerThread.class.getSimpleName(), period);
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
