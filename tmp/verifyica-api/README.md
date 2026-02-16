# Verifyica API Concurrent Utilities

This package provides thread-safe managers for common concurrency primitives keyed by strings.

## Classes

### KeyedMutexManager
Manages reentrant locks (mutexes) by string keys, allowing thread-safe synchronization across different parts of an application using the same key.

**Features:**
- Reentrant locking support
- tryLock with optional timeout
- Automatic cleanup when locks are released
- Thread safety verification

**Usage:**
```java
KeyedMutexManager.lock("resource-1");
try {
    // Critical section
} finally {
    KeyedMutexManager.unlock("resource-1");
}
```

### KeyedLatchManager
Manages CountDownLatches by string keys, allowing thread synchronization where one or more threads can wait for a set of operations to complete.

**Features:**
- Create latches with specified counts
- Count down and await operations
- Timeout support for await
- Multiple threads can wait on the same latch

**Usage:**
```java
KeyedLatchManager.createLatch("startup", 3);

// In different threads
KeyedLatchManager.countDown("startup");

// Wait for all countdowns
KeyedLatchManager.await("startup");
```

### KeyedSemaphoreManager
Manages Semaphores by string keys, allowing control over the number of threads that can access a particular resource or pool of resources concurrently.

**Features:**
- Create semaphores with specified permits
- Fair/unfair ordering policies
- Acquire/release single or multiple permits
- tryAcquire with optional timeout
- Drain all permits

**Usage:**
```java
KeyedSemaphoreManager.createSemaphore("db-connections", 10);

KeyedSemaphoreManager.acquire("db-connections");
try {
    // Use resource
} finally {
    KeyedSemaphoreManager.release("db-connections");
}
```

## Key Features

All managers provide:
- **Key trimming**: Keys are automatically trimmed of whitespace
- **Null/blank validation**: Proper validation with clear error messages
- **Thread safety**: All operations are thread-safe
- **Automatic cleanup**: Resources are cleaned up when no longer in use
- **Comprehensive tests**: Full JUnit 5 test coverage using AssertJ

## Requirements

- Java 8 or higher
- JUnit 5 (for tests)
- AssertJ (for tests)

## License

Licensed under the Apache License, Version 2.0
