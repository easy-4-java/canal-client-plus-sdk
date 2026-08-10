package com.alibaba.otter.canal.annotation.event;

import com.alibaba.otter.canal.annotation.OnCanalEvent;
import com.alibaba.otter.canal.protocol.CanalEntry;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * Shorthand annotation for listening to INSERT events.
 * When placed on a method, it is invoked when a new row is inserted
 * into the specified table.
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 3.0.0
 * @see OnCanalEvent
 */

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@OnCanalEvent(eventType = CanalEntry.EventType.INSERT)
public @interface OnInsertEvent {

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
