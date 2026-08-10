package com.alibaba.otter.canal.handler;

import lombok.extern.slf4j.Slf4j;

/**
 * Default uncaught exception handler for Canal client worker threads.
 * Logs the thread name and exception at ERROR level.
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 3.0.0
 */
@Slf4j
public class CanalThreadUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {

    /**
     * Called when the given thread terminates due to an uncaught exception.
     *
     * @param t the thread that raised the exception
     * @param e the uncaught exception
     */
    @Override
    public void uncaughtException(Thread t, Throwable e) {
        log.error("thread "+ t.getName()+" have a exception",e);
    }

}
