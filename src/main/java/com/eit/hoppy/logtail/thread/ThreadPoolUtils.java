package com.eit.hoppy.logtail.thread;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

/**
 * description: 创建通用的线程池
 * <p>
 * corePoolSize：线程池中核心线程数量
 * maximumPoolSize：线程池同时允许存在的最大线程数量
 * 内部处理逻辑如下：
 * 当线程池中工作线程数小于corePoolSize，创建新的工作线程来执行该任务，不管线程池中是否存在空闲线程。
 * 如果线程池中工作线程数达到corePoolSize，新任务尝试放入队列，入队成功的任务将等待工作线程空闲时调度。
 * 1. 如果队列满并且线程数小于maximumPoolSize，创建新的线程执行该任务(注意：队列中的任务继续排序)。
 * 2. 如果队列满且线程数超过maximumPoolSize，拒绝该任务
 * <p>
 * keepAliveTime
 * 当线程池中工作线程数大于corePoolSize，并且线程空闲时间超过keepAliveTime，则这些线程将被终止。
 * 同样，可以将这种策略应用到核心线程，通过调用allowCoreThreadTimeout来实现。
 * <p>
 * BlockingQueue
 * 任务等待队列，用于缓存暂时无法执行的任务。分为如下三种堵塞队列：
 * 1. 直接递交，如SynchronousQueue，该策略直接将任务直接交给工作线程。如果当前没有空闲工作线程，创建新线程。
 * 这种策略最好是配合unbounded线程数来使用，从而避免任务被拒绝。但当任务生产速度大于消费速度，将导致线程数不断的增加。
 * 2. 无界队列，如LinkedBlockingQueue，当工作的线程数达到核心线程数时，新的任务被放在队列上。
 * 因此，永远不会有大于corePoolSize的线程被创建，maximumPoolSize参数失效。
 * 这种策略比较适合所有的任务都不相互依赖，独立执行。
 * 但是当任务处理速度小于任务进入速度的时候会引起队列的无限膨胀。
 * 3. 有界队列，如ArrayBlockingQueue，按前面描述的corePoolSize、maximumPoolSize、BlockingQueue处理逻辑处理。
 * 队列长度和maximumPoolSize两个值会相互影响：
 * 长队列 + 小maximumPoolSize。会减少CPU的使用、操作系统资源、上下文切换的消耗，但是会降低吞吐量，
 * 如果任务被频繁的阻塞如IO线程，系统其实可以调度更多的线程。
 * 短队列 + 大maximumPoolSize。CPU更忙，但会增加线程调度的消耗.
 * 总结一下，IO密集型可以考虑多些线程来平衡CPU的使用，CPU密集型可以考虑少些线程减少线程调度的消耗
 *
 * @author Hlingoes
 * @citation https://blog.csdn.net/wanghao112956/article/details/99292107
 * @citation https://www.jianshu.com/p/896b8e18501b
 * @date 2020/2/26 0:46
 */
public class ThreadPoolUtils {
    private static Logger logger = LoggerFactory.getLogger(ThreadPoolUtils.class);

    /**
     * description: 创建线程池
     *
     * @param
     * @return java.util.concurrent.ThreadPoolExecutor
     * @author Hlingoes 2020/3/20
     */
    /**
     * description: 创建线程池
     *
     * @param threadNamePrefix 线程名前缀
     * @param corePoolSize 核心线程数
     * @param maxPoolSize 最大线程数
     * @param keepAliveTime 线程存活时间(ms)
     * @param blockingQueueSize 阻塞队列的大小
     * @return java.util.concurrent.ThreadPoolExecutor
     * @author Hlingoes 2022/6/12
     */
    public static ThreadPoolExecutor createThreadPool(String threadNamePrefix, int corePoolSize, int maxPoolSize,
                                                      long keepAliveTime, int blockingQueueSize) {
        ThreadFactory factory = new ThreadFactoryBuilder().setNameFormat(threadNamePrefix + "-%d").build();
        BlockingQueue<Runnable> queue = new ArrayBlockingQueue<>(blockingQueueSize);
        ThreadPoolExecutor poolExecutor = new ThreadPoolExecutor(corePoolSize, maxPoolSize, keepAliveTime,
                TimeUnit.MILLISECONDS, queue, factory, defineRejectedExecutionHandler()) {
            /**
             * description: 针对提交给线程池的任务可能会抛出异常这一问题，
             * 可自行实现线程池的afterExecute方法，或者实现Thread的UncaughtExceptionHandler接口
             * ThreadFactoryBuilder中已经实现了UncaughtExceptionHandler接口，这里是为了进一步兼容
             *
             * @param r
             * @param t
             * @return void
             * @author Hlingoes 2020/5/27
             */
            @Override
            protected void afterExecute(Runnable r, Throwable t) {
                super.afterExecute(r, t);
                if (t == null && r instanceof Future<?>) {
                    try {
                        Future<?> future = (Future<?>) r;
                        future.get();
                    } catch (CancellationException ce) {
                        t = ce;
                    } catch (ExecutionException ee) {
                        t = ee.getCause();
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
                if (t != null) {
                    logger.error("customThreadPool error msg: {}", t.getMessage(), t);
                }
            }
        };
        return poolExecutor;
    }

    /**
     * description: 自定义的拒绝策略
     * 当提交给线程池的某一个新任务无法直接被线程池中“核心线程”直接处理，
     * 又无法加入等待队列，也无法创建新的线程执行；
     * 又或者线程池已经调用shutdown()方法停止了工作；
     * 又或者线程池不是处于正常的工作状态；
     * 这时候ThreadPoolExecutor线程池会拒绝处理这个任务
     *
     * @param
     * @return java.util.concurrent.RejectedExecutionHandler
     * @author Hlingoes 2022/6/12
     */
    private static RejectedExecutionHandler defineRejectedExecutionHandler() {
        return (r, executor) -> {
            if (!executor.isShutdown()) {
                logger.warn("ThreadPoolExecutor is over working, please check the thread tasks! ");
            }
            throw new RejectedExecutionException("Task " + r.toString() + " rejected from " + executor.toString());
        };
    }

}