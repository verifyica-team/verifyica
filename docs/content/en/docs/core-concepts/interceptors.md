---
title: "Interceptors"
linkTitle: "Interceptors"
weight: 4
description: >
  Hook into test execution with ClassInterceptor and EngineInterceptor
---

Interceptors provide powerful extension points to hook into test execution for cross-cutting concerns like logging, metrics, resource management, or custom behavior.

## Interceptor Types

Verifyica provides two interceptor interfaces:

1. **ClassInterceptor** - Intercepts class-level and argument-level execution
2. **EngineInterceptor** - Intercepts engine-level execution

## ClassInterceptor

The `ClassInterceptor` interface provides hooks at multiple lifecycle points for each test class and argument.

### Interface Overview

```java
public interface ClassInterceptor {
    // Initialization
    void initialize(EngineContext engineContext) throws Throwable;

    // Filter which classes this interceptor applies to
    Predicate<ClassContext> predicate();

    // Class instantiation
    void preInstantiate(EngineContext engineContext, Class<?> testClass) throws Throwable;
    void postInstantiate(EngineContext engineContext, Class<?> testClass,
                         Object testInstance, Throwable throwable) throws Throwable;

    // Lifecycle hooks
    void prePrepare(ClassContext classContext, Method method) throws Throwable;
    void postPrepare(ClassContext classContext, Method method, Throwable throwable) throws Throwable;

    void preBeforeAll(ArgumentContext argumentContext, Method method) throws Throwable;
    void postBeforeAll(ArgumentContext argumentContext, Method method, Throwable throwable) throws Throwable;

    void preBeforeEach(ArgumentContext argumentContext, Method method) throws Throwable;
    void postBeforeEach(ArgumentContext argumentContext, Method method, Throwable throwable) throws Throwable;

    void preTest(ArgumentContext argumentContext, Method method) throws Throwable;
    void postTest(ArgumentContext argumentContext, Method method, Throwable throwable) throws Throwable;

    void preAfterEach(ArgumentContext argumentContext, Method method) throws Throwable;
    void postAfterEach(ArgumentContext argumentContext, Method method, Throwable throwable) throws Throwable;

    void preAfterAll(ArgumentContext argumentContext, Method method) throws Throwable;
    void postAfterAll(ArgumentContext argumentContext, Method method, Throwable throwable) throws Throwable;

    void preConclude(ClassContext classContext, Method method) throws Throwable;
    void postConclude(ClassContext classContext, Method method, Throwable throwable) throws Throwable;

    // Cleanup
    void destroy(EngineContext engineContext) throws Throwable;
}
```

All methods have default implementations, so you only need to override the hooks you need.

### Creating a ClassInterceptor

Implement the `ClassInterceptor` interface and register it via ServiceLoader:

#### Step 1: Implement ClassInterceptor

```java
package com.example.interceptors;

import org.verifyica.api.ArgumentContext;
import org.verifyica.api.ClassContext;
import org.verifyica.api.ClassInterceptor;
import org.verifyica.api.EngineContext;
import java.lang.reflect.Method;

public class LoggingInterceptor implements ClassInterceptor {

    @Override
    public void initialize(EngineContext engineContext) {
        System.out.println("Logging interceptor initialized");
    }

    @Override
    public void preBeforeAll(ArgumentContext argumentContext, Method method) {
        String argumentName = argumentContext.getArgument().getName();
        System.out.println("Starting tests for argument: " + argumentName);
    }

    @Override
    public void preTest(ArgumentContext argumentContext, Method method) {
        String argumentName = argumentContext.getArgument().getName();
        String methodName = method.getName();
        System.out.println("Executing test: " + methodName + " with " + argumentName);
    }

    @Override
    public void postTest(ArgumentContext argumentContext, Method method, Throwable throwable) {
        String methodName = method.getName();
        if (throwable != null) {
            System.out.println("Test failed: " + methodName + " - " + throwable.getMessage());
        } else {
            System.out.println("Test passed: " + methodName);
        }
    }

    @Override
    public void postAfterAll(ArgumentContext argumentContext, Method method, Throwable throwable) {
        String argumentName = argumentContext.getArgument().getName();
        System.out.println("Completed tests for argument: " + argumentName);
    }

    @Override
    public void destroy(EngineContext engineContext) {
        System.out.println("Logging interceptor destroyed");
    }
}
```

#### Step 2: Register via ServiceLoader

Create `META-INF/services/org.verifyica.api.ClassInterceptor`:

```
com.example.interceptors.LoggingInterceptor
```

### Filtering Classes with Predicate

Use `predicate()` to control which test classes the interceptor applies to:

```java
public class DatabaseInterceptor implements ClassInterceptor {

    @Override
    public Predicate<ClassContext> predicate() {
        // Only apply to classes annotated with @DatabaseTest
        return classContext -> {
            Class<?> testClass = classContext.getTestClass();
            return testClass.isAnnotationPresent(DatabaseTest.class);
        };
    }

    @Override
    public void preBeforeAll(ArgumentContext argumentContext, Method method) {
        // Only executed for classes with @DatabaseTest
        startDatabaseConnection();
    }
}
```

## Common Interceptor Patterns

### Timing and Performance Monitoring

```java
public class TimingInterceptor implements ClassInterceptor {

    private final Map<String, Long> startTimes = new ConcurrentHashMap<>();

    @Override
    public void preTest(ArgumentContext argumentContext, Method method) {
        String key = getKey(argumentContext, method);
        startTimes.put(key, System.nanoTime());
    }

    @Override
    public void postTest(ArgumentContext argumentContext, Method method, Throwable throwable) {
        String key = getKey(argumentContext, method);
        Long startTime = startTimes.remove(key);
        if (startTime != null) {
            long duration = System.nanoTime() - startTime;
            System.out.printf("Test %s took %d ms%n", key, duration / 1_000_000);
        }
    }

    private String getKey(ArgumentContext argumentContext, Method method) {
        return argumentContext.getArgument().getName() + ":" + method.getName();
    }
}
```

### Resource Management

```java
public class ResourceInterceptor implements ClassInterceptor {

    @Override
    public void preBeforeAll(ArgumentContext argumentContext, Method method) {
        String argumentName = argumentContext.getArgument().getName();
        Resource resource = Resource.create(argumentName);

        // Store resource in ArgumentContext map
        argumentContext.getMap().put("resource", resource);
    }

    @Override
    public void postAfterAll(ArgumentContext argumentContext, Method method, Throwable throwable) {
        // Retrieve and cleanup resource
        Resource resource = (Resource) argumentContext.getMap().get("resource");
        if (resource != null) {
            resource.close();
        }
    }
}
```

### Retry Logic

```java
public class RetryInterceptor implements ClassInterceptor {

    private static final int MAX_RETRIES = 3;

    @Override
    public void postTest(ArgumentContext argumentContext, Method method, Throwable throwable)
            throws Throwable {
        if (throwable != null && shouldRetry(throwable)) {
            for (int i = 0; i < MAX_RETRIES; i++) {
                try {
                    // Retry the test
                    Object testInstance = argumentContext.getClassContext().getTestInstance();
                    method.invoke(testInstance, getMethodArguments(argumentContext, method));
                    return; // Success, no need to rethrow
                } catch (Throwable retryThrowable) {
                    if (i == MAX_RETRIES - 1) {
                        throw retryThrowable; // Final retry failed
                    }
                    Thread.sleep(1000 * (i + 1)); // Backoff
                }
            }
        }
        rethrow(throwable); // Rethrow original exception if not retried
    }

    private boolean shouldRetry(Throwable throwable) {
        return throwable instanceof TransientException;
    }
}
```

### Test Result Collection

```java
public class ResultCollectorInterceptor implements ClassInterceptor {

    private final List<TestResult> results = new ArrayList<>();

    @Override
    public void postTest(ArgumentContext argumentContext, Method method, Throwable throwable) {
        TestResult result = new TestResult(
            argumentContext.getArgument().getName(),
            method.getName(),
            throwable == null,
            throwable != null ? throwable.getMessage() : null
        );
        synchronized (results) {
            results.add(result);
        }
    }

    @Override
    public void destroy(EngineContext engineContext) {
        // Generate report with all results
        ReportGenerator.generate(results);
    }
}
```

## EngineInterceptor

The `EngineInterceptor` interface provides hooks at the engine level, before any test classes are processed.

### Interface Overview

```java
public interface EngineInterceptor {
    void initialize(EngineContext engineContext) throws Throwable;
    void preDiscovery(EngineContext engineContext) throws Throwable;
    void postDiscovery(EngineContext engineContext) throws Throwable;
    void destroy(EngineContext engineContext) throws Throwable;
}
```

### Example: Global Setup

```java
package com.example.interceptors;

import org.verifyica.api.EngineContext;
import org.verifyica.api.EngineInterceptor;

public class GlobalSetupInterceptor implements EngineInterceptor {

    @Override
    public void initialize(EngineContext engineContext) {
        System.out.println("Engine starting");
    }

    @Override
    public void preDiscovery(EngineContext engineContext) {
        System.out.println("Starting test discovery");
        // Start global services
        GlobalServices.start();
    }

    @Override
    public void postDiscovery(EngineContext engineContext) {
        System.out.println("Test discovery complete");
    }

    @Override
    public void destroy(EngineContext engineContext) {
        System.out.println("Engine shutting down");
        // Stop global services
        GlobalServices.stop();
    }
}
```

Register via `META-INF/services/org.verifyica.api.EngineInterceptor`:

```
com.example.interceptors.GlobalSetupInterceptor
```

## Interceptor Best Practices

### Keep Interceptors Fast

Interceptors run for every test, so keep them efficient:

```java
// Good: Fast logging
@Override
public void preTest(ArgumentContext argumentContext, Method method) {
    logger.debug("Test: {}", method.getName());
}

// Bad: Slow synchronous operations
@Override
public void preTest(ArgumentContext argumentContext, Method method) {
    sendHttpRequest("http://slow-service.com/log"); // Blocks test execution!
}
```

### Handle Exceptions Carefully

Exceptions in interceptors can affect test execution:

```java
@Override
public void postTest(ArgumentContext argumentContext, Method method, Throwable throwable) {
    try {
        // Interceptor logic that might fail
        saveTestResult(method, throwable);
    } catch (Exception e) {
        // Log but don't throw - don't cascade failures
        logger.error("Failed to save test result", e);
    }
}
```

### Use Thread-Safe Collections

When collecting data across tests:

```java
// Good: Thread-safe collection
private final ConcurrentHashMap<String, TestResult> results = new ConcurrentHashMap<>();

// Bad: Not thread-safe (will cause issues with parallel execution)
private final HashMap<String, TestResult> results = new HashMap<>();
```

### Rethrow Original Exceptions

When intercepting failures, rethrow the original exception if you don't handle it:

```java
@Override
public void postTest(ArgumentContext argumentContext, Method method, Throwable throwable)
        throws Throwable {
    if (throwable != null) {
        logger.error("Test failed: {}", method.getName(), throwable);
    }
    rethrow(throwable); // Rethrow so test is marked as failed
}
```

### Use Predicates for Targeted Interception

Don't process every test class if you only care about specific ones:

```java
@Override
public Predicate<ClassContext> predicate() {
    return classContext -> {
        Class<?> testClass = classContext.getTestClass();
        // Only intercept integration tests
        return testClass.getPackage().getName().contains(".integration.");
    };
}
```

## Example: Complete Monitoring Interceptor

Here's a complete example that demonstrates multiple interceptor features:

```java
package com.example.interceptors;

import org.verifyica.api.*;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

public class MonitoringInterceptor implements ClassInterceptor {

    private final Map<String, Metrics> metrics = new ConcurrentHashMap<>();

    @Override
    public void initialize(EngineContext engineContext) {
        System.out.println("Monitoring initialized");
    }

    @Override
    public Predicate<ClassContext> predicate() {
        // Only monitor classes annotated with @Monitored
        return classContext -> classContext.getTestClass()
            .isAnnotationPresent(Monitored.class);
    }

    @Override
    public void preBeforeAll(ArgumentContext argumentContext, Method method) {
        String key = getKey(argumentContext);
        metrics.put(key, new Metrics());
        metrics.get(key).startTime = System.currentTimeMillis();
    }

    @Override
    public void preTest(ArgumentContext argumentContext, Method method) {
        String key = getKey(argumentContext);
        metrics.get(key).testCount++;
        metrics.get(key).currentTestStart = System.nanoTime();
    }

    @Override
    public void postTest(ArgumentContext argumentContext, Method method, Throwable throwable) {
        String key = getKey(argumentContext);
        Metrics m = metrics.get(key);
        long duration = System.nanoTime() - m.currentTestStart;
        m.totalTestTime += duration;

        if (throwable != null) {
            m.failureCount++;
        } else {
            m.successCount++;
        }
    }

    @Override
    public void postAfterAll(ArgumentContext argumentContext, Method method, Throwable throwable) {
        String key = getKey(argumentContext);
        Metrics m = metrics.get(key);
        m.endTime = System.currentTimeMillis();

        System.out.printf("Metrics for %s:%n", key);
        System.out.printf("  Total time: %d ms%n", m.endTime - m.startTime);
        System.out.printf("  Test count: %d%n", m.testCount);
        System.out.printf("  Successes: %d%n", m.successCount);
        System.out.printf("  Failures: %d%n", m.failureCount);
        System.out.printf("  Avg test time: %.2f ms%n",
            m.totalTestTime / (double) m.testCount / 1_000_000);
    }

    @Override
    public void destroy(EngineContext engineContext) {
        System.out.println("Monitoring destroyed");
        // Could save aggregate metrics here
    }

    private String getKey(ArgumentContext argumentContext) {
        return argumentContext.getClassContext().getTestClass().getSimpleName() +
               ":" + argumentContext.getArgument().getName();
    }

    private static class Metrics {
        long startTime;
        long endTime;
        int testCount;
        int successCount;
        int failureCount;
        long totalTestTime;
        long currentTestStart;
    }
}
```

## Next Steps

- [Execution Model](../execution-model/) - Understand when interceptors fire during execution
- [API Reference → Interceptor API](../../api-reference/interceptor-api/) - Complete interceptor API reference
- [Examples → Interceptor Examples](../../examples/interceptor-examples/) - See more interceptor examples
