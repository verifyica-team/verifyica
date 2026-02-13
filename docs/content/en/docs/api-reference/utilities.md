---
title: "Utilities"
linkTitle: "Utilities"
weight: 5
description: >
  Utility classes for common testing tasks
---

Verifyica provides utility classes for common testing scenarios.

## CleanupExecutor

Manages cleanup tasks in reverse order (LIFO).

```java
CleanupExecutor cleanupExecutor = new CleanupExecutor();

cleanupExecutor.add(() -> resource1.close());
cleanupExecutor.add(() -> resource2.close());

// Executes resource2.close() then resource1.close()
cleanupExecutor.execute();
```

See [Advanced → Cleanup Executor](../../advanced/cleanup-executor/) for detailed usage.

## TemporaryDirectory

Creates temporary directories for tests.

```java
TemporaryDirectory tempDir = TemporaryDirectory.create();
Path path = tempDir.getPath();

// Use directory...

tempDir.close(); // Deletes directory and contents
```

## RandomUtil

Utilities for random values in tests.

```java
int randomInt = RandomUtil.nextInt(100);
String randomString = RandomUtil.nextString(10);
```

## See Also

- [Advanced → Cleanup Executor](../../advanced/cleanup-executor/) - Detailed CleanupExecutor guide
- [Core Concepts](../../core-concepts/) - Core testing concepts
