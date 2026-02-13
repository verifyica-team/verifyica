---
title: "Test Ordering"
linkTitle: "Ordering"
weight: 3
description: >
  Control test method execution order with @Order
---

The `@Order` annotation allows you to specify the execution order of test methods within an argument.

## Overview

By default, test methods execute in an undefined order. Use `@Order` when tests must run in a specific sequence.

## Basic Usage

```java
import org.verifyica.api.Order;
import org.verifyica.api.Verifyica;

public class OrderedTest {

    @Verifyica.ArgumentSupplier
    public static Object arguments() {
        return List.of("test-data");
    }

    @Verifyica.Test
    @Order(1)
    public void firstTest(String argument) {
        System.out.println("Runs first");
    }

    @Verifyica.Test
    @Order(2)
    public void secondTest(String argument) {
        System.out.println("Runs second");
    }

    @Verifyica.Test
    @Order(3)
    public void thirdTest(String argument) {
        System.out.println("Runs third");
    }
}
```

## Order Values

- **Lower values execute first** (e.g., `@Order(1)` before `@Order(2)`)
- Methods without `@Order` have default order value `Integer.MAX_VALUE`
- Methods with the same order value execute in undefined order

## Example: Setup and Teardown Sequence

```java
public class DatabaseMigrationTest {

    @Verifyica.ArgumentSupplier
    public static Object arguments() {
        return List.of(Argument.of("production-db", new DbConfig()));
    }

    @Verifyica.Test
    @Order(1)
    public void createTables(ArgumentContext argumentContext) {
        // Must run first
        database.createTables();
    }

    @Verifyica.Test
    @Order(2)
    public void insertData(ArgumentContext argumentContext) {
        // Runs after tables are created
        database.insertTestData();
    }

    @Verifyica.Test
    @Order(3)
    public void verifyData(ArgumentContext argumentContext) {
        // Runs after data is inserted
        assert database.countRows() > 0;
    }
}
```

## Combining with @DependsOn

You can use both `@Order` and `@DependsOn` together:

```java
@Verifyica.Test
@Order(1)
public void setupTest() {
    // Runs first
}

@Verifyica.Test
@Order(2)
@DependsOn("setupTest")
public void mainTest() {
    // Runs second, and only if setupTest passes
}
```

## Best Practices

### Use Order Sparingly

```java
// Good: Use only when necessary
@Verifyica.Test
@Order(1)
public void initialize() { }

// Bad: Don't order everything
@Verifyica.Test
@Order(1)
public void test1() { }

@Verifyica.Test
@Order(2)
public void test2() { }

@Verifyica.Test
@Order(3)
public void test3() { }
```

### Prefer Test Independence

Independent tests are better than ordered tests:

```java
// Good: Independent tests
@Verifyica.Test
public void testCreate() {
    User user = createUser();
    assert user != null;
}

@Verifyica.Test
public void testUpdate() {
    User user = createUser(); // Creates its own user
    user.setName("Updated");
    assert user.getName().equals("Updated");
}

// Less ideal: Dependent tests
@Verifyica.Test
@Order(1)
public void testCreate() {
    sharedUser = createUser();
}

@Verifyica.Test
@Order(2)
public void testUpdate() {
    sharedUser.setName("Updated"); // Depends on testCreate
}
```

### Leave Gaps in Order Numbers

```java
// Good: Gaps allow inserting tests later
@Order(10)
public void test1() { }

@Order(20)
public void test2() { }

@Order(30)
public void test3() { }

// Now you can add @Order(15) without renumbering
```

## Execution Order with Multiple Arguments

Order applies to each argument independently:

```java
@Verifyica.ArgumentSupplier
public static Object arguments() {
    return List.of("arg1", "arg2");
}

@Verifyica.Test
@Order(1)
public void first(String arg) { }

@Verifyica.Test
@Order(2)
public void second(String arg) { }
```

Execution sequence:
```
arg1: first(arg1) → second(arg1)
arg2: first(arg2) → second(arg2)
```

## See Also

- [Dependencies](../dependencies/) - @DependsOn for conditional execution
- [Lifecycle](../../core-concepts/lifecycle/) - Test lifecycle phases
