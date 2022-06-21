package com.eit.hoppy.logtail.polling;

import com.eit.hoppy.logtail.CacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * description: 读取采集的日志
 *
 * @author Hlingoes
 * @date 2022/6/21 21:02
 */
public class ConsumerPollingThread extends AbstractPollingThread {

    private static final Logger logger = LoggerFactory.getLogger(ConsumerPollingThread.class);

    public ConsumerPollingThread() {
        super(ConsumerPollingThread.class.getSimpleName(), 1000L);
    }

    public ConsumerPollingThread(long period) {
        super(ConsumerPollingThread.class.getSimpleName(), period);
    }

    @Override
    void polling() {
        List<String> batchReadContents = CacheManager.batchReadContents(10);
        batchReadContents.forEach(content -> {
            logger.info(content);
        });
    }
}
