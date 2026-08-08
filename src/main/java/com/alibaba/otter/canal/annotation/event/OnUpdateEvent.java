package com.alibaba.otter.canal.annotation.event;

import com.alibaba.otter.canal.annotation.OnCanalEvent;
import com.alibaba.otter.canal.protocol.CanalEntry;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * Shorthand annotation for listening to UPDATE events.
 * When placed on a method, it is invoked when an existing row is updated
 * in the specified table.
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 3.0.0
 * @see OnCanalEvent
 */

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@OnCanalEvent(eventType = CanalEntry.EventType.UPDATE)
public @interface OnUpdateEvent {

    /**
     * The Canal destination (instance) to listen on.
     * Defaults to {@code ""} which matches all destinations.
     *
     * @return the Canal destination identifier
     */
    @AliasFor(annotation = OnCanalEvent.class)
    String destination() default "";

    /**
     * The database schema to listen on. Required.
     *
     * @return the database schema name
     */
    @AliasFor(annotation = OnCanalEvent.class)
    String schema();

    /**
     * The table name to listen on. Required.
     *
     * @return the table name
     */
    @AliasFor(annotation = OnCanalEvent.class)
    String table();

}
