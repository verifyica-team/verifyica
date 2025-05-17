# API

Verifyica has various core interfaces / classes / annotations that make up the API.

---

## Annotations

See [ANNOTATIONS](ANNOTATIONS.md) for details.

---

## Argument

The [Argument](api/src/main/java/org/verifyica/api/Argument.java) interface is used to contain test arguments.

Any `ArgumentSupplier` method that returns a non-`Argument` Object will be wrapped as an `Argument`.

- the `Argument` name will be `argument[<POSITIONAL INDEX>]`
- the `Argument` value type will be `Object`

**Notes**

- static `Argument` factory methods create arguments that do NOT implement `equals()`

---

## Context

Interface with common methods for all [Context](api/src/main/java/org/verifyica/api/Context.java) implementations.

It's common purpose to get a reference to an scoped `Map<String, Object>`

## EngineContext

An [EngineContext](api/src/main/java/org/verifyica/api/EngineContext.java) is used to ...

- get the Engine [Configuration](api/src/main/java/org/verifyica/api/Configuration.java)
- get the associated context `Map<String, Object>`

## ClassContext

The [ClassContext](api/src/main/java/org/verifyica/api/ClassContext.java) is used to ...

- get the [EngineContext](api/src/main/java/org/verifyica/api/EngineContext.java)
- get information regarding the specific test class
  - test class name
  - test class instance
  - test class argument parallelism
- get the associated context `Map<String, Object>`

## ArgumentContext

The [ArgumentContext](api/src/main/java/org/verifyica/api/ArgumentContext.java) is used to ...

- get the [ClassContext](api/src/main/java/org/verifyica/api/ClassContext.java)
- get the test argument being tested
  - test argument name
  - test argument value
  - test argument index
- get the associated context `Map<String, Object>`

---

## Configuration

[Configuration](api/src/main/java/org/verifyica/api/Configuration.java) has `Properties` to get configuration.

---

## LockManager

[LockManager](api/src/main/java/org/verifyica/api/LockManager.java) provides way to implement locking semantics.

---

## Trap

[Trap](api/src/main/java/org/verifyica/api/Trap.java) provides a way to run a task, capturing any exceptions that may occur.

- Use to clean up resources

---

## Execution

[Execution](api/src/main/java/org/verifyica/api/Execution.java) provides static methods to throw a `Execution.ExecutionSkippedException` to skip test execution and report the associated method as skipped.

It can only be used in methods annotated with...

- `@Verifyica.Prepare`
- `@Verifyica.BeforeAll`
- `@Verifyica.BeforeEach`
- `@Verifyica.Test`
- `@Verifyica.AfterEach`
- `@Verifyica.AfterAll`
- `@Verifyica.Conclude`

---

Copyright (C) Verifyica project authors and contributors