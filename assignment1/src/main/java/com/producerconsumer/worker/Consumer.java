package com.producerconsumer.worker;

import com.producerconsumer.exception.WorkerOperationException;
import com.producerconsumer.model.DataPacket;
import com.producerconsumer.queue.CustomBlockingQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class Consumer implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(Consumer.class);

    private final CustomBlockingQueue<DataPacket> queue;
    private final List<DataPacket> auditLog;

    public Consumer(CustomBlockingQueue<DataPacket> queue, List<DataPacket> auditLog) {
        this.queue = queue;
        this.auditLog = auditLog;
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    DataPacket packet = queue.take();

                    // Simulate processing
                    Thread.sleep(20);

                    auditLog.add(packet);
                } catch (RuntimeException e) {
                    // Capture processing failures
                    logger.error("Critical error processing packet in Consumer", e);
                    throw new WorkerOperationException("Consumer failed to process item", e);
                }
            }
        } catch (InterruptedException e) {
            logger.warn("Consumer interrupted, stopping.");
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            // General safety net to ensure errors are logged to error.log
            logger.error("Unexpected fatal error in Consumer", e);
        }
    }
}
