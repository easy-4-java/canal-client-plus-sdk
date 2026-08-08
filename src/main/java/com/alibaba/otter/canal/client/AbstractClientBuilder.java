package com.alibaba.otter.canal.client;

import com.alibaba.otter.canal.handler.MessageHandler;
import com.alibaba.otter.canal.protocol.CanalEntry;
import lombok.experimental.Accessors;
import org.apache.commons.lang.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Abstract builder for constructing {@link CanalClient} instances.
 * Provides a fluent API for configuring common client properties
 * such as filter, batch size, timeout, and message handler.
 *
 * @param <R> the Canal client type to build
 * @param <C> the Canal connector type
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 3.0.0
 * @see CanalClient
 */
@Accessors(chain = true)
public abstract class AbstractClientBuilder<R extends CanalClient, C extends CanalConnector> {

    /**
     * Message filter expression.
     */
    protected String filter = StringUtils.EMPTY;
    /**
     * Batch size for fetching messages.
     */
    protected Integer batchSize = 1;
    /**
     * Timeout for fetching data.
     */
    protected Long timeout = 1L;
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
    protected MessageHandler messageHandler;

    /**
     * Sets the filter expression for the client.
     *
     * @param filter the filter expression
     * @return this builder for chaining
     */
    public AbstractClientBuilder filter(String filter) {
        this.filter = filter;
        return this;
    }

    /**
     * Sets the batch size for the client.
     *
     * @param batchSize the batch size
     * @return this builder for chaining
     */
    public AbstractClientBuilder batchSize(Integer batchSize) {
        this.batchSize = batchSize;
        return this;
    }

    /**
     * Sets the timeout for the client.
     *
     * @param timeout the timeout value
     * @return this builder for chaining
     */
    public AbstractClientBuilder timeout(Long timeout) {
        this.timeout = timeout;
        return this;
    }

    /**
     * Sets the time unit for the timeout.
     *
     * @param unit the time unit
     * @return this builder for chaining
     */
    public AbstractClientBuilder unit(TimeUnit unit) {
        this.unit = unit;
        return this;
    }

    /**
     * Sets the subscribed entry types.
     *
     * @param subscribeTypes the entry types to subscribe to
     * @return this builder for chaining
     */
    public AbstractClientBuilder setSubscribeTypes(List<CanalEntry.EntryType> subscribeTypes) {
        this.subscribeTypes = subscribeTypes;
        return this;
    }

    /**
     * Sets the message handler.
     *
     * @param messageHandler the message handler
     * @return this builder for chaining
     */
    public AbstractClientBuilder messageHandler(MessageHandler messageHandler) {
        this.messageHandler = messageHandler;
        return this;
    }

    /**
     * Builds the Canal client with the given connectors.
     *
     * @param connectors the list of Canal connectors
     * @return the built Canal client
     */
    public abstract R build(List<C> connectors);

}
