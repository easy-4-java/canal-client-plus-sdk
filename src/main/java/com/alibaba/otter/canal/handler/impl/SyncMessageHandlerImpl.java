package com.alibaba.otter.canal.handler.impl;


import com.alibaba.otter.canal.handler.AbstractMessageHandler;
import com.alibaba.otter.canal.handler.EntryHandler;
import com.alibaba.otter.canal.handler.RowDataHandler;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.alibaba.otter.canal.protocol.Message;

import java.util.List;

/**
 * Synchronous implementation of {@link AbstractMessageHandler}.
 * Processes Canal messages on the calling thread, blocking until
 * all entries have been handled.
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 3.0.0
 * @see AbstractMessageHandler
 * @see AsyncMessageHandlerImpl
 */
public class SyncMessageHandlerImpl extends AbstractMessageHandler {


    /**
     * Constructs a synchronous message handler with default entry types.
     *
     * @param entryHandlers  the list of entry handlers
     * @param rowDataHandler the row data handler
     */
    public SyncMessageHandlerImpl(List<? extends EntryHandler> entryHandlers,
                                  RowDataHandler<CanalEntry.RowData> rowDataHandler) {
        super(null, entryHandlers, rowDataHandler);
    }

    /**
     * Constructs a synchronous message handler with custom entry types.
     *
     * @param subscribeTypes the entry types to subscribe to
     * @param entryHandlers  the list of entry handlers
     * @param rowDataHandler the row data handler
     */
    public SyncMessageHandlerImpl(List<CanalEntry.EntryType> subscribeTypes,
                                  List<? extends EntryHandler> entryHandlers,
                                  RowDataHandler<CanalEntry.RowData> rowDataHandler) {
        super(subscribeTypes, entryHandlers, rowDataHandler);
    }

    /**
     * Handles the message synchronously on the calling thread.
     *
     * @param destination the Canal destination name
     * @param message     the Canal message to process
     */
    @Override
    public void handleMessage(String destination, Message message) {
        super.handleMessage(destination, message);
    }


}
