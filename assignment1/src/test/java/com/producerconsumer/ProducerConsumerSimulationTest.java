package com.producerconsumer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Producer-Consumer Simulation Tests")
class ProducerConsumerSimulationTest {

    @Test
    @DisplayName("Scenario 1: Simple 1 Producer → 1 Consumer")
    void scenarioSimple1P1C() throws InterruptedException {
        CustomBlockingQueue<String> queue = new CustomBlockingQueue<>(5);

        Producer producer = new Producer(queue, 10, 0, "P1");
        Consumer consumer = new Consumer(queue, 10, 0, "C1");

        Thread p = new Thread(producer);
        Thread c = new Thread(consumer);

        p.start();
        c.start();

        p.join();
        c.join();

        assertTrue(queue.isEmpty());
        assertEquals(0, queue.getSize());
    }

    @Test
    @DisplayName("Scenario 2: 2 Producers → 1 Consumer (Same Rate)")
    void scenario2P1C_SameRate() throws InterruptedException {
        CustomBlockingQueue<String> queue = new CustomBlockingQueue<>(10);

        Producer p1 = new Producer(queue, 5, 5, "P1");
        Producer p2 = new Producer(queue, 5, 5, "P2");
        Consumer c = new Consumer(queue, 10, 5, "C1");

        Thread t1 = new Thread(p1);
        Thread t2 = new Thread(p2);
        Thread t3 = new Thread(c);

        t1.start();
        t2.start();
        t3.start();

        t1.join();
        t2.join();
        t3.join();

        assertTrue(queue.isEmpty());
    }

    @Test
    @DisplayName("Scenario 3: 3 Producers → 1 Consumer (High Contention)")
    void scenario3P1C_HighContention() throws InterruptedException {
        CustomBlockingQueue<String> queue = new CustomBlockingQueue<>(3);

        Producer p1 = new Producer(queue, 5, 0, "P1");
        Producer p2 = new Producer(queue, 5, 0, "P2");
        Producer p3 = new Producer(queue, 5, 0, "P3");
        Consumer c = new Consumer(queue, 15, 1, "C1");

        Thread[] threads = {
                new Thread(p1),
                new Thread(p2),
                new Thread(p3),
                new Thread(c)
        };

        for (Thread t : threads) t.start();
        for (Thread t : threads) t.join(5000);

        assertTrue(queue.isEmpty());
    }

    // ===== Multiple Consumers =====

    @Test
    @DisplayName("Scenario 4: 1 Producer → 2 Consumers (Same Rate)")
    void scenario1P2C_SameRate() throws InterruptedException {
        CustomBlockingQueue<String> queue = new CustomBlockingQueue<>(5);

        Producer p = new Producer(queue, 10, 5, "P1");
        Consumer c1 = new Consumer(queue, 5, 5, "C1");
        Consumer c2 = new Consumer(queue, 5, 5, "C2");

        Thread t1 = new Thread(p);
        Thread t2 = new Thread(c1);
        Thread t3 = new Thread(c2);

        t1.start();
        t2.start();
        t3.start();

        t1.join();
        t2.join();
        t3.join();

        assertTrue(queue.isEmpty());
    }

    @Test
    @DisplayName("Scenario 5: 1 Producer → 3 Consumers (High Contention)")
    void scenario1P3C_HighContention() throws InterruptedException {
        CustomBlockingQueue<String> queue = new CustomBlockingQueue<>(2);

        Producer p = new Producer(queue, 15, 0, "P1");
        Consumer c1 = new Consumer(queue, 5, 0, "C1");
        Consumer c2 = new Consumer(queue, 5, 0, "C2");
        Consumer c3 = new Consumer(queue, 5, 0, "C3");

        Thread[] threads = {
                new Thread(p),
                new Thread(c1),
                new Thread(c2),
                new Thread(c3)
        };

        for (Thread t : threads) t.start();
        for (Thread t : threads) t.join(5000);

        assertTrue(queue.isEmpty());
    }


    @Test
    @DisplayName("Scenario 6: Fast Producer → Slow Consumer (Producer blocks)")
    void scenarioFastProducerSlowConsumer() throws InterruptedException {
        CustomBlockingQueue<String> queue = new CustomBlockingQueue<>(3);

        // Producer fast, consumer slow (should cause queue to fill and block producer)
        Producer p = new Producer(queue, 10, 0, "FastProducer");
        Consumer c = new Consumer(queue, 10, 50, "SlowConsumer");

        long startTime = System.currentTimeMillis();

        Thread t1 = new Thread(p);
        Thread t2 = new Thread(c);

        t1.start();
        t2.start();

        t1.join(10000);
        t2.join(10000);

        long duration = System.currentTimeMillis() - startTime;
        assertTrue(queue.isEmpty());
        // Should take time due to consumer being slow
        assertTrue(duration > 300);
    }

    @Test
    @DisplayName("Scenario 7: Slow Producer → Fast Consumer (Consumer blocks)")
    void scenarioSlowProducerFastConsumer() throws InterruptedException {
        CustomBlockingQueue<String> queue = new CustomBlockingQueue<>(3);

        // Producer slow, consumer fast (should cause consumer to block waiting)
        Producer p = new Producer(queue, 10, 50, "SlowProducer");
        Consumer c = new Consumer(queue, 10, 0, "FastConsumer");

        long startTime = System.currentTimeMillis();

        Thread t1 = new Thread(p);
        Thread t2 = new Thread(c);

        t1.start();
        t2.start();

        t1.join(10000);
        t2.join(10000);

        long duration = System.currentTimeMillis() - startTime;
        assertTrue(queue.isEmpty());
        // Should take time due to producer being slow
        assertTrue(duration > 300);
    }

    @Test
    @DisplayName("Scenario 8: 2 Producers → 2 Consumers (Balanced)")
    void scenarioBalanced2P2C() throws InterruptedException {
        CustomBlockingQueue<String> queue = new CustomBlockingQueue<>(5);

        Producer p1 = new Producer(queue, 10, 5, "P1");
        Producer p2 = new Producer(queue, 10, 5, "P2");
        Consumer c1 = new Consumer(queue, 10, 5, "C1");
        Consumer c2 = new Consumer(queue, 10, 5, "C2");

        Thread[] threads = {
                new Thread(p1),
                new Thread(p2),
                new Thread(c1),
                new Thread(c2)
        };

        for (Thread t : threads) t.start();
        for (Thread t : threads) t.join(10000);

        assertTrue(queue.isEmpty());
    }

    @Test
    @DisplayName("Scenario 9: High Concurrency (16 Producers → 16 Consumers)")
    void scenarioHighConcurrency4P4C() throws InterruptedException {
        CustomBlockingQueue<String> queue = new CustomBlockingQueue<>(5);

        Thread[] threads = new Thread[32];

        // 4 producers
        for (int i = 0; i < 16; i++) {
            Producer p = new Producer(queue, 5, 0, "P" + (i + 1));
            threads[i] = new Thread(p);
        }

        // 4 consumers
        for (int i = 0; i < 16; i++) {
            Consumer c = new Consumer(queue, 5, 0, "C" + (i + 1));
            threads[16 + i] = new Thread(c);
        }

        for (Thread t : threads) t.start();
        for (Thread t : threads) t.join(10000);

        assertTrue(queue.isEmpty());
    }


    @Test
    @DisplayName("Scenario 10: Dynamic Queue with 2 Fast Producers → 1 Slow Consumer")
    void scenarioDynamicQueueAutoResize() throws InterruptedException {
        DynamicBlockingQueue<String> queue = new DynamicBlockingQueue<>(3);

        Producer p1 = new Producer(queue, 15, 0, "FastP1");
        Producer p2 = new Producer(queue, 15, 0, "FastP2");
        Consumer c = new Consumer(queue, 30, 10, "SlowC");

        Thread t1 = new Thread(p1);
        Thread t2 = new Thread(p2);
        Thread t3 = new Thread(c);

        t1.start();
        t2.start();
        t3.start();

        t1.join(10000);
        t2.join(10000);
        t3.join(10000);

        assertTrue(queue.isEmpty());
        // Queue should have auto-resized
        assertTrue(queue.getCapacity() > 3);
    }

    @Test
    @DisplayName("Scenario 11: Dynamic Queue Stress Test (Heavy Load)")
    void scenarioDynamicQueueStress() throws InterruptedException {
        DynamicBlockingQueue<String> queue = new DynamicBlockingQueue<>(2);

        // Multiple producers, multiple consumers, all fast
        Thread[] threads = new Thread[8];

        for (int i = 0; i < 4; i++) {
            Producer p = new Producer(queue, 20, 0, "P" + (i + 1));
            threads[i] = new Thread(p);
        }

        for (int i = 0; i < 4; i++) {
            Consumer c = new Consumer(queue, 20, 0, "C" + (i + 1));
            threads[4 + i] = new Thread(c);
        }

        for (Thread t : threads) t.start();
        for (Thread t : threads) t.join(10000);

        assertTrue(queue.isEmpty());
    }


    @Test
    @DisplayName("Scenario 12: Producer-Consumer with Minimal Capacity (Risk of Deadlock)")
    void scenarioMinimalCapacityNoDeadlock() throws InterruptedException {
        // Queue size 1 with 2 producers and 2 consumers
        CustomBlockingQueue<String> queue = new CustomBlockingQueue<>(1);

        Producer p1 = new Producer(queue, 5, 0, "P1");
        Producer p2 = new Producer(queue, 5, 0, "P2");
        Consumer c1 = new Consumer(queue, 5, 0, "C1");
        Consumer c2 = new Consumer(queue, 5, 0, "C2");

        Thread[] threads = {
                new Thread(p1),
                new Thread(p2),
                new Thread(c1),
                new Thread(c2)
        };

        for (Thread t : threads) t.start();

        // Set timeout to detect potential deadlock
        for (Thread t : threads) {
            t.join(5000);
            assertFalse(t.isAlive(), "Thread deadlock detected!");
        }

        assertTrue(queue.isEmpty());
    }

    @Test
    @DisplayName("Scenario 13: Unequal Producer-Consumer Counts (3P → 2C)")
    void scenarioUnequalCounts() throws InterruptedException {
        CustomBlockingQueue<String> queue = new CustomBlockingQueue<>(4);

        Producer p1 = new Producer(queue, 5, 5, "P1");
        Producer p2 = new Producer(queue, 5, 5, "P2");
        Producer p3 = new Producer(queue, 5, 5, "P3");
        Consumer c1 = new Consumer(queue, 8, 5, "C1");
        Consumer c2 = new Consumer(queue, 7, 5, "C2");

        Thread[] threads = {
                new Thread(p1),
                new Thread(p2),
                new Thread(p3),
                new Thread(c1),
                new Thread(c2)
        };

        for (Thread t : threads) t.start();
        for (Thread t : threads) t.join(10000);

        assertTrue(queue.isEmpty());
    }
}
