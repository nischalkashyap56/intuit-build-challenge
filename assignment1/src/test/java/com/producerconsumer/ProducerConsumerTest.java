package com.producerconsumer;

import com.producerconsumer.exception.QueueConfigurationException;
import com.producerconsumer.model.DataPacket;
import com.producerconsumer.queue.CustomBlockingQueue;
import com.producerconsumer.queue.DynamicCustomBlockingQueue;
import com.producerconsumer.worker.Consumer;
import com.producerconsumer.worker.Producer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.*;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

@Timeout(value = 10, unit = TimeUnit.SECONDS) // Global timeout to prevent hangs
class ProducerConsumerTest {

    // Wait and notify mechanism
    @Test
    @DisplayName("Producer blocks when queue is full (wait mechanism)")
    void testProducerBlockingOnFull() throws InterruptedException {
        CustomBlockingQueue<DataPacket> queue = new CustomBlockingQueue<>(1);
        queue.put(new DataPacket(1, LocalDateTime.now(), "Filler"));

        // Try to put another packet of data
        Thread producerThread = new Thread(() -> {
            try {
                queue.put(new DataPacket(2, LocalDateTime.now(), "Blocked"));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        producerThread.start();

        // Should wait since lock held
        await().atMost(2, TimeUnit.SECONDS)
                .until(() -> producerThread.getState() == Thread.State.WAITING);

        queue.take();
    }

    @Test
    @DisplayName("Consumer blocks here when queue is empty (Wait mechanism enforced)")
    void testConsumerBlockingOnEmpty() {
        CustomBlockingQueue<DataPacket> queue = new CustomBlockingQueue<>(5);

        Thread consumerThread = new Thread(() -> {
            try {
                queue.take();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        consumerThread.start();

        await().atMost(2, TimeUnit.SECONDS)
                .until(() -> consumerThread.getState() == Thread.State.WAITING);

        consumerThread.interrupt();
    }

    // Dynamic resizing logic here
    @Test
    @DisplayName("Dynamic queue resizes exactly at threshold and not before or after")
    void testDynamicResizingTriggersCorrectly() throws InterruptedException {
        DynamicCustomBlockingQueue<DataPacket> queue = new DynamicCustomBlockingQueue<>(4, 0.75);
        DataPacket packet = new DataPacket(0, LocalDateTime.now(), "test");

        assertEquals(4, queue.getCapacity());

        // Half full so no resize here
        queue.put(packet);
        queue.put(packet);
        assertEquals(4, queue.getCapacity(), "Capacity should remain 4 at 50% load");

        // Add 3rd item (75%) - RESIZE TRIGGER
        // Resize should happen here
        queue.put(packet);

        queue.put(packet);

        assertEquals(8, queue.getCapacity(), "Capacity should have doubled to 8");
    }


    @Test
    @DisplayName("10 Producers vs 10 Consumers test (many workers")
    void testHighConcurrencyDataIntegrity() throws InterruptedException {
        CustomBlockingQueue<DataPacket> queue = new CustomBlockingQueue<>(50);
        List<DataPacket> auditLog = new CopyOnWriteArrayList<>();

        int numProducers = 10;
        int numConsumers = 10;
        int itemsPerProducer = 100;
        int totalItemsExpected = numProducers * itemsPerProducer;

        ExecutorService executor = Executors.newCachedThreadPool();

        for (int i = 0; i < numConsumers; i++) {
            executor.submit(new Consumer(queue, auditLog));
        }

        for (int i = 0; i < numProducers; i++) {
            executor.submit(new Producer(queue, itemsPerProducer));
        }

        await().atMost(10, TimeUnit.SECONDS)
                .until(() -> auditLog.size() == totalItemsExpected);

        executor.shutdownNow();

        assertEquals(totalItemsExpected, auditLog.size(), "Total consumed items matches total produced");
    }



    @Test
    @DisplayName("Custom exception handling of blocking queue")
    void testInvalidCapacityException() {
        assertThrows(QueueConfigurationException.class, () -> {
            new CustomBlockingQueue<>(-1);
        });
    }

    @Test
    @DisplayName("Custom exception handling of dynamic blcking queue")
    void testInvalidThresholdException() {
        assertThrows(QueueConfigurationException.class, () -> {
            new DynamicCustomBlockingQueue<>(10, 1.5); // Threshold > 1.0
        });
    }
}
