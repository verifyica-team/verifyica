---
title: "TestContainers Examples"
linkTitle: "TestContainers Examples"
weight: 4
description: >
  Examples integrating Verifyica with TestContainers
---

Examples demonstrating how to integrate Verifyica with TestContainers for container-based testing.

## Nginx Container Test

This example shows testing multiple Nginx versions in parallel using TestContainers.

### Test Environment Class

The `NginxTestEnvironment` implements `Argument<T>` to provide test data:

```java
package org.verifyica.examples.testcontainers.nginx;

import java.time.Duration;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.NginxContainer;
import org.testcontainers.utility.DockerImageName;
import org.verifyica.api.Argument;

public class NginxTestEnvironment implements Argument<NginxTestEnvironment> {

    private final String dockerImageName;
    private NginxContainer<?> nginxContainer;

    public NginxTestEnvironment(String dockerImageName) {
        this.dockerImageName = dockerImageName;
    }

    @Override
    public String getName() {
        return dockerImageName;
    }

    @Override
    public NginxTestEnvironment getPayload() {
        return this;
    }

    public void initialize(Network network) {
        nginxContainer = new NginxContainer<>(DockerImageName.parse(dockerImageName))
                .withNetwork(network)
                .withStartupTimeout(Duration.ofSeconds(30));

        try {
            nginxContainer.start();
        } catch (Exception e) {
            nginxContainer.stop();
            throw e;
        }
    }

    public boolean isRunning() {
        return nginxContainer.isRunning();
    }

    public NginxContainer<?> getNginxContainer() {
        return nginxContainer;
    }

    public void destroy() {
        if (nginxContainer != null) {
            nginxContainer.stop();
            nginxContainer = null;
        }
    }
}
```

### Test Class

```java
package org.verifyica.examples.testcontainers.nginx;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URLConnection;
import java.util.stream.Stream;
import org.testcontainers.containers.Network;
import org.verifyica.api.Verifyica;
import org.verifyica.api.util.CleanupExecutor;

public class NginxTest {

    private final ThreadLocal<Network> networkThreadLocal = new ThreadLocal<>();

    @Verifyica.ArgumentSupplier(parallelism = Integer.MAX_VALUE)
    public static Stream<NginxTestEnvironment> arguments() throws IOException {
        return Stream.of(
            new NginxTestEnvironment("nginx:1.25"),
            new NginxTestEnvironment("nginx:1.24"),
            new NginxTestEnvironment("nginx:1.23")
        );
    }

    @Verifyica.BeforeAll
    public void initializeTestEnvironment(NginxTestEnvironment nginxTestEnvironment) {
        System.out.println("[" + nginxTestEnvironment.getName() +
            "] initialize test environment ...");

        Network network = Network.newNetwork();
        network.getId();

        networkThreadLocal.set(network);
        nginxTestEnvironment.initialize(network);

        assertThat(nginxTestEnvironment.isRunning()).isTrue();
    }

    @Verifyica.Test
    @Verifyica.Order(1)
    public void testGet(NginxTestEnvironment nginxTestEnvironment) throws Throwable {
        System.out.println("[" + nginxTestEnvironment.getName() +
            "] testing testGet() ...");

        int port = nginxTestEnvironment.getNginxContainer().getMappedPort(80);

        String content = doGet("http://localhost:" + port);

        assertThat(content).contains("Welcome to nginx!");
    }

    @Verifyica.AfterAll
    public void destroyTestEnvironment(NginxTestEnvironment nginxTestEnvironment)
            throws Throwable {
        System.out.println("[" + nginxTestEnvironment.getName() +
            "] destroy test environment ...");

        new CleanupExecutor()
                .addTask(nginxTestEnvironment::destroy)
                .addTaskIfPresent(networkThreadLocal::get, Network::close)
                .addTask(networkThreadLocal::remove)
                .throwIfFailed();
    }

    private static String doGet(String url) throws Throwable {
        StringBuilder result = new StringBuilder();
        URLConnection connection = URI.create(url).toURL().openConnection();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(connection.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
        }

        return result.toString();
    }
}
```

## Key Patterns

### Unlimited Parallelism

```java
@Verifyica.ArgumentSupplier(parallelism = Integer.MAX_VALUE)
```

This allows all Nginx versions to be tested concurrently, significantly reducing total test time.

### CleanupExecutor for Resource Management

```java
new CleanupExecutor()
        .addTask(nginxTestEnvironment::destroy)
        .addTaskIfPresent(networkThreadLocal::get, Network::close)
        .addTask(networkThreadLocal::remove)
        .throwIfFailed();
```

**Benefits:**

- **LIFO Order:** Resources cleaned up in reverse order (last added, first cleaned)
- **Exception Handling:** Continues cleanup even if one task fails
- **Conditional Cleanup:** `addTaskIfPresent` only adds task if value exists
- **Failure Propagation:** `throwIfFailed()` throws if any cleanup failed

### ThreadLocal for Per-Argument Isolation

```java
private final ThreadLocal<Network> networkThreadLocal = new ThreadLocal<>();
```

Since a single test instance is shared across all parallel arguments, `ThreadLocal` ensures each argument has its own `Network` instance.

### Custom Argument Types

By implementing `Argument<T>`, you can create rich test data objects:

```java
public class NginxTestEnvironment implements Argument<NginxTestEnvironment> {
    @Override
    public String getName() {
        return dockerImageName;  // Display name in test reports
    }

    @Override
    public NginxTestEnvironment getPayload() {
        return this;  // The environment itself is the payload
    }
}
```

## Execution Flow

For 3 Nginx versions with `parallelism = Integer.MAX_VALUE`:

1. All 3 arguments execute concurrently:
   - `initializeTestEnvironment()` starts 3 containers in parallel
   - `testGet()` runs 3 HTTP tests in parallel
   - `destroyTestEnvironment()` cleans up 3 containers in parallel

**Timeline:**
```
Time 0s:  [nginx:1.25, nginx:1.24, nginx:1.23] all start
Time 5s:  [nginx:1.25, nginx:1.24, nginx:1.23] all containers ready
Time 6s:  [nginx:1.25, nginx:1.24, nginx:1.23] all tests complete
Time 8s:  [nginx:1.25, nginx:1.24, nginx:1.23] all cleanup complete
Total: 8 seconds
```

**Sequential Equivalent:**
```
Time 0s:  [nginx:1.25] start
Time 5s:  [nginx:1.25] ready
Time 6s:  [nginx:1.25] test complete
Time 8s:  [nginx:1.25] cleanup complete
Time 8s:  [nginx:1.24] start
Time 13s: [nginx:1.24] ready
Time 14s: [nginx:1.24] test complete
Time 16s: [nginx:1.24] cleanup complete
Time 16s: [nginx:1.23] start
Time 21s: [nginx:1.23] ready
Time 22s: [nginx:1.23] test complete
Time 24s: [nginx:1.23] cleanup complete
Total: 24 seconds
```

**Performance Gain:** 3x faster with parallel execution

## Additional TestContainers Examples

### Kafka Integration

```java
@Verifyica.ArgumentSupplier(parallelism = 2)
public static Stream<KafkaTestEnvironment> arguments() {
    return Stream.of(
        new KafkaTestEnvironment("confluentinc/cp-kafka:7.5.0"),
        new KafkaTestEnvironment("confluentinc/cp-kafka:7.4.0")
    );
}

@Verifyica.BeforeAll
public void setup(KafkaTestEnvironment env) {
    env.start();
}

@Verifyica.Test
public void testProduceConsume(KafkaTestEnvironment env) {
    // Produce and consume messages
}

@Verifyica.AfterAll
public void teardown(KafkaTestEnvironment env) {
    env.stop();
}
```

### MongoDB Integration

```java
@Verifyica.ArgumentSupplier(parallelism = Integer.MAX_VALUE)
public static Stream<MongoTestEnvironment> arguments() {
    return Stream.of(
        new MongoTestEnvironment("mongo:7.0"),
        new MongoTestEnvironment("mongo:6.0"),
        new MongoTestEnvironment("mongo:5.0")
    );
}

@Verifyica.BeforeAll
public void connect(MongoTestEnvironment env) {
    env.startContainer();
    MongoClient client = env.getClient();
    // Initialize test data
}

@Verifyica.Test
public void testQuery(MongoTestEnvironment env) {
    // Run queries
}
```

### PostgreSQL Integration

```java
@Verifyica.ArgumentSupplier(parallelism = 3)
public static Stream<PostgresTestEnvironment> arguments() {
    return Stream.of(
        new PostgresTestEnvironment("postgres:16"),
        new PostgresTestEnvironment("postgres:15"),
        new PostgresTestEnvironment("postgres:14")
    );
}

@Verifyica.BeforeAll
public void setupDatabase(PostgresTestEnvironment env) {
    env.start();
    env.executeSql("CREATE TABLE users (id SERIAL PRIMARY KEY, name TEXT)");
}

@Verifyica.Test
public void testInsertSelect(PostgresTestEnvironment env) {
    // Test database operations
}
```

## Best Practices

### Resource Isolation

Always use `ThreadLocal` or context classes for per-argument resources:

```java
// UNSAFE: Shared across all arguments
private Network network;  // Race condition!

// SAFE: Isolated per argument thread
private final ThreadLocal<Network> networkThreadLocal = new ThreadLocal<>();
```

### Cleanup Ordering

Use `CleanupExecutor` to ensure proper cleanup order:

```java
new CleanupExecutor()
        .addTask(container::stop)      // Clean up container first
        .addTask(network::close)        // Then network
        .addTask(tempDir::delete)       // Finally temp files
        .throwIfFailed();
```

### Error Handling

Always stop containers on startup failure:

```java
try {
    container.start();
} catch (Exception e) {
    container.stop();  // Prevent resource leak
    throw e;
}
```

### Startup Timeouts

Set appropriate timeouts for container startup:

```java
new NginxContainer<>(dockerImageName)
        .withStartupTimeout(Duration.ofSeconds(30))
```

## See Also

- [API Reference → Utilities](../../api-reference/utilities/) - CleanupExecutor API
- [Advanced → Parallelism](../../advanced/parallelism/) - Advanced parallel patterns
- [Integrations → TestContainers](../../integrations/testcontainers/) - TestContainers integration guide
- [Core Concepts → Arguments](../../core-concepts/arguments/) - Custom argument types
