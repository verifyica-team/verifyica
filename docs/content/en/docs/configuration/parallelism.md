---
title: "Parallelism"
linkTitle: "Parallelism"
weight: 3
description: >
  Configure parallel test execution at class, argument, and test levels
---

Verifyica provides three levels of parallelism control to optimize test execution time while maintaining test isolation.

## Parallelism Levels

Verifyica supports parallelism at three levels:

1. **Class-level** - Multiple test classes execute in parallel
2. **Argument-level** - Multiple arguments within a class execute in parallel
3. **Test-level** - Multiple test methods within an argument execute in parallel

## Class-Level Parallelism

Control how many test classes execute concurrently.

### Configuration

```properties
verifyica.engine.class.parallelism=4
```

### Example

With `class.parallelism=4`:

```
TestClass1 (Thread 1)
TestClass2 (Thread 2)
TestClass3 (Thread 3)
TestClass4 (Thread 4)
TestClass5 (waits for a thread to become available)
```

### Recommendations

- **Small test suites**: `1-2`
- **Medium test suites**: `2-4`
- **Large test suites**: `4-8`
- **CI/CD with many cores**: Match available CPU cores

## Argument-Level Parallelism

Control how many arguments within a test class execute concurrently.

### Configuration

**Default for all classes:**

```properties
verifyica.engine.argument.parallelism=2
```

**Per-class override:**

```java
@Verifyica.ArgumentSupplier(parallelism = 4)
public static Collection<String> arguments() {
    return generateArguments();
}
```

### Example

With `parallelism = 2` and 5 arguments:

```
For TestClass1:
  arg1 (Thread 1)
  arg2 (Thread 2)
  arg3 (waits for Thread 1 or 2)
  arg4 (waits)
  arg5 (waits)
```

### When to Use

**Good candidates for high parallelism:**
- Independent test arguments
- I/O-bound tests (database, network, file operations)
- Tests with significant wait times
- Container-based tests

**Keep parallelism low (1-2) when:**
- Tests share mutable state
- Tests use limited resources (ports, database connections)
- Tests are CPU-intensive
- Debugging tests

### Example: Independent Arguments

```java
@Verifyica.ArgumentSupplier(parallelism = 4)
public static Collection<Argument<Config>> arguments() {
    // Each config is completely independent
    return Arrays.asList(
        Argument.of("h2", new Config("jdbc:h2:mem:test1")),
        Argument.of("postgres", new Config("jdbc:postgresql://localhost/test2")),
        Argument.of("mysql", new Config("jdbc:mysql://localhost/test3")),
        Argument.of("oracle", new Config("jdbc:oracle://localhost/test4"))
    );
}
```

### Example: Resource-Constrained

```java
@Verifyica.ArgumentSupplier(parallelism = 1)
public static Collection<Argument<Integer>> arguments() {
    // All use same port - must be sequential
    return Arrays.asList(
        Argument.of("port-8080", 8080),
        Argument.of("port-8080-again", 8080)  // Would conflict if parallel
    );
}
```

## Test-Level Parallelism

Control how many test methods within an argument execute concurrently.

**Warning:** Test-level parallelism requires careful state management to avoid race conditions.

### Configuration

```properties
# Currently not configurable via annotation
# Controlled globally via properties
# (Check latest documentation for current support)
```

### Thread Safety Requirements

If test methods run in parallel within an argument:

- Instance variables must be thread-safe
- Use thread-safe collections
- Synchronize access to shared state

### Example: Parallel-Safe Tests

```java
public class ParallelSafeTest {

    private final AtomicInteger counter = new AtomicInteger(0);
    private final Connection connection; // Shared, must be thread-safe

    @Verifyica.BeforeAll
    public void beforeAll(Config config) {
        connection = database.connect(config);
    }

    @Verifyica.Test
    public void test1(Config config) {
        int value = counter.incrementAndGet(); // Thread-safe
        connection.execute("SELECT " + value);  // Connection must be thread-safe!
    }

    @Verifyica.Test
    public void test2(Config config) {
        int value = counter.incrementAndGet(); // Thread-safe
        connection.execute("SELECT " + value);
    }
}
```

## Thread Types

Configure the type of threads used for parallel execution.

### Configuration

```properties
verifyica.engine.thread.type=virtual
```

### Thread Type Options

| Type | Description | Best For | Java Version |
|------|-------------|----------|--------------|
| `virtual` | Java virtual threads | High concurrency, I/O-bound | Java 21+ |
| `platform` | Traditional OS threads | CPU-bound, Java 8+ | Java 8+ |
| `platform-ephemeral` | Threads discarded after use | ThreadLocal state isolation | Java 8+ |

### Virtual Threads (Java 21+)

Virtual threads enable massive concurrency with minimal overhead:

```properties
verifyica.engine.thread.type=virtual
verifyica.engine.argument.parallelism=100
```

**Benefits:**
- Extremely lightweight (1000s of virtual threads possible)
- Ideal for I/O-bound operations
- Minimal memory footprint

**Example:**

```java
// With virtual threads, high parallelism is practical
@Verifyica.ArgumentSupplier(parallelism = 50)
public static Collection<String> arguments() {
    // 50 concurrent arguments with minimal overhead
    return IntStream.range(0, 1000)
        .mapToObj(i -> "arg-" + i)
        .collect(Collectors.toList());
}
```

### Platform Threads

Traditional OS threads:

```properties
verifyica.engine.thread.type=platform
verifyica.engine.argument.parallelism=4
```

**Benefits:**
- Compatible with all Java versions
- Predictable behavior
- Better for CPU-bound operations

### Platform Ephemeral Threads

Platform threads that are discarded after use:

```properties
verifyica.engine.thread.type=platform-ephemeral
```

**Benefits:**
- Isolates ThreadLocal state between arguments
- Prevents ThreadLocal memory leaks
- Useful for tests that use ThreadLocal

## Parallelism Best Practices

### Start Sequential, Then Parallelize

```java
// Step 1: Get tests working sequentially
@Verifyica.ArgumentSupplier(parallelism = 1)
public static Collection<String> arguments() {
    return generateArguments();
}

// Step 2: Once stable, increase parallelism
@Verifyica.ArgumentSupplier(parallelism = 4)
public static Collection<String> arguments() {
    return generateArguments();
}
```

### Match Parallelism to Resources

```java
// Good: Parallelism matches available database connections
private static final int MAX_CONNECTIONS = 4;

@Verifyica.ArgumentSupplier(parallelism = 4)
public static Collection<Config> arguments() {
    // 4 arguments can run in parallel with 4 connections
    return generateConfigs();
}
```

### Use Virtual Threads for I/O-Bound Tests

```properties
# I/O-bound tests (network, database, files)
verifyica.engine.thread.type=virtual
verifyica.engine.argument.parallelism=20

# CPU-bound tests (computation, algorithms)
verifyica.engine.thread.type=platform
verifyica.engine.argument.parallelism=4
```

### Isolate State for Parallel Execution

```java
// Good: Isolated state per argument using context classes
public class IsolatedTest {
    // Define a context class to encapsulate per-argument state
    public static class TestContext {
        private final Connection connection;

        public TestContext(Connection connection) {
            this.connection = connection;
        }

        public Connection getConnection() {
            return connection;
        }
    }

    @Verifyica.ArgumentSupplier(parallelism = 4)
    public static Collection<Argument<Config>> arguments() {
        return getArguments();
    }

    @Verifyica.BeforeAll
    public void beforeAll(ArgumentContext argumentContext) {
        Config config = argumentContext.getArgument().getPayloadAs(Config.class);
        Connection conn = database.connect(config);

        // Store context in ArgumentContext map (thread-safe)
        TestContext context = new TestContext(conn);
        argumentContext.getMap().put("testContext", context);
    }

    @Verifyica.Test
    public void test(ArgumentContext argumentContext) {
        TestContext context = (TestContext) argumentContext.getMap().get("testContext");
        context.getConnection().execute("SELECT 1");
    }

    @Verifyica.AfterAll
    public void afterAll(ArgumentContext argumentContext) {
        TestContext context = (TestContext) argumentContext.getMap().get("testContext");
        if (context != null && context.getConnection() != null) {
            context.getConnection().close();
        }
    }
}

// Bad: Shared mutable instance variable with parallelism
public class SharedStateTest {

    private Connection connection; // UNSAFE: race condition with parallel arguments!

    @Verifyica.ArgumentSupplier(parallelism = 4)
    public static Collection<Config> arguments() {
        return getConfigs();
    }

    @Verifyica.BeforeAll
    public void beforeAll(Config config) {
        connection = database.connect(config); // Race condition!
    }

    @Verifyica.Test
    public void test(Config config) {
        connection.execute("SELECT 1"); // May use wrong connection!
    }
}
```

### Monitor Performance

Use interceptors to measure the impact of parallelism:

```java
public class PerformanceInterceptor implements ClassInterceptor {
    private long startTime;

    @Override
    public void prePrepare(ClassContext classContext, Method method) {
        startTime = System.currentTimeMillis();
    }

    @Override
    public void postConclude(ClassContext classContext, Method method, Throwable throwable) {
        long duration = System.currentTimeMillis() - startTime;
        String className = classContext.getTestClass().getSimpleName();
        System.out.printf("%s completed in %d ms%n", className, duration);
    }
}
```

## Parallelism Troubleshooting

### Tests Fail Only When Parallel

**Symptoms:**
- Tests pass with `parallelism=1`
- Tests fail with `parallelism>1`

**Causes:**
1. Shared mutable state
2. Race conditions
3. Resource contention
4. Improper synchronization

**Solution:**

```java
// Use thread-safe collections
private final ConcurrentHashMap<String, Result> results = new ConcurrentHashMap<>();

// Use atomic operations
private final AtomicInteger counter = new AtomicInteger(0);

// Synchronize access to shared resources
private synchronized void updateSharedState() {
    // Critical section
}
```

### Tests Are Slower with Parallelism

**Causes:**
1. Too much parallelism (thread overhead)
2. Resource contention (database, CPU)
3. Shared locks or bottlenecks

**Solution:**

```properties
# Reduce parallelism
verifyica.engine.argument.parallelism=2

# Try different thread type
verifyica.engine.thread.type=platform
```

### Deadlocks or Hangs

**Causes:**
1. Circular dependencies
2. Resource exhaustion
3. Improper locking

**Solution:**

```properties
# Disable parallelism for debugging
verifyica.engine.argument.parallelism=1

# Enable debug logging
verifyica.engine.logger.level=DEBUG
```

## Performance Examples

### Example 1: TestContainers with Parallelism

```java
@Verifyica.ArgumentSupplier(parallelism = 3)
public static Collection<Argument<Container>> arguments() {
    // Start 3 containers in parallel
    return Arrays.asList(
        Argument.of("nginx", new NginxContainer()),
        Argument.of("postgres", new PostgreSQLContainer()),
        Argument.of("redis", new RedisContainer())
    );
}
```

With parallelism:
- Sequential: 90 seconds (30s × 3)
- Parallel (3): 30 seconds (3 containers start simultaneously)

### Example 2: API Testing

```java
@Verifyica.ArgumentSupplier(parallelism = 10)
public static Collection<String> arguments() {
    // Test 100 API endpoints
    return generateEndpoints(); // Returns 100 endpoints
}
```

With `parallelism=10` and virtual threads:
- Sequential: 1000 seconds (10s × 100)
- Parallel (10): 100 seconds (10 at a time)

## Configuration Examples

### Development Environment

```properties
# Low parallelism for easier debugging
verifyica.engine.class.parallelism=1
verifyica.engine.argument.parallelism=1
verifyica.engine.thread.type=platform
verifyica.engine.logger.level=DEBUG
```

### CI/CD Environment

```properties
# High parallelism for speed
verifyica.engine.class.parallelism=8
verifyica.engine.argument.parallelism=4
verifyica.engine.thread.type=virtual
verifyica.engine.logger.level=INFO
```

### Integration Testing

```properties
# Moderate parallelism, balanced for resources
verifyica.engine.class.parallelism=2
verifyica.engine.argument.parallelism=2
verifyica.engine.thread.type=platform
```

## Next Steps

- [Properties](../properties/) - Complete properties reference
- [Advanced → Parallelism](../../advanced/parallelism/) - Advanced parallel patterns
- [Core Concepts → Execution Model](../../core-concepts/execution-model/) - Understand execution flow
