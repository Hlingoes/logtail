package com.eit.hoppy.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * description:
 *
 * @author Hlingoes 2022/6/5
 * @citation Created by jaren.han on 2020-01-10.
 */
public class LogTest {

    static Logger loggerInfo = LoggerFactory.getLogger("infoLog");
    static Logger loggerError = LoggerFactory.getLogger("errorLog");
    static Logger loggerWarn = LoggerFactory.getLogger("warnLog");

    static AtomicInteger atomicInteger = new AtomicInteger();

    public static void main(String[] args) {

        final Thread info_thread = new Thread(() -> {
            while (true) {
                loggerInfo.info("[write info]now time:{} uuid:{} num:{}", new Date(), UUID.randomUUID().toString(), atomicInteger.incrementAndGet());
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        info_thread.start();

        final Thread error_thread = new Thread(() -> {
            while (true) {
                loggerError.error("[error info]now time:{},uuid:{} num:{}", new Date(), UUID.randomUUID().toString(), atomicInteger.incrementAndGet());
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        error_thread.start();

        final Thread warn_thread = new Thread(() -> {
            while (true) {
                loggerWarn.warn("[warn info]now time:{},uuid:{} num:{}", new Date(), UUID.randomUUID().toString(), atomicInteger.incrementAndGet());
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {

                }
            }
        });
        warn_thread.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            info_thread.interrupt();
            error_thread.interrupt();
            warn_thread.interrupt();
        }));
    }
}
