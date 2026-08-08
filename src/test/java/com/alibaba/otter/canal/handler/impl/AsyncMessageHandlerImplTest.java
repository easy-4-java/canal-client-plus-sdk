package com.alibaba.otter.canal.handler.impl;

import com.alibaba.otter.canal.handler.EntryHandler;
import com.alibaba.otter.canal.handler.RowDataHandler;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.alibaba.otter.canal.protocol.Message;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AsyncMessageHandlerImplTest {

    static class TestEntryHandler implements EntryHandler<String> {}

    static class NoOpRowDataHandler implements RowDataHandler<CanalEntry.RowData> {
        @Override
        public <R> void handlerRowData(CanalEntry.RowData t, EntryHandler<R> entryHandler, CanalEntry.EventType eventType) {}
    }

    @Test
    void shouldCreateWithDefaultEntryTypes() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.initialize();

        TestEntryHandler entryHandler = new TestEntryHandler();
        AsyncMessageHandlerImpl handler = new AsyncMessageHandlerImpl(
                List.of(entryHandler), new NoOpRowDataHandler(), executor);
        assertNotNull(handler);
        executor.shutdown();
    }

    @Test
    void shouldCreateWithCustomEntryTypes() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.initialize();

        TestEntryHandler entryHandler = new TestEntryHandler();
        List<CanalEntry.EntryType> types = List.of(CanalEntry.EntryType.ROWDATA);
        AsyncMessageHandlerImpl handler = new AsyncMessageHandlerImpl(
                types, List.of(entryHandler), new NoOpRowDataHandler(), executor);
        assertNotNull(handler);
        executor.shutdown();
    }

    @Test
    void shouldHandleEmptyMessageAsync() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.initialize();

        TestEntryHandler entryHandler = new TestEntryHandler();
        AsyncMessageHandlerImpl handler = new AsyncMessageHandlerImpl(
                List.of(entryHandler), new NoOpRowDataHandler(), executor);

        Message message = new Message(1L, new ArrayList<>());

        assertDoesNotThrow(() -> handler.handleMessage("test-dest", message));

        executor.shutdown();
    }
}
