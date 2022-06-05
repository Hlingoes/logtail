package com.eit.hoppy;

import com.eit.hoppy.core.FileWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * description:
 *
 * @author Hlingoes 2022/6/5
 * @citation Created by jaren.han on 2020-01-10.
 */
public class LogtailApplication {

    static Logger logger = LoggerFactory.getLogger(LogtailApplication.class);

    public static void main(String[] args) {
        final FileWatch fileWatch = new FileWatch("/opt/logs/logtail/");
        // final FileWatch fileWatch = new FileWatch("D:\\opt\\logs\\quechub-lwm2m");
        try {
            fileWatch.start();
            logger.info("start");
        } catch (Exception ex) {
            logger.error("start error", ex);
        }
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            // send de-registration request before destroy
            fileWatch.stop();
        }));
    }
}
