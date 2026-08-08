package com.alibaba.otter.canal.util;

import com.alibaba.otter.canal.protocol.CanalEntry;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CanalUtilsTest {

    @Test
    void shouldBuildPositionForDump() {
        CanalEntry.Header header = CanalEntry.Header.newBuilder()
                .setLogfileName("mysql-bin.000001")
                .setLogfileOffset(1234)
                .setExecuteTime(System.currentTimeMillis())
                .setSchemaName("test_db")
                .setTableName("test_table")
                .build();
        CanalEntry.Entry entry = CanalEntry.Entry.newBuilder()
                .setHeader(header)
                .setEntryType(CanalEntry.EntryType.ROWDATA)
                .build();

        String position = CanalUtils.buildPositionForDump(entry);
        assertNotNull(position);
        assertTrue(position.contains("mysql-bin.000001"));
        assertTrue(position.contains("1234"));
    }

    @Test
    void shouldBuildPositionForDumpWithGtid() {
        CanalEntry.Header header = CanalEntry.Header.newBuilder()
                .setLogfileName("mysql-bin.000002")
                .setLogfileOffset(5678)
                .setExecuteTime(System.currentTimeMillis())
                .setGtid("uuid:123")
                .build();
        CanalEntry.Entry entry = CanalEntry.Entry.newBuilder()
                .setHeader(header)
                .setEntryType(CanalEntry.EntryType.ROWDATA)
                .build();

        String position = CanalUtils.buildPositionForDump(entry);
        assertTrue(position.contains("gtid(uuid:123)"));
    }

    @Test
    void shouldReturnEmptyStringWhenGtidNotInHeader() {
        CanalEntry.Header header = CanalEntry.Header.newBuilder()
                .setLogfileName("mysql-bin.000001")
                .setLogfileOffset(0)
                .setExecuteTime(0)
                .build();

        String gtid = CanalUtils.getCurrentGtid(header);
        assertEquals("", gtid);
    }

    @Test
    void shouldReturnGtidFromHeader() {
        CanalEntry.Pair pair = CanalEntry.Pair.newBuilder()
                .setKey("curtGtid").setValue("uuid:456").build();
        CanalEntry.Header header = CanalEntry.Header.newBuilder()
                .addProps(pair).build();

        String gtid = CanalUtils.getCurrentGtid(header);
        assertEquals("uuid:456", gtid);
    }

    @Test
    void shouldReturnEmptyStringWhenGtidSnNotInHeader() {
        CanalEntry.Header header = CanalEntry.Header.newBuilder().build();
        assertEquals("", CanalUtils.getCurrentGtidSn(header));
    }

    @Test
    void shouldReturnGtidSnFromHeader() {
        CanalEntry.Pair pair = CanalEntry.Pair.newBuilder()
                .setKey("curtGtidSn").setValue("sn-value").build();
        CanalEntry.Header header = CanalEntry.Header.newBuilder()
                .addProps(pair).build();

        assertEquals("sn-value", CanalUtils.getCurrentGtidSn(header));
    }

    @Test
    void shouldReturnEmptyStringWhenGtidLctNotInHeader() {
        CanalEntry.Header header = CanalEntry.Header.newBuilder().build();
        assertEquals("", CanalUtils.getCurrentGtidLct(header));
    }

    @Test
    void shouldReturnGtidLctFromHeader() {
        CanalEntry.Pair pair = CanalEntry.Pair.newBuilder()
                .setKey("curtGtidLct").setValue("lct-value").build();
        CanalEntry.Header header = CanalEntry.Header.newBuilder()
                .addProps(pair).build();

        assertEquals("lct-value", CanalUtils.getCurrentGtidLct(header));
    }

    @Test
    void shouldPrintColumnWithoutThrowing() {
        CanalEntry.Column column = CanalEntry.Column.newBuilder()
                .setName("id").setValue("1").setMysqlType("int").build();
        List<CanalEntry.Column> columns = List.of(column);

        assertDoesNotThrow(() -> CanalUtils.printColumn(columns));
    }

    @Test
    void shouldPrintColumnWithUpdatedFlag() {
        CanalEntry.Column column = CanalEntry.Column.newBuilder()
                .setName("name").setValue("test").setMysqlType("varchar").setUpdated(true).build();
        assertDoesNotThrow(() -> CanalUtils.printColumn(List.of(column)));
    }

    @Test
    void shouldHandleNullPairsInPrintXAInfo() {
        assertDoesNotThrow(() -> CanalUtils.printXAInfo(null));
    }

    @Test
    void shouldHandleEmptyPairsInPrintXAInfo() {
        assertDoesNotThrow(() -> CanalUtils.printXAInfo(Collections.emptyList()));
    }

    @Test
    void shouldPrintXAInfoWhenPresent() {
        CanalEntry.Pair xaType = CanalEntry.Pair.newBuilder()
                .setKey("XA_TYPE").setValue("XA_START").build();
        CanalEntry.Pair xaXid = CanalEntry.Pair.newBuilder()
                .setKey("XA_XID").setValue("xid-123").build();

        assertDoesNotThrow(() -> CanalUtils.printXAInfo(List.of(xaType, xaXid)));
    }

    @Test
    void shouldReturnEmptyListForPropsWithNoMatchingKey() {
        CanalEntry.Pair pair = CanalEntry.Pair.newBuilder()
                .setKey("otherKey").setValue("value").build();
        CanalEntry.Header header = CanalEntry.Header.newBuilder()
                .addProps(pair).build();

        assertEquals("", CanalUtils.getCurrentGtid(header));
        assertEquals("", CanalUtils.getCurrentGtidSn(header));
        assertEquals("", CanalUtils.getCurrentGtidLct(header));
    }
}
