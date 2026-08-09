package com.alibaba.otter.canal.handler.impl;

import com.alibaba.otter.canal.factory.IModelFactory;
import com.alibaba.otter.canal.handler.EntryHandler;
import com.alibaba.otter.canal.protocol.CanalEntry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MapRowDataHandlerImplTest {

    @Mock
    private IModelFactory<Map<String, String>> modelFactory;

    @Mock
    private EntryHandler<String> entryHandler;

    @Test
    void shouldReturnWhenListIsNull() throws Exception {
        MapRowDataHandlerImpl handler = new MapRowDataHandlerImpl(modelFactory);
        assertDoesNotThrow(() -> handler.handlerRowData(null, entryHandler, CanalEntry.EventType.INSERT));
    }

    @Test
    void shouldReturnWhenEntryHandlerIsNull() throws Exception {
        MapRowDataHandlerImpl handler = new MapRowDataHandlerImpl(modelFactory);
        List<Map<String, String>> list = Arrays.asList(Collections.singletonMap("id", "1"));
        assertDoesNotThrow(() -> handler.handlerRowData(list, null, CanalEntry.EventType.INSERT));
    }

    @Test
    void shouldReturnWhenEventTypeIsNull() throws Exception {
        MapRowDataHandlerImpl handler = new MapRowDataHandlerImpl(modelFactory);
        List<Map<String, String>> list = Arrays.asList(Collections.singletonMap("id", "1"));
        assertDoesNotThrow(() -> handler.handlerRowData(list, entryHandler, null));
    }

    @Test
    void shouldHandleInsertEvent() throws Exception {
        MapRowDataHandlerImpl handler = new MapRowDataHandlerImpl(modelFactory);
        Map<String, String> data = new HashMap<String, String>() {{ put("id", "1"); put("name", "test"); }};
        List<Map<String, String>> list = Arrays.asList(data);

        when(modelFactory.newInstance(any(), any(Map.class))).thenReturn("model");

        handler.handlerRowData(list, entryHandler, CanalEntry.EventType.INSERT);

        verify(entryHandler).insert("model");
    }

    @Test
    void shouldHandleDeleteEvent() throws Exception {
        MapRowDataHandlerImpl handler = new MapRowDataHandlerImpl(modelFactory);
        Map<String, String> data = Collections.singletonMap("id", "1");
        List<Map<String, String>> list = Arrays.asList(data);

        when(modelFactory.newInstance(any(), any(Map.class))).thenReturn("model");

        handler.handlerRowData(list, entryHandler, CanalEntry.EventType.DELETE);

        verify(entryHandler).delete("model");
    }

    @Test
    void shouldHandleUpdateEvent() throws Exception {
        MapRowDataHandlerImpl handler = new MapRowDataHandlerImpl(modelFactory);
        Map<String, String> afterData = Collections.singletonMap("id", "2");
        Map<String, String> beforeData = Collections.singletonMap("id", "1");
        // Code: list.get(0) = after (new data), list.get(1) = before (old data)
        List<Map<String, String>> list = Arrays.asList(afterData, beforeData);

        // Code calls: first newInstance(handler, list.get(1)) for before,
        // then newInstance(handler, list.get(0)) for after
        when(modelFactory.newInstance(any(), any(Map.class)))
                .thenReturn("before")  // first call: list.get(1)
                .thenReturn("after");  // second call: list.get(0)

        handler.handlerRowData(list, entryHandler, CanalEntry.EventType.UPDATE);

        verify(entryHandler).update("before", "after");
    }

    @Test
    void shouldDoNothingForUnhandledEventType() throws Exception {
        MapRowDataHandlerImpl handler = new MapRowDataHandlerImpl(modelFactory);
        List<Map<String, String>> list = Arrays.asList(Collections.singletonMap("id", "1"));

        handler.handlerRowData(list, entryHandler, CanalEntry.EventType.QUERY);

        verifyNoInteractions(entryHandler);
        verifyNoInteractions(modelFactory);
    }
}
