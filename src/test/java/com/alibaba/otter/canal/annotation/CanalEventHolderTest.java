package com.alibaba.otter.canal.annotation;

import com.alibaba.otter.canal.annotation.event.OnInsertEvent;
import com.alibaba.otter.canal.annotation.event.OnUpdateEvent;
import com.alibaba.otter.canal.annotation.event.OnDeleteEvent;
import com.alibaba.otter.canal.protocol.CanalEntry;
import org.junit.jupiter.api.Test;
import org.springframework.core.annotation.AnnotatedElementUtils;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class CanalEventHolderTest {

    @OnInsertEvent(schema = "test", table = "users")
    public void insertMethod() {
    }

    @OnUpdateEvent(schema = "test", table = "users")
    public void updateMethod() {
    }

    @OnDeleteEvent(schema = "test", table = "users")
    public void deleteMethod() {
    }

    private OnCanalEvent getMergedEvent(String methodName) throws Exception {
        Method method = CanalEventHolderTest.class.getMethod(methodName);
        return AnnotatedElementUtils.findMergedAnnotation(method, OnCanalEvent.class);
    }

    @Test
    void shouldReturnTarget() throws Exception {
        CanalEventHolderTest target = new CanalEventHolderTest();
        Method method = CanalEventHolderTest.class.getMethod("insertMethod");
        OnCanalEvent event = getMergedEvent("insertMethod");
        CanalEventHolder holder = new CanalEventHolder(target, method, event);

        assertSame(target, holder.getTarget());
    }

    @Test
    void shouldReturnMethod() throws Exception {
        CanalEventHolderTest target = new CanalEventHolderTest();
        Method method = CanalEventHolderTest.class.getMethod("insertMethod");
        OnCanalEvent event = getMergedEvent("insertMethod");
        CanalEventHolder holder = new CanalEventHolder(target, method, event);

        assertSame(method, holder.getMethod());
    }

    @Test
    void shouldReturnEvent() throws Exception {
        CanalEventHolderTest target = new CanalEventHolderTest();
        Method method = CanalEventHolderTest.class.getMethod("insertMethod");
        OnCanalEvent event = getMergedEvent("insertMethod");
        CanalEventHolder holder = new CanalEventHolder(target, method, event);

        assertNotNull(holder.getEvent());
        assertEquals(1, holder.getEvent().eventType().length);
    }

    @Test
    void shouldMatchDeclaredEventType() throws Exception {
        Method method = CanalEventHolderTest.class.getMethod("insertMethod");
        OnCanalEvent event = getMergedEvent("insertMethod");
        CanalEventHolder holder = new CanalEventHolder(this, method, event);

        assertTrue(holder.isMatch(CanalEntry.EventType.INSERT));
        assertFalse(holder.isMatch(CanalEntry.EventType.DELETE));
    }

    @Test
    void shouldMatchWhenEventTypeIsNull() throws Exception {
        Method method = CanalEventHolderTest.class.getMethod("insertMethod");
        OnCanalEvent event = getMergedEvent("insertMethod");
        CanalEventHolder holder = new CanalEventHolder(this, method, event);

        assertTrue(holder.isMatch(null));
    }

    @Test
    void shouldMatchUpdateEventType() throws Exception {
        Method method = CanalEventHolderTest.class.getMethod("updateMethod");
        OnCanalEvent event = getMergedEvent("updateMethod");
        CanalEventHolder holder = new CanalEventHolder(this, method, event);

        assertTrue(holder.isMatch(CanalEntry.EventType.UPDATE));
        assertFalse(holder.isMatch(CanalEntry.EventType.INSERT));
    }

    @Test
    void shouldMatchDeleteEventType() throws Exception {
        Method method = CanalEventHolderTest.class.getMethod("deleteMethod");
        OnCanalEvent event = getMergedEvent("deleteMethod");
        CanalEventHolder holder = new CanalEventHolder(this, method, event);

        assertTrue(holder.isMatch(CanalEntry.EventType.DELETE));
        assertFalse(holder.isMatch(CanalEntry.EventType.INSERT));
    }
}
