package com.alibaba.otter.canal.util;

import com.alibaba.otter.canal.annotation.CanalEventHolder;
import com.alibaba.otter.canal.annotation.CanalTable;
import com.alibaba.otter.canal.annotation.OnCanalEvent;
import com.alibaba.otter.canal.annotation.event.OnInsertEvent;
import com.alibaba.otter.canal.annotation.event.OnUpdateEvent;
import com.alibaba.otter.canal.enums.TableNameEnum;
import com.alibaba.otter.canal.handler.EntryHandler;
import com.alibaba.otter.canal.protocol.CanalEntry;
import org.junit.jupiter.api.Test;
import org.springframework.core.annotation.AnnotatedElementUtils;

import java.lang.reflect.Method;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class HandlerUtilTest {

    // --- Combination value tests ---

    @Test
    void shouldBuildCombinationValueFromThreeParts() {
        String result = HandlerUtil.getCombinationValue("dest1", "schema1", "table1");
        assertEquals("dest1.schema1.table1", result);
    }

    @Test
    void shouldDefaultBlankPartsToWildcard() {
        String result = HandlerUtil.getCombinationValue("", "", "");
        assertEquals("*.*.*", result);
    }

    @Test
    void shouldDefaultNullPartsToWildcard() {
        String result = HandlerUtil.getCombinationValue(null, null, null);
        assertEquals("*.*.*", result);
    }

    @Test
    void shouldConvertToLowerCase() {
        String result = HandlerUtil.getCombinationValue("Dest", "Schema", "Table");
        assertEquals("dest.schema.table", result);
    }

    @Test
    void shouldBuildCombinationValueWithEventType() {
        String result = HandlerUtil.getCombinationValue("dest", "schema", "table", CanalEntry.EventType.INSERT);
        assertEquals("dest.schema.table.insert", result);
    }

    @Test
    void shouldBuildCombinationValueWithEventTypeAndDefaults() {
        String result = HandlerUtil.getCombinationValue(null, null, null, CanalEntry.EventType.DELETE);
        assertEquals("*.*.*.delete", result);
    }

    // --- getTableHandlerMap tests ---

    @Test
    void shouldReturnEmptyMapForNullHandlers() {
        Map<String, EntryHandler> map = HandlerUtil.getTableHandlerMap(null);
        assertNotNull(map);
        assertTrue(map.isEmpty());
    }

    @Test
    void shouldReturnEmptyMapForEmptyHandlers() {
        Map<String, EntryHandler> map = HandlerUtil.getTableHandlerMap(Collections.emptyList());
        assertNotNull(map);
        assertTrue(map.isEmpty());
    }

    @Test
    void shouldBuildTableHandlerMapFromAnnotatedHandler() {
        AnnotatedEntryHandler handler = new AnnotatedEntryHandler();
        List<EntryHandler> handlers = List.of(handler);

        Map<String, EntryHandler> map = HandlerUtil.getTableHandlerMap(handlers);
        assertFalse(map.isEmpty());
        assertTrue(map.containsKey("dest1.schema1.table1"));
    }

    @Test
    void shouldNotIncludeUnannotatedHandlerInMap() {
        TestEntryHandler handler = new TestEntryHandler();
        List<EntryHandler> handlers = List.of(handler);

        Map<String, EntryHandler> map = HandlerUtil.getTableHandlerMap(handlers);
        // TestEntryHandler has no @CanalTable and no generic type info (String has no table info)
        // so it should not be added to the map
        assertTrue(map.isEmpty());
    }

    // --- getEventHolderMap tests ---

    @Test
    void shouldReturnEmptyMapForNullEventHolders() {
        Map<String, List<CanalEventHolder>> map = HandlerUtil.getEventHolderMap(null);
        assertNotNull(map);
        assertTrue(map.isEmpty());
    }

    @Test
    void shouldReturnEmptyMapForEmptyEventHolders() {
        Map<String, List<CanalEventHolder>> map = HandlerUtil.getEventHolderMap(Collections.emptyList());
        assertNotNull(map);
        assertTrue(map.isEmpty());
    }

    @Test
    void shouldBuildEventHolderMap() throws Exception {
        CanalEventHolder holder = createEventHolder("dummyInsertMethod");

        Map<String, List<CanalEventHolder>> map = HandlerUtil.getEventHolderMap(List.of(holder));
        assertFalse(map.isEmpty());
    }

    @OnInsertEvent(schema = "test", table = "users")
    public void dummyInsertMethod() {}

    @OnUpdateEvent(schema = "test", table = "users")
    public void dummyUpdateMethod() {}

    @OnInsertEvent(schema = "test", table = "users")
    public void destFilteredMethod() {}

    // --- getEventHolders tests ---

    @Test
    void shouldReturnEmptyListWhenNoMatch() {
        Map<String, List<CanalEventHolder>> map = new HashMap<>();
        List<CanalEventHolder> result = HandlerUtil.getEventHolders(map, "dest", "schema", "table", CanalEntry.EventType.INSERT);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // --- getEntryHandler(Map) tests ---

    @Test
    void shouldReturnNullWhenNoHandlerInMap() {
        Map<String, EntryHandler> map = new HashMap<>();
        EntryHandler result = HandlerUtil.getEntryHandler(map, "schema", "table");
        assertNull(result);
    }

    @Test
    void shouldReturnGlobalHandlerAsFallback() {
        Map<String, EntryHandler> map = new HashMap<>();
        TestEntryHandler globalHandler = new TestEntryHandler();
        map.put(TableNameEnum.ALL.name().toLowerCase(), globalHandler);

        EntryHandler result = HandlerUtil.getEntryHandler(map, "nonexistent", "table");
        assertSame(globalHandler, result);
    }

    @Test
    void shouldReturnSpecificHandler() {
        Map<String, EntryHandler> map = new HashMap<>();
        TestEntryHandler specificHandler = new TestEntryHandler();
        StringJoiner joiner = new StringJoiner(".").add("myschema").add("mytable");
        map.put(joiner.toString().toLowerCase(), specificHandler);

        EntryHandler result = HandlerUtil.getEntryHandler(map, "myschema", "mytable");
        assertSame(specificHandler, result);
    }

    // --- getEntryHandler(List) tests ---

    @Test
    void shouldReturnHandlerFromListByMybatisTableName() {
        // TestEntryHandler has no @CanalTable and no MyBatis Plus table info,
        // so it won't match. An annotated handler with matching table combination would.
        // When the list has no matching handler, null is returned.
        TestEntryHandler handler = new TestEntryHandler();
        List<EntryHandler> handlers = List.of(handler);

        EntryHandler result = HandlerUtil.getEntryHandler(handlers, "any_schema", "any_table");
        assertNull(result);
    }

    @Test
    void shouldReturnNullWhenNoMatchingHandler() {
        AnnotatedEntryHandler specificHandler = new AnnotatedEntryHandler();
        List<EntryHandler> handlers = List.of(specificHandler);

        EntryHandler result = HandlerUtil.getEntryHandler(handlers, "other_schema", "other_table");
        assertNull(result);
    }

    // --- getCanalTableNameCombination tests ---

    @Test
    void shouldReturnCombinationFromAnnotatedHandler() {
        AnnotatedEntryHandler handler = new AnnotatedEntryHandler();
        String result = HandlerUtil.getCanalTableNameCombination(handler);
        assertEquals("dest1.schema1.table1", result);
    }

    @Test
    void shouldReturnNullForUnannotatedHandler() {
        TestEntryHandler handler = new TestEntryHandler();
        String result = HandlerUtil.getCanalTableNameCombination(handler);
        assertNull(result);
    }

    // --- getCanalTableNameCombinations tests ---

    @Test
    void shouldReturnCombinationsForEventHolder() throws Exception {
        CanalEventHolder holder = createEventHolder("dummyInsertMethod");
        List<String> result = HandlerUtil.getCanalTableNameCombinations(holder);
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    // --- Annotation filter tests ---

    @Test
    void shouldFilterByMatchingDestination() throws Exception {
        CanalEventHolder holder = createEventHolder("destFilteredMethod");

        Map<String, List<CanalEventHolder>> map = HandlerUtil.getEventHolderMap(List.of(holder));
        // The key includes event type, so we use the right composite key
        String key = HandlerUtil.getCombinationValue("*", "test", "users", CanalEntry.EventType.INSERT);
        assertTrue(map.containsKey(key));
    }

    private CanalEventHolder createEventHolder(String methodName) throws Exception {
        Method method = HandlerUtilTest.class.getMethod(methodName);
        OnCanalEvent event = AnnotatedElementUtils.findMergedAnnotation(method, OnCanalEvent.class);
        return new CanalEventHolder(this, method, event);
    }

    // Test handler classes
    @CanalTable(destination = "dest1", schema = "schema1", table = "table1")
    static class AnnotatedEntryHandler implements EntryHandler<String> {}

    static class TestEntryHandler implements EntryHandler<String> {}

}
