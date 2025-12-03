package com.producerconsumer.queue;

import com.producerconsumer.exception.QueueConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.Queue;

// Customer bounded queue implementation
public class CustomBlockingQueue<T> {
    private static final Logger logger = LoggerFactory.getLogger(CustomBlockingQueue.class);

    protected final Queue<T> queue;
    protected int capacity;

    public CustomBlockingQueue(int capacity) {
        if (capacity <= 0) {
            String errorMsg = "Capacity must be positive. Received: " + capacity;
            logger.error(errorMsg); // Logs to error.log
            throw new QueueConfigurationException(errorMsg);
        }
        this.queue = new LinkedList<>();
        this.capacity = capacity;
    }

    // Insertion but also waits if space is not there
    public synchronized void put(T item) throws InterruptedException {
        while (queue.size() == capacity) {
            logger.debug("{} waiting. Queue full (size: {})", Thread.currentThread().getName(), queue.size());
            wait();
        }

        queue.add(item);
        logger.debug("{} produced: {}", Thread.currentThread().getName(), item);

        notifyAll();
    }

    // Removes front of queue or else waits if it isnt there
    public synchronized T take() throws InterruptedException {
        while (queue.isEmpty()) {
            logger.debug("{} waiting. Queue empty.", Thread.currentThread().getName());
            wait();
        }

        T item = queue.remove();
        logger.debug("{} consumed: {}", Thread.currentThread().getName(), item);

        notifyAll();
        return item;
    }

    public synchronized int size() {
        return queue.size();
    }

    public synchronized int getCapacity() {
        return capacity;
    }
}