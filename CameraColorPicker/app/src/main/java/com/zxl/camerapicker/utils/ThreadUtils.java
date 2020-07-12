package com.zxl.camerapicker.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by carry on 18-9-5.
 */

public class ThreadUtils {

    /**
     * 异步可变式线程池执行任务
     * @param r
     */
    public static void asynCachedExecuteTask(Runnable r){
        CachedThreadPool.executor.execute(r);
    }

    /**
     * 异步固定线程池执行任务
     * @param r
     */
    public static void asynFixedExecuteTask(Runnable r){
        FixedThreadPool.executor.execute(r);
    }

    /**
     * 异步单一线程池执行任务(任务需要排队等待，需要保证执行顺序的可以用此方法)
     * @param r
     */
    public static void asynSingleExecuteTask(Runnable r){
        SingleThreadPool.executor.execute(r);
    }

    /**
     * 延时xxx毫秒执行任务
     * @param r
     * @param delayMillis 毫秒
     */
    public static void asynScheduledExecuteTask(Runnable r,long delayMillis){
        ScheduledThreadPool.executor.schedule(r,delayMillis, TimeUnit.MILLISECONDS);
    }

    /**
     * 每隔xxx毫秒，执行固定任务
     * @param r
     * @param initialDelayMillis  第一次执行的时延
     * @param periodMillis    间隔时间
     */
    public static void asynScheduleAtFixedRate(Runnable r,long initialDelayMillis, long periodMillis){
        ScheduledThreadPool.executor.scheduleAtFixedRate(r,initialDelayMillis,periodMillis,TimeUnit.MILLISECONDS);
    }

    /**
     * 上次任务执行结束后，间隔xxx毫秒再次执行任务
     * @param r
     * @param initialDelayMillis  第一次执行的时延
     * @param delayMillis 等待时延
     */
    public static void asynScheduleWithFixedDelay(Runnable r,long initialDelayMillis, long delayMillis){
        ScheduledThreadPool.executor.scheduleWithFixedDelay(r,initialDelayMillis,delayMillis,TimeUnit.MILLISECONDS);
    }

    private static class CachedThreadPool {
        /**
         * - CachedThreadPool是一种可以无限扩容的线程池；
           - CachedThreadPool比较适合执行时间片比较小的任务；
           - keepAliveTime为60，意味着线程空闲时间超过60s就会被杀死；
           - 阻塞队列采用SynchronousQueue，这种阻塞队列没有存储空间，意味着只要有任务到来，就必须得有一个工作线程来处理，如果当前没有空闲线程，那么就再创建一个新的线程。
         */
        public static ExecutorService executor = Executors.newCachedThreadPool();
    }

    private static class FixedThreadPool {
        /**
         * - FixedThreadPool是一种容量固定的线程池；
           - 阻塞队列采用LinkedBlockingQueue，它是一种无界队列；
           - 由于阻塞队列是一个无界队列，因此永远不可能拒绝执行任务；
           - 由于采用无界队列，实际线程数将永远维持在nThreads，因此maximumPoolSize和keepAliveTime将无效。
         */
        public static ExecutorService executor = Executors.newFixedThreadPool(5);
    }

    private static class SingleThreadPool {
        /**
         * - SingleThreadExecutor只会创建一个工作线程来处理任务。
         */
        public static ExecutorService executor = Executors.newSingleThreadExecutor();
    }

    private static class ScheduledThreadPool {
        /**
         *  - ScheduledThreadPool接收SchduledFutureTask类型的任务，提交任务的方式有2种；
             1. scheduledAtFixedRate；
             2. scheduledWithFixedDelay；
            - SchduledFutureTask接收参数：
             time：任务开始时间
             sequenceNumber：任务序号
             period：任务执行的时间间隔
            - 阻塞队列采用DelayQueue，它是一种无界队列；
            - DelayQueue内部封装了一个PriorityQueue，它会根据time的先后排序，若time相同，则根据sequenceNumber排序；
            - 工作线程执行流程：
             1. 工作线程会从DelayQueue中取出已经到期的任务去执行；
             2. 执行结束后重新设置任务的到期时间，再次放回DelayQueue。
         */
        public static ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
    }

}
