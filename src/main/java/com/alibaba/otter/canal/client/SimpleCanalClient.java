package com.alibaba.otter.canal.client;

import com.alibaba.otter.canal.client.impl.SimpleCanalConnector;
import com.alibaba.otter.canal.protocol.ClientIdentity;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Canal client for simple (direct TCP) mode. Connects to a single
 * Canal server instance using a {@link SimpleCanalConnector}.
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 3.0.0
 * @see AbstractCanalClient
 * @see ClusterCanalClient
 */
public class SimpleCanalClient extends AbstractCanalClient<SimpleCanalConnector> {

    private SimpleCanalClient(List<SimpleCanalConnector> connectors) {
        super(connectors);
    }

    /**
     * Returns the destination name from the connector's client identity.
     *
     * @param connector the simple Canal connector
     * @return the destination name
     */
    @Override
    protected String getDestination(SimpleCanalConnector connector) {
        Field clientIdentityField = ReflectionUtils.findField(SimpleCanalConnector.class, "clientIdentity");
        ReflectionUtils.makeAccessible(clientIdentityField);
        ClientIdentity clientIdentity = (ClientIdentity) ReflectionUtils.getField(clientIdentityField, connector);
        return clientIdentity.getDestination();
    }

    /**
     * Builder for constructing {@link SimpleCanalClient} instances.
     */
    public static final class Builder extends AbstractClientBuilder<SimpleCanalClient, SimpleCanalConnector> {

        /**
         * Builds a {@link SimpleCanalClient} with the configured properties.
         *
         * @param connectors the list of simple Canal connectors
         * @return the built client
         */
        @Override
        public SimpleCanalClient build(List<SimpleCanalConnector> connectors) {
            SimpleCanalClient canalClient = new SimpleCanalClient(connectors);
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
