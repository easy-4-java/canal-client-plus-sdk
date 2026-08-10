/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.otter.canal.util;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Utility class for creating and managing thread pools, thread factories,
 * and graceful shutdown of threads and executor services.
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 3.0.0
 */
@Slf4j
public final class ThreadUtils {

    /**
     * Creates a new thread pool executor with the given parameters and a custom thread factory.
     *
     * @param corePoolSize    the core pool size
     * @param maximumPoolSize the maximum pool size
     * @param keepAliveTime   the keep-alive time for idle threads
     * @param unit            the time unit for keep-alive
     * @param workQueue       the blocking queue for tasks
     * @param processName     the name prefix for threads
     * @param isDaemon        whether threads should be daemon threads
     * @return the new thread pool executor
     */
    public static ExecutorService newThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime,
                                                        TimeUnit unit, BlockingQueue<Runnable> workQueue, String processName, boolean isDaemon) {
        return new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, newThreadFactory(processName, isDaemon));
    }

    /**
     * Creates a new single-thread executor with a custom thread factory.
     *
     * @param processName the name prefix for the thread
     * @param isDaemon    whether the thread should be a daemon thread
     * @return the new single-thread executor
     */
    public static ExecutorService newSingleThreadExecutor(String processName, boolean isDaemon) {
        return Executors.newSingleThreadExecutor(newThreadFactory(processName, isDaemon));
    }

    /**
     * Creates a new single-thread scheduled executor with a custom thread factory.
     *
     * @param processName the name prefix for the thread
     * @param isDaemon    whether the thread should be a daemon thread
     * @return the new single-thread scheduled executor
     */
    public static ScheduledExecutorService newSingleThreadScheduledExecutor(String processName, boolean isDaemon) {
        return Executors.newSingleThreadScheduledExecutor(newThreadFactory(processName, isDaemon));
    }

    /**
     * Creates a new fixed-size scheduled thread pool with a custom thread factory.
     *
     * @param nThreads    the number of threads in the pool
     * @param processName the name prefix for threads
     * @param isDaemon    whether threads should be daemon threads
     * @return the new scheduled thread pool
     */
    public static ScheduledExecutorService newFixedThreadScheduledPool(int nThreads, String processName,
                                                                       boolean isDaemon) {
        return Executors.newScheduledThreadPool(nThreads, newThreadFactory(processName, isDaemon));
    }

    /**
     * Creates a new thread factory with the given process name and daemon flag.
     *
     * @param processName the name prefix for threads
     * @param isDaemon    whether threads should be daemon threads
     * @return the new thread factory
     */
    public static ThreadFactory newThreadFactory(String processName, boolean isDaemon) {
        return newGenericThreadFactory("Remoting-" + processName, isDaemon);
    }

    /**
     * Creates a new generic thread factory (non-daemon) with the given process name.
     *
     * @param processName the name prefix for threads
     * @return the new thread factory
     */
    public static ThreadFactory newGenericThreadFactory(String processName) {
        return newGenericThreadFactory(processName, false);
    }

    /**
     * Creates a new generic thread factory (non-daemon) with the given process name and thread count.
     *
     * @param processName the name prefix for threads
     * @param threads     the thread count identifier
     * @return the new thread factory
     */
    public static ThreadFactory newGenericThreadFactory(String processName, int threads) {
        return newGenericThreadFactory(processName, threads, false);
    }

    /**
     * Creates a new generic thread factory with the given process name and daemon flag.
     *
     * @param processName the name prefix for threads
     * @param isDaemon    whether threads should be daemon threads
     * @return the new thread factory
     */
    public static ThreadFactory newGenericThreadFactory(final String processName, final boolean isDaemon) {
        return new ThreadFactory() {
            private AtomicInteger threadIndex = new AtomicInteger(0);

            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r, String.format("%s_%d", processName, this.threadIndex.incrementAndGet()));
                thread.setDaemon(isDaemon);
                return thread;
            }
        };
    }

    /**
     * Creates a new generic thread factory with the given process name, thread count, and daemon flag.
     *
     * @param processName the name prefix for threads
     * @param threads     the thread count identifier
     * @param isDaemon    whether threads should be daemon threads
     * @return the new thread factory
     */
    public static ThreadFactory newGenericThreadFactory(final String processName, final int threads,
                                                        final boolean isDaemon) {
        return new ThreadFactory() {
            private AtomicInteger threadIndex = new AtomicInteger(0);

            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r, String.format("%s_%d_%d", processName, threads, this.threadIndex.incrementAndGet()));
                thread.setDaemon(isDaemon);
                return thread;
            }
        };
    }

    /**
     * Creates a new thread with the given name, runnable, and daemon flag.
     * Installs a default uncaught exception handler that logs errors.
     *
     * @param name     the thread name
     * @param runnable the work for the thread to do
     * @param daemon   whether the thread should be a daemon thread
     * @return the unstarted thread
     */
    public static Thread newThread(String name, Runnable runnable, boolean daemon) {
        Thread thread = new Thread(runnable, name);
        thread.setDaemon(daemon);
        thread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                log.error("Uncaught exception in thread '" + t.getName() + "':", e);
            }
        });
        return thread;
    }

    /**
     * Gracefully shuts down the given thread by interrupting and joining it.
     *
     * @param t the thread to stop
     */
    public static void shutdownGracefully(final Thread t) {
        shutdownGracefully(t, 0);
    }

    /**
     * Gracefully shuts down the given thread by interrupting and joining it
     * with the specified timeout.
     *
     * @param t     the thread to stop
     * @param millis the join timeout in milliseconds (0 to wait forever)
     */
    public static void shutdownGracefully(final Thread t, final long millis) {
        if (t == null) {
            return;
        }
        while (t.isAlive()) {
            try {
                t.interrupt();
                t.join(millis);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Gracefully shuts down the given executor service following the
     * recommended sequence from {@link ExecutorService}: disable new
     * tasks, wait for termination, then force shutdown if needed.
     *
     * @param executor the executor service to shut down
     * @param timeout  the timeout value
     * @param timeUnit the timeout time unit
     */
    public static void shutdownGracefully(ExecutorService executor, long timeout, TimeUnit timeUnit) {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(timeout, timeUnit)) {
                executor.shutdownNow();
                if (!executor.awaitTermination(timeout, timeUnit)) {
                    log.warn(String.format("%s didn't terminate!", executor));
                }
            }
        } catch (InterruptedException ie) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Private constructor to prevent instantiation.
     */
    private ThreadUtils() {
        // Unused

    }
}
