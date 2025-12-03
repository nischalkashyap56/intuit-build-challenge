package com.producerconsumer.queue;

import com.producerconsumer.exception.QueueConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Custom blokcing queue which doubles capacity when it needs to dynamically adjust
// Most arrays do this as well and it's common practice to do (capacity * 2)
public class DynamicCustomBlockingQueue<T> extends CustomBlockingQueue<T> {
    private static final Logger logger = LoggerFactory.getLogger(DynamicCustomBlockingQueue.class);

    private final double expansionThreshold;

    public DynamicCustomBlockingQueue(int initialCapacity, double expansionThreshold) {
        super(initialCapacity);
        if (expansionThreshold <= 0 || expansionThreshold >= 1) {
            String errorMsg = "Threshold must be between 0.0 and 1.0. Received: " + expansionThreshold;
            logger.error(errorMsg); // Logs to error.log
            throw new QueueConfigurationException(errorMsg);
        }
        this.expansionThreshold = expansionThreshold;
    }

    @Override
    public synchronized void put(T item) throws InterruptedException {
        if (queue.size() >= (capacity * expansionThreshold)) {
            resize();
        }
        super.put(item);
    }

    private void resize() {
        int oldCapacity = this.capacity;
        int newCapacity = oldCapacity * 2;
        this.capacity = newCapacity;

        logger.info(">>> RESIZING QUEUE: Capacity expanded from {} to {} (Load factor hit {})",
                oldCapacity, newCapacity, expansionThreshold);

        notifyAll();
    }
}