package com.alibaba.otter.canal.handler.impl;


import com.alibaba.otter.canal.handler.AbstractFlatMessageHandler;
import com.alibaba.otter.canal.handler.EntryHandler;
import com.alibaba.otter.canal.handler.RowDataHandler;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.alibaba.otter.canal.protocol.FlatMessage;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.List;
import java.util.Map;

/**
 * Asynchronous implementation of {@link AbstractFlatMessageHandler}.
 * Delegates flat message processing to a {@link ThreadPoolTaskExecutor}
 * so that the Canal polling thread is not blocked.
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 3.0.0
 * @see AbstractFlatMessageHandler
 * @see SyncFlatMessageHandlerImpl
 */
public class AsyncFlatMessageHandlerImpl extends AbstractFlatMessageHandler {

    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    /**
     * Constructs an async flat message handler with default entry types.
     *
     * @param entryHandlers       the list of entry handlers
     * @param rowDataHandler      the row data handler
     * @param threadPoolTaskExecutor the thread pool for async execution
     */
    public AsyncFlatMessageHandlerImpl(List<? extends EntryHandler> entryHandlers,
                                       RowDataHandler<List<Map<String, String>>> rowDataHandler,
                                       ThreadPoolTaskExecutor threadPoolTaskExecutor) {
        super(null, entryHandlers, rowDataHandler);
        this.threadPoolTaskExecutor = threadPoolTaskExecutor;
    }

    /**
     * Constructs an async flat message handler with custom entry types.
     *
     * @param subscribeTypes      the entry types to subscribe to
     * @param entryHandlers       the list of entry handlers
     * @param rowDataHandler      the row data handler
     * @param threadPoolTaskExecutor the thread pool for async execution
     */
    public AsyncFlatMessageHandlerImpl(List<CanalEntry.EntryType> subscribeTypes,
                                       List<? extends EntryHandler> entryHandlers,
                                       RowDataHandler<List<Map<String, String>>> rowDataHandler,
                                       ThreadPoolTaskExecutor threadPoolTaskExecutor) {
        super(subscribeTypes, entryHandlers, rowDataHandler);
        this.threadPoolTaskExecutor = threadPoolTaskExecutor;
    }

    /**
     * Submits flat message processing to the thread pool for asynchronous execution.
     *
     * @param destination the Canal destination name
     * @param flatMessage the flat message to process
     */
    @Override
    public void handleMessage(String destination, FlatMessage flatMessage) {
        threadPoolTaskExecutor.execute(() -> super.handleMessage(destination, flatMessage));
    }


}
