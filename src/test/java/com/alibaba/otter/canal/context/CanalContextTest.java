package com.alibaba.otter.canal.context;

import com.alibaba.otter.canal.model.CanalModel;
import com.alibaba.otter.canal.protocol.CanalEntry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class CanalContextTest {

    @AfterEach
    void cleanup() {
        CanalContext.removeModel();
    }

    @Test
    void shouldReturnNullWhenNoModelSet() {
        assertNull(CanalContext.getModel());
    }

    @Test
    void shouldSetAndRetrieveModel() {
        CanalModel model = CanalModel.builder()
                .id(1L)
                .schema("test_db")
                .table("users")
                .eventType(CanalEntry.EventType.INSERT)
                .build();

        CanalContext.setModel(model);

        CanalModel retrieved = CanalContext.getModel();
        assertNotNull(retrieved);
        assertEquals(1L, retrieved.getId());
        assertEquals("test_db", retrieved.getSchema());
        assertEquals("users", retrieved.getTable());
    }

    @Test
    void shouldRemoveModel() {
        CanalModel model = CanalModel.builder().id(1L).build();
        CanalContext.setModel(model);
        assertNotNull(CanalContext.getModel());

        CanalContext.removeModel();
        assertNull(CanalContext.getModel());
    }

    @Test
    void shouldOverwriteExistingModel() {
        CanalModel first = CanalModel.builder().id(1L).build();
        CanalModel second = CanalModel.builder().id(2L).build();

        CanalContext.setModel(first);
        CanalContext.setModel(second);

        assertEquals(2L, CanalContext.getModel().getId());
    }

    @Test
    void shouldIsolateBetweenCleanThreads() throws InterruptedException {
        // Note: TransmittableThreadLocal propagates context to child threads by design.
        // To test true isolation, we use a thread that was started before setting context.
        CanalModel mainModel = CanalModel.builder().id(1L).build();
        AtomicReference<CanalModel> otherThreadModel = new AtomicReference<>();
        CountDownLatch beforeContext = new CountDownLatch(1);
        CountDownLatch afterCheck = new CountDownLatch(1);

        Thread otherThread = new Thread(() -> {
            // Read model BEFORE main thread sets context
            beforeContext.countDown();
            try {
                afterCheck.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            otherThreadModel.set(CanalContext.getModel());
        });
        otherThread.start();
        beforeContext.await();

        // Set context on main thread after the other thread has been created
        CanalContext.setModel(mainModel);

        // Let the other thread check
        afterCheck.countDown();
        otherThread.join();

        // Main thread has the model
        assertNotNull(CanalContext.getModel());
        // Other thread should not have it (it was started before context was set)
        assertNull(otherThreadModel.get());
    }
}
