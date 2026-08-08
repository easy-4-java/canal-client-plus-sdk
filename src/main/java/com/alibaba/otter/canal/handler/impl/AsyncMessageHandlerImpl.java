package com.alibaba.otter.canal.handler.impl;


import com.alibaba.otter.canal.handler.AbstractMessageHandler;
import com.alibaba.otter.canal.handler.EntryHandler;
import com.alibaba.otter.canal.handler.RowDataHandler;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.alibaba.otter.canal.protocol.Message;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.List;

/**
 * Asynchronous implementation of {@link AbstractMessageHandler}.
 * Delegates message processing to a {@link ThreadPoolTaskExecutor}
 * so that the Canal polling thread is not blocked.
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 3.0.0
 * @see AbstractMessageHandler
 * @see SyncMessageHandlerImpl
 */
public class AsyncMessageHandlerImpl extends AbstractMessageHandler {

    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    /**
     * Constructs an async message handler with default entry types.
     *
     * @param entryHandlers       the list of entry handlers
     * @param rowDataHandler      the row data handler
     * @param threadPoolTaskExecutor the thread pool for async execution
     */
    public AsyncMessageHandlerImpl(List<? extends EntryHandler> entryHandlers,
                                   RowDataHandler<CanalEntry.RowData> rowDataHandler,
                                   ThreadPoolTaskExecutor threadPoolTaskExecutor) {
        super(null, entryHandlers, rowDataHandler);
        this.threadPoolTaskExecutor = threadPoolTaskExecutor;
    }

    /**
     * Constructs an async message handler with custom entry types.
     *
     * @param subscribeTypes      the entry types to subscribe to
     * @param entryHandlers       the list of entry handlers
     * @param rowDataHandler      the row data handler
     * @param threadPoolTaskExecutor the thread pool for async execution
     */
    public AsyncMessageHandlerImpl(List<CanalEntry.EntryType> subscribeTypes,
                                   List<? extends EntryHandler> entryHandlers,
                                   RowDataHandler<CanalEntry.RowData> rowDataHandler,
                                   ThreadPoolTaskExecutor threadPoolTaskExecutor) {
        super(subscribeTypes, entryHandlers, rowDataHandler);
        this.threadPoolTaskExecutor = threadPoolTaskExecutor;
    }

    /**
     * Submits message processing to the thread pool for asynchronous execution.
     *
     * @param destination the Canal destination name
     * @param message     the Canal message to process
     */
    @Override
    public void handleMessage(String destination, Message message) {
        threadPoolTaskExecutor.execute(() -> super.handleMessage(destination, message));
    }

}
