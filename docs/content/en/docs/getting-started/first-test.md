---
title: "Your First Test"
linkTitle: "First Test"
weight: 3
description: >
  Deep dive into creating and understanding Verifyica tests
---

This guide provides a comprehensive walkthrough of creating Verifyica tests, explaining each component in detail.

## Test Class Structure

A Verifyica test class is a plain Java class with annotated methods. Unlike JUnit Jupiter tests that use `@Test` on every test method, Verifyica tests use several annotations to define the complete lifecycle.

### Basic Structure

```java
package com.example.tests;

import java.util.ArrayList;
import java.util.Collection;
import org.verifyica.api.Verifyica;
import org.verifyica.api.ClassContext;

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

  /**
   * The actual test.
   */
  @Verifyica.Test
  public void testWithArgument(String argument) {
    System.out.println("Testing with: " + argument);
    assert argument != null;
    assert argument.startsWith("argument-");
  }
}
```

### Required Elements

A minimal Verifyica test requires:
1. **One `@ArgumentSupplier` method** - Provides test arguments
2. **At least one `@Test` method** - Contains test logic

## The Argument Supplier

The argument supplier is the heart of Verifyica tests. It provides the data that drives your tests.

### Method Signature

```java
@Verifyica.ArgumentSupplier
public static Object arguments() {
    // Return Collection, array, Stream, or Argument<T>
}
```

Requirements:

- Must be `static`
- Must be `public`
- Return type must be one of:
  - `Collection<?>` (most common)
  - Array (e.g., `String[]`, `Object[]`)
  - `Stream<?>` (for large datasets)
  - `Iterable<?>` (for large datasets)
  - Single or multiple `Argument<T>` objects

### Return Type Examples

#### Collection (Recommended)

```java
@Verifyica.ArgumentSupplier
public static Collection<String> arguments() {
    return Arrays.asList("test1", "test2", "test3");
}
```

#### Array

```java
@Verifyica.ArgumentSupplier
public static String[] arguments() {
    return new String[] {"test1", "test2", "test3"};
}
```

#### Stream (for large datasets)

```java
@Verifyica.ArgumentSupplier
public static Stream<Integer> arguments() {
    return IntStream.range(0, 1000).boxed();
}
```

#### Argument&lt;T&gt; Objects

```java
@Verifyica.ArgumentSupplier
public static Collection<Argument<String>> arguments() {
    return Arrays.asList(
        Argument.of("test-1", "value1"),
        Argument.of("test-2", "value2")
    );
}
```

The `Argument<T>` interface provides:

- Named arguments (first parameter is the display name)
- Type safety
- Better test reporting

### Parallelism Configuration

Control how many arguments execute in parallel:

```java
@Verifyica.ArgumentSupplier(parallelism = 4)
public static Collection<String> arguments() {
    return generateArguments();
}
```

- `parallelism = 1` (default) - Sequential execution
- `parallelism = 2+` - Number of arguments executing concurrently

## Test Lifecycle Annotations

Verifyica provides a complete lifecycle for each argument.

### Complete Lifecycle

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

### Lifecycle Execution Order

For 3 arguments and 2 test methods:

```
1. Prepare (once)
2. For argument-1:
   - BeforeAll
   - BeforeEach → Test1 → AfterEach
   - BeforeEach → Test2 → AfterEach
   - AfterAll
3. For argument-2:
   - BeforeAll
   - BeforeEach → Test1 → AfterEach
   - BeforeEach → Test2 → AfterEach
   - AfterAll
4. For argument-3:
   - BeforeAll
   - BeforeEach → Test1 → AfterEach
   - BeforeEach → Test2 → AfterEach
   - AfterAll
5. Conclude (once)
```

### Method Parameters

Lifecycle methods can accept the current argument:

```java
@Verifyica.BeforeAll
public void beforeAll(String argument) {
    // Receives current argument
}

@Verifyica.Test
public void test(String argument) {
    // Receives current argument
}
```

Methods without the current argument as a parameter:

- `@Prepare` - No argument (called before argument processing)
- `@Conclude` - No argument (called after all arguments)

Methods that can optionally receive the argument:

- `@BeforeAll`, `@BeforeEach`, `@Test`, `@AfterEach`, `@AfterAll`

## Writing Test Methods

Test methods contain your actual test logic.

### Basic Test Method

```java
@Verifyica.Test
public void testSomething(String argument) {
    // Arrange
    Service service = new Service(argument);

    // Act
    Result result = service.process();

    // Assert
    assert result.isSuccess();
}
```

### Multiple Test Methods

You can have multiple `@Test` methods in a class:

```java
@Verifyica.Test
public void testCreation(Config config) {
    Service service = new Service(config);
    assert service != null;
}

@Verifyica.Test
public void testProcessing(Config config) {
    Service service = new Service(config);
    Result result = service.process();
    assert result.isSuccess();
}
```

Each test method runs **for every argument** with its own `@BeforeEach` and `@AfterEach`.

### Using Assertions

You can use any assertion library:

#### Standard Java Assertions

```java
@Verifyica.Test
public void test(String argument) {
    assert argument != null;
    assert argument.length() > 0;
}
```

#### JUnit Jupiter Assertions

```java
import static org.junit.jupiter.api.Assertions.*;

@Verifyica.Test
public void test(String argument) {
    assertNotNull(argument);
    assertTrue(argument.length() > 0);
}
```

#### AssertJ

```java
import static org.assertj.core.api.Assertions.*;

@Verifyica.Test
public void test(String argument) {
    assertThat(argument).isNotNull().isNotEmpty();
}
```

## Real-World Example

Here's a complete example testing a database with multiple configurations:

```java
package com.example.tests;

import org.verifyica.api.Argument;
import org.verifyica.api.Verifyica;
import java.util.Arrays;
import java.util.Collection;

public class DatabaseTest {

    // Define a context class to encapsulate per-argument state
    public static class TestContext {
        private final DatabaseConnection connection;

        public TestContext(DatabaseConnection connection) {
            this.connection = connection;
        }

        public DatabaseConnection getConnection() {
            return connection;
        }
    }

    @Verifyica.ArgumentSupplier(parallelism = 2)
    public static Collection<Argument<DatabaseConfig>> arguments() {
        return Arrays.asList(
            Argument.of("h2-memory", new DatabaseConfig("jdbc:h2:mem:test")),
            Argument.of("h2-file", new DatabaseConfig("jdbc:h2:file:./testdb")),
            Argument.of("postgresql", new DatabaseConfig("jdbc:postgresql://localhost/test"))
        );
    }

    @Verifyica.BeforeAll
    public void beforeAll(ArgumentContext argumentContext) {
        DatabaseConfig config = argumentContext.getArgument().getPayloadAs(DatabaseConfig.class);

        DatabaseConnection connection = new DatabaseConnection(config);
        connection.connect();
        connection.createSchema();

        // Store context in ArgumentContext map (thread-safe)
        TestContext context = new TestContext(connection);
        argumentContext.getMap().put("testContext", context);
    }

    @Verifyica.BeforeEach
    public void beforeEach(ArgumentContext argumentContext) {
        TestContext context = (TestContext) argumentContext.getMap().get("testContext");
        context.getConnection().clearData();
    }

    @Verifyica.Test
    public void testInsert(ArgumentContext argumentContext) {
        TestContext context = (TestContext) argumentContext.getMap().get("testContext");
        DatabaseConnection connection = context.getConnection();

        connection.insert("users", Map.of("id", 1, "name", "Alice"));
        List<User> users = connection.query("SELECT * FROM users");
        assert users.size() == 1;
        assert users.get(0).getName().equals("Alice");
    }

    @Verifyica.Test
    public void testUpdate(ArgumentContext argumentContext) {
        TestContext context = (TestContext) argumentContext.getMap().get("testContext");
        DatabaseConnection connection = context.getConnection();

        connection.insert("users", Map.of("id", 1, "name", "Alice"));
        connection.update("users", Map.of("name", "Bob"), "id = 1");
        User user = connection.queryOne("SELECT * FROM users WHERE id = 1");
        assert user.getName().equals("Bob");
    }

    @Verifyica.AfterEach
    public void afterEach(ArgumentContext argumentContext) {
        TestContext context = (TestContext) argumentContext.getMap().get("testContext");
        context.getConnection().clearData();
    }

    @Verifyica.AfterAll
    public void afterAll(ArgumentContext argumentContext) {
        TestContext context = (TestContext) argumentContext.getMap().get("testContext");
        if (context != null && context.getConnection() != null) {
            context.getConnection().dropSchema();
            context.getConnection().disconnect();
        }
    }
}
```

This test:

- Runs against 3 different database configurations
- Executes 2 arguments in parallel
- Has 2 test methods that run for each configuration
- Total test executions: 6 (3 configs × 2 tests)
- Properly manages connection lifecycle per configuration

## Common Patterns

### Skipping Arguments Conditionally

```java
@Verifyica.BeforeAll
public void beforeAll(String argument) {
    if (shouldSkip(argument)) {
        throw new TestSkippedException("Skipping: " + argument);
    }
}
```

### Sharing State Between Tests

Use instance variables (lifecycle methods run for the same argument sequentially):

```java
public class StatefulTest {

    private Service service;

    @Verifyica.BeforeAll
    public void beforeAll(Config config) {
        service = new Service(config);
    }

    @Verifyica.Test
    public void test1(Config config) {
        service.doSomething();
    }

    @Verifyica.Test
    public void test2(Config config) {
        // service is still available from beforeAll
        assert service.getState() == State.READY;
    }
}
```

### Dynamic Argument Generation

```java
@Verifyica.ArgumentSupplier
public static Collection<String> arguments() {
    List<String> args = new ArrayList<>();

    // Read from file
    args.addAll(readConfigsFromFile("configs.txt"));

    // Generate programmatically
    for (int i = 0; i < 10; i++) {
        args.add("generated-" + i);
    }

    // Filter based on environment
    if (System.getenv("RUN_FULL_SUITE") != null) {
        args.addAll(getFullTestSuite());
    }

    return args;
}
```

## Next Steps

Now that you understand the basics, explore:

- [Core Concepts](../../core-concepts/) - Deep dive into Arguments, Lifecycle, and Contexts
- [Configuration](../../configuration/) - Configure parallelism and filters
- [Examples](../../examples/) - See more complex patterns
- [API Reference](../../api-reference/) - Complete annotation reference
