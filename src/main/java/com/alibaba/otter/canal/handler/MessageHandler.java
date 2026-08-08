package com.alibaba.otter.canal.handler;

/**
 * Functional interface for processing Canal messages. Implementations
 * receive a destination identifier and a message object and perform
 * the actual event dispatching or processing logic.
 *
 * @param <T> the message type (e.g., {@code Message} or {@code FlatMessage})
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 3.0.0
 * @see AbstractMessageHandler
 * @see AbstractFlatMessageHandler
 */
@FunctionalInterface
public interface MessageHandler<T> {

    /**
     * Processes a Canal message from the given destination.
     *
     * @param destination the Canal destination (instance) name
     * @param t           the message to process
     */
    void handleMessage(String destination, T t);

}
