package com.alibaba.otter.canal.handler;

/**
 * Strategy interface for handling CRUD events on a specific entity type.
 * Implementations define how to process insert, update, and delete
 * operations for rows of type {@code R}.
 *
 * <p>All methods have default empty implementations so that handlers
 * can override only the events they care about.</p>
 *
 * @param <R> the entity type this handler processes
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 3.0.0
 * @see com.alibaba.otter.canal.annotation.CanalTable
 * @see RowDataHandler
 */


public interface EntryHandler<R> {

    /**
     * Called when a new row of type {@code R} is inserted.
     *
     * @param t the newly inserted entity
     */
    default void insert(R t) {

    }


    /**
     * Called when an existing row of type {@code R} is updated.
     *
     * @param before the entity state before the update
     * @param after  the entity state after the update
     */
    default void update(R before, R after) {

    }


    /**
     * Called when an existing row of type {@code R} is deleted.
     *
     * @param t the deleted entity
     */
    default void delete(R t) {

    }
}
