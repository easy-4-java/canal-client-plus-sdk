package com.alibaba.otter.canal.client;

import com.alibaba.otter.canal.handler.MessageHandler;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.alibaba.otter.canal.protocol.Message;
import com.alibaba.otter.canal.protocol.exception.CanalClientException;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class AbstractClientBuilderTest {

    // Concrete test builder implementation
    static class TestClient implements CanalClient<CanalConnector> {
        private Integer batchSize;
        private String filter;
        private MessageHandler messageHandler;
        private Long timeout;
        private TimeUnit unit;

        public void setBatchSize(Integer batchSize) { this.batchSize = batchSize; }
        public void setFilter(String filter) { this.filter = filter; }
        public void setMessageHandler(MessageHandler messageHandler) { this.messageHandler = messageHandler; }
        public void setTimeout(Long timeout) { this.timeout = timeout; }
        public void setUnit(TimeUnit unit) { this.unit = unit; }

        @Override public void start() {}
        @Override public void stop() {}
        @Override public void process(CanalConnector connector) {}
        @Override public void destroy() {}
    }

    static class TestCanalConnector implements CanalConnector {
        @Override public void connect() throws CanalClientException {}
        @Override public void disconnect() throws CanalClientException {}
        @Override public boolean checkValid() throws CanalClientException { return false; }
        @Override public void subscribe(String filter) throws CanalClientException {}
        @Override public void subscribe() throws CanalClientException {}
        @Override public void unsubscribe() throws CanalClientException {}
        @Override public Message get(int batchSize) throws CanalClientException { return null; }
        @Override public Message get(int batchSize, Long timeout, TimeUnit unit) throws CanalClientException { return null; }
        @Override public Message getWithoutAck(int batchSize) throws CanalClientException { return null; }
        @Override public Message getWithoutAck(int batchSize, Long timeout, TimeUnit unit) throws CanalClientException { return null; }
        @Override public void ack(long batchId) throws CanalClientException {}
        @Override public void rollback(long batchId) throws CanalClientException {}
        @Override public void rollback() throws CanalClientException {}
    }

    static class TestClientBuilder extends AbstractClientBuilder<TestClient, TestCanalConnector> {
        @Override
        public TestClient build(List<TestCanalConnector> connectors) {
            TestClient client = new TestClient();
            client.setBatchSize(batchSize);
            client.setFilter(filter);
            client.setMessageHandler(messageHandler);
            client.setTimeout(timeout);
            client.setUnit(unit);
            return client;
        }
    }

    @Test
    void shouldBuildWithDefaultValues() {
        TestClientBuilder builder = new TestClientBuilder();
        TestClient client = builder.build(Collections.emptyList());
        assertNotNull(client);
    }

    @Test
    void shouldSetFilter() {
        TestClientBuilder builder = new TestClientBuilder();
        builder.filter("test.*");
        TestClient client = builder.build(Collections.emptyList());
        assertNotNull(client);
    }

    @Test
    void shouldSetBatchSize() {
        TestClientBuilder builder = new TestClientBuilder();
        builder.batchSize(100);
        TestClient client = builder.build(Collections.emptyList());
        assertNotNull(client);
    }

    @Test
    void shouldSetTimeout() {
        TestClientBuilder builder = new TestClientBuilder();
        builder.timeout(5000L);
        TestClient client = builder.build(Collections.emptyList());
        assertNotNull(client);
    }

    @Test
    void shouldSetUnit() {
        TestClientBuilder builder = new TestClientBuilder();
        builder.unit(TimeUnit.MILLISECONDS);
        TestClient client = builder.build(Collections.emptyList());
        assertNotNull(client);
    }

    @Test
    void shouldSetSubscribeTypes() {
        TestClientBuilder builder = new TestClientBuilder();
        builder.setSubscribeTypes(Arrays.asList(CanalEntry.EntryType.ROWDATA));
        TestClient client = builder.build(Collections.emptyList());
        assertNotNull(client);
    }

    @Test
    void shouldSetMessageHandler() {
        TestClientBuilder builder = new TestClientBuilder();
        MessageHandler<Message> handler = (dest, msg) -> {};
        builder.messageHandler(handler);
        TestClient client = builder.build(Collections.emptyList());
        assertNotNull(client);
    }

    @Test
    void shouldChainFluently() {
        TestClientBuilder builder = new TestClientBuilder();
        AbstractClientBuilder result = builder
                .filter("test")
                .batchSize(10)
                .timeout(1000L)
                .unit(TimeUnit.SECONDS);
        assertNotNull(result);
    }
}
