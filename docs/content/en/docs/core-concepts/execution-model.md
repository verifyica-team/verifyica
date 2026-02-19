---
title: "Execution Model"
linkTitle: "Execution Model"
weight: 5
description: >
  Understanding how Verifyica executes tests, manages parallelism, and processes arguments
---

Understanding Verifyica's execution model is crucial for writing efficient, parallel-friendly tests and reasoning about test behavior.

## Execution Overview

Verifyica executes tests in a structured lifecycle that provides isolation, parallelism, and predictable ordering.

### High-Level Flow

```
1. Test Discovery
   ↓
2. Engine Initialization
   ↓
3. For each Test Class:
   a. Instantiate test class
   b. Call @Prepare
   c. Call @ArgumentSupplier
   d. For each Argument (potentially parallel):
      - Call @BeforeAll
      - For each @Test method:
         - Call @BeforeEach
         - Call @Test
         - Call @AfterEach
      - Call @AfterAll
   e. Call @Conclude
   ↓
4. Engine Shutdown
```

## Test Discovery

Verifyica uses the JUnit Platform test discovery mechanism to find test classes.

### Discovery Rules

A class is discovered as a Verifyica test if it:

1. Contains at least one `@Verifyica.ArgumentSupplier` method
2. Contains at least one `@Verifyica.Test` method
3. Is not abstract
4. Has a no-arg constructor (public or package-private)

### Discovery Example

```java
// Discovered: Has ArgumentSupplier and Test
public class ValidTest {

    @Verifyica.ArgumentSupplier
    public static Object arguments() { return List.of("arg"); }

    @Verifyica.Test
    public void test(String arg) { }
}

// NOT discovered: Missing ArgumentSupplier
public class NotATest {

    @Verifyica.Test
    public void test() { }
}

// NOT discovered: Abstract class
public abstract class AbstractTest {
    @Verifyica.ArgumentSupplier
    public static Object arguments() { return List.of("arg"); }

    @Verifyica.Test
    public void test(String arg) { }
}
```

## Class Instantiation

A single test class instance is created and shared across all arguments.

### Single Shared Instance

```java
public class InstanceTest {

    private int counter = 0;

    @Verifyica.ArgumentSupplier
    public static Collection<String> arguments() {
        return Arrays.asList("arg1", "arg2", "arg3");
    }

    @Verifyica.BeforeAll
    public void beforeAll(String argument) {
        counter++; // Increments with each argument
    }

    @Verifyica.Test
    public void test(String argument) {
        System.out.println("Counter: " + counter);
        // Sequential: prints 1, 2, 3
        // Parallel: prints unpredictable values due to race condition!
    }
}
```

With 3 arguments, there is **one instance** of `InstanceTest`, and the `counter` field is **shared** across all arguments.

### Implication: Instance Variables Require Careful Management

Since all arguments share the same instance, instance variables must be managed carefully:

#### ✅ Safe Pattern: Argument-Specific State with Context Classes

```java
public class SafeStateTest {
    // Define a context class to hold per-argument state
    public static class TestContext {
        private final Connection connection;

        public TestContext(Connection connection) {
            this.connection = connection;
        }

        public Connection getConnection() {
            return connection;
        }
    }

    @Verifyica.BeforeAll
    public void beforeAll(ArgumentContext argumentContext) {
        Config config = argumentContext.getArgument().getPayloadAs(Config.class);
        Connection conn = database.connect(config);

        // Store context in ArgumentContext map
        TestContext context = new TestContext(conn);
        argumentContext.getMap().put("testContext", context);
    }

    @Verifyica.Test
    public void test1(ArgumentContext argumentContext) {
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
```

#### ❌ Unsafe Pattern: Shared Mutable State

```java
public class UnsafeStateTest {

    private Connection connection; // UNSAFE: shared across arguments!

    @Verifyica.BeforeAll
    public void beforeAll(Config config) {
        connection = database.connect(config);
        // If arguments run in parallel, this creates race conditions!
    }

    @Verifyica.Test
    public void test(Config config) {
        connection.execute("SELECT 1");
        // May use wrong connection or encounter race conditions!
    }
}
```

## Argument Processing

Arguments are processed sequentially by default, but can be parallelized.

### Sequential Processing (Default)

```java
@Verifyica.ArgumentSupplier
public static Collection<String> arguments() {
    return Arrays.asList("arg1", "arg2", "arg3");
}
```

Execution order:
```
1. Process arg1 completely (BeforeAll → Tests → AfterAll)
2. Process arg2 completely (BeforeAll → Tests → AfterAll)
3. Process arg3 completely (BeforeAll → Tests → AfterAll)
```

### Parallel Processing

```java
@Verifyica.ArgumentSupplier(parallelism = 2)
public static Collection<String> arguments() {
    return Arrays.asList("arg1", "arg2", "arg3");
}
```

Execution with parallelism = 2:
```
1. Process arg1 and arg2 in parallel
2. When one completes, start arg3
```

## Test Method Execution

Within an argument, test methods execute sequentially by default.

### Sequential Test Execution

```java
@Verifyica.Test
public void test1(String argument) {
    System.out.println("Test 1: " + argument);
}

@Verifyica.Test
public void test2(String argument) {
    System.out.println("Test 2: " + argument);
}

@Verifyica.Test
public void test3(String argument) {
    System.out.println("Test 3: " + argument);
}
```

For each argument, tests execute in order:
```
BeforeAll
  BeforeEach → Test1 → AfterEach
  BeforeEach → Test2 → AfterEach
  BeforeEach → Test3 → AfterEach
AfterAll
```

### Test Method Ordering

Control test method order with `@Order`:

```java
@Verifyica.Test
@Order(3)
public void test1() { }

@Verifyica.Test
@Order(1)
public void test2() { } // Executes first

@Verifyica.Test
@Order(2)
public void test3() { }
```

See [Advanced → Ordering](../../advanced/ordering/) for details.

## Parallelism Levels

Verifyica supports three levels of parallelism:

### 1. Class-Level Parallelism

Multiple test classes execute in parallel (controlled by test execution framework):

```
TestClass1 (parallel with)
TestClass2 (parallel with)
TestClass3
```

### 2. Argument-Level Parallelism

Multiple arguments within a class execute in parallel:

```java
@Verifyica.ArgumentSupplier(parallelism = 4)
public static Collection<String> arguments() {
    return generateArguments();
}
```

With parallelism = 4:
```
arg1 (parallel)
arg2 (parallel)
arg3 (parallel)
arg4 (parallel)
arg5 (waits for slot)
```

### 3. Test Method Parallelism

Test methods within an argument can execute in parallel (configured globally):

```properties
# verifyica.properties
verifyica.test.parallelism=4
```

```
For each argument:
  BeforeAll
    BeforeEach → Test1 → AfterEach (parallel)
    BeforeEach → Test2 → AfterEach (parallel)
    BeforeEach → Test3 → AfterEach (parallel)
    BeforeEach → Test4 → AfterEach (parallel)
  AfterAll
```

**Warning:** Test method parallelism requires careful state management to avoid race conditions.

See [Configuration → Parallelism](../../configuration/parallelism/) for complete details.

## Thread Safety Considerations

### Safe Patterns

#### Instance Variables with Sequential Arguments (parallelism = 1)

```java
public class SequentialArgumentsTest {

    private Connection connection; // Safe ONLY with parallelism = 1

    @Verifyica.ArgumentSupplier(parallelism = 1) // Sequential execution
    public static Collection<Config> arguments() {
        return getConfigs();
    }

    @Verifyica.BeforeAll
    public void beforeAll(Config config) {
        connection = database.connect(config);
    }

    @Verifyica.Test
    public void test(Config config) {
        connection.query("SELECT 1"); // Safe: one argument at a time
    }
}
```

#### Instance Variables with Parallel Arguments

For parallel argument execution, use context classes:

```java
public class ParallelArgumentsTest {
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

    @Verifyica.ArgumentSupplier(parallelism = 4) // Parallel execution
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
        context.getConnection().query("SELECT 1"); // Safe: isolated per argument
    }

    @Verifyica.AfterAll
    public void afterAll(ArgumentContext argumentContext) {
        TestContext context = (TestContext) argumentContext.getMap().get("testContext");
        if (context != null && context.getConnection() != null) {
            context.getConnection().close();
        }
    }
}
```

#### Static Variables with Proper Synchronization

```java
public class SynchronizedTest {

    private static final AtomicInteger counter = new AtomicInteger(0);

    @Verifyica.Test
    public void test(String argument) {
        counter.incrementAndGet(); // Safe: atomic operation
    }
}
```

### Unsafe Patterns

#### Unsynchronized Static State

```java
public class UnsafeTest {

    private static int counter = 0; // UNSAFE with parallelism!

    @Verifyica.Test
    public void test(String argument) {
        counter++; // Race condition!
    }
}
```

#### Shared Mutable State in Instance Variables with Test Parallelism

```java
public class UnsafeWithTestParallelism {

    private int value = 0; // UNSAFE if test methods run in parallel

    @Verifyica.Test
    public void test1(String argument) {
        value = 1; // Race condition if test2 runs concurrently
    }

    @Verifyica.Test
    public void test2(String argument) {
        value = 2; // Race condition if test1 runs concurrently
    }
}
```

## Error Handling and Propagation

### Exception in @Prepare

```java
@Verifyica.Prepare
public void prepare() {
    throw new RuntimeException("Setup failed");
}
```

**Effect:** All test execution stops. No arguments are processed.

### Exception in @BeforeAll

```java
@Verifyica.BeforeAll
public void beforeAll(String argument) {
    throw new RuntimeException("Argument setup failed");
}
```

**Effect:** All tests for this argument are skipped. Other arguments continue.

### Exception in @Test

```java
@Verifyica.Test
public void test(String argument) {
    throw new AssertionError("Test failed");
}
```

**Effect:** Test marked as failed. `@AfterEach` still executes. Other tests continue.

### @AfterEach and @AfterAll Always Execute

Cleanup methods execute even if tests fail:

```java
@Verifyica.AfterEach
public void afterEach(String argument) {
    // Executes even if test failed
    cleanup();
}

@Verifyica.AfterAll
public void afterAll(String argument) {
    // Executes even if all tests failed
    connection.close();
}
```

## Execution State Machine

Each argument progresses through states:

```
PENDING → RUNNING_BEFORE_ALL → RUNNING_TESTS → RUNNING_AFTER_ALL → COMPLETED
                                     ↓
                              (If test fails: continues to AFTER_ALL)
```

Failed states:
```
PENDING → FAILED_BEFORE_ALL → SKIPPED (tests skipped)
PENDING → RUNNING_BEFORE_ALL → RUNNING_TESTS → FAILED_TEST → RUNNING_AFTER_ALL → COMPLETED
```

## Performance Considerations

### Optimize Argument-Level Parallelism

```java
// Good: Parallel argument processing for independent arguments
@Verifyica.ArgumentSupplier(parallelism = 4)
public static Collection<Config> arguments() {
    return getIndependentConfigs(); // Each config is independent
}
```

### Keep @Prepare and @Conclude Fast

```java
// Good: Fast global setup
@Verifyica.Prepare
public void prepare() {
    System.setProperty("test.mode", "true");
}

// Bad: Slow operations block everything
@Verifyica.Prepare
public void prepare() throws InterruptedException {
    Thread.sleep(10000); // Blocks all test execution!
}
```

### Use @BeforeAll for Expensive Per-Argument Setup

```java
// Good: Expensive setup once per argument
@Verifyica.BeforeAll
public void beforeAll(Config config) {
    connection = database.connect(config); // Expensive, do once per argument
}

// Bad: Expensive setup repeated for every test
@Verifyica.BeforeEach
public void beforeEach(Config config) {
    connection = database.connect(config); // Wasteful, reconnects for every test!
}
```

**Note:** This example demonstrates efficiency patterns. For thread-safe state management with parallel arguments, use context classes stored in the ArgumentContext map. See the [Safe Patterns](#safe-patterns) section above.

## Execution Timeline Example

For this test:

```java
@Verifyica.ArgumentSupplier(parallelism = 2)
public static Collection<String> arguments() {
    return Arrays.asList("arg1", "arg2", "arg3");
}
```

With 2 test methods, the timeline is:

```
Time 0: Prepare
Time 1: ArgumentSupplier returns ["arg1", "arg2", "arg3"]
Time 2: Start arg1 and arg2 in parallel
  Thread 1:
    - BeforeAll(arg1)
    - BeforeEach(arg1) → Test1(arg1) → AfterEach(arg1)
    - BeforeEach(arg1) → Test2(arg1) → AfterEach(arg1)
    - AfterAll(arg1)
  Thread 2 (parallel):
    - BeforeAll(arg2)
    - BeforeEach(arg2) → Test1(arg2) → AfterEach(arg2)
    - BeforeEach(arg2) → Test2(arg2) → AfterEach(arg2)
    - AfterAll(arg2)
Time 3: First thread to finish picks up arg3
  Thread 1 or 2:
    - BeforeAll(arg3)
    - BeforeEach(arg3) → Test1(arg3) → AfterEach(arg3)
    - BeforeEach(arg3) → Test2(arg3) → AfterEach(arg3)
    - AfterAll(arg3)
Time 4: Conclude
```

## Next Steps

- [Configuration → Parallelism](../../configuration/parallelism/) - Configure parallelism in detail
- [Advanced → Parallelism](../../advanced/parallelism/) - Advanced parallel execution patterns
- [Advanced → Ordering](../../advanced/ordering/) - Control test method execution order
