package com.alibaba.otter.canal.util;

import com.alibaba.otter.canal.protocol.CanalEntry;

import java.util.List;
import java.util.Objects;

/**
 * Utility class for extracting column values from Canal row data.
 * Provides helper methods to retrieve before and after values for
 * a named column from {@link CanalEntry.RowData} objects.
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 3.0.0
 * @see CanalEntry.RowData
 */
public class RowDataUtil {

    /**
     * Returns the value of the named column from the "before" columns list.
     *
     * @param rowData    the Canal row data
     * @param columnName the column name (case-insensitive match)
     * @return the column value as a string, or {@code null} if not found
     */
    public static String getBeforeValue(CanalEntry.RowData rowData, String columnName) {
        if(Objects.isNull(rowData)){
            return null;
        }
        List<CanalEntry.Column> beforeColumnsList = rowData.getBeforeColumnsList();
        if(Objects.isNull(beforeColumnsList)){
            return null;
        }
        for (CanalEntry.Column column : beforeColumnsList) {
            if (column.getName().equalsIgnoreCase(columnName)) {
                return Objects.toString(column.getValue(), null);
            }
        }
        return null;
    }

    /**
     * Returns the value of the named column from the "after" columns list.
     *
     * @param rowData    the Canal row data
     * @param columnName the column name (case-insensitive match)
     * @return the column value as a string, or {@code null} if not found
     */
    public static String getAfterValue(CanalEntry.RowData rowData, String columnName) {
        if(Objects.isNull(rowData)){
            return null;
        }
        List<CanalEntry.Column> afterColumnsList = rowData.getAfterColumnsList();
        if(Objects.isNull(afterColumnsList)){
            return null;
        }
        for (CanalEntry.Column column : afterColumnsList) {
            if (column.getName().equalsIgnoreCase(columnName)) {
                return  Objects.toString(column.getValue(), null);
            }
        }
        return null;
    }

    /**
     * Returns the value of the named column, checking the "before" columns
     * first and falling back to the "after" columns.
     *
     * @param rowData    the Canal row data
     * @param columnName the column name (case-insensitive match)
     * @return the column value as a string, or {@code null} if not found
     */
    public static String getValue(CanalEntry.RowData rowData, String columnName) {
        String value = getBeforeValue(rowData, columnName);
        if(Objects.isNull(value)){
            return getAfterValue(rowData, columnName);
        }
        return value;
    }

}
