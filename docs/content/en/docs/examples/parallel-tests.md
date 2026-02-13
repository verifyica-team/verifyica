---
title: "Parallel Tests"
linkTitle: "Parallel Tests"
weight: 2
description: >
  Examples demonstrating parallel argument execution
---

Examples using parallel execution to run multiple arguments concurrently.

## Parallel Argument Test

Parallel execution processes multiple arguments simultaneously.

### Example Code

```java
package org.verifyica.examples.simple;

import java.util.ArrayList;
import java.util.Collection;
import org.verifyica.api.Verifyica;

public class ParallelArgumentTest {

    @Verifyica.ArgumentSupplier(parallelism = 2)
    public static Object arguments() {
        Collection<String> collection = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            collection.add("string-" + i);
        }

        return collection;
    }

    @Verifyica.Prepare
    public void prepare() {
        System.out.println("prepare()");
    }

    @Verifyica.BeforeAll
    public void beforeAll(String argument) {
        System.out.println("beforeAll() argument [" + argument + "]");
    }

    @Verifyica.BeforeEach
    public void beforeEach(String argument) {
        System.out.println("beforeEach() argument [" + argument + "]");
    }

    @Verifyica.Test
    public void test1(String argument) {
        System.out.println("test1() argument [" + argument + "]");
    }

    @Verifyica.Test
    public void test2(String argument) {
        System.out.println("test2() argument [" + argument + "]");
    }

    @Verifyica.Test
    public void test3(String argument) {
        System.out.println("test3() argument [" + argument + "]");
    }

    @Verifyica.AfterEach
    public void afterEach(String argument) {
        System.out.println("afterEach() argument [" + argument + "]");
    }

    @Verifyica.AfterAll
    public void afterAll(String argument) {
        System.out.println("afterAll() argument [" + argument + "]");
    }

    @Verifyica.Conclude
    public void conclude() {
        System.out.println("conclude()");
    }
}
```

### Key Difference from Sequential

The only change from sequential execution is the `parallelism` parameter:

```java
@Verifyica.ArgumentSupplier(parallelism = 2)
```

This allows up to 2 arguments to execute concurrently.

### Execution Flow

With `parallelism = 2` and 10 arguments:

1. `prepare()` - Once
2. Process arguments in batches of 2:
   - Batch 1: string-0 and string-1 run concurrently
   - Batch 2: string-2 and string-3 run concurrently
   - Batch 3: string-4 and string-5 run concurrently
   - Batch 4: string-6 and string-7 run concurrently
   - Batch 5: string-8 and string-9 run concurrently
3. `conclude()` - Once

### Thread Safety Considerations

**Single Instance Model**

A single test class instance is shared across all arguments:

```java
public class ParallelArgumentTest {
    // UNSAFE: Shared mutable state without synchronization
    private int sharedCounter = 0;  // Will cause race conditions!

    @Verifyica.Test
    public void test(String argument) {
        sharedCounter++;  // Race condition with parallel execution
    }
}
```

**Safe Patterns**

Use context classes to isolate per-argument state:

```java
public static class TestContext {
    private final String data;
    private int counter = 0;  // Safe: isolated per argument

    public TestContext(String data) {
        this.data = data;
    }

    public void increment() {
        counter++;
    }
}

@Verifyica.BeforeAll
public void beforeAll(ArgumentContext argumentContext) {
    String argument = argumentContext.getArgument().getPayloadAs(String.class);
    TestContext context = new TestContext(argument);
    argumentContext.getMap().put("testContext", context);
}

@Verifyica.Test
public void test(ArgumentContext argumentContext) {
    TestContext context = (TestContext) argumentContext.getMap().get("testContext");
    context.increment();  // Safe: each argument has its own TestContext
}
```

## Parallelism Levels

### ArgumentSupplier Level

Control parallelism at the argument level:

```java
@Verifyica.ArgumentSupplier(parallelism = 4)  // Up to 4 concurrent arguments
```

### Test Method Level

Control parallelism for individual test methods:

```java
@Verifyica.Test(parallelism = 3)  // Up to 3 concurrent executions of this test
public void test(String argument) {
    // Test logic
}
```

### Unlimited Parallelism

Use `Integer.MAX_VALUE` for maximum parallelism:

```java
@Verifyica.ArgumentSupplier(parallelism = Integer.MAX_VALUE)
```

This allows all arguments to execute concurrently (subject to available threads).

## Performance Comparison

**Sequential (parallelism = 1)**
- 10 arguments × 3 tests × 1 second = 30 seconds total

**Parallel (parallelism = 5)**
- 10 arguments ÷ 5 concurrent = 2 batches
- 2 batches × 3 tests × 1 second = 6 seconds total
- **5x faster**

## When to Use Parallel Tests

Parallel execution is beneficial when:

- Arguments are independent
- Tests are CPU or I/O bound
- Large number of arguments to process
- Resources support concurrent access
- Faster test feedback is needed

## See Also

- [Simple Tests](../simple-tests/) - Sequential execution patterns
- [Advanced → Parallelism](../../advanced/parallelism/) - Advanced parallelism techniques
- [Core Concepts → Execution Model](../../core-concepts/execution-model/) - Understanding thread safety
- [Configuration → Parallelism](../../configuration/parallelism/) - Parallelism configuration
