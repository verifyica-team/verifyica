---
title: "Context API"
linkTitle: "Context API"
weight: 3
description: >
  API reference for EngineContext, ClassContext, and ArgumentContext
---

Context objects provide access to test execution state and metadata.

## ArgumentContext

The most commonly used context, providing access to the current argument.

### Interface

```java
public interface ArgumentContext extends Context {
    ClassContext getClassContext();
    int getArgumentIndex();
    Argument<?> getArgument();
    <V> Argument<V> getArgumentAs(Class<V> type);
    Map<String, Object> getMap();
}
```

### Methods

| Method | Description |
|--------|-------------|
| `getArgument()` | Returns the current Argument<?> |
| `getArgumentAs(Class<V>)` | Returns the argument cast to specific type |
| `getArgumentIndex()` | Returns the argument's index (0-based) |
| `getClassContext()` | Returns the parent ClassContext |
| `getMap()` | Returns a thread-safe map for storing per-argument state |

## ClassContext

Provides access to test class-level information.

### Interface

```java
public interface ClassContext extends Context {
    EngineContext getEngineContext();
    Class<?> getTestClass();
    Object getTestInstance();
    Map<String, Object> getMap();
}
```

### Methods

| Method | Description |
|--------|-------------|
| `getTestClass()` | Returns the test class (Class<?>) |
| `getTestInstance()` | Returns the test instance object |
| `getEngineContext()` | Returns the parent EngineContext |
| `getMap()` | Returns a thread-safe map for storing class-level state |

## EngineContext

Provides access to engine-level configuration.

### Interface

```java
public interface EngineContext extends Context {
    Configuration getConfiguration();
    Map<String, Object> getMap();
}
```

### Methods

| Method | Description |
|--------|-------------|
| `getConfiguration()` | Returns the Configuration object |
| `getMap()` | Returns a thread-safe map for storing engine-level state |

## Context Map Usage

All context objects provide a `getMap()` method that returns a thread-safe map for storing state:

```java
@Verifyica.BeforeAll
public void beforeAll(ArgumentContext argumentContext) {
    TestContext context = new TestContext(/* ... */);
    argumentContext.getMap().put("testContext", context);
}

@Verifyica.Test
public void test(ArgumentContext argumentContext) {
    TestContext context = (TestContext) argumentContext.getMap().get("testContext");
    // Use context...
}
```

## See Also

- [Contexts](../../core-concepts/contexts/) - Understanding and using contexts
- [Lifecycle](../../core-concepts/lifecycle/) - Test lifecycle management
