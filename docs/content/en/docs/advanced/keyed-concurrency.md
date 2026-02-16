---
title: "Keyed Concurrency Utilities"
linkTitle: "Keyed Concurrency"
weight: 6
description: >
  Advanced concurrency control with keyed utilities
---

Verifyica provides keyed concurrency utilities for fine-grained synchronization in parallel tests.

## Overview

Keyed concurrency utilities allow you to synchronize on specific keys rather than globally:
- **KeyedMutexManager** - Mutual exclusion per key
- **KeyedLatchManager** - Count-down latches per key
- **KeyedSemaphoreManager** - Semaphores per key

## KeyedMutexManager

Provides mutual exclusion on a per-key basis.

### Basic Usage

```java
import org.verifyica.api.concurrent.KeyedMutexManager;

public class MutexTest {

    @Verifyica.ArgumentSupplier(parallelism = 10)
    public static Object arguments() {
        return List.of("user-1", "user-2", "user-3");
    }

    @Verifyica.Test
    public void testConcurrentAccess(String userId) {
        // Only one test can access this user at a time
        KeyedMutexManager.lock(userId);
        try {
            updateUser(userId);
        } finally {
            KeyedMutexManager.unlock(userId);
        }
    }
}
```

### Example: Database Row Locking

```java
public class RowLockTest {

    @Verifyica.Test
    public void testRowUpdate(String rowId) {
        KeyedMutexManager.lock(rowId);
        try {
            // Exclusive access to this row
            Row row = database.getRow(rowId);
            row.setValue(row.getValue() + 1);
            database.updateRow(row);
        } finally {
            KeyedMutexManager.unlock(rowId);
        }
    }
}
```

## KeyedLatchManager

Count-down latches per key for coordination.

### Basic Usage

```java
import org.verifyica.api.concurrent.KeyedLatchManager;

public class LatchTest {

    @Verifyica.BeforeAll
    public void beforeAll(String group) {
        // Initialize latch for this group (3 tests must complete)
        KeyedLatchManager.createLatch(group, 3);
    }

    @Verifyica.Test
    public void test1(String group) throws InterruptedException {
        performTest1();
        KeyedLatchManager.countDown(group);

        // Wait for all 3 tests to complete
        KeyedLatchManager.await(group);

        // Now all tests have completed
        verifyResults(group);
    }
}
```

### Example: Barrier Synchronization

```java
public class BarrierTest {

    @Verifyica.ArgumentSupplier(parallelism = 5)
    public static Object arguments() {
        return List.of("batch-1", "batch-2");
    }

    @Verifyica.BeforeAll
    public void beforeAll(String batch) {
        // Each batch has 5 tests that must sync
        KeyedLatchManager.createLatch(batch, 5);
    }

    @Verifyica.Test
    public void coordinatedTest(String batch) throws InterruptedException {
        // Phase 1: Prepare
        prepare();

        // Signal ready and wait for others
        KeyedLatchManager.countDown(batch);
        KeyedLatchManager.await(batch);

        // Phase 2: All execute together
        executeTest();
    }
}
```

## KeyedSemaphoreManager

Semaphores per key for resource limiting.

### Basic Usage

```java
import org.verifyica.api.concurrent.KeyedSemaphoreManager;

public class SemaphoreTest {

    @Verifyica.BeforeAll
    public void beforeAll(String resource) {
        // Limit to 3 concurrent accesses per resource
        KeyedSemaphoreManager.createSemaphore(resource, 3);
    }

    @Verifyica.Test
    public void testLimitedAccess(String resource) throws InterruptedException {
        KeyedSemaphoreManager.acquire(resource);
        try {
            // Max 3 tests can access this resource concurrently
            accessResource(resource);
        } finally {
            KeyedSemaphoreManager.release(resource);
        }
    }
}
```

### Example: API Rate Limiting

```java
public class RateLimitTest {

    @Verifyica.BeforeAll
    public void beforeAll(String apiEndpoint) {
        // Each endpoint allows 10 concurrent requests
        KeyedSemaphoreManager.createSemaphore(apiEndpoint, 10);
    }

    @Verifyica.Test
    public void testApiCall(String apiEndpoint) throws InterruptedException {
        KeyedSemaphoreManager.acquire(apiEndpoint);
        try {
            ApiClient client = new ApiClient();
            Response response = client.call(apiEndpoint);
            assert response.getStatus() == 200;
        } finally {
            KeyedSemaphoreManager.release(apiEndpoint);
        }
    }
}
```

## Combined Example: Multi-Level Coordination

```java
public class ComplexCoordinationTest {

    @Verifyica.ArgumentSupplier(parallelism = 20)
    public static Object arguments() {
        List<Argument<TestData>> args = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            String userId = "user-" + (i % 10); // 10 unique users
            String pool = "pool-" + (i % 5);    // 5 pools
            String phase = "phase-1";            // All in same phase
            args.add(Argument.of("test-" + i, new TestData(userId, pool, phase)));
        }
        return args;
    }

    @Verifyica.BeforeAll
    public void beforeAll(TestData data) {
        // Limit concurrent access per pool
        KeyedSemaphoreManager.createSemaphore(data.getPool(), 5);

        // Initialize phase gate for coordination
        KeyedLatchManager.createLatch(data.getPhase(), 100);
    }

    @Verifyica.Test
    public void complexTest(TestData data) throws InterruptedException {
        // 1. Acquire pool permit (max 5 per pool)
        KeyedSemaphoreManager.acquire(data.getPool());
        try {
            // 2. Lock specific user (exclusive access)
            KeyedMutexManager.lock(data.getUserId());
            try {
                performUserOperation(data.getUserId());
            } finally {
                KeyedMutexManager.unlock(data.getUserId());
            }

            // 3. Signal phase completion
            KeyedLatchManager.countDown(data.getPhase());

            // 4. Wait for all to complete phase
            KeyedLatchManager.await(data.getPhase());

            // 5. All proceed together
            performCoordinatedOperation(data);

        } finally {
            KeyedSemaphoreManager.release(data.getPool());
        }
    }
}
```

## Best Practices

### Always Use Try-Finally

```java
// Good: Always unlock/release in finally
KeyedMutexManager.lock(key);
try {
    // Critical section
} finally {
    KeyedMutexManager.unlock(key);
}

// Bad: Might not unlock on exception
KeyedMutexManager.lock(key);
// Critical section
KeyedMutexManager.unlock(key); // Might not execute!
```

### Clean Up Resources

```java
@Verifyica.Conclude
public void conclude(ClassContext classContext) {
    // Note: KeyedMutexManager, KeyedLatchManager, and KeyedSemaphoreManager
    // automatically clean up keys when they are no longer in use.
    // Manual cleanup is typically not required.
}
```

### Avoid Deadlocks

```java
// Bad: Can cause deadlock
KeyedMutexManager.lock(key1);
KeyedMutexManager.lock(key2); // Another thread might lock in reverse order

// Good: Lock in consistent order
List<String> keys = List.of(key1, key2);
Collections.sort(keys);
for (String key : keys) {
    KeyedMutexManager.lock(key);
}
try {
    // Critical section
} finally {
    for (String key : keys) {
        KeyedMutexManager.unlock(key);
    }
}
```

### Set Appropriate Timeouts

```java
// Good: Use timeouts to avoid infinite waits
if (!KeyedLatchManager.await(key, 30, TimeUnit.SECONDS)) {
    throw new TimeoutException("Coordination timed out");
}
```

## See Also

- [Parallelism](../parallelism/) - Advanced parallelism patterns
- [API Reference → Concurrency](../../api-reference/concurrency/) - Complete API documentation
- [Execution Model](../../core-concepts/execution-model/) - How tests execute
