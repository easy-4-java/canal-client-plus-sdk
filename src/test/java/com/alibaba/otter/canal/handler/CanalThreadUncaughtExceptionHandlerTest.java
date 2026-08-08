package com.alibaba.otter.canal.handler;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CanalThreadUncaughtExceptionHandlerTest {

    @Test
    void shouldHandleUncaughtExceptionWithoutThrowing() {
        CanalThreadUncaughtExceptionHandler handler = new CanalThreadUncaughtExceptionHandler();
        Thread testThread = new Thread("test-thread");
        RuntimeException exception = new RuntimeException("test error");

        // Should not throw
        assertDoesNotThrow(() -> handler.uncaughtException(testThread, exception));
    }

    @Test
    void shouldHandleNullThrowableGracefully() {
        CanalThreadUncaughtExceptionHandler handler = new CanalThreadUncaughtExceptionHandler();
        Thread testThread = new Thread("test-thread");

        // Should not throw even with a null-ish scenario (logging handles it)
        assertDoesNotThrow(() -> handler.uncaughtException(testThread, new RuntimeException()));
    }
}
