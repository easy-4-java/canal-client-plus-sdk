package com.alibaba.otter.canal.handler.impl;

import com.alibaba.otter.canal.factory.IModelFactory;
import com.alibaba.otter.canal.handler.EntryHandler;
import com.alibaba.otter.canal.protocol.CanalEntry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RowDataHandlerImplTest {

    @Mock
    private IModelFactory<List<CanalEntry.Column>> modelFactory;

    @Mock
    private EntryHandler<String> entryHandler;

    @Test
    void shouldReturnWhenRowDataIsNull() throws Exception {
        RowDataHandlerImpl handler = new RowDataHandlerImpl(modelFactory);
        assertDoesNotThrow(() -> handler.handlerRowData(null, entryHandler, CanalEntry.EventType.INSERT));
    }

    @Test
    void shouldReturnWhenEntryHandlerIsNull() throws Exception {
        RowDataHandlerImpl handler = new RowDataHandlerImpl(modelFactory);
        CanalEntry.RowData rowData = CanalEntry.RowData.newBuilder().build();
        assertDoesNotThrow(() -> handler.handlerRowData(rowData, null, CanalEntry.EventType.INSERT));
    }

    @Test
    void shouldReturnWhenEventTypeIsNull() throws Exception {
        RowDataHandlerImpl handler = new RowDataHandlerImpl(modelFactory);
        CanalEntry.RowData rowData = CanalEntry.RowData.newBuilder().build();
        assertDoesNotThrow(() -> handler.handlerRowData(rowData, entryHandler, null));
    }

    @Test
    void shouldHandleInsertEvent() throws Exception {
        RowDataHandlerImpl handler = new RowDataHandlerImpl(modelFactory);
        CanalEntry.Column col = CanalEntry.Column.newBuilder().setName("id").setValue("1").build();
        CanalEntry.RowData rowData = CanalEntry.RowData.newBuilder()
                .addAfterColumns(col).build();

        when(modelFactory.newInstance(any(), anyList())).thenReturn("model");

        handler.handlerRowData(rowData, entryHandler, CanalEntry.EventType.INSERT);

        verify(entryHandler).insert("model");
    }

    @Test
    void shouldHandleDeleteEvent() throws Exception {
        RowDataHandlerImpl handler = new RowDataHandlerImpl(modelFactory);
        CanalEntry.Column col = CanalEntry.Column.newBuilder().setName("id").setValue("1").build();
        CanalEntry.RowData rowData = CanalEntry.RowData.newBuilder()
                .addBeforeColumns(col).build();

        when(modelFactory.newInstance(any(), anyList())).thenReturn("model");

        handler.handlerRowData(rowData, entryHandler, CanalEntry.EventType.DELETE);

        verify(entryHandler).delete("model");
    }

    @Test
    void shouldHandleUpdateEvent() throws Exception {
        RowDataHandlerImpl handler = new RowDataHandlerImpl(modelFactory);
        CanalEntry.Column beforeCol = CanalEntry.Column.newBuilder()
                .setName("id").setValue("1").setUpdated(true).build();
        CanalEntry.Column afterCol = CanalEntry.Column.newBuilder()
                .setName("id").setValue("2").setUpdated(true).build();
        CanalEntry.RowData rowData = CanalEntry.RowData.newBuilder()
                .addBeforeColumns(beforeCol)
                .addAfterColumns(afterCol).build();

        when(modelFactory.newInstance(any(), anyList())).thenReturn("after");
        when(modelFactory.newInstance(any(), anyList(), any())).thenReturn("before");

        handler.handlerRowData(rowData, entryHandler, CanalEntry.EventType.UPDATE);

        verify(entryHandler).update("before", "after");
    }

    @Test
    void shouldDoNothingForUnhandledEventType() throws Exception {
        RowDataHandlerImpl handler = new RowDataHandlerImpl(modelFactory);
        CanalEntry.RowData rowData = CanalEntry.RowData.newBuilder().build();

        handler.handlerRowData(rowData, entryHandler, CanalEntry.EventType.QUERY);

        verifyNoInteractions(entryHandler);
        verifyNoInteractions(modelFactory);
    }
}
