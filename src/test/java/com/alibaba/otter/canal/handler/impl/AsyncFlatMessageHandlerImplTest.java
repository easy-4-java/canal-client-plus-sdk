package com.alibaba.otter.canal.handler.impl;

import com.alibaba.otter.canal.handler.EntryHandler;
import com.alibaba.otter.canal.handler.RowDataHandler;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.alibaba.otter.canal.protocol.FlatMessage;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AsyncFlatMessageHandlerImplTest {

    static class TestEntryHandler implements EntryHandler<String> {}

    static class NoOpMapRowDataHandler implements RowDataHandler<List<Map<String, String>>> {
        @Override
        public <R> void handlerRowData(List<Map<String, String>> t, EntryHandler<R> entryHandler, CanalEntry.EventType eventType) {}
    }

    @Test
    void shouldCreateWithDefaultEntryTypes() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.initialize();

        TestEntryHandler entryHandler = new TestEntryHandler();
        AsyncFlatMessageHandlerImpl handler = new AsyncFlatMessageHandlerImpl(
                Arrays.asList(entryHandler), new NoOpMapRowDataHandler(), executor);
        assertNotNull(handler);
        executor.shutdown();
    }

    @Test
    void shouldCreateWithCustomEntryTypes() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.initialize();

        TestEntryHandler entryHandler = new TestEntryHandler();
        List<CanalEntry.EntryType> types = Collections.unmodifiableList(Arrays.asList(CanalEntry.EntryType.ROWDATA));
        AsyncFlatMessageHandlerImpl handler = new AsyncFlatMessageHandlerImpl(
                types, Arrays.asList(entryHandler), new NoOpMapRowDataHandler(), executor);
        assertNotNull(handler);
        executor.shutdown();
    }

    @Test
    void shouldHandleEmptyDataMessageAsync() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.initialize();

        TestEntryHandler entryHandler = new TestEntryHandler();
        AsyncFlatMessageHandlerImpl handler = new AsyncFlatMessageHandlerImpl(
                Arrays.asList(entryHandler), new NoOpMapRowDataHandler(), executor);

        FlatMessage flatMessage = new FlatMessage();
        flatMessage.setId(1L);

        assertDoesNotThrow(() -> handler.handleMessage("test-dest", flatMessage));

        executor.shutdown();
    }
}
