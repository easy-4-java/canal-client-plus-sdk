package com.alibaba.otter.canal.util;

import com.alibaba.otter.canal.protocol.CanalEntry;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RowDataUtilTest {

    @Test
    void shouldReturnNullWhenRowDataIsNull() {
        assertNull(RowDataUtil.getBeforeValue(null, "col"));
        assertNull(RowDataUtil.getAfterValue(null, "col"));
        assertNull(RowDataUtil.getValue(null, "col"));
    }

    @Test
    void shouldGetBeforeValue() {
        CanalEntry.Column column = CanalEntry.Column.newBuilder()
                .setName("id").setValue("1").build();
        CanalEntry.RowData rowData = CanalEntry.RowData.newBuilder()
                .addBeforeColumns(column).build();

        assertEquals("1", RowDataUtil.getBeforeValue(rowData, "id"));
    }

    @Test
    void shouldGetBeforeValueCaseInsensitive() {
        CanalEntry.Column column = CanalEntry.Column.newBuilder()
                .setName("UserName").setValue("admin").build();
        CanalEntry.RowData rowData = CanalEntry.RowData.newBuilder()
                .addBeforeColumns(column).build();

        assertEquals("admin", RowDataUtil.getBeforeValue(rowData, "username"));
    }

    @Test
    void shouldReturnNullWhenBeforeColumnNotFound() {
        CanalEntry.Column column = CanalEntry.Column.newBuilder()
                .setName("id").setValue("1").build();
        CanalEntry.RowData rowData = CanalEntry.RowData.newBuilder()
                .addBeforeColumns(column).build();

        assertNull(RowDataUtil.getBeforeValue(rowData, "nonexistent"));
    }

    @Test
    void shouldGetAfterValue() {
        CanalEntry.Column column = CanalEntry.Column.newBuilder()
                .setName("name").setValue("test").build();
        CanalEntry.RowData rowData = CanalEntry.RowData.newBuilder()
                .addAfterColumns(column).build();

        assertEquals("test", RowDataUtil.getAfterValue(rowData, "name"));
    }

    @Test
    void shouldGetAfterValueCaseInsensitive() {
        CanalEntry.Column column = CanalEntry.Column.newBuilder()
                .setName("Email").setValue("test@test.com").build();
        CanalEntry.RowData rowData = CanalEntry.RowData.newBuilder()
                .addAfterColumns(column).build();

        assertEquals("test@test.com", RowDataUtil.getAfterValue(rowData, "email"));
    }

    @Test
    void shouldReturnNullWhenAfterColumnNotFound() {
        CanalEntry.Column column = CanalEntry.Column.newBuilder()
                .setName("id").setValue("1").build();
        CanalEntry.RowData rowData = CanalEntry.RowData.newBuilder()
                .addAfterColumns(column).build();

        assertNull(RowDataUtil.getAfterValue(rowData, "nonexistent"));
    }

    @Test
    void shouldGetValueFromBeforeColumns() {
        CanalEntry.Column column = CanalEntry.Column.newBuilder()
                .setName("id").setValue("42").build();
        CanalEntry.RowData rowData = CanalEntry.RowData.newBuilder()
                .addBeforeColumns(column)
                .addAfterColumns(CanalEntry.Column.newBuilder().setName("id").setValue("99").build())
                .build();

        assertEquals("42", RowDataUtil.getValue(rowData, "id"));
    }

    @Test
    void shouldGetValueFallbackToAfterColumns() {
        CanalEntry.Column column = CanalEntry.Column.newBuilder()
                .setName("id").setValue("55").build();
        CanalEntry.RowData rowData = CanalEntry.RowData.newBuilder()
                .addAfterColumns(column).build();

        assertEquals("55", RowDataUtil.getValue(rowData, "id"));
    }

    @Test
    void shouldReturnNullWhenColumnNotFoundAnywhere() {
        CanalEntry.RowData rowData = CanalEntry.RowData.newBuilder().build();
        assertNull(RowDataUtil.getValue(rowData, "missing"));
    }
}
