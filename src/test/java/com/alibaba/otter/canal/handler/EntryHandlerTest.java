package com.alibaba.otter.canal.handler;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EntryHandlerTest {

    @Test
    void shouldHaveDefaultInsertMethod() {
        EntryHandler<String> handler = new EntryHandler<String>() {};
        assertDoesNotThrow(() -> handler.insert("test"));
    }

    @Test
    void shouldHaveDefaultUpdateMethod() {
        EntryHandler<String> handler = new EntryHandler<String>() {};
        assertDoesNotThrow(() -> handler.update("before", "after"));
    }

    @Test
    void shouldHaveDefaultDeleteMethod() {
        EntryHandler<String> handler = new EntryHandler<String>() {};
        assertDoesNotThrow(() -> handler.delete("test"));
    }

    @Test
    void shouldAllowSelectiveOverride() {
        EntryHandler<String> handler = new EntryHandler<String>() {
            boolean inserted = false;
            @Override
            public void insert(String t) {
                inserted = true;
            }
        };
        handler.insert("test");
        // No assertion needed - just verifying no exception on default methods
        assertDoesNotThrow(() -> handler.update("a", "b"));
        assertDoesNotThrow(() -> handler.delete("c"));
    }
}
