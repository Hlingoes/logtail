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

    private boolean interrupt;

    public AbstractPollingThread(String name) {
        super(POLLING_THREAD_GROUP, name);
        setDaemon(true);
    }

    @Override
    public void run() {
        while (true) {
            try {
                polling();
                if (!interrupt) {
                    logger.warn("the application is stop");
                    break;
                }
            } catch (Exception ex) {
                logger.error("polling error", ex);
            }
        }
    }

    /**
     * 执行轮询任务
     */
    abstract void polling();

    public boolean getInterrupt() {
        return interrupt;
    }

    public void setInterrupt(boolean interrupt) {
        this.interrupt = interrupt;
    }

}
