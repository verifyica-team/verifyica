---
title: "Quick Start"
linkTitle: "Quick Start"
weight: 2
description: >
  Write and run your first Verifyica test in 5 minutes. Beginner-friendly guide for Java developers.
date: 2026-02-15
lastmod: 2026-02-15
---

This guide will walk you through creating and running your first Verifyica test.

## Your First Test

Create a new test class with the following code:

```java
package com.example.tests;

import java.util.ArrayList;
import java.util.Collection;
import org.verifyica.api.Verifyica;

public class MyFirstTest {

    // Arguments are tested in parallel by default
    @Verifyica.ArgumentSupplier
    public static Object arguments() {
        Collection<String> collection = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            collection.add("argument-" + i);
        }
        return collection;
    }

    @Verifyica.BeforeAll
    public void beforeAll(String argument) {
        System.out.println("Setting up for: " + argument);
    }

    @Verifyica.Test
    public void testWithArgument(String argument) {
        System.out.println("Testing with: " + argument);
        assert argument != null;
        assert argument.startsWith("argument-");
    }

    @Verifyica.AfterAll
    public void afterAll(String argument) {
        System.out.println("Cleaning up: " + argument);
    }
}
```

## Understanding the Test

Let's break down what's happening:

### 1. Argument Supplier

```java
@Verifyica.ArgumentSupplier
public static Object arguments() {
    Collection<String> collection = new ArrayList<>();
    for (int i = 0; i < 5; i++) {
        collection.add("argument-" + i);
    }
    return collection;
}
```

The `@ArgumentSupplier` method provides the test arguments. This method:

- Must be `static`
- Returns a `Collection`, array, `Stream`, or `Argument<T>` instances
- Is called once before test execution begins

### 2. Test Method

```java
@Verifyica.Test
public void testWithArgument(String argument) {
    System.out.println("Testing with: " + argument);
    assert argument != null;
    assert argument.startsWith("argument-");
}
```

The `@Test` method:

- Executes once **for each argument** (5 times in this example)
- Receives the current argument as a parameter
- Contains your test logic

### 3. Lifecycle Methods

```java
@Verifyica.BeforeAll
public void beforeAll(String argument) {
    System.out.println("Setting up for: " + argument);
}

@Verifyica.AfterAll
public void afterAll(String argument) {
    System.out.println("Cleaning up: " + argument);
}
```

- `@BeforeAll` runs once before all tests for each argument
- `@AfterAll` runs once after all tests for each argument

## Running the Test

### Using Maven

```bash
mvn test
```

### Using Your IDE

Run the test class directly from your IDE by:

- Right-clicking the class and selecting "Run"
- Clicking the green arrow next to the class name

## Test Output

When you run the test, you'll see output like:

```
Setting up for: argument-0
Testing with: argument-0
Cleaning up: argument-0
Setting up for: argument-1
Testing with: argument-1
Cleaning up: argument-1
... (continues for all 5 arguments)
```

The test method runs 5 times (once per argument), and each execution has its own setup and cleanup.

## Controlling Parallelism

Want to control argument test parallelism? Add `parallelism` to the argument supplier:

```java
@Verifyica.ArgumentSupplier(parallelism = 2)
public static Object arguments() {
    Collection<String> collection = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
        collection.add("argument-" + i);
    }
    return collection;
}
```

Now only 2 arguments will execute in parallel.

## Complete Example with All Lifecycle Methods

Here's a more complete example showing the full lifecycle:

```java
package com.example.tests;

import java.util.ArrayList;
import java.util.Collection;

import org.verifyica.api.ClassContext;
import org.verifyica.api.Verifyica;

public class CompleteLifecycleTest {

    @Verifyica.ArgumentSupplier(parallelism = 2)
    public static Object arguments() {
        Collection<String> collection = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            collection.add("string-" + i);
        }
        return collection;
    }

    @Verifyica.Prepare
    public void prepare(ClassContext classContext) {
        System.out.println("Prepare: Called once before all arguments");
    }

    @Verifyica.BeforeAll
    public void beforeAll(String argument) {
        System.out.println("BeforeAll: " + argument);
    }

    @Verifyica.BeforeEach
    public void beforeEach(String argument) {
        System.out.println("BeforeEach: " + argument);
    }

    @Verifyica.Test
    public void test1(String argument) {
        System.out.println("Test1: " + argument);
    }

    @Verifyica.Test
    public void test2(String argument) {
        System.out.println("Test2: " + argument);
    }

    @Verifyica.AfterEach
    public void afterEach(String argument) {
        System.out.println("AfterEach: " + argument);
    }

    @Verifyica.AfterAll
    public void afterAll(String argument) {
        System.out.println("AfterAll: " + argument);
    }

    @Verifyica.Conclude
    public void conclude(ClassContext classContext) {
        System.out.println("Conclude: Called once after all arguments");
    }
}
```

### Execution Flow

For each argument, the lifecycle is:

1. `@Prepare` (once for all arguments)
2. For each argument:
   - `@BeforeAll` (once per argument)
   - For each `@Test` method:
     - `@BeforeEach`
     - `@Test`
     - `@AfterEach`
   - `@AfterAll` (once per argument)
3. `@Conclude` (once for all arguments)

With 10 arguments and 2 test methods, this test will execute:

- 1 Prepare
- 10 BeforeAll
- 20 BeforeEach (10 arguments × 2 tests)
- 20 Test executions
- 20 AfterEach
- 10 AfterAll
- 1 Conclude

## Common Patterns

### Testing with Different Types

Arguments can be any type:

```java
@Verifyica.ArgumentSupplier
public static Object arguments() {
    return Arrays.asList(
        Argument.of("test1", new DatabaseConfig("localhost", 5432)),
        Argument.of("test2", new DatabaseConfig("prod", 5432))
    );
}

@Verifyica.Test
public void testDatabase(DatabaseConfig config) {
    // Test with config
}
```

### Conditional Execution

Skip tests conditionally:

```java
@Verifyica.Test
public void conditionalTest(String argument) {
    if (argument.equals("skip-me")) {
        throw new TestSkippedException("Skipping this argument");
    }
    // Test logic
}
```

## Next Steps

Now that you've created your first test, learn more about:

- [First Test Deep Dive](../first-test/) - Understand test structure in detail
- [Core Concepts](../../core-concepts/) - Learn about Arguments, Lifecycle, and more
- [Configuration](../../configuration/) - Configure parallelism and filters
- [Examples](../../examples/) - See more complex examples
