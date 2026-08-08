package com.alibaba.otter.canal.util;

import org.junit.jupiter.api.Test;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class ThreadUtilsTest {

    @Test
    void shouldCreateNewThread() {
        AtomicBoolean ran = new AtomicBoolean(false);
        Thread thread = ThreadUtils.newThread("test-thread", () -> ran.set(true), true);
        assertNotNull(thread);
        assertEquals("test-thread", thread.getName());
        assertTrue(thread.isDaemon());
    }

    @Test
    void shouldCreateDaemonThread() {
        Thread thread = ThreadUtils.newThread("daemon-thread", () -> {}, true);
        assertTrue(thread.isDaemon());
    }

    @Test
    void shouldCreateNonDaemonThread() {
        Thread thread = ThreadUtils.newThread("non-daemon", () -> {}, false);
        assertFalse(thread.isDaemon());
    }

    @Test
    void shouldCreateThreadPoolExecutor() {
        ExecutorService executor = ThreadUtils.newThreadPoolExecutor(
                1, 2, 60, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(10), "test-pool", true);
        assertNotNull(executor);
        executor.shutdown();
    }

    @Test
    void shouldCreateSingleThreadExecutor() {
        ExecutorService executor = ThreadUtils.newSingleThreadExecutor("single", true);
        assertNotNull(executor);
        executor.shutdown();
    }

    @Test
    void shouldCreateSingleThreadScheduledExecutor() {
        ScheduledExecutorService executor = ThreadUtils.newSingleThreadScheduledExecutor("scheduled", true);
        assertNotNull(executor);
        executor.shutdown();
    }

    @Test
    void shouldCreateFixedThreadScheduledPool() {
        ScheduledExecutorService executor = ThreadUtils.newFixedThreadScheduledPool(2, "fixed-scheduled", true);
        assertNotNull(executor);
        executor.shutdown();
    }

    @Test
    void shouldCreateThreadFactory() {
        ThreadFactory factory = ThreadUtils.newThreadFactory("factory", true);
        assertNotNull(factory);
        Thread thread = factory.newThread(() -> {});
        assertNotNull(thread);
        assertTrue(thread.isDaemon());
        assertTrue(thread.getName().startsWith("Remoting-factory"));
    }

    @Test
    void shouldCreateGenericThreadFactory() {
        ThreadFactory factory = ThreadUtils.newGenericThreadFactory("generic");
        assertNotNull(factory);
        Thread thread = factory.newThread(() -> {});
        assertTrue(thread.getName().startsWith("generic"));
    }

    @Test
    void shouldCreateGenericThreadFactoryWithThreads() {
        ThreadFactory factory = ThreadUtils.newGenericThreadFactory("generic", 3);
        assertNotNull(factory);
        Thread thread = factory.newThread(() -> {});
        assertTrue(thread.getName().contains("generic_3"));
    }

    @Test
    void shouldCreateGenericThreadFactoryWithDaemonFlag() {
        ThreadFactory factory = ThreadUtils.newGenericThreadFactory("daemon-factory", true);
        Thread thread = factory.newThread(() -> {});
        assertTrue(thread.isDaemon());
    }

    @Test
    void shouldCreateGenericThreadFactoryWithThreadsAndDaemon() {
        ThreadFactory factory = ThreadUtils.newGenericThreadFactory("multi", 5, false);
        Thread thread = factory.newThread(() -> {});
        assertFalse(thread.isDaemon());
        assertTrue(thread.getName().contains("multi_5"));
    }

    @Test
    void shouldShutdownGracefullyWithZeroTimeout() {
        Thread thread = new Thread(() -> {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                // expected
            }
        });
        thread.start();
        assertDoesNotThrow(() -> ThreadUtils.shutdownGracefully(thread, 100));
    }

    @Test
    void shouldHandleNullThreadGracefully() {
        assertDoesNotThrow(() -> ThreadUtils.shutdownGracefully(null));
        assertDoesNotThrow(() -> ThreadUtils.shutdownGracefully(null, 0));
    }

    @Test
    void shouldShutdownExecutorGracefully() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {});
        assertDoesNotThrow(() -> ThreadUtils.shutdownGracefully(executor, 1, TimeUnit.SECONDS));
        assertTrue(executor.isShutdown());
    }
}
