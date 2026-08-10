package com.alibaba.otter.canal.annotation;

import com.alibaba.otter.canal.protocol.CanalEntry;

import java.lang.annotation.*;

/**
 * Marks a method as a Canal event listener. Methods annotated with
 * {@code @OnCanalEvent} are invoked when a matching database change event
 * is received from the Canal server.
 *
 * <p>The annotation attributes specify the destination, schema, table,
 * and event types to filter on. Only events matching all specified
 * criteria will trigger the annotated method.</p>
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 3.0.0
 * @see CanalEventHandler
 * @see CanalTable
 */

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OnCanalEvent {

    /**
     * The Canal destination (instance) to listen on.
     * Defaults to {@code ""} which matches all destinations.
     *
     * @return the Canal destination identifier
     */
    String destination() default "";

    /**
     * The database schema to listen on.
     * Defaults to {@code "*"} which matches all schemas.
     *
     * @return the database schema name
     */
    String schema() default "*";

    /**
     * The table name to listen on.
     * Defaults to {@code "*"} which matches all tables.
     *
     * @return the table name
     */
    String table() default "*";

    /**
     * The Canal event types to listen for (e.g., INSERT, UPDATE, DELETE).
     * This attribute is required and must contain at least one event type.
     *
     * @return the array of Canal event types
     */
    CanalEntry.EventType[] eventType();

}
