---
title: "Concurrency Utilities"
linkTitle: "Concurrency"
weight: 6
description: >
  Keyed concurrency utilities for parallel testing
---

Keyed concurrency utilities for fine-grained synchronization.

## KeyedMutexManager

Provides mutual exclusion per key using static methods.

```java
KeyedMutexManager.lock("key");
try {
    // Critical section for this key
} finally {
    KeyedMutexManager.unlock("key");
}
```

## KeyedLatchManager

Count-down latches per key using static methods.

```java
KeyedLatchManager.createLatch("key", 3); // Count of 3
KeyedLatchManager.countDown("key");
KeyedLatchManager.await("key"); // Waits until count reaches 0
```

## KeyedSemaphoreManager

Semaphores per key for resource limiting using static methods.

```java
KeyedSemaphoreManager.createSemaphore("resource", 5); // Max 5 permits
KeyedSemaphoreManager.acquire("resource");
try {
    // Use resource
} finally {
    KeyedSemaphoreManager.release("resource");
}
```

## See Also

- [Advanced → Keyed Concurrency](../../advanced/keyed-concurrency/) - Detailed usage guide
- [Advanced → Parallelism](../../advanced/parallelism/) - Parallel execution patterns
