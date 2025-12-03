package com.producerconsumer;


// Automatic resizing dynamic blocking queue based on how much is used (80% is the limit set)
public class DynamicBlockingQueue<E> extends CustomBlockingQueue<E> {
    private static final double GROWTH_THRESHOLD = 0.8; // Resize at 80% utilization
    private static final double GROWTH_FACTOR = 1.5;    // Grow by 50%

    // Creates queue
    public DynamicBlockingQueue(int initialCapacity) {
        super(initialCapacity <= 0 ? DEFAULT_CAPACITY : initialCapacity);
    }

    public DynamicBlockingQueue() {
        super(DEFAULT_CAPACITY);
    }

    // Enqueueing element here with auto resize
    @Override
    public synchronized void enqueue(E element) throws InterruptedException {
        if (element == null) {
            throw new IllegalArgumentException("Element cannot be null");
        }

        // Check if resizing is needed (utilization >= 80%)
        double utilization = (double) size / capacity;
//        System.out.println(utilization);
        if (utilization >= GROWTH_THRESHOLD) {
            int newCapacity = (int) (capacity * GROWTH_FACTOR);
            int oldCapacity = capacity;
            resize(newCapacity);
            System.out.println("[DynamicQueue] Resized from " + oldCapacity + " to " + newCapacity);
        }

        rear = (rear + 1) % capacity;
        elements[rear] = element;
        size++;

        notifyAll();
    }

    // Elements are copied to new array while resizing
    @SuppressWarnings("unchecked")
    private void resize(int newCapacity) {
        Object[] newElements = new Object[newCapacity];

        // Copy elements in order from front to rear (unwrap circular array)
        for (int i = 0; i < size; i++) {
            newElements[i] = elements[(front + i) % capacity];
        }

        elements = newElements;
        front = 0;
        rear = size - 1;
        capacity = newCapacity;
    }
}

