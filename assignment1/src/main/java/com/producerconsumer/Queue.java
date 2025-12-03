package com.producerconsumer;

public abstract class Queue<E> {
    protected static final int DEFAULT_CAPACITY = 10;
    protected volatile int capacity;
    protected volatile int size;

    public Queue(int capacity) {
        this.capacity = capacity;
        this.size = 0;
    }

    // Blocking only if queue is full otherwise adds element
    public abstract void enqueue(E element) throws InterruptedException;

    // Blocking if empty otherwise removes element
    public abstract E dequeue() throws InterruptedException;


    public abstract boolean isFull();
    public abstract boolean isEmpty();

    // Current number of elements in queue
    public abstract int getSize();
    public abstract int getCapacity();
}
