package com.alibaba.otter.canal.annotation;

import java.lang.annotation.*;

/**
 * Identifies a Canal table-mapped entity or handler. When placed on a class
 * that implements {@link com.alibaba.otter.canal.handler.EntryHandler},
 * this annotation binds the handler to a specific destination, schema, and
 * table combination.
 *
 * <p>All attributes default to wildcards ({@code "*"}), meaning the handler
 * will match all destinations, schemas, and tables unless constrained.</p>
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 3.0.0
 * @see OnCanalEvent
 * @see com.alibaba.otter.canal.handler.EntryHandler
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CanalTable {

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

}
