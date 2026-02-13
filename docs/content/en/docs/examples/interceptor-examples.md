---
title: "Interceptor Examples"
linkTitle: "Interceptor Examples"
weight: 3
description: >
  Examples of ClassInterceptor usage
---

Examples demonstrating how to use ClassInterceptor to hook into test lifecycle events.

## ClassInterceptor Example

Interceptors allow you to inject custom logic around test lifecycle phases.

### Custom Interceptor Implementation

```java
package org.verifyica.examples.interceptor;

import java.lang.reflect.Method;
import org.verifyica.api.ArgumentContext;
import org.verifyica.api.ClassInterceptor;
import org.verifyica.api.EngineContext;

public class CustomClassInterceptor2 implements ClassInterceptor {

    @Override
    public void initialize(EngineContext engineContext) throws Throwable {
        System.out.println("initialize()");
    }

    @Override
    public void preTest(ArgumentContext argumentContext, Method testMethod) {
        System.out.println("preTest() test class [" +
            argumentContext.getClassContext().getTestClass().getSimpleName() +
            "] test method [" + testMethod.getName() + "]");
    }

    @Override
    public void postTest(ArgumentContext argumentContext, Method testMethod, Throwable throwable)
            throws Throwable {
        System.out.println("postTest() test class [" +
            argumentContext.getClassContext().getTestClass().getSimpleName() +
            "] test method [" + testMethod.getName() + "]");

        rethrow(throwable);
    }

    @Override
    public void destroy(EngineContext engineContext) throws Throwable {
        System.out.println("destroy()");
    }
}
```

### Test Class Using Interceptor

```java
package org.verifyica.examples.interceptor;

import java.util.ArrayList;
import java.util.Collection;
import org.verifyica.api.ClassInterceptor;
import org.verifyica.api.Verifyica;

public class ClassInterceptorTest {

    @Verifyica.ClassInterceptorSupplier
    public static Collection<ClassInterceptor> classInterceptors() {
        Collection<ClassInterceptor> collections = new ArrayList<>();
        collections.add(new CustomClassInterceptor2());
        return collections;
    }

    @Verifyica.ArgumentSupplier
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

## Execution Flow

With the interceptor active, execution for each test method follows:

1. `preTest()` - Interceptor runs before test method
2. Test method executes
3. `postTest()` - Interceptor runs after test method

### Output Example

For a single argument:

```
initialize()
prepare()
beforeAll() argument [string-0]
  beforeEach() argument [string-0]
  preTest() test class [ClassInterceptorTest] test method [test1]
  test1() argument [string-0]
  postTest() test class [ClassInterceptorTest] test method [test1]
  afterEach() argument [string-0]

  beforeEach() argument [string-0]
  preTest() test class [ClassInterceptorTest] test method [test2]
  test2() argument [string-0]
  postTest() test class [ClassInterceptorTest] test method [test2]
  afterEach() argument [string-0]

  beforeEach() argument [string-0]
  preTest() test class [ClassInterceptorTest] test method [test3]
  test3() argument [string-0]
  postTest() test class [ClassInterceptorTest] test method [test3]
  afterEach() argument [string-0]
afterAll() argument [string-0]
conclude()
destroy()
```

## ClassInterceptor Lifecycle Hooks

ClassInterceptor provides hooks for all lifecycle phases:

### Engine-Level Hooks

```java
void initialize(EngineContext engineContext)  // Before any tests
void destroy(EngineContext engineContext)     // After all tests
```

### Class-Level Hooks

```java
void prePrepare(ClassContext classContext, Method method)
void postPrepare(ClassContext classContext, Method method, Throwable throwable)

void preConclude(ClassContext classContext, Method method)
void postConclude(ClassContext classContext, Method method, Throwable throwable)
```

### Argument-Level Hooks

```java
void preBeforeAll(ArgumentContext argumentContext, Method method)
void postBeforeAll(ArgumentContext argumentContext, Method method, Throwable throwable)

void preAfterAll(ArgumentContext argumentContext, Method method)
void postAfterAll(ArgumentContext argumentContext, Method method, Throwable throwable)
```

### Test-Level Hooks

```java
void preBeforeEach(ArgumentContext argumentContext, Method method)
void postBeforeEach(ArgumentContext argumentContext, Method method, Throwable throwable)

void preTest(ArgumentContext argumentContext, Method method)
void postTest(ArgumentContext argumentContext, Method method, Throwable throwable)

void preAfterEach(ArgumentContext argumentContext, Method method)
void postAfterEach(ArgumentContext argumentContext, Method method, Throwable throwable)
```

## Use Cases

**Logging and Monitoring**
- Track test execution timing
- Log test start/end events
- Monitor resource usage

**Resource Management**
- Setup shared resources before tests
- Cleanup resources after tests
- Connection pooling

**Test Context Management**
- Store test metadata in context maps
- Share state across lifecycle phases
- Track test execution state

**Error Handling**
- Capture and log exceptions
- Retry failed tests
- Skip dependent tests on failure

**Metrics Collection**
- Measure test duration
- Count test executions
- Track success/failure rates

## Registration Methods

### Per-Class Registration

Use `@ClassInterceptorSupplier` to register interceptors for a specific test class:

```java
@Verifyica.ClassInterceptorSupplier
public static Collection<ClassInterceptor> classInterceptors() {
    return List.of(new MyInterceptor());
}
```

### Global Registration

Register interceptors globally via ServiceLoader in `META-INF/services/org.verifyica.api.ClassInterceptor`:

```
com.example.MyGlobalInterceptor
com.example.AnotherInterceptor
```

## Best Practices

**Selective Interception**
- Use `predicate()` to filter which classes the interceptor applies to
- Avoid intercepting every test unnecessarily

**Error Propagation**
- Always call `rethrow(throwable)` in `post*` methods to propagate test failures
- Don't swallow exceptions unintentionally

**Minimal Overhead**
- Keep interceptor logic lightweight
- Avoid expensive operations in hot paths

**Thread Safety**
- Interceptors may be called concurrently with parallel execution
- Use thread-safe data structures if maintaining state

## See Also

- [API Reference → Interceptor API](../../api-reference/interceptor-api/) - Complete API reference
- [Core Concepts → Interceptors](../../core-concepts/interceptors/) - Interceptor concepts
