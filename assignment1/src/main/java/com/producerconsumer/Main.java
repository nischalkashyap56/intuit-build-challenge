package com.producerconsumer;
import com.producerconsumer.model.DataPacket;
import com.producerconsumer.queue.CustomBlockingQueue;
import com.producerconsumer.queue.DynamicCustomBlockingQueue;
import com.producerconsumer.worker.Consumer;
import com.producerconsumer.worker.Producer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


// Since console output was asked
public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws InterruptedException {
        logger.info("--------------");
        logger.info("SCENARIO 1: Standard Blocking Behavior");
        runStandardBlockingScenario();

        Thread.sleep(2000); // Pause for readability

        logger.info("--------------");
        logger.info("SCENARIO 2: Dynamic Resizing Behavior");
        logger.info("--------------");
        runDynamicResizingScenario();
    }

    private static void runStandardBlockingScenario() throws InterruptedException {
        // Capacity of 2 but producer tries to put 5 so should block
        CustomBlockingQueue<DataPacket> queue = new CustomBlockingQueue<>(2);
        List<DataPacket> auditLog = new CopyOnWriteArrayList<>();

        ExecutorService executor = Executors.newFixedThreadPool(2);

        executor.submit(new Producer(queue, 5));

        executor.submit(() -> {
            try {
                Thread.sleep(1000); // Delay consumer
                new Consumer(queue, auditLog).run();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        Thread.sleep(3000);
        executor.shutdownNow();
        executor.awaitTermination(1, TimeUnit.SECONDS);

        System.out.println("Items consumed: " + auditLog.size());
        System.out.println("Queue size: " + queue.size());
    }

    private static void runDynamicResizingScenario() throws InterruptedException {
        // Start with Capacity 2, Threshold 0.5 (50%)
        // When size hits 1 (>= 2*0.5), it should double to 4
        DynamicCustomBlockingQueue<DataPacket> queue = new DynamicCustomBlockingQueue<>(2, 0.5);
        List<DataPacket> auditLog = new CopyOnWriteArrayList<>();

        ExecutorService executor = Executors.newFixedThreadPool(2);

        // Producer pushes 10 items very fast
        executor.submit(new Producer(queue, 10));
        executor.submit(new Consumer(queue, auditLog));

        Thread.sleep(2000);
        executor.shutdownNow();
        executor.awaitTermination(1, TimeUnit.SECONDS);

        System.out.println("Items consumed: " + auditLog.size());
        System.out.println("Final capacity: " + queue.getCapacity());
    }
}