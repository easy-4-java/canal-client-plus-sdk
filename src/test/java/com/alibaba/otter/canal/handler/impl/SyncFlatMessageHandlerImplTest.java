package com.alibaba.otter.canal.handler.impl;

import com.alibaba.otter.canal.handler.EntryHandler;
import com.alibaba.otter.canal.handler.RowDataHandler;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.alibaba.otter.canal.protocol.FlatMessage;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SyncFlatMessageHandlerImplTest {

    static class TestEntryHandler implements EntryHandler<String> {}

    static class NoOpMapRowDataHandler implements RowDataHandler<List<Map<String, String>>> {
        @Override
        public <R> void handlerRowData(List<Map<String, String>> t, EntryHandler<R> entryHandler, CanalEntry.EventType eventType) {}
    }

    @Test
    void shouldCreateWithDefaultEntryTypes() {
        TestEntryHandler entryHandler = new TestEntryHandler();
        SyncFlatMessageHandlerImpl handler = new SyncFlatMessageHandlerImpl(
                List.of(entryHandler), new NoOpMapRowDataHandler());
        assertNotNull(handler);
    }

    @Test
    void shouldCreateWithCustomEntryTypes() {
        TestEntryHandler entryHandler = new TestEntryHandler();
        List<CanalEntry.EntryType> types = List.of(CanalEntry.EntryType.ROWDATA);
        SyncFlatMessageHandlerImpl handler = new SyncFlatMessageHandlerImpl(
                types, List.of(entryHandler), new NoOpMapRowDataHandler());
        assertNotNull(handler);
    }

    @Test
    void shouldHandleEmptyDataMessage() {
        TestEntryHandler entryHandler = new TestEntryHandler();
        SyncFlatMessageHandlerImpl handler = new SyncFlatMessageHandlerImpl(
                List.of(entryHandler), new NoOpMapRowDataHandler());

        FlatMessage flatMessage = new FlatMessage();
        flatMessage.setId(1L);
        flatMessage.setDatabase("test_db");
        flatMessage.setTable("test_table");
        flatMessage.setType("INSERT");

        assertDoesNotThrow(() -> handler.handleMessage("test-dest", flatMessage));
    }
}
