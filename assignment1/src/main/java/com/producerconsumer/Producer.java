package com.producerconsumer;

// Adds items to queue using producer thread
public class Producer implements Runnable {
    private final Queue<?> queue;
    private final int itemCount;
    private final long delayMs;
    private final String name;

    public Producer(Queue<?> queue, int itemCount, long delayMs, String name) {
        this.queue = queue;
        this.itemCount = itemCount;
        this.delayMs = delayMs;
        this.name = name;
    }

    @Override
    public void run() {
        for (int i = 1; i <= itemCount; i++) {
            try {
                String item = name + "-Item-" + i;
                @SuppressWarnings("unchecked")
                Queue<String> stringQueue = (Queue<String>) queue;
                stringQueue.enqueue(item);

                System.out.println("[" + name + "] Produced: " + item);

                if (delayMs > 0) {
                    Thread.sleep(delayMs);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("[" + name + "] Interrupted during production!");
                break;
            }
        }
        System.out.println("[" + name + "] ✓ Production complete!");
    }
}
