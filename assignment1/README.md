Producer-Consumer Pattern with Custom Blocking Queue
=====================================================

A complete Java 21 implementation of the producer-consumer pattern with a custom blocking queue built from scratch, without using java.util.concurrent.BlockingQueue.

## Project Overview

This project demonstrates thread synchronization, concurrent programming, and blocking data structures using:

- Custom blocking queue with circular array implementation
- Thread synchronization via synchronized methods and wait/notify pattern
- Dynamic queue extension that auto-resizes based on 80 percent utilization
- Comprehensive JUnit 5 test suite with 38+ tests
- Producer and Consumer Runnable implementations

## Project Structure

```
producer-consumer-queue/
|-- pom.xml
|-- src/
|   |-- main/java/
|   |   |-- queue/
|   |   |   |-- Queue.java (abstract base class)
|   |   |   |-- CustomBlockingQueue.java (main implementation)
|   |   |   `-- DynamicBlockingQueue.java (auto-resizing extension)
|   |   `-- producer_consumer/
|   |       |-- Producer.java
|   |       `-- Consumer.java
|   `-- test/java/
|       |-- CustomBlockingQueueTest.java (15 tests)
|       |-- DynamicBlockingQueueTest.java (10 tests)
|       `-- ProducerConsumerSimulationTest.java (13 tests)
|-- README.md
|-- QUICKSTART.md
`-- INTELLIJ_SETUP.md
```

## Core Components

### Queue.java

Abstract base class defining the queue contract. Provides generic Queue<E> interface with abstract methods for enqueue, dequeue, and state checks.

### CustomBlockingQueue.java

Main implementation using circular array with O(1) enqueue and dequeue operations. Features:

- Circular array for efficient memory usage
- Synchronized methods for thread safety
- wait() for blocking producers when full
- wait() for blocking consumers when empty
- notifyAll() to wake waiting threads
- Capacity constraints and bounded queue support

### DynamicBlockingQueue.java

Extension of CustomBlockingQueue that automatically resizes when utilization reaches 80 percent. Features:

- Automatic capacity expansion by 1.5x factor
- Maintains FIFO order after resize
- Handles circular array unwrapping correctly
- Stress-tested with high concurrency scenarios

### Producer.java

Implements Runnable for adding items to the queue. Configurable item count and delay between operations.

### Consumer.java

Implements Runnable for removing items from the queue. Configurable item count and delay between operations.

## Installation and Setup

### Prerequisites

- Java 21 or higher
- Maven 3.6 or higher
- IntelliJ IDEA (recommended) or command line

### Setup Instructions

1. Create the project structure as shown above

2. Copy all Java source files to correct locations:
    - Queue classes to src/main/java/queue/
    - Producer/Consumer to src/main/java/producer_consumer/
    - Test classes to src/test/java/

3. Copy pom.xml to project root

4. Open in IntelliJ:
   File > Open > Select project directory
   IntelliJ auto-detects pom.xml and configures Maven

5. Maven downloads dependencies automatically

## Running Tests

### Option 1: IntelliJ IDE

Right-click test file or class > Run

Or press Ctrl+Shift+F10 (Windows/Linux) or Cmd+Shift+R (Mac)

### Option 2: Maven CLI

```
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=CustomBlockingQueueTest

# Run specific test method
mvn test -Dtest=CustomBlockingQueueTest#testFIFOOrder

# Build and run
mvn clean test
```

### Output Screenshots

![Screenshot 2025-12-03 at 11 35 07 PM](https://github.com/user-attachments/assets/4d966162-611f-471e-be0a-e3e6ae5d670d)
![Screenshot 2025-12-03 at 11 34 58 PM](https://github.com/user-attachments/assets/b0fa4825-9782-422a-8f9d-15ec607574ce)
![Screenshot 2025-12-03 at 11 34 52 PM](https://github.com/user-attachments/assets/fdce416d-0c86-4174-9bed-21fa11fccdf9)
![Screenshot 2025-12-03 at 11 34 45 PM](https://github.com/user-attachments/assets/370d65e7-769f-4b77-8f65-b8899789ac14)
![Screenshot 2025-12-03 at 11 34 37 PM](https://github.com/user-attachments/assets/67e44f6d-29d6-41da-b2be-cdd8e0eb5929)
![Screenshot 2025-12-03 at 11 34 30 PM](https://github.com/user-attachments/assets/54f2010c-664e-4fcb-a8e8-2da5405a3065)
![Screenshot 2025-12-03 at 11 34 23 PM](https://github.com/user-attachments/assets/185d5f9f-42ea-42e1-aec7-8caed30edf27)
![Screenshot 2025-12-03 at 11 34 16 PM](https://github.com/user-attachments/assets/5a8a6917-68a1-4ee5-bdbe-1a92dfe6a130)
![Screenshot 2025-12-03 at 11 34 10 PM](https://github.com/user-attachments/assets/7f77c86b-b0c0-42f9-a933-3b5d37812581)
![Screenshot 2025-12-03 at 11 34 03 PM](https://github.com/user-attachments/assets/b9b0dcef-047e-4fb9-8647-2db255ef590d)
![Screenshot 2025-12-03 at 11 33 54 PM](https://github.com/user-attachments/assets/e4492970-4787-44ba-a4a2-84374a770301)
![Screenshot 2025-12-03 at 11 33 46 PM](https://github.com/user-attachments/assets/3612a5e5-f452-4b26-aad7-e686d58f3e02)
![Screenshot 2025-12-03 at 11 33 38 PM](https://github.com/user-attachments/assets/9fda4474-b9cb-44f6-be04-eafd01d8772a)
![Screenshot 2025-12-03 at 11 33 29 PM](https://github.com/user-attachments/assets/270861c2-54f7-4d3a-b7f2-fa0c379b6237)
![Screenshot 2025-12-03 at 11 33 21 PM](https://github.com/user-attachments/assets/f7cc77af-f3d2-47ba-ad6a-3aed5553413a)
![Screenshot 2025-12-03 at 11 33 13 PM](https://github.com/user-attachments/assets/822b337a-cff0-45f0-ac4a-17f8ab0768eb)
![Screenshot 2025-12-03 at 11 32 41 PM](https://github.com/user-attachments/assets/f5f83abf-0e83-4558-b141-391972c5a68d)
![Screenshot 2025-12-03 at 11 32 26 PM](https://github.com/user-attachments/assets/df1d41e1-2185-4d4c-a8f7-e5827c8d8dd7)
![Screenshot 2025-12-03 at 11 32 19 PM](https://github.com/user-attachments/assets/0c9d9cc9-1823-4ea8-ab9d-7f60f342153e)
![Screenshot 2025-12-03 at 11 32 12 PM](https://github.com/user-attachments/assets/46d0511c-1beb-45ae-b7d6-08dc7afd573f)
![Screenshot 2025-12-03 at 11 31 51 PM](https://github.com/user-attachments/assets/60b0937d-638b-4c76-890b-0fb714d34c5b)
![Screenshot 2025-12-03 at 11 31 40 PM](https://github.com/user-attachments/assets/42e7d32c-c6e0-4d33-a9ce-6daa270b38ff)
![Screenshot 2025-12-03 at 11 31 26 PM](https://github.com/user-attachments/assets/e9483087-84be-45be-bbeb-498bf02e651b)


## Usage Example

In case of custom test cases in main then follow this:

```java
// Create a custom blocking queue with capacity 10
CustomBlockingQueue<String> queue = new CustomBlockingQueue<>(10);

// Create producers and consumers
Producer producer = new Producer(queue, 5, 100, "Producer1");
Consumer consumer = new Consumer(queue, 5, 100, "Consumer1");

// Run in separate threads
Thread p = new Thread(producer);
Thread c = new Thread(consumer);

p.start();
c.start();

// Wait for completion
p.join();
c.join();

// Verify queue is empty
assert queue.isEmpty();
```

## Thread Synchronization Details

The implementation uses synchronized methods combined with wait/notify pattern:

1. When queue is full, producers call wait() and block
2. When queue is empty, consumers call wait() and block
3. After enqueue, notifyAll() wakes all waiting threads
4. After dequeue, notifyAll() wakes all waiting threads
5. Use notifyAll() instead of notify() to prevent thread starvation
6. Condition checks in while loops to handle spurious wakeups

## Performance Characteristics

| Operation | Time Complexity | Space Complexity |
|-----------|-----------------|------------------|
| enqueue() | O(1) amortized  | O(capacity)      |
| dequeue() | O(1) amortized  | O(capacity)      |
| isFull()  | O(1)            |                  |
| isEmpty() | O(1)            |                  |
| Resize    | O(n)            | O(new capacity)  |

## Troubleshooting

### Tests not running in IntelliJ

Ensure Java 21+ is configured in Project Settings > SDK.
Run File > Invalidate Caches > Restart.
Then Build > Rebuild Project.

### Maven dependency issues

Run mvn clean install -U to force update dependencies.

### Deadlock suspicion

All tests have 5-10 second timeouts to detect deadlocks.
If thread does not complete, check if producer/consumer are waiting indefinitely.
Verify front/rear pointer management in circular array logic.

