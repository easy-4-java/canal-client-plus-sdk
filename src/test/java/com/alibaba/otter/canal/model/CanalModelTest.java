package com.alibaba.otter.canal.model;

import com.alibaba.otter.canal.protocol.CanalEntry;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CanalModelTest {

    @Test
    void shouldBuildModelWithAllFields() {
        CanalModel model = CanalModel.builder()
                .id(1L)
                .destination("test-dest")
                .schema("test_db")
                .table("test_table")
                .eventType(CanalEntry.EventType.INSERT)
                .executeTime(1234567890L)
                .createTime(1234567891L)
                .build();

        assertEquals(1L, model.getId());
        assertEquals("test-dest", model.getDestination());
        assertEquals("test_db", model.getSchema());
        assertEquals("test_table", model.getTable());
        assertEquals(CanalEntry.EventType.INSERT, model.getEventType());
        assertEquals(1234567890L, model.getExecuteTime());
        assertEquals(1234567891L, model.getCreateTime());
    }

    @Test
    void shouldSetAndGetId() {
        CanalModel model = CanalModel.builder().build();
        model.setId(42L);
        assertEquals(42L, model.getId());
    }

    @Test
    void shouldSetAndGetDestination() {
        CanalModel model = CanalModel.builder().build();
        model.setDestination("dest");
        assertEquals("dest", model.getDestination());
    }

    @Test
    void shouldSetAndGetSchema() {
        CanalModel model = CanalModel.builder().build();
        model.setSchema("mydb");
        assertEquals("mydb", model.getSchema());
    }

    @Test
    void shouldSetAndGetTable() {
        CanalModel model = CanalModel.builder().build();
        model.setTable("mytable");
        assertEquals("mytable", model.getTable());
    }

    @Test
    void shouldSetAndGetEventType() {
        CanalModel model = CanalModel.builder().build();
        model.setEventType(CanalEntry.EventType.UPDATE);
        assertEquals(CanalEntry.EventType.UPDATE, model.getEventType());
    }

    @Test
    void shouldSetAndGetExecuteTime() {
        CanalModel model = CanalModel.builder().build();
        model.setExecuteTime(999L);
        assertEquals(999L, model.getExecuteTime());
    }

    @Test
    void shouldSetAndGetCreateTime() {
        CanalModel model = CanalModel.builder().build();
        model.setCreateTime(888L);
        assertEquals(888L, model.getCreateTime());
    }

    @Test
    void shouldFormatToString() {
        CanalModel model = CanalModel.builder()
                .id(1L)
                .schema("test_db")
                .table("users")
                .eventType(CanalEntry.EventType.INSERT)
                .executeTime(1000L)
                .createTime(2000L)
                .build();

        String result = model.toString();
        assertTrue(result.contains("id=1"));
        assertTrue(result.contains("test_db"));
        assertTrue(result.contains("users"));
        assertTrue(result.contains("INSERT"));
        assertTrue(result.contains("1000"));
        assertTrue(result.contains("2000"));
    }
}
