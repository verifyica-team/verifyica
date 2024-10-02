# Interceptors

Verifyica uses a concept of an interceptor to hook into the test engine and test class lifecycle.

---

### EngineInterceptor

An [EngineInterceptor](api/src/main/java/org/verifyica/api/interceptor/engine/EngineInterceptor.java) has callback methods for global events within the test engine.

- optional
- global
- automatically loaded using the `@Verifyica.Autowired` annotation

---

### ClassInterceptor

A [ClassInterceptor](api/src/main/java/org/verifyica/api/interceptor/ClassInterceptor.java) has pre/post test class lifecycle callback methods.

- optional
- may be global
  - automatically loaded using the `@Verifyica.Autowired` annotation
- may be class specific
  - using a static method annotated with the `@Verifyica.ClassInterceptorSupplier` annotation


Examples:

Examples can be found in the `tests` module.

- EngineInterceptor [examples](tests/src/test/java/org/verifyica/test/interceptor/engine)
- ClassInterceptor [examples](tests/src/test/java/org/verifyica/test/interceptor)

A concrete example using a `ClassInterceptor` to test the test engine lifecycle method functionality.

- [LifecycleTest.java](tests/src/test/java/org/verifyica/test/LifecycleTest.java)

---

Copyright (C) 2024 The Verifyica project authors
