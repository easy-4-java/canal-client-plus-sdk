package com.alibaba.otter.canal.handler.impl;


import com.alibaba.otter.canal.handler.AbstractFlatMessageHandler;
import com.alibaba.otter.canal.handler.EntryHandler;
import com.alibaba.otter.canal.handler.RowDataHandler;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.alibaba.otter.canal.protocol.FlatMessage;

import java.util.List;
import java.util.Map;

/**
 * Synchronous implementation of {@link AbstractFlatMessageHandler}.
 * Processes {@link FlatMessage} objects on the calling thread,
 * blocking until all data rows have been handled.
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 3.0.0
 * @see AbstractFlatMessageHandler
 * @see AsyncFlatMessageHandlerImpl
 */
public class SyncFlatMessageHandlerImpl extends AbstractFlatMessageHandler {

    /**
     * Constructs a synchronous flat message handler with default entry types.
     *
     * @param entryHandlers  the list of entry handlers
     * @param rowDataHandler the row data handler
     */
    public SyncFlatMessageHandlerImpl(List<? extends EntryHandler> entryHandlers,
                                      RowDataHandler<List<Map<String, String>>> rowDataHandler) {
        super(null, entryHandlers, rowDataHandler);
    }

    /**
     * Constructs a synchronous flat message handler with custom entry types.
     *
     * @param subscribeTypes the entry types to subscribe to
     * @param entryHandlers  the list of entry handlers
     * @param rowDataHandler the row data handler
     */
    public SyncFlatMessageHandlerImpl(List<CanalEntry.EntryType> subscribeTypes,
                                      List<? extends EntryHandler> entryHandlers,
                                      RowDataHandler<List<Map<String, String>>> rowDataHandler) {
        super(subscribeTypes, entryHandlers, rowDataHandler);
    }

    /**
     * Handles the flat message synchronously on the calling thread.
     *
     * @param destination the Canal destination name
     * @param flatMessage the flat message to process
     */
    @Override
    public void handleMessage(String destination, FlatMessage flatMessage) {
        super.handleMessage(destination, flatMessage);
    }
}
