# Concurrency

Verifyica does not (by design) have an annotation to perform locking.

Annotations require...

- a static lock key
- a static lock mode
  - read / write
- a static scope
  - parent / parent + children

## Locking

[Locks](api/src/main/java/org/antublue/verifyica/api/Locks.java) provides a mechanism to create global exclusive locks.

Example 1:

```java
Locks.lock("lock.key");
try {
    // ... code omitted ...
} finally {
    Locks.unlock("lock.key");
}
```

Example 2:

```java
Key lockKey = Key.of("lock", "key");
Locks.lock(lockKey);
try {
    // ... code omitted ...
} finally {
    Locks.unlock(lockKey);
}
```

[Locks](api/src/main/java/org/antublue/verifyica/api/Locks.java) also has convenience methods to allow execution of a `Runnable` or `Callable` using a ...

- lock key
- `Lock`
- `Store` (uses the Store's lock)
- [Context](api/src/main/java/org/antublue/verifyica/api/Context.java)
  - all subclasses
  - uses the Context's `Lock`

**Notes**

- a lock key should be immutable

---

Copyright (C) 2024 The Verifyica project authors