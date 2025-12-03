package com.producerconsumer;

// Circular array implementation of blocking queue
public class CustomBlockingQueue<E> extends Queue<E> {
    protected Object[] elements;
    protected int front;
    protected int rear;

    // aBounded queue creation
    @SuppressWarnings("unchecked")
    public CustomBlockingQueue(int capacity) {
        super(capacity <= 0 ? DEFAULT_CAPACITY : capacity);
        this.elements = new Object[this.capacity];
        this.front = 0;
        this.rear = -1;
    }

    @SuppressWarnings("unchecked")
    public CustomBlockingQueue() {
        this(DEFAULT_CAPACITY);
    }

    // Enqueue and element and blocks if at capacity
    @Override
    public synchronized void enqueue(E element) throws InterruptedException {
        if (element == null) {
            throw new IllegalArgumentException("Element cannot be null");
        }

        // Wait if queue is full (only for bounded queues)
        while (isFull() && capacity > 0) {
            System.out.println("Queue is full hence waiting to produce");
            wait();
        }

        rear = (rear + 1) % capacity;
        elements[rear] = element;
        size++;

        // Notify all waiting threads (consumers waiting to dequeue)
        notifyAll();
    }

    // Deuque element and blocks if queue is empty
    @Override
    public synchronized E dequeue() throws InterruptedException {
        // Wait if queue is empty
        while (isEmpty()) {
            System.out.println("Queue is empty hence waiting to consume");
            wait();
        }

        E element = (E) elements[front];
        elements[front] = null; // Help garbage collection
        front = (front + 1) % capacity;
        size--;

        // Notify all waiting threads (producers waiting to enqueue)
        notifyAll();
        return element;
    }

    // Checks max capacity and returns boolean if it is full or not
    @Override
    public synchronized boolean isFull() {
        return capacity > 0 && size == capacity;
    }


    @Override
    public synchronized boolean isEmpty() {
        return size == 0;
    }

    // Size of queue
    @Override
    public synchronized int getSize() {
        return size;
    }

    @Override
    public synchronized int getCapacity() {
        return capacity;
    }
}
