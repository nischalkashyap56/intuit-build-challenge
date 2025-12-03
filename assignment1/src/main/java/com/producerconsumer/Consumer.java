package com.producerconsumer;

// Removes item from queue and uses runnable
public class Consumer implements Runnable {
    private final Queue<?> queue;
    private final int itemCount;
    private final long delayMs;
    private final String name;

    // Creating a consumer here
    public Consumer(Queue<?> queue, int itemCount, long delayMs, String name) {
        this.queue = queue;
        this.itemCount = itemCount;
        this.delayMs = delayMs;
        this.name = name;
    }

    @Override
    public void run() {
        for (int i = 0; i < itemCount; i++) {
            try {
                @SuppressWarnings("unchecked")
                Queue<String> stringQueue = (Queue<String>) queue;
                String item = stringQueue.dequeue();

                System.out.println("[" + name + "] Consumed: " + item);

                if (delayMs > 0) {
                    Thread.sleep(delayMs);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("[" + name + "] Interrupted during consumption!");
                break;
            }
        }
        System.out.println("[" + name + "] ✓ Consumption complete!");
    }
}
