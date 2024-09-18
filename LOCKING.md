# Locking

Verifyica does not (by design) have an annotation to perform locking.

Annotations require static values for locking semantics...

- a lock key
- a lock mode
  - i.e. read / write
- a scope
  - i.e. self / self + children

Annotations also don't address scenarios when threads created/used in test.

## LockManager

[LockManager](api/src/main/java/org/antublue/verifyica/api/LockManager.java) provides a mechanism to create global exclusive locks.

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

## Context / Store Locking

All Contexts (and it's associated Store) have a `Lock` that can be used for locking on the object.

### Examples

Example 1:

```java
classContext.getLock().lock();
try {
    // ... code omitted ...
} finally {
        classContext.getLock().unlock();
}
```

Example 2:

```java
classContext.getStore().getLock().lock();
try {
    // ... code omitted ...
} finally {
        classContext.getStore().getLock().unlock();
}
```

**Notes**

- A Context's `getLock()` method is just convenience method to get the associated Store's `Lock`
  - i.e. `classContext.getLock().lock()` and `classContext.getStore().getLock().lock()` reference the same `Lock`

---

Copyright (C) 2024 The Verifyica project authors