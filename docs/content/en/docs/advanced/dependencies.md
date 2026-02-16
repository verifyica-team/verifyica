---
title: "Test Dependencies"
linkTitle: "Dependencies"
weight: 2
description: >
  Define test dependencies with @DependsOn
---

The `@DependsOn` annotation allows you to specify that a test method should only run if another test method passes.

## Overview

Use `@DependsOn` when a test requires another test to complete successfully first. If the dependency fails or is skipped, the dependent test is also skipped.

## Basic Usage

```java
import org.verifyica.api.DependsOn;
import org.verifyica.api.Verifyica;

public class DependencyTest {

    @Verifyica.ArgumentSupplier
    public static Object arguments() {
        return List.of("test-data");
    }

    @Verifyica.Test
    public void prerequisite(String argument) {
        // This must pass for dependent tests to run
        setupEnvironment();
    }

    @Verifyica.Test
    @DependsOn("prerequisite")
    public void dependentTest(String argument) {
        // Only runs if prerequisite passes
        performMainTest();
    }
}
```

## Multiple Dependencies

A test can depend on multiple other tests:

```java
@Verifyica.Test
public void setup1() {
    // First setup
}

@Verifyica.Test
public void setup2() {
    // Second setup
}

@Verifyica.Test
@DependsOn({"setup1", "setup2"})
public void mainTest() {
    // Only runs if BOTH setup1 AND setup2 pass
}
```

## Dependency Chains

You can create chains of dependencies:

```java
@Verifyica.Test
public void createDatabase() {
    database.create();
}

@Verifyica.Test
@DependsOn("createDatabase")
public void createTables() {
    database.createTables();
}

@Verifyica.Test
@DependsOn("createTables")
public void insertData() {
    database.insertData();
}

@Verifyica.Test
@DependsOn("insertData")
public void verifyData() {
    assert database.countRows() > 0;
}
```

## Example: Environment Setup

```java
public class IntegrationTest {

    @Verifyica.ArgumentSupplier
    public static Object arguments() {
        return List.of(
            Argument.of("dev", new Environment("dev")),
            Argument.of("staging", new Environment("staging"))
        );
    }

    @Verifyica.Test
    public void checkConnection(ArgumentContext argumentContext) {
        Environment env = argumentContext.getArgument().getPayloadAs(Environment.class);
        assert env.isReachable();
    }

    @Verifyica.Test
    @DependsOn("checkConnection")
    public void authenticate(ArgumentContext argumentContext) {
        // Only runs if connection check passes
        Environment env = argumentContext.getArgument().getPayloadAs(Environment.class);
        assert env.authenticate();
    }

    @Verifyica.Test
    @DependsOn("authenticate")
    public void runTests(ArgumentContext argumentContext) {
        // Only runs if authentication succeeds
        performIntegrationTests();
    }
}
```

## Behavior When Dependencies Fail

- If a dependency **fails**, dependent tests are **skipped**
- If a dependency is **skipped**, dependent tests are **skipped**
- If a dependency **passes**, dependent tests run normally

Example:
```java
@Verifyica.Test
public void mayFail() {
    if (random.nextBoolean()) {
        throw new RuntimeException("Failed");
    }
}

@Verifyica.Test
@DependsOn("mayFail")
public void dependent() {
    // Skipped if mayFail() throws exception
}
```

## Combining with @Order

Use both annotations together for explicit ordering:

```java
@Verifyica.Test
@Order(1)
public void first() {
    // Runs first
}

@Verifyica.Test
@Order(2)
@DependsOn("first")
public void second() {
    // Runs second (if first passes)
}
```

## Dependencies Per Argument

Dependencies are evaluated per argument independently:

```java
@Verifyica.ArgumentSupplier
public static Object arguments() {
    return List.of("arg1", "arg2");
}

@Verifyica.Test
public void setup(String arg) {
    // For arg1: might pass
    // For arg2: might fail
}

@Verifyica.Test
@DependsOn("setup")
public void main(String arg) {
    // For arg1: runs if setup(arg1) passed
    // For arg2: skipped if setup(arg2) failed
}
```

## Best Practices

### Use for True Prerequisites

```java
// Good: Genuine prerequisite
@Verifyica.Test
public void databaseAvailable() {
    assert database.ping();
}

@Verifyica.Test
@DependsOn("databaseAvailable")
public void testQueries() {
    // Makes sense to skip if DB is unavailable
}

// Less ideal: Creating unnecessary coupling
@Verifyica.Test
public void testA() { }

@Verifyica.Test
@DependsOn("testA")
public void testB() { } // Why does B depend on A?
```

### Avoid Circular Dependencies

```java
// Bad: Circular dependency (not allowed)
@Verifyica.Test
@DependsOn("test2")
public void test1() { }

@Verifyica.Test
@DependsOn("test1")
public void test2() { }
// This will cause an error
```

### Keep Dependency Chains Short

```java
// Good: Short chain
@DependsOn("setup")

// Less ideal: Long chain
@DependsOn("a")
public void b() { }

@DependsOn("b")
public void c() { }

@DependsOn("c")
public void d() { }
// Hard to understand and maintain
```

## See Also

- [Ordering](../ordering/) - @Order for controlling execution sequence
- [Lifecycle](../../core-concepts/lifecycle/) - Test lifecycle phases
