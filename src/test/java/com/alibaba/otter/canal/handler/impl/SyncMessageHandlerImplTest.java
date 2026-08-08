package com.alibaba.otter.canal.handler.impl;

import com.alibaba.otter.canal.handler.EntryHandler;
import com.alibaba.otter.canal.handler.RowDataHandler;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.alibaba.otter.canal.protocol.Message;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SyncMessageHandlerImplTest {

    static class TestEntryHandler implements EntryHandler<String> {}

    static class NoOpRowDataHandler implements RowDataHandler<CanalEntry.RowData> {
        @Override
        public <R> void handlerRowData(CanalEntry.RowData t, EntryHandler<R> entryHandler, CanalEntry.EventType eventType) {}
    }

    @Test
    void shouldCreateWithDefaultEntryTypes() {
        TestEntryHandler entryHandler = new TestEntryHandler();
        SyncMessageHandlerImpl handler = new SyncMessageHandlerImpl(
                List.of(entryHandler), new NoOpRowDataHandler());
        assertNotNull(handler);
    }

    @Test
    void shouldCreateWithCustomEntryTypes() {
        TestEntryHandler entryHandler = new TestEntryHandler();
        List<CanalEntry.EntryType> types = List.of(CanalEntry.EntryType.ROWDATA);
        SyncMessageHandlerImpl handler = new SyncMessageHandlerImpl(
                types, List.of(entryHandler), new NoOpRowDataHandler());
        assertNotNull(handler);
    }

    @Test
    void shouldHandleEmptyMessage() {
        TestEntryHandler entryHandler = new TestEntryHandler();
        SyncMessageHandlerImpl handler = new SyncMessageHandlerImpl(
                List.of(entryHandler), new NoOpRowDataHandler());
        Message message = new Message(1L, new ArrayList<>());

        assertDoesNotThrow(() -> handler.handleMessage("test-dest", message));
    }
}
