---
title: "Annotations"
linkTitle: "Annotations"
weight: 1
description: >
  Complete reference for all @Verifyica annotations
---

All Verifyica annotations are nested within the `@Verifyica` interface.

## Lifecycle Annotations

### @Verifyica.Prepare

Marks a method to execute once before any arguments are processed.

**Method signature:**
```java
// No parameters
@Verifyica.Prepare
public void prepare() { }

// OR ClassContext only
@Verifyica.Prepare
public void prepare(ClassContext classContext) { }
```

**Supported parameters:** None OR ClassContext only (NOT EngineContext, NOT ArgumentContext)

**Execution:** Once per test class, before argument processing
**Use for:** Global setup, starting services, initializing shared resources

### @Verifyica.ArgumentSupplier

Marks a static method that provides test arguments.

**Method signature:**
```java
@Verifyica.ArgumentSupplier
public static Object arguments() { }

// With parallelism
@Verifyica.ArgumentSupplier(parallelism = 4)
public static Collection<String> arguments() { }
```

**Parameters:**
- `parallelism` - Number of arguments to execute in parallel (default: 1)

**Return types:** Collection, Array, Stream, or Argument<T> instances

### @Verifyica.BeforeAll

Marks a method to execute once per argument before its tests.

**Method signature:**
```java
// Unwrapped argument
@Verifyica.BeforeAll
public void beforeAll(ArgumentType argument) { }

// OR ArgumentContext
@Verifyica.BeforeAll
public void beforeAll(ArgumentContext argumentContext) { }
```

**Supported parameters:** Unwrapped argument OR ArgumentContext only (choose one, not both)

**Execution:** Once per argument, before test methods
**Use for:** Argument-specific setup, creating connections

### @Verifyica.BeforeEach

Marks a method to execute before each test method.

**Method signature:**
```java
@Verifyica.BeforeEach
public void beforeEach(ArgumentType argument) { }
```

**Execution:** Before each `@Test` method for each argument
**Use for:** Test-specific setup, resetting state

### @Verifyica.Test

Marks a test method.

**Method signature:**
```java
// Unwrapped argument
@Verifyica.Test
public void testMethod(ArgumentType argument) { }

// OR ArgumentContext
@Verifyica.Test
public void testMethod(ArgumentContext argumentContext) { }
```

**Supported parameters:** Unwrapped argument OR ArgumentContext only (choose one, not both)

**Execution:** Once per argument
**Use for:** Test logic

### @Verifyica.AfterEach

Marks a method to execute after each test method.

**Method signature:**
```java
@Verifyica.AfterEach
public void afterEach(ArgumentType argument) { }
```

**Execution:** After each `@Test` method for each argument
**Use for:** Test-specific cleanup

### @Verifyica.AfterAll

Marks a method to execute once per argument after its tests.

**Method signature:**
```java
@Verifyica.AfterAll
public void afterAll(ArgumentType argument) { }
```

**Execution:** Once per argument, after all test methods
**Use for:** Argument-specific cleanup, closing connections

### @Verifyica.Conclude

Marks a method to execute once after all arguments are processed.

**Method signature:**
```java
// No parameters
@Verifyica.Conclude
public void conclude() { }

// OR ClassContext only
@Verifyica.Conclude
public void conclude(ClassContext classContext) { }
```

**Supported parameters:** None OR ClassContext only (NOT EngineContext, NOT ArgumentContext)

**Execution:** Once per test class, after all argument processing
**Use for:** Global cleanup, stopping services

## Supporting Annotations

### @Tag

Marks tests with tags for filtering.

```java
import org.verifyica.api.Tag;

@Tag("integration")
@Tag("database")
public class DatabaseTest { }

// Method-level tags
@Verifyica.Test
@Tag("slow")
public void slowTest(String argument) { }
```

**Use for:** Filtering tests with include/exclude filters

### @Order

Controls test method execution order.

```java
import org.verifyica.api.Order;

@Verifyica.Test
@Order(1)
public void firstTest(String argument) { }

@Verifyica.Test
@Order(2)
public void secondTest(String argument) { }
```

**Note:** Lower order values execute first

### @DependsOn

Specifies test dependencies.

```java
import org.verifyica.api.DependsOn;

@Verifyica.Test
public void test1(String argument) { }

@Verifyica.Test
@DependsOn("test1")
public void test2(String argument) { }
```

**Use for:** Ensuring tests execute in specific order

## Annotation Examples

### Complete Lifecycle

```java
public class FullLifecycleTest {

    @Verifyica.Prepare
    public void prepare() {
        // Global setup
    }

    @Verifyica.ArgumentSupplier(parallelism = 2)
    public static Collection<String> arguments() {
        return Arrays.asList("arg1", "arg2");
    }

    @Verifyica.BeforeAll
    public void beforeAll(String argument) {
        // Per-argument setup
    }

    @Verifyica.BeforeEach
    public void beforeEach(String argument) {
        // Per-test setup
    }

    @Verifyica.Test
    @Order(1)
    @Tag("unit")
    public void test1(String argument) {
        // Test logic
    }

    @Verifyica.Test
    @Order(2)
    @DependsOn("test1")
    public void test2(String argument) {
        // Depends on test1
    }

    @Verifyica.AfterEach
    public void afterEach(String argument) {
        // Per-test cleanup
    }

    @Verifyica.AfterAll
    public void afterAll(String argument) {
        // Per-argument cleanup
    }

    @Verifyica.Conclude
    public void conclude() {
        // Global cleanup
    }
}
```

## See Also

- [Core Concepts → Lifecycle](../../core-concepts/lifecycle/)
- [Advanced → Ordering](../../advanced/ordering/)
- [Advanced → Dependencies](../../advanced/dependencies/)
- [Configuration → Filters](../../configuration/filters/)
