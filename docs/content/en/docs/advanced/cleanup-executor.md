---
title: "Cleanup Executor"
linkTitle: "Cleanup Executor"
weight: 5
description: >
  Advanced cleanup patterns with CleanupExecutor
---

The `CleanupExecutor` utility helps manage cleanup tasks that must execute in reverse order, even when exceptions occur.

## Overview

`CleanupExecutor` is a specialized utility that:

- Executes cleanup tasks in reverse order (LIFO - Last In, First Out)
- Continues executing all cleanup tasks even if some fail
- Collects all exceptions that occur during cleanup

## Basic Usage

```java
import org.verifyica.api.CleanupExecutor;

public class CleanupTest {

    @Verifyica.BeforeAll
    public void beforeAll(ArgumentContext argumentContext) {
        CleanupExecutor cleanupExecutor = new CleanupExecutor();

        try {
            // Setup resources and register cleanup
            Connection conn = database.connect();
            cleanupExecutor.add(() -> conn.close());

            Server server = new Server();
            server.start();
            cleanupExecutor.add(() -> server.stop());

            // Store cleanup executor in context
            argumentContext.getMap().put("cleanup", cleanupExecutor);

        } catch (Exception e) {
            // If setup fails, execute cleanup
            cleanupExecutor.execute();
            throw e;
        }
    }

    @Verifyica.AfterAll
    public void afterAll(ArgumentContext argumentContext) throws Throwable {
        CleanupExecutor cleanupExecutor =
            (CleanupExecutor) argumentContext.getMap().get("cleanup");

        if (cleanupExecutor != null) {
            cleanupExecutor.execute();
        }
    }
}
```

## Example: Nested Resource Setup

```java
public class NestedResourceTest {

    public static class TestContext {
        private final CleanupExecutor cleanupExecutor;

        public TestContext(CleanupExecutor cleanupExecutor) {
            this.cleanupExecutor = cleanupExecutor;
        }

        public CleanupExecutor getCleanupExecutor() {
            return cleanupExecutor;
        }
    }

    @Verifyica.BeforeAll
    public void beforeAll(ArgumentContext argumentContext) {
        CleanupExecutor cleanupExecutor = new CleanupExecutor();

        try {
            // 1. Create database
            Database db = Database.create("test-db");
            cleanupExecutor.add(() -> db.drop());

            // 2. Connect to database
            Connection conn = db.connect();
            cleanupExecutor.add(() -> conn.close());

            // 3. Create schema
            conn.execute("CREATE TABLE users (id INT, name VARCHAR(100))");
            cleanupExecutor.add(() -> conn.execute("DROP TABLE users"));

            // 4. Insert test data
            conn.execute("INSERT INTO users VALUES (1, 'Alice')");
            cleanupExecutor.add(() -> conn.execute("DELETE FROM users"));

            TestContext context = new TestContext(cleanupExecutor);
            argumentContext.getMap().put("testContext", context);

        } catch (Exception e) {
            cleanupExecutor.execute();
            throw new RuntimeException("Setup failed", e);
        }
    }

    @Verifyica.Test
    public void test(ArgumentContext argumentContext) {
        // Test logic
    }

    @Verifyica.AfterAll
    public void afterAll(ArgumentContext argumentContext) throws Throwable {
        TestContext context = (TestContext) argumentContext.getMap().get("testContext");
        if (context != null && context.getCleanupExecutor() != null) {
            // Executes in reverse order:
            // 4. DELETE FROM users
            // 3. DROP TABLE users
            // 2. conn.close()
            // 1. db.drop()
            context.getCleanupExecutor().execute();
        }
    }
}
```

## Error Handling

CleanupExecutor collects all exceptions:

```java
@Verifyica.AfterAll
public void afterAll(ArgumentContext argumentContext) {
    CleanupExecutor cleanupExecutor =
        (CleanupExecutor) argumentContext.getMap().get("cleanup");

    if (cleanupExecutor != null) {
        try {
            cleanupExecutor.execute();
        } catch (Throwable t) {
            // t contains all cleanup exceptions
            logger.error("Cleanup failed", t);
            throw new RuntimeException("Cleanup failed", t);
        }
    }
}
```

## TestContainers Integration

Ideal for managing container lifecycles:

```java
public class ContainerTest {

    @Verifyica.BeforeAll
    public void beforeAll(ArgumentContext argumentContext) {
        CleanupExecutor cleanupExecutor = new CleanupExecutor();

        try {
            // Start database container
            GenericContainer<?> dbContainer = new GenericContainer<>("postgres:15")
                .withExposedPorts(5432);
            dbContainer.start();
            cleanupExecutor.add(() -> dbContainer.stop());

            // Start application container
            GenericContainer<?> appContainer = new GenericContainer<>("myapp:latest")
                .withExposedPorts(8080)
                .withEnv("DB_HOST", dbContainer.getHost());
            appContainer.start();
            cleanupExecutor.add(() -> appContainer.stop());

            argumentContext.getMap().put("cleanup", cleanupExecutor);

        } catch (Exception e) {
            cleanupExecutor.execute();
            throw e;
        }
    }

    @Verifyica.AfterAll
    public void afterAll(ArgumentContext argumentContext) throws Throwable {
        CleanupExecutor cleanupExecutor =
            (CleanupExecutor) argumentContext.getMap().get("cleanup");

        if (cleanupExecutor != null) {
            // Stops app container first, then db container
            cleanupExecutor.execute();
        }
    }
}
```

## File System Cleanup

```java
public class FileSystemTest {

    @Verifyica.BeforeAll
    public void beforeAll(ArgumentContext argumentContext) {
        CleanupExecutor cleanupExecutor = new CleanupExecutor();

        try {
            // Create temp directory
            Path tempDir = Files.createTempDirectory("test");
            cleanupExecutor.add(() -> deleteDirectory(tempDir));

            // Create test files
            Path file1 = tempDir.resolve("file1.txt");
            Files.write(file1, "content".getBytes());
            cleanupExecutor.add(() -> Files.deleteIfExists(file1));

            Path file2 = tempDir.resolve("file2.txt");
            Files.write(file2, "content".getBytes());
            cleanupExecutor.add(() -> Files.deleteIfExists(file2));

            argumentContext.getMap().put("cleanup", cleanupExecutor);

        } catch (Exception e) {
            cleanupExecutor.execute();
            throw new RuntimeException("Setup failed", e);
        }
    }

    @Verifyica.AfterAll
    public void afterAll(ArgumentContext argumentContext) throws Throwable {
        CleanupExecutor cleanupExecutor =
            (CleanupExecutor) argumentContext.getMap().get("cleanup");

        if (cleanupExecutor != null) {
            // Deletes files first, then directory
            cleanupExecutor.execute();
        }
    }

    private void deleteDirectory(Path dir) throws IOException {
        if (Files.exists(dir)) {
            Files.walk(dir)
                .sorted((a, b) -> b.compareTo(a)) // Reverse order
                .forEach(path -> {
                    try {
                        Files.delete(path);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
        }
    }
}
```

## Best Practices

### Register Cleanup Immediately

```java
// Good: Register cleanup right after resource creation
Resource resource = create();
cleanupExecutor.add(() -> resource.close());

// Less ideal: Register cleanup later
Resource resource = create();
// ... lots of code ...
cleanupExecutor.add(() -> resource.close()); // Easy to forget
```

### Handle Cleanup Exceptions

```java
// Good: Handle and log cleanup exceptions
@Verifyica.AfterAll
public void afterAll(ArgumentContext argumentContext) {
    CleanupExecutor cleanupExecutor =
        (CleanupExecutor) argumentContext.getMap().get("cleanup");

    if (cleanupExecutor != null) {
        try {
            cleanupExecutor.execute();
        } catch (Throwable t) {
            logger.error("Cleanup failed", t);
            // Don't rethrow - cleanup failures shouldn't fail passing tests
        }
    }
}
```

### Cleanup Even on Setup Failure

```java
// Good: Execute cleanup if setup fails
@Verifyica.BeforeAll
public void beforeAll(ArgumentContext argumentContext) {
    CleanupExecutor cleanupExecutor = new CleanupExecutor();

    try {
        // Setup resources
    } catch (Exception e) {
        cleanupExecutor.execute(); // Important!
        throw e;
    }

    argumentContext.getMap().put("cleanup", cleanupExecutor);
}
```

## See Also

- [Utilities](../../api-reference/utilities/) - Other utility classes
- [Lifecycle](../../core-concepts/lifecycle/) - Test lifecycle management
