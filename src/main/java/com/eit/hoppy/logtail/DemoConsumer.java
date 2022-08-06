package com.eit.hoppy.logtail;

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
 * description:
 *
 * @author Hlingoes
 * @date 2022/7/18 21:33
 */
public class DemoConsumer implements Consumer {
    private static final Logger logger = LoggerFactory.getLogger(DemoConsumer.class);

    public DemoConsumer() {
    }

    @Override
    public boolean consumer(List<String> datas) {
        Path path = Paths.get("E:\\hulin_workspace\\file-message-server\\logs\\web-server-info.log");
        datas.forEach(content -> {
            try {
                Files.write(path, (content + "\n").getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
            } catch (IOException e) {
                logger.warn("consumer content error", e);
            }
        });
        return true;
    }
}
