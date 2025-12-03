package com.producerconsumer.worker;

import com.producerconsumer.exception.WorkerOperationException;
import com.producerconsumer.model.DataPacket;
import com.producerconsumer.queue.CustomBlockingQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Random;

public class Producer implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(Producer.class);

    private final CustomBlockingQueue<DataPacket> queue;
    private final int itemsToProduce;
    private final Random random = new Random();

    public Producer(CustomBlockingQueue<DataPacket> queue, int itemsToProduce) {
        this.queue = queue;
        this.itemsToProduce = itemsToProduce;
    }

    @Override
    public void run() {
        try {
            for (int i = 0; i < itemsToProduce; i++) {
                if (Thread.currentThread().isInterrupted()) break;

                // Simulate a potential runtime error (e.g., data generation failure) to test error logging
                try {
                    DataPacket packet = new DataPacket(i, LocalDateTime.now(), "Payload-" + i);
                    queue.put(packet);
                    Thread.sleep(random.nextInt(10, 50));
                } catch (RuntimeException e) {
                    // Capture unexpected runtime errors during production
                    logger.error("Critical error in Producer thread", e);
                    throw new WorkerOperationException("Producer failed to generate/queue item", e);
                }
            }
        } catch (InterruptedException e) {
            logger.warn("Producer interrupted, stopping.");
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            // General safety net to ensure errors are logged to error.log before thread dies
            logger.error("Unexpected fatal error in Producer", e);
        }
    }
}
