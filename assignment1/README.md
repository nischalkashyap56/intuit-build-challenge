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

### Sample Output



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

