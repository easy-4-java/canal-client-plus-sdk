package com.alibaba.otter.canal.model;


import com.alibaba.otter.canal.protocol.CanalEntry;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Data model representing a Canal change event. Contains metadata about
 * a single database change captured by Canal, including the message id,
 * schema, table, event type, and timestamps.
 *
 * <p>Instances are typically created by message handlers and placed into
 * the {@link com.alibaba.otter.canal.context.CanalContext} thread-local
 * for use by downstream event handler methods.</p>
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 3.0.0
 * @see com.alibaba.otter.canal.context.CanalContext
 */
@Setter
@Getter
@Builder
public class CanalModel {


    /**
     * The Canal message id.
     */
    private long id;

    /**
     * The Canal destination (instance) name.
     */
    private String destination;

    /**
     * The database schema name.
     */
    private String schema;
    /**
     * The table name.
     */
    private String table;
    /**
     * The type of database change event.
     */
    private CanalEntry.EventType eventType;
    /**
     * The binlog execution timestamp in milliseconds.
     */
    private Long executeTime;

    /**
     * The DML build timestamp in milliseconds.
     */
    private Long createTime;

    /**
     * Returns a string representation of this model for debugging.
     *
     * @return a formatted string with all field values
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CanalModel{");
        sb.append("id=").append(id);
        sb.append(", schema='").append(schema).append('\'');
        sb.append(", table='").append(table).append('\'');
        sb.append(", eventType='").append(eventType).append('\'');
        sb.append(", executeTime=").append(executeTime);
        sb.append(", createTime=").append(createTime);
        sb.append('}');
        return sb.toString();
    }

}
