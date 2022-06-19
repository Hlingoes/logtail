package com.eit.hoppy.logtail.polling;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * description: 定义线程组
 *
 * @author Hlingoes
 * @date 2022/6/11 22:41
 */
public abstract class AbstractPollingThread extends Thread {
    private static Logger logger = LoggerFactory.getLogger(AbstractPollingThread.class);

    private static final ThreadGroup POLLING_THREAD_GROUP = new ThreadGroup("PollingThread");

    private boolean stopped;
    /**
     * 轮询间隔，默认是5000毫秒
     */
    private long period = 5000L;

    public AbstractPollingThread(String name, long period) {
        super(POLLING_THREAD_GROUP, name);
        this.period = period;
//        setDaemon(true);
    }

    @Override
    public void run() {
        while (true) {
            try {
                polling();
                if (stopped) {
                    logger.warn("the application is stopped");
                    break;
                }
                Thread.sleep(period);
            } catch (Exception ex) {
                logger.error("polling error", ex);
            }
        }
    }

    /**
     * 执行轮询任务
     */
    abstract void polling();

    public boolean isStopped() {
        return stopped;
    }

    public void setStopped(boolean stopped) {
        this.stopped = stopped;
    }

    public long getPeriod() {
        return period;
    }

    public void setPeriod(long period) {
        this.period = period;
    }
}
