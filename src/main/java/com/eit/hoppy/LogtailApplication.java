package com.eit.hoppy;

import com.eit.hoppy.logtail.LogtailClient;
import com.eit.hoppy.logtail.DemoConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * description: 文件会从末尾读取
 *
 * @author Hlingoes 2022/6/5
 * @citation Created by jaren.han on 2020-01-10.
 */
public class LogtailApplication {

    private static Logger logger = LoggerFactory.getLogger(LogtailApplication.class);

    public static void main(String[] args) {
        try {
            LogtailClient.start(new DemoConsumer());
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                // send de-registration request before destroy
                LogtailClient.stop();
            }));
        } catch (IOException e) {
            logger.error("Register logtail fail", e);
        }

    }
}
