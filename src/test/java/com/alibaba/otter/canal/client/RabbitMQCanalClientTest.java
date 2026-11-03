package com.alibaba.otter.canal.client;

import com.alibaba.otter.canal.client.rabbitmq.RabbitMQCanalConnector;
import com.alibaba.otter.canal.handler.MessageHandler;
import com.alibaba.otter.canal.protocol.FlatMessage;
import com.alibaba.otter.canal.protocol.exception.CanalClientException;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RabbitMQCanalClientTest {
    private RabbitMQCanalClient client(RabbitMQCanalConnector connector) {
        return new RabbitMQCanalClient.Builder().build(Collections.singletonList(connector));
    }

    @Test
    void builderRetainsConfigurationAndDestinationWithoutConnecting() {
        RabbitMQCanalConnector connector = new RabbitMQCanalConnector(
                "broker.invalid", "/events", "queue", "", "", "user", "secret", null, true);
        RabbitMQCanalClient.Builder builder = new RabbitMQCanalClient.Builder();
        MessageHandler<FlatMessage> handler = (destination, message) -> {};
        builder.batchSize(42).filter("db.*").timeout(25L).unit(TimeUnit.MILLISECONDS)
                .messageHandler(handler);
        RabbitMQCanalClient client = builder.build(Collections.singletonList(connector));
        assertEquals("broker.invalid", client.getDestination(connector));
        assertEquals(42, client.batchSize);
        assertEquals("db.*", client.filter);
        assertEquals(25L, client.timeout);
        assertEquals(TimeUnit.MILLISECONDS, client.unit);
        assertSame(handler, client.getMessageHandler());
    }

    @Test
    void connectionFailureStopsDeterministicallyAndCleansUp() {
        RabbitMQCanalConnector connector = mock(RabbitMQCanalConnector.class);
        RabbitMQCanalClient client = client(connector);
        client.running = true;
        doAnswer(invocation -> {
            client.running = false;
            throw new CanalClientException("simulated connection failure");
        }).when(connector).connect();
        assertDoesNotThrow(() -> client.process(connector));
        verify(connector).unsubscribe();
        verify(connector).disconnect();
        verify(connector, never()).subscribe();
    }

    @Test
    void failedHandlerDoesNotAcknowledgeMessage() {
        RabbitMQCanalConnector connector = mock(RabbitMQCanalConnector.class);
        RabbitMQCanalClient client = client(connector);
        FlatMessage message = new FlatMessage(1L);
        when(connector.getFlatListWithoutAck(client.timeout, client.unit))
                .thenReturn(Collections.singletonList(message));
        client.setMessageHandler((destination, value) -> {
            client.running = false;
            throw new IllegalStateException("simulated handler failure");
        });
        client.running = true;
        assertDoesNotThrow(() -> client.process(connector));
        verify(connector, never()).ack();
        verify(connector).disconnect();
    }
}
