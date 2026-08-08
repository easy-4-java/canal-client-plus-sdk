package com.alibaba.otter.canal.util;

import com.alibaba.otter.canal.handler.EntryHandler;
import com.alibaba.otter.canal.model.CanalModel;
import com.alibaba.otter.canal.protocol.CanalEntry;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GenericUtilTest {

    // Test handler implementations for generic resolution
    static class StringEntryHandler implements EntryHandler<String> {
    }

    static class IntegerEntryHandler implements EntryHandler<Integer> {
    }

    @Test
    void shouldResolveStringGenericClass() {
        StringEntryHandler handler = new StringEntryHandler();
        Class<?> clazz = GenericUtil.getTableClass(handler);
        assertEquals(String.class, clazz);
    }

    @Test
    void shouldResolveIntegerGenericClass() {
        IntegerEntryHandler handler = new IntegerEntryHandler();
        Class<?> clazz = GenericUtil.getTableClass(handler);
        assertEquals(Integer.class, clazz);
    }

    @Test
    void shouldCacheResolvedClass() {
        StringEntryHandler handler = new StringEntryHandler();
        Class<?> first = GenericUtil.getTableClass(handler);
        Class<?> second = GenericUtil.getTableClass(handler);
        assertSame(first, second);
    }

    @Test
    void shouldGetInvokeArgsForRowChange() throws Exception {
        Method method = SampleHandler.class.getMethod("handleWithRowChange", CanalModel.class, CanalEntry.RowChange.class, CanalEntry.EventType.class);
        CanalModel model = CanalModel.builder().id(1L).build();
        CanalEntry.RowChange rowChange = CanalEntry.RowChange.newBuilder().build();
        CanalEntry.EventType eventType = CanalEntry.EventType.INSERT;

        Object[] args = GenericUtil.getInvokeArgs(method, model, rowChange, eventType);
        assertEquals(3, args.length);
        assertSame(model, args[0]);
        assertSame(rowChange, args[1]);
        assertSame(eventType, args[2]);
    }

    @Test
    void shouldGetInvokeArgsForListMap() throws Exception {
        Method method = SampleHandler.class.getMethod("handleWithListMap", CanalModel.class, List.class, CanalEntry.EventType.class);
        CanalModel model = CanalModel.builder().id(1L).build();
        List<Map<String, String>> rowData = Arrays.asList(new HashMap<>());
        CanalEntry.EventType eventType = CanalEntry.EventType.UPDATE;

        Object[] args = GenericUtil.getInvokeArgs(method, model, rowData, eventType);
        assertEquals(3, args.length);
        assertSame(model, args[0]);
        assertSame(rowData, args[1]);
        assertSame(eventType, args[2]);
    }

    @Test
    void shouldReturnNullForUnknownParameterType() throws Exception {
        Method method = SampleHandler.class.getMethod("handleWithUnknown", String.class, CanalModel.class);
        CanalModel model = CanalModel.builder().id(1L).build();

        Object[] args = GenericUtil.getInvokeArgs(method, model, (CanalEntry.RowChange) null, CanalEntry.EventType.INSERT);
        assertEquals(2, args.length);
        assertNull(args[0]); // String is not a known type
        assertSame(model, args[1]);
    }

    // Helper class with handler methods for reflection testing
    public static class SampleHandler {
        public void handleWithRowChange(CanalModel model, CanalEntry.RowChange rowChange, CanalEntry.EventType eventType) {}
        public void handleWithListMap(CanalModel model, List<Map<String, String>> rowData, CanalEntry.EventType eventType) {}
        public void handleWithUnknown(String unknown, CanalModel model) {}
    }
}
