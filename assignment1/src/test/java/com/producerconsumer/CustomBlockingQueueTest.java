package com.producerconsumer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CustomBlockingQueue Tests")
class CustomBlockingQueueTest {

    private CustomBlockingQueue<String> queue;

    @BeforeEach
    void setUp() {
        queue = new CustomBlockingQueue<>(5);
    }

    @Test
    @DisplayName("Should create queue with correct capacity")
    void testQueueCreation() {
        assertEquals(5, queue.getCapacity());
        assertEquals(0, queue.getSize());
        assertTrue(queue.isEmpty());
    }

    @Test
    @DisplayName("Should enqueue and dequeue single element")
    void testSingleEnqueueDequeue() throws InterruptedException {
        queue.enqueue("Item1");
        assertEquals(1, queue.getSize());
        assertEquals("Item1", queue.dequeue());
        assertEquals(0, queue.getSize());
        assertTrue(queue.isEmpty());
    }

    @Test
    @DisplayName("Should enqueue multiple elements")
    void testMultipleEnqueues() throws InterruptedException {
        for (int i = 1; i <= 5; i++) {
            queue.enqueue("Item" + i);
        }
        assertEquals(5, queue.getSize());
        assertTrue(queue.isFull());
    }

    @Test
    @DisplayName("Should maintain FIFO order")
    void testFIFOOrder() throws InterruptedException {
        queue.enqueue("First");
        queue.enqueue("Second");
        queue.enqueue("Third");

        assertEquals("First", queue.dequeue());
        assertEquals("Second", queue.dequeue());
        assertEquals("Third", queue.dequeue());
    }

    // ===== Capacity & State Checks =====

    @Test
    @DisplayName("Should correctly identify empty queue")
    void testEmptyQueue() throws InterruptedException {
        assertTrue(queue.isEmpty());
        queue.enqueue("Item");
        assertFalse(queue.isEmpty());
        queue.dequeue();
        assertTrue(queue.isEmpty());
    }

    @Test
    @DisplayName("Should correctly identify full queue")
    void testFullQueue() throws InterruptedException {
        assertFalse(queue.isFull());
        for (int i = 0; i < 5; i++) {
            queue.enqueue("Item" + i);
        }
        assertTrue(queue.isFull());
    }

    @Test
    @DisplayName("Should correctly report size")
    void testSizeTracking() throws InterruptedException {
        assertEquals(0, queue.getSize());
        for (int i = 1; i <= 5; i++) {
            queue.enqueue("Item" + i);
            assertEquals(i, queue.getSize());
        }
        for (int i = 4; i >= 0; i--) {
            queue.dequeue();
            assertEquals(i, queue.getSize());
        }
    }

    // ===== Edge Cases =====

    @Test
    @DisplayName("Should reject null elements")
    void testNullElementRejection() {
        assertThrows(IllegalArgumentException.class, () -> queue.enqueue(null));
    }

    @Test
    @DisplayName("Should handle circular array wrapping")
    void testCircularArrayWrapping() throws InterruptedException {
        // Fill queue
        for (int i = 0; i < 5; i++) {
            queue.enqueue("Item" + i);
        }

        // Dequeue 2
        queue.dequeue();
        queue.dequeue();

        // Enqueue 2 more (wrap around)
        queue.enqueue("Item5");
        queue.enqueue("Item6");

        // Verify order
        assertEquals("Item2", queue.dequeue());
        assertEquals("Item3", queue.dequeue());
        assertEquals("Item4", queue.dequeue());
        assertEquals("Item5", queue.dequeue());
        assertEquals("Item6", queue.dequeue());
    }

    @Test
    @DisplayName("Should handle rapid enqueue-dequeue cycles")
    void testRapidCycles() throws InterruptedException {
        for (int cycle = 0; cycle < 10; cycle++) {
            for (int i = 0; i < 5; i++) {
                queue.enqueue("Cycle" + cycle + "-Item" + i);
            }
            for (int i = 0; i < 5; i++) {
                queue.dequeue();
            }
            assertTrue(queue.isEmpty());
        }
    }

    // ===== Thread Synchronization =====

    @Test
    @DisplayName("Should block producer when queue is full")
    void testProducerBlocking() throws InterruptedException {
        // Fill the queue
        for (int i = 0; i < 5; i++) {
            queue.enqueue("Item" + i);
        }

        // Verify full
        assertTrue(queue.isFull());

        // Producer thread that tries to enqueue
        Thread producer = new Thread(() -> {
            try {
                queue.enqueue("BlockedItem");
                System.out.println("Producer unblocked!");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        producer.start();
        Thread.sleep(200); // Give producer time to block

        // Producer should still be alive and blocked
        assertTrue(producer.isAlive());

        // Dequeue to unblock
        queue.dequeue();
        producer.join(1000);

        // Producer should have completed
        assertFalse(producer.isAlive());
        assertEquals(5, queue.getSize());
    }

    @Test
    @DisplayName("Should block consumer when queue is empty")
    void testConsumerBlocking() throws InterruptedException {
        // Verify empty
        assertTrue(queue.isEmpty());

        // Consumer thread that tries to dequeue
        Thread consumer = new Thread(() -> {
            try {
                queue.dequeue();
                System.out.println("Consumer unblocked!");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        consumer.start();
        Thread.sleep(200); // Give consumer time to block

        // Consumer should still be alive and blocked
        assertTrue(consumer.isAlive());

        // Enqueue to unblock
        queue.enqueue("UnblockItem");
        consumer.join(1000);

        // Consumer should have completed
        assertFalse(consumer.isAlive());
        assertTrue(queue.isEmpty());
    }

}

