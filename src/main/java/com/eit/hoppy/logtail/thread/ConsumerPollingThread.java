package com.eit.hoppy.logtail.thread;

import com.eit.hoppy.logtail.CacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
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
        List<String> batchReadContents = CacheManager.batchReadContents(1000);
        Path path = Paths.get("E:\\hulin_workspace\\file-message-server\\logs\\web-server-info.log");
        batchReadContents.forEach(content -> {
            try {
                Files.write(path, (content + "\n").getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
            } catch (IOException e) {
                logger.warn("consumer content error", e);
            }
        });
    }
}
