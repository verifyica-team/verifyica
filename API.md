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

It's common purpose to get a reference to the associated [Store](src/main/java/org/verifyica/api/Store.java).

## EngineContext

An [EngineContext](api/src/main/java/org/verifyica/api/EngineContext.java) is used to ...

- get the Engine [Configuration](api/src/main/java/org/verifyica/api/Configuration.java)
- get the Engine [Store](src/main/java/org/verifyica/api/Store.java)

## ClassContext

The [ClassContext](api/src/main/java/org/verifyica/api/ClassContext.java) is used to ...

- get the [EngineContext](api/src/main/java/org/verifyica/api/EngineContext.java)
- get information regarding the specific test class
  - test class name
  - test class instance
  - test class argument parallelism
- get the associated test class [Store](src/main/java/org/verifyica/api/Store.java)

## ArgumentContext

The [ArgumentContext](api/src/main/java/org/verifyica/api/ArgumentContext.java) is used to ...

- get the [ClassContext](api/src/main/java/org/verifyica/api/ClassContext.java)
- get the test argument being tested
  - test argument name
  - test argument value
  - test argument index
- get the associated test argument [Store](src/main/java/org/verifyica/api/Store.java)

---

## Store

A [Store](api/src/main/java/org/verifyica/api/Store.java) is used to ...

- store Objects/data that is to be shared
  - between test methods
  - between test classes
  - globally

A Store is thread safe, but allow locking the Store lock for chained atomic operations.

```java
    private static final Key CLASS_CONTEXT_STORE_KEY = Key.of("class.context.key");

    // ... code omitted ...

    @Verifyica.Test
    public void test(ArgumentContext argumentContext) throws Throwable {
        System.out.printf("test(%s)%n", argumentContext.getTestArgument().getPayload());
    
        Store store = argumentContext.getClassContext().getStore();
        store.getLock().lock();
        try {
          store.put(CLASS_CONTEXT_STORE_KEY.append("foo"), "FOO");
          store.put(CLASS_CONTEXT_STORE_KEY.append("bar"), "BAR");
        } finally {
          store.getLock().unlock();
        }
    }
```

**Notes**

- Objects implementing `AutoClosable` in a Store will automatically get closed when the `Store` goes out of scope.

---

## Configuration

[Configuration](api/src/main/java/org/verifyica/api/Configuration.java) has `Properties` to get configuration.

---

## LockManager

[LockManager](api/src/main/java/org/verifyica/api/LockManager.java) provides way to implement locking semantics.

**Notes**

- a lock key should be immutable

---

## Key

[Key](api/src/main/java/org/verifyica/api/Key.java) is a helper to easily build a key used for a [Store](api/src/main/java/org/verifyica/api/Store.java).

**Notes**

- **Objects used to build a key must be immutable**


- a `Key` is immutable, but methods exists to derive new a `Key`
  - `append()`
  - `remove()`
  - `duplicate()`

---

Copyright (C) 2024 The Verifyica project authors