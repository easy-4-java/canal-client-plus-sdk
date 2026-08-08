package com.alibaba.otter.canal.enums;

import java.util.StringJoiner;

/**
 * Enumeration of well-known Canal table name constants. Currently defines
 * a single wildcard entry {@link #ALL} that matches any destination, schema,
 * and table combination.
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 3.0.0
 * @see com.alibaba.otter.canal.annotation.CanalTable
 */
public enum TableNameEnum {

    /** Wildcard entry matching all destinations, schemas, and tables. */
    ALL("*", "*", "*");

    /** Delimiter used to join destination, schema, and table into a composite key. */
    public static final CharSequence DELIMITER = ".";

    String destination;
    String schema;
    String table;

    TableNameEnum(String destination, String schema, String table) {
        this.destination = destination;
        this.schema = schema;
        this.table = table;
    }

    /**
     * Returns the destination value.
     *
     * @return the destination string
     */
    public String getDestination() {
        return destination;
    }

    /**
     * Returns the schema value.
     *
     * @return the schema string
     */
    public String getSchema() {
        return schema;
    }

    /**
     * Returns the table value.
     *
     * @return the table string
     */
    public String getTable() {
        return table;
    }

    /**
     * Returns a dot-separated representation of {@code schema.table}.
     *
     * @return the string representation
     */
    @Override
    public String toString() {
        StringJoiner joiner = new StringJoiner(".").add(schema).add(table);
        return joiner.toString();
    }

}
