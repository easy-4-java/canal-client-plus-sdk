package com.alibaba.otter.canal.client;

import com.alibaba.otter.canal.handler.MessageHandler;
import com.alibaba.otter.canal.protocol.FlatMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * Abstract base implementation of {@link CanalClient} for message-queue-based
 * Canal connectors (Kafka, RocketMQ, RabbitMQ, PulsarMQ). Processes
 * {@link FlatMessage} objects instead of protobuf-based {@code Message}
 * objects, making it suitable for MQ pipelines that deliver JSON payloads.
 *
 * @param <C> the MQ Canal connector type
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 3.0.0
 * @see KafkaCanalClient
 * @see RocketMQCanalClient
 * @see RabbitMQCanalClient
 * @see PulsarMQCanalClient
 */
@Slf4j
public abstract class AbstractMQCanalClient<C extends CanalMQConnector> extends AbstractCanalClient<C> {

    /**
     * Constructs a new MQ client with the given connectors.
     *
     * @param connectors the MQ Canal connectors
     */
    public AbstractMQCanalClient(List<C> connectors) {
        super(connectors);
    }

    /**
     * Main processing loop that connects to the MQ source, subscribes,
     * and continuously fetches and dispatches flat messages until stopped.
     *
     * @param connector the MQ Canal connector to consume events from
     */
    @Override
    public void process(C connector) {
        String destination = this.getDestination(connector);
        MessageHandler messageHandler = super.getMessageHandler();
        while (running) {
            try {
                connector.connect();
                connector.subscribe();
                while (running) {
                    try {
                        List<FlatMessage> messages = connector.getFlatListWithoutAck(timeout, unit);
                        if (CollectionUtils.isEmpty(messages)) {
                            continue;
                        }
                        for (FlatMessage flatMessage : messages) {
                            messageHandler.handleMessage(destination, flatMessage);
                        }
                        connector.ack();
                    } catch (Exception e) {
                        log.error("canal consume exception", e);
                    }
                }
            } catch (Exception e) {
                log.error("canal connection exception", e);
            }
        }
        connector.unsubscribe();
        connector.disconnect();
    }

}
