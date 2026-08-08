package com.alibaba.otter.canal.client;

import com.alibaba.otter.canal.client.kafka.KafkaCanalConnector;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Canal client for Kafka mode. Consumes Canal events from a Kafka topic
 * using a {@link KafkaCanalConnector}.
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 3.0.0
 * @see AbstractMQCanalClient
 */
@Slf4j
public class KafkaCanalClient extends AbstractMQCanalClient<KafkaCanalConnector> {

    private KafkaCanalClient(List<KafkaCanalConnector> connectors) {
        super(connectors);
    }

    /**
     * Returns the destination (topic) name from the Kafka connector's field.
     *
     * @param connector the Kafka Canal connector
     * @return the topic name
     */
    @Override
    protected String getDestination(KafkaCanalConnector connector) {
        Field topicField =  ReflectionUtils.findField(KafkaCanalConnector.class, "topic");
        ReflectionUtils.makeAccessible(topicField);
        return (String) ReflectionUtils.getField(topicField, connector);
    }

    /**
     * Builder for constructing {@link KafkaCanalClient} instances.
     */
    public static final class Builder extends AbstractClientBuilder<KafkaCanalClient, KafkaCanalConnector> {

        /**
         * Builds a {@link KafkaCanalClient} with the configured properties.
         *
         * @param connectors the list of Kafka Canal connectors
         * @return the built client
         */
        @Override
        public KafkaCanalClient build(List<KafkaCanalConnector> connectors) {
            KafkaCanalClient canalClient = new KafkaCanalClient(connectors);
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
