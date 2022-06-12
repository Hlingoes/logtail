package com.eit.hoppy.logtail.thread;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

/**
 * description:
 *
 * @author Hlingoes
 * @date 2022/6/12 15:46
 */
public class ThreadFactoryBuilder {
    private static Logger logger = LoggerFactory.getLogger(ThreadFactoryBuilder.class);

    private String nameFormat = null;
    private boolean daemon = false;
    private int priority = Thread.NORM_PRIORITY;

    public ThreadFactoryBuilder setNameFormat(String nameFormat) {
        if (nameFormat == null) {
            throw new NullPointerException();
        }
        this.nameFormat = nameFormat;
        return this;
    }

    public ThreadFactoryBuilder setDaemon(boolean daemon) {
        this.daemon = daemon;
        return this;
    }

    public ThreadFactoryBuilder setPriority(int priority) {
        if (priority < Thread.MIN_PRIORITY) {
            throw new IllegalArgumentException(String.format(
                    "Thread priority (%s) must be >= %s", priority, Thread.MIN_PRIORITY));
        }

        if (priority > Thread.MAX_PRIORITY) {
            throw new IllegalArgumentException(String.format(
                    "Thread priority (%s) must be <= %s", priority, Thread.MAX_PRIORITY));
        }

        this.priority = priority;
        return this;
    }

    public ThreadFactory build() {
        return build(this);
    }

    private static ThreadFactory build(ThreadFactoryBuilder builder) {
        final String nameFormat = builder.nameFormat;
        final Boolean daemon = builder.daemon;
        final Integer priority = builder.priority;
        final AtomicLong count = new AtomicLong(0);

        return (Runnable runnable) -> {
            Thread thread = new Thread(runnable);
            if (nameFormat != null) {
                thread.setName(String.format(nameFormat, count.getAndIncrement()));
            }
            if (daemon != null) {
                thread.setDaemon(daemon);
            }
            thread.setPriority(priority);
            thread.setUncaughtExceptionHandler((t, e) -> {
                String threadName = t.getName();
                logger.error("error occurred! threadName: {}, error msg: {}", threadName, e.getMessage(), e);
            });
            return thread;
        };
    }
}
