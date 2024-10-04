# Locking

Verifyica does not (by design) have an annotation to perform locking.

Annotations require static values for locking semantics...

- a lock key
- a lock mode
  - i.e. read / write
- a scope
  - i.e. self / self + children

Annotations also don't address scenarios when threads are created/used in test (i.e. async testing.)

## LockManager

[LockManager](api/src/main/java/org/verifyica/api/LockManager.java) provides way to implement locking semantics.

**Notes**

- a lock key should be immutable

### Examples

Example 1:

```java
LockManager.lock("lock.key");
try {
    // ... code omitted ...
} finally {
    LockManager.unlock("lock.key");
}
```

Example 2:

```java
Key lockKey = Key.of("lock", "key");
LockManager.lock(lockKey);
try {
    // ... code omitted ...
} finally {
    LockManager.unlock(lockKey);
}
```

---

Copyright (C) 2024 The Verifyica project authors