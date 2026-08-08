package com.alibaba.otter.canal.handler.impl;


import com.alibaba.otter.canal.factory.IModelFactory;
import com.alibaba.otter.canal.handler.EntryHandler;
import com.alibaba.otter.canal.handler.RowDataHandler;
import com.alibaba.otter.canal.protocol.CanalEntry;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementation of {@link RowDataHandler} for protobuf-based
 * {@link CanalEntry.RowData}. Dispatches INSERT, UPDATE, and DELETE
 * events to the appropriate {@link EntryHandler} methods after
 * converting column data into model objects via the configured
 * {@link IModelFactory}.
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 3.0.0
 * @see RowDataHandler
 * @see IModelFactory
 */
public class RowDataHandlerImpl implements RowDataHandler<CanalEntry.RowData> {


    private IModelFactory<List<CanalEntry.Column>> modelFactory;

    /**
     * Constructs a new handler with the given model factory.
     *
     * @param modelFactory the factory for converting column lists to model objects
     */
    public RowDataHandlerImpl(IModelFactory modelFactory) {
        this.modelFactory = modelFactory;
    }

    /**
     * Dispatches row data to the entry handler based on the event type.
     * For INSERT events, the after-columns are used. For DELETE events,
     * the before-columns are used. For UPDATE events, both before and
     * after columns are converted and passed to the update method.
     *
     * @param rowData      the Canal row data
     * @param entryHandler the handler to delegate to
     * @param eventType    the event type
     * @param <R>          the entity type
     * @throws Exception if model creation or handler invocation fails
     */
    @Override
    public <R> void handlerRowData(CanalEntry.RowData rowData, EntryHandler<R> entryHandler, CanalEntry.EventType eventType) throws Exception {
        if (Objects.isNull(rowData) || Objects.isNull(entryHandler) || Objects.isNull(eventType)) {
            return;
        }
        switch (eventType) {
            case INSERT:
                R object = modelFactory.newInstance(entryHandler, rowData.getAfterColumnsList());
                entryHandler.insert(object);
                break;
            case UPDATE:
                Set<String> updateColumnSet = rowData.getAfterColumnsList().stream().filter(CanalEntry.Column::getUpdated)
                        .map(CanalEntry.Column::getName).collect(Collectors.toSet());
                R before = modelFactory.newInstance(entryHandler, rowData.getBeforeColumnsList(),updateColumnSet);
                R after = modelFactory.newInstance(entryHandler, rowData.getAfterColumnsList());
                entryHandler.update(before, after);
                break;
            case DELETE:
                R o = modelFactory.newInstance(entryHandler, rowData.getBeforeColumnsList());
                entryHandler.delete(o);
                break;
            default:
                break;
        }
    }
}
