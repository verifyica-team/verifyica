---
title: "Interceptor API"
linkTitle: "Interceptor API"
weight: 4
description: >
  API reference for ClassInterceptor and EngineInterceptor
---

Interceptors allow hooking into the test lifecycle.

## ClassInterceptor

Intercepts lifecycle events at the class level.

### Interface

```java
public interface ClassInterceptor {
    // Initialization
    void initialize(EngineContext engineContext);
    Predicate<ClassContext> predicate();

    // Lifecycle hooks
    void prePrepare(ClassContext classContext, Method method);
    void postPrepare(ClassContext classContext, Method method, Throwable throwable);

    void preBeforeAll(ArgumentContext argumentContext, Method method);
    void postBeforeAll(ArgumentContext argumentContext, Method method, Throwable throwable);

    void preBeforeEach(ArgumentContext argumentContext, Method method);
    void postBeforeEach(ArgumentContext argumentContext, Method method, Throwable throwable);

    void preTest(ArgumentContext argumentContext, Method method);
    void postTest(ArgumentContext argumentContext, Method method, Throwable throwable);

    void preAfterEach(ArgumentContext argumentContext, Method method);
    void postAfterEach(ArgumentContext argumentContext, Method method, Throwable throwable);

    void preAfterAll(ArgumentContext argumentContext, Method method);
    void postAfterAll(ArgumentContext argumentContext, Method method, Throwable throwable);

    void preConclude(ClassContext classContext, Method method);
    void postConclude(ClassContext classContext, Method method, Throwable throwable);

    void destroy(EngineContext engineContext);
}
```

All methods have default implementations, so you only need to override the ones you need.

### Registration

Register via ServiceLoader in `META-INF/services/org.verifyica.api.ClassInterceptor`:

```
com.example.MyClassInterceptor
```

## EngineInterceptor

Intercepts lifecycle events at the engine level.

### Interface

```java
public interface EngineInterceptor {
    void initialize(EngineContext engineContext);
    void beforeAll(EngineContext engineContext);
    void afterAll(EngineContext engineContext);
    void destroy(EngineContext engineContext);
}
```

### Registration

Register via ServiceLoader in `META-INF/services/org.verifyica.api.EngineInterceptor`:

```
com.example.MyEngineInterceptor
```

## See Also

- [Interceptors](../../core-concepts/interceptors/) - Using interceptors
- [Examples → Interceptor Examples](../../examples/interceptor-examples/) - Example interceptors
