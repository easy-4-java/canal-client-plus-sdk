package com.alibaba.otter.canal.handler;

import com.alibaba.otter.canal.protocol.Message;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class MessageHandlerTest {

    @Test
    void shouldCreateFunctionalHandler() {
        AtomicReference<String> capturedDest = new AtomicReference<>();
        MessageHandler<Message> handler = (destination, message) -> capturedDest.set(destination);

        handler.handleMessage("test-dest", null);
        assertEquals("test-dest", capturedDest.get());
    }

    @Test
    void shouldBeFunctionalInterface() {
        assertTrue(MessageHandler.class.isAnnotationPresent(FunctionalInterface.class));
    }
}
