package com.alibaba.otter.canal.client;

import com.alibaba.otter.canal.handler.MessageHandler;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.alibaba.otter.canal.protocol.Message;
import com.alibaba.otter.canal.util.CanalUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.slf4j.MDC;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Abstract base implementation of {@link CanalClient} for direct TCP
 * Canal connectors. Manages the lifecycle of worker threads, each of
 * which runs a continuous polling loop that fetches messages from a
 * Canal connector and dispatches them to the configured
 * {@link MessageHandler}.
 *
 * @param <C> the Canal connector type
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 3.0.0
 * @see SimpleCanalClient
 * @see ClusterCanalClient
 */
@Slf4j
public abstract class AbstractCanalClient<C extends CanalConnector> implements CanalClient<C> {

    protected Thread.UncaughtExceptionHandler handler            = (t, e) -> log.error("parse events has an error",
            e);
    /**
     * Whether the client is currently running.
     */
    protected volatile boolean running;
    /**
     * The list of Canal connectors.
     */
    private List<C> connectors;
    /**
     * Message filter expression.
     */
    protected String filter = StringUtils.EMPTY;
    /**
     * Batch size for fetching messages.
     */
    protected Integer batchSize = 1;
    /**
     * Timeout for fetching data (-1 means no timeout control).
     */
    protected Long timeout = -1L;
    /**
     * Time unit for the timeout value.
     */
    protected TimeUnit unit = TimeUnit.SECONDS;
    /**
     * The entry types to subscribe to.
     */
    protected List<CanalEntry.EntryType> subscribeTypes = Arrays.asList(CanalEntry.EntryType.ROWDATA);
    /**
     * The message handler for processing Canal events.
     */
    private MessageHandler messageHandler;
    /**
     * Worker threads, one per connector.
     */
    private Thread[] workThreads;

    /**
     * Constructs a new client with the given connectors.
     *
     * @param connectors the Canal connectors
     */
    public AbstractCanalClient(List<C> connectors) {
        this.connectors = connectors;
    }

    /**
     * Starts the client by spawning a worker thread for each connector.
     */
    @Override
    public void start() {
        log.info("start canal client");
        workThreads = new Thread[connectors.size()];
        for (int i = 0; i < connectors.size(); i++) {
            C connector = connectors.get(i);
            Thread workThread = new Thread(() -> process(connector));
            workThread.setName("canal-client-thread-" + i);
            workThread.setUncaughtExceptionHandler(handler);
            workThreads[i] = workThread;
            workThread.start();
        }
        running = true;
    }

    /**
     * Stops the client by setting the running flag to false and
     * interrupting all worker threads.
     */
    @Override
    public void stop() {
        log.info("stop canal client");
        running = false;
        for (Thread workThread : workThreads) {
            if (Objects.nonNull(workThread) && workThread.isAlive()){
                workThread.interrupt();
            }
        }
    }

    /**
     * Returns the destination name for the given connector.
     *
     * @param connector the Canal connector
     * @return the destination name
     */
    protected abstract String getDestination(C connector);

    /**
     * Main processing loop that connects to the Canal server, subscribes,
     * and continuously fetches and dispatches messages until stopped.
     *
     * @param connector the Canal connector to consume events from
     */
    @Override
    public void process(C connector) {
        String destination = this.getDestination(connector);
        while (running) {
            try {
                MDC.put("destination", destination);
                connector.connect();
                connector.subscribe(filter);
                while (running) {
                    Message message = connector.getWithoutAck(batchSize, timeout, unit);
                    long batchId = message.getId();
                    int size = message.getEntries().size();
                    if (batchId == -1 || size == 0) {
                         try {
                            Thread.sleep(1000);
                         } catch (InterruptedException e) {
                         }
                    } else {
                        CanalUtils.printSummary(message, batchId, size);
                        CanalUtils.printEntry(message.getEntries());
                        messageHandler.handleMessage(destination, message);
                    }

                    if (batchId != -1) {
                        connector.ack(batchId);
                    }

                }
            } catch (Exception e) {
                log.error("process error!", e);
                try {
                    Thread.sleep(2000L);
                } catch (InterruptedException e1) {
                    // ignore
                }
                connector.rollback();
            } finally {
                connector.disconnect();
            }
        }
    }

    /**
     * Calls {@link #stop()} when the Spring bean is destroyed.
     *
     * @throws Exception if destruction fails
     */
    @Override
    public void destroy() throws Exception {
        stop();
    }

    /**
     * Sets the batch size for fetching messages.
     *
     * @param batchSize the batch size
     */
    public void setBatchSize(Integer batchSize) {
        this.batchSize = batchSize;
    }

    /**
     * Sets the message filter expression.
     *
     * @param filter the filter expression
     */
    public void setFilter(String filter) {
        this.filter = filter;
    }

    /**
     * Sets the message handler.
     *
     * @param messageHandler the message handler
     */
    public void setMessageHandler(MessageHandler messageHandler) {
        this.messageHandler = messageHandler;
    }

    /**
     * Sets the timeout for fetching data.
     *
     * @param timeout the timeout value
     */
    public void setTimeout(Long timeout) {
        this.timeout = timeout;
    }

    /**
     * Sets the time unit for the timeout.
     *
     * @param unit the time unit
     */
    public void setUnit(TimeUnit unit) {
        this.unit = unit;
    }

    /**
     * Sets the subscribed entry types.
     *
     * @param subscribeTypes the entry types to subscribe to
     */
    public void setSubscribeTypes(List<CanalEntry.EntryType> subscribeTypes) {
        this.subscribeTypes = subscribeTypes;
    }

    /**
     * Returns the message handler.
     *
     * @return the message handler
     */
    public MessageHandler getMessageHandler() {
        return messageHandler;
    }

}
