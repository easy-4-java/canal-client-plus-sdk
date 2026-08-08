package com.alibaba.otter.canal.client;

import com.alibaba.otter.canal.client.rocketmq.RocketMQCanalConnector;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Canal client for RocketMQ mode. Consumes Canal events from a RocketMQ
 * topic using a {@link RocketMQCanalConnector}.
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 3.0.0
 * @see AbstractMQCanalClient
 */
public class RocketMQCanalClient extends AbstractMQCanalClient<RocketMQCanalConnector> {

    /**
     * Constructs a new RocketMQ Canal client with the given connectors.
     *
     * @param connectors the RocketMQ Canal connectors
     */
    public RocketMQCanalClient(List<RocketMQCanalConnector> connectors) {
        super(connectors);
    }

    /**
     * Returns the destination (topic) name from the RocketMQ connector's field.
     *
     * @param connector the RocketMQ Canal connector
     * @return the topic name
     */
    @Override
    protected String getDestination(RocketMQCanalConnector connector) {
        Field topicField =  ReflectionUtils.findField(RocketMQCanalConnector.class, "topic");
        ReflectionUtils.makeAccessible(topicField);
        return (String) ReflectionUtils.getField(topicField, connector);
    }

    /**
     * Builder for constructing {@link RocketMQCanalClient} instances.
     */
    public static final class Builder extends AbstractClientBuilder<RocketMQCanalClient, RocketMQCanalConnector> {

        /**
         * Builds a {@link RocketMQCanalClient} with the configured properties.
         *
         * @param connectors the list of RocketMQ Canal connectors
         * @return the built client
         */
        @Override
        public RocketMQCanalClient build(List<RocketMQCanalConnector> connectors) {
            RocketMQCanalClient canalClient = new RocketMQCanalClient(connectors);
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
