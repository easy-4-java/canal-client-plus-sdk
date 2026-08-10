package com.alibaba.otter.canal.client;

import com.alibaba.otter.canal.client.pulsarmq.PulsarMQCanalConnector;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Canal client for PulsarMQ mode. Consumes Canal events from an Apache
 * Pulsar topic using a {@link PulsarMQCanalConnector}.
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 3.0.0
 * @see AbstractMQCanalClient
 */
@Slf4j
public class PulsarMQCanalClient extends AbstractMQCanalClient<PulsarMQCanalConnector> {

    private PulsarMQCanalClient(List<PulsarMQCanalConnector> connectors) {
        super(connectors);
    }

    /**
     * Returns the destination (topic) name from the Pulsar connector's field.
     *
     * @param connector the PulsarMQ Canal connector
     * @return the topic name
     */
    @Override
    protected String getDestination(PulsarMQCanalConnector connector) {
        Field topicField =  ReflectionUtils.findField(PulsarMQCanalConnector.class, "topic");
        ReflectionUtils.makeAccessible(topicField);
        return (String) ReflectionUtils.getField(topicField, connector);
    }

    /**
     * Builder for constructing {@link PulsarMQCanalClient} instances.
     */
    public static final class Builder extends AbstractClientBuilder<PulsarMQCanalClient, PulsarMQCanalConnector> {

        /**
         * Builds a {@link PulsarMQCanalClient} with the configured properties.
         *
         * @param connectors the list of PulsarMQ Canal connectors
         * @return the built client
         */
        @Override
        public PulsarMQCanalClient build(List<PulsarMQCanalConnector> connectors) {
            PulsarMQCanalClient canalClient = new PulsarMQCanalClient(connectors);
            canalClient.setBatchSize(batchSize);
            canalClient.setFilter(filter);
            canalClient.setMessageHandler(messageHandler);
            canalClient.setTimeout(timeout);
            canalClient.setUnit(unit);
            canalClient.setSubscribeTypes(subscribeTypes);
            return canalClient;
        }

    }
}
