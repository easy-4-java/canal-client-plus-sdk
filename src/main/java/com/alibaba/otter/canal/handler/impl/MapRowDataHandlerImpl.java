package com.alibaba.otter.canal.handler.impl;


import com.alibaba.otter.canal.factory.IModelFactory;
import com.alibaba.otter.canal.handler.EntryHandler;
import com.alibaba.otter.canal.handler.RowDataHandler;
import com.alibaba.otter.canal.protocol.CanalEntry;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Implementation of {@link RowDataHandler} for flat map-based row data.
 * Used with the FlatMessage pipeline where each row is represented as
 * a {@code Map<String, String>}. Dispatches INSERT, UPDATE, and DELETE
 * events to the appropriate {@link EntryHandler} methods after
 * converting map data into model objects via the configured
 * {@link IModelFactory}.
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 3.0.0
 * @see RowDataHandler
 * @see IModelFactory
 */
public class MapRowDataHandlerImpl implements RowDataHandler<List<Map<String, String>>> {

    private IModelFactory<Map<String,String>> modelFactory;

    /**
     * Constructs a new handler with the given model factory.
     *
     * @param modelFactory the factory for converting maps to model objects
     */
    public MapRowDataHandlerImpl(IModelFactory<Map<String, String>> modelFactory) {
        this.modelFactory = modelFactory;
    }

    /**
     * Dispatches flat row data to the entry handler based on the event type.
     * The first element of the list is the "after" data; for UPDATE events,
     * the second element is the "before" data.
     *
     * @param list         the list of row data maps (index 0 = after, index 1 = before for updates)
     * @param entryHandler the handler to delegate to
     * @param eventType    the event type
     * @param <R>          the entity type
     * @throws Exception if model creation or handler invocation fails
     */
    @Override
    public <R> void handlerRowData(List<Map<String, String>> list, EntryHandler<R> entryHandler, CanalEntry.EventType eventType) throws Exception{
        if (Objects.isNull(list) || Objects.isNull(entryHandler) || Objects.isNull(eventType)) {
            return;
        }
        switch (eventType) {
            case INSERT:
                R entry  = modelFactory.newInstance(entryHandler, list.get(0));
                entryHandler.insert(entry);
                break;
            case UPDATE:
                R before = modelFactory.newInstance(entryHandler, list.get(1));
                R after = modelFactory.newInstance(entryHandler, list.get(0));
                entryHandler.update(before, after);
                break;
            case DELETE:
                R o = modelFactory.newInstance(entryHandler, list.get(0));
                entryHandler.delete(o);
                break;
            default:
                break;
        }
    }
}
