package com.alibaba.otter.canal.client;

import org.springframework.beans.factory.DisposableBean;

/**
 * Core interface for Canal client implementations. Defines the lifecycle
 * (start/stop) and the main processing loop for consuming Canal events.
 *
 * <p>Implementations support different transport modes such as TCP direct
 * connection, cluster mode, Kafka, RocketMQ, RabbitMQ, and PulsarMQ.</p>
 *
 * @param <C> the Canal connector type
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 3.0.0
 * @see AbstractCanalClient
 * @see AbstractMQCanalClient
 */
public interface CanalClient<C extends CanalConnector> extends DisposableBean {

    /**
     * Starts the client, spawning worker threads for each connector.
     */
    void start();

    /**
     * Stops the client and interrupts all worker threads.
     */
    void stop();

    /**
     * Processes Canal events from the given connector in a continuous loop.
     *
     * @param connector the Canal connector to consume events from
     */
    void process(C connector);

}
