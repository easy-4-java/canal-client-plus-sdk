package com.alibaba.otter.canal.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TableNameEnumTest {

    @Test
    void shouldHaveAllEnumValue() {
        assertNotNull(TableNameEnum.ALL);
    }

    @Test
    void shouldReturnWildcardDestination() {
        assertEquals("*", TableNameEnum.ALL.getDestination());
    }

    @Test
    void shouldReturnWildcardSchema() {
        assertEquals("*", TableNameEnum.ALL.getSchema());
    }

    @Test
    void shouldReturnWildcardTable() {
        assertEquals("*", TableNameEnum.ALL.getTable());
    }

    @Test
    void shouldHaveDotDelimiter() {
        assertEquals(".", TableNameEnum.DELIMITER.toString());
    }

    @Test
    void shouldFormatToStringAsSchemaTable() {
        String result = TableNameEnum.ALL.toString();
        assertEquals("*.*", result);
    }
}
