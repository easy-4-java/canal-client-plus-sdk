package com.alibaba.otter.canal.client;

import com.alibaba.otter.canal.client.impl.ClusterCanalConnector;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Canal client for cluster mode. Connects to a Canal cluster using
 * a {@link ClusterCanalConnector} which supports automatic failover.
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 3.0.0
 * @see AbstractCanalClient
 * @see SimpleCanalClient
 */
public class ClusterCanalClient extends AbstractCanalClient<ClusterCanalConnector> {

    private ClusterCanalClient(List<ClusterCanalConnector> connectors) {
        super(connectors);
    }

    /**
     * Returns the destination name from the cluster connector's field.
     *
     * @param connector the cluster Canal connector
     * @return the destination name
     */
    @Override
    protected String getDestination(ClusterCanalConnector connector) {
        Field destinationField =  ReflectionUtils.findField(ClusterCanalConnector.class, "destination");
        ReflectionUtils.makeAccessible(destinationField);
        return (String) ReflectionUtils.getField(destinationField, connector);
    }

    /**
     * Builder for constructing {@link ClusterCanalClient} instances.
     */
    public static final class Builder extends AbstractClientBuilder<ClusterCanalClient, ClusterCanalConnector> {

        /**
         * Builds a {@link ClusterCanalClient} with the configured properties.
         *
         * @param connectors the list of cluster Canal connectors
         * @return the built client
         */
        @Override
        public ClusterCanalClient build(List<ClusterCanalConnector> connectors) {
            ClusterCanalClient canalClient = new ClusterCanalClient(connectors);
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
