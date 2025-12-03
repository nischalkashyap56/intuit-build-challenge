package com.producerconsumer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DynamicBlockingQueue Tests")
class DynamicBlockingQueueTest {

    private DynamicBlockingQueue<String> queue;

    @BeforeEach
    void setUp() {
        queue = new DynamicBlockingQueue<>(5);
    }

    @Test
    @DisplayName("Should create dynamic queue with initial capacity")
    void testDynamicQueueCreation() {
        assertEquals(5, queue.getCapacity());
        assertEquals(0, queue.getSize());
        assertTrue(queue.isEmpty());
    }

    @Test
    @DisplayName("Should enqueue and dequeue elements")
    void testBasicEnqueueDequeue() throws InterruptedException {
        queue.enqueue("Item1");
        queue.enqueue("Item2");

        assertEquals("Item1", queue.dequeue());
        assertEquals("Item2", queue.dequeue());
        assertTrue(queue.isEmpty());
    }

    // ===== Resizing Tests =====

    @Test
    @DisplayName("Should resize when reaching 80% utilization")
    void testAutomaticResize() throws InterruptedException {
        // Initial capacity is 5
        // Threshold: 0.8 * 5 = 4 elements (80%)

        assertEquals(5, queue.getCapacity());

        // Enqueue 4 elements (80% utilization)
        queue.enqueue("Item1");
        queue.enqueue("Item2");
        queue.enqueue("Item3");
        queue.enqueue("Item4");

        assertEquals(5, queue.getCapacity());
        // At this point, utilization = 4/5 = 0.8, so resize should trigger
        // so next enqueue should change capacity
        queue.enqueue("Item5");


        int capacityAfterResize = queue.getCapacity();
        assertEquals(7, capacityAfterResize); // 5 * 1.5 = 7.5 -> 7
    }

    @Test
    @DisplayName("Should maintain element order after resize")
    void testElementOrderAfterResize() throws InterruptedException {
        // Add elements
        queue.enqueue("First");
        queue.enqueue("Second");
        queue.enqueue("Third");
        queue.enqueue("Fourth");
        queue.enqueue("Fifth");

        // This should trigger resize (5 elements in capacity 5)
        queue.enqueue("Sixth");

        // Verify elements are in correct order
        assertEquals("First", queue.dequeue());
        assertEquals("Second", queue.dequeue());
        assertEquals("Third", queue.dequeue());
        assertEquals("Fourth", queue.dequeue());
        assertEquals("Fifth", queue.dequeue());
        assertEquals("Sixth", queue.dequeue());
    }

    @Test
    @DisplayName("Should handle multiple resizes")
    void testMultipleResizes() throws InterruptedException {
        // Start with capacity 5
        assertEquals(5, queue.getCapacity());

        // Add enough elements to trigger multiple resizes
        for (int i = 1; i <= 20; i++) {
            queue.enqueue("Item" + i);
        }

        // Capacity should have grown
        assertTrue(queue.getCapacity() >= 20);

        // Verify all elements are still there
        assertEquals(20, queue.getSize());

        // Verify FIFO order is maintained
        for (int i = 1; i <= 20; i++) {
            assertEquals("Item" + i, queue.dequeue());
        }

        assertTrue(queue.isEmpty());
    }

    @Test
    @DisplayName("Should handle circular array wrapping before resize")
    void testCircularWrappingBeforeResize() throws InterruptedException {
        // Fill and partially empty queue to test circular wrapping
        for (int i = 0; i < 5; i++) {
            queue.enqueue("Item" + i);
        }

        // Dequeue 2
        queue.dequeue();
        queue.dequeue();

        // Enqueue 3 more (should trigger resize due to 60% utilization + new items)
        queue.enqueue("Item5");
        queue.enqueue("Item6");
        queue.enqueue("Item7");

        // Verify order
        assertEquals("Item2", queue.dequeue());
        assertEquals("Item3", queue.dequeue());
        assertEquals("Item4", queue.dequeue());
        assertEquals("Item5", queue.dequeue());
        assertEquals("Item6", queue.dequeue());
        assertEquals("Item7", queue.dequeue());
    }

    @Test
    @DisplayName("Should not resize below threshold")
    void testNoResizeBelowThreshold() throws InterruptedException {
        int initialCapacity = queue.getCapacity();

        // Add 3 elements (60% utilization, below 80% threshold)
        queue.enqueue("Item1");
        queue.enqueue("Item2");
        queue.enqueue("Item3");

        // Capacity should not change
        assertEquals(initialCapacity, queue.getCapacity());
    }
}
