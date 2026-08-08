package com.alibaba.otter.canal.client;

import com.alibaba.otter.canal.client.rabbitmq.RabbitMQCanalConnector;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Canal client for RabbitMQ mode. Consumes Canal events from a RabbitMQ
 * exchange using a {@link RabbitMQCanalConnector}.
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 3.0.0
 * @see AbstractMQCanalClient
 */
public class RabbitMQCanalClient extends AbstractMQCanalClient<RabbitMQCanalConnector> {

    private RabbitMQCanalClient(List<RabbitMQCanalConnector> connectors) {
        super(connectors);
    }

    /**
     * Returns the destination (name server) from the RabbitMQ connector's field.
     *
     * @param connector the RabbitMQ Canal connector
     * @return the name server string
     */
    @Override
    protected String getDestination(RabbitMQCanalConnector connector) {
        Field nameServerField =  ReflectionUtils.findField(RabbitMQCanalConnector.class, "nameServer");
        ReflectionUtils.makeAccessible(nameServerField);
        return (String) ReflectionUtils.getField(nameServerField, connector);
    }

    /**
     * Builder for constructing {@link RabbitMQCanalClient} instances.
     */
    public static final class Builder extends AbstractClientBuilder<RabbitMQCanalClient, RabbitMQCanalConnector> {

        /**
         * Builds a {@link RabbitMQCanalClient} with the configured properties.
         *
         * @param connectors the list of RabbitMQ Canal connectors
         * @return the built client
         */
        @Override
        public RabbitMQCanalClient build(List<RabbitMQCanalConnector> connectors) {
            RabbitMQCanalClient canalClient = new RabbitMQCanalClient(connectors);
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
