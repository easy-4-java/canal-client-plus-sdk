package com.alibaba.otter.canal.handler;

import com.alibaba.otter.canal.protocol.CanalEntry;

/**
 * Strategy interface for dispatching row-level data to an
 * {@link EntryHandler} based on the Canal event type. Implementations
 * extract before/after column data and delegate to the appropriate
 * handler method (insert, update, or delete).
 *
 * @param <T> the row data type (e.g., {@code RowData} or {@code List<Map<String,String>>})
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 3.0.0
 * @see com.alibaba.otter.canal.handler.impl.RowDataHandlerImpl
 * @see com.alibaba.otter.canal.handler.impl.MapRowDataHandlerImpl
 */
public interface RowDataHandler<T> {

    /**
     * Dispatches the given row data to the entry handler based on the event type.
     *
     * @param t            the row data
     * @param entryHandler the handler to delegate to
     * @param eventType    the Canal event type (INSERT, UPDATE, DELETE)
     * @param <R>          the entity type
     * @throws Exception if handler invocation fails
     */
    <R> void handlerRowData(T t, EntryHandler<R> entryHandler, CanalEntry.EventType eventType) throws Exception;

}
