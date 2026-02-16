---
title: "Advanced Parallelism"
linkTitle: "Parallelism"
weight: 1
description: >
  Advanced parallel execution patterns and strategies
---

This page covers advanced parallelism patterns beyond the basics. See [Configuration → Parallelism](../../configuration/parallelism/) for basic configuration.

## Resource Pooling with Parallel Arguments

When running arguments in parallel with limited resources, use a pool:

```java
public class PooledResourceTest {

    private static final Semaphore resourcePool = new Semaphore(3); // Max 3 concurrent

    public static class TestContext {
        private final Resource resource;

        public TestContext(Resource resource) {
            this.resource = resource;
        }

        public Resource getResource() {
            return resource;
        }
    }

    @Verifyica.ArgumentSupplier(parallelism = 10)
    public static Object arguments() {
        return IntStream.range(0, 10)
            .mapToObj(i -> "arg-" + i)
            .collect(Collectors.toList());
    }

    @Verifyica.BeforeAll
    public void beforeAll(ArgumentContext argumentContext) throws InterruptedException {
        // Acquire from pool (blocks if pool is full)
        resourcePool.acquire();

        Resource resource = ResourcePool.getInstance().acquire();
        TestContext context = new TestContext(resource);
        argumentContext.getMap().put("testContext", context);
    }

    @Verifyica.Test
    public void test(ArgumentContext argumentContext) {
        TestContext context = (TestContext) argumentContext.getMap().get("testContext");
        context.getResource().use();
    }

    @Verifyica.AfterAll
    public void afterAll(ArgumentContext argumentContext) {
        TestContext context = (TestContext) argumentContext.getMap().get("testContext");
        if (context != null && context.getResource() != null) {
            ResourcePool.getInstance().release(context.getResource());
            resourcePool.release(); // Return to pool
        }
    }
}
```

## Dynamic Port Allocation

Avoid port conflicts when running parallel tests:

```java
public class PortAllocationTest {

    private static final AtomicInteger portCounter = new AtomicInteger(8000);

    public static class TestContext {
        private final int port;
        private final Server server;

        public TestContext(int port, Server server) {
            this.port = port;
            this.server = server;
        }

        public Server getServer() {
            return server;
        }
    }

    @Verifyica.ArgumentSupplier(parallelism = 5)
    public static Object arguments() {
        return List.of("service-1", "service-2", "service-3", "service-4", "service-5");
    }

    @Verifyica.BeforeAll
    public void beforeAll(ArgumentContext argumentContext) {
        // Allocate unique port for this argument
        int port = portCounter.getAndIncrement();

        Server server = new Server(port);
        server.start();

        TestContext context = new TestContext(port, server);
        argumentContext.getMap().put("testContext", context);
    }

    @Verifyica.Test
    public void testService(ArgumentContext argumentContext) {
        TestContext context = (TestContext) argumentContext.getMap().get("testContext");
        HttpClient client = new HttpClient("localhost", context.getServer().getPort());
        Response response = client.get("/health");
        assert response.getStatus() == 200;
    }

    @Verifyica.AfterAll
    public void afterAll(ArgumentContext argumentContext) {
        TestContext context = (TestContext) argumentContext.getMap().get("testContext");
        if (context != null && context.getServer() != null) {
            context.getServer().stop();
        }
    }
}
```

## Partitioned Data Processing

Divide large datasets across parallel arguments:

```java
public class DataPartitionTest {

    @Verifyica.ArgumentSupplier(parallelism = 4)
    public static Object arguments() {
        List<Integer> allData = IntStream.range(0, 1000)
            .boxed()
            .collect(Collectors.toList());

        // Partition into 4 chunks
        int partitionSize = allData.size() / 4;
        List<Argument<List<Integer>>> partitions = new ArrayList<>();

        for (int i = 0; i < 4; i++) {
            int start = i * partitionSize;
            int end = (i == 3) ? allData.size() : (i + 1) * partitionSize;
            List<Integer> partition = allData.subList(start, end);
            partitions.add(Argument.of("partition-" + i, partition));
        }

        return partitions;
    }

    @Verifyica.Test
    public void processPartition(List<Integer> partition) {
        // Each argument processes its partition in parallel
        partition.forEach(this::processItem);
    }

    private void processItem(int item) {
        // Process individual item
    }
}
```

## Coordinating Parallel Tests

Use CountDownLatch to synchronize parallel arguments:

```java
public class CoordinatedTest {

    private static final CountDownLatch readyLatch = new CountDownLatch(3);
    private static final CountDownLatch startLatch = new CountDownLatch(1);

    @Verifyica.ArgumentSupplier(parallelism = 3)
    public static Object arguments() {
        return List.of("client-1", "client-2", "client-3");
    }

    @Verifyica.Test
    public void coordinatedTest(String client) throws InterruptedException {
        // Signal ready
        System.out.println(client + " is ready");
        readyLatch.countDown();

        // Wait for all to be ready
        readyLatch.await();

        // All start together
        System.out.println(client + " starting test");
        performTest(client);
    }
}
```

## Mixing Sequential and Parallel Execution

Run some arguments sequentially, others in parallel:

```java
public class MixedExecutionTest {

    @Verifyica.ArgumentSupplier(parallelism = 1) // Sequential
    public static Object criticalArguments() {
        return List.of(
            Argument.of("production-db", new DbConfig("prod"))
        );
    }

    // In another test class with parallelism
    @Verifyica.ArgumentSupplier(parallelism = 4) // Parallel
    public static Object testArguments() {
        return List.of(
            Argument.of("test-db-1", new DbConfig("test1")),
            Argument.of("test-db-2", new DbConfig("test2")),
            Argument.of("test-db-3", new DbConfig("test3")),
            Argument.of("test-db-4", new DbConfig("test4"))
        );
    }
}
```

## Monitoring Parallel Execution

Track parallel test execution with metrics:

```java
public class MonitoredParallelTest {

    private static final AtomicInteger activeTests = new AtomicInteger(0);
    private static final AtomicInteger completedTests = new AtomicInteger(0);

    @Verifyica.ArgumentSupplier(parallelism = 8)
    public static Object arguments() {
        return IntStream.range(0, 20)
            .mapToObj(i -> "arg-" + i)
            .collect(Collectors.toList());
    }

    @Verifyica.BeforeAll
    public void beforeAll(String argument) {
        int active = activeTests.incrementAndGet();
        System.out.println("Active tests: " + active + " (" + argument + ")");
    }

    @Verifyica.Test
    public void test(String argument) {
        // Test logic
    }

    @Verifyica.AfterAll
    public void afterAll(String argument) {
        activeTests.decrementAndGet();
        int completed = completedTests.incrementAndGet();
        System.out.println("Completed tests: " + completed + " (" + argument + ")");
    }
}
```

## Best Practices

### Choose Appropriate Parallelism

```java
// Good: Match parallelism to resources
@Verifyica.ArgumentSupplier(parallelism = 4) // 4 CPU cores
public static Object arguments() {
    return getCpuBoundTests();
}

// Good: Higher parallelism for I/O bound
@Verifyica.ArgumentSupplier(parallelism = 20) // I/O bound
public static Object arguments() {
    return getNetworkTests();
}
```

### Avoid Excessive Parallelism

```java
// Bad: Too much parallelism
@Verifyica.ArgumentSupplier(parallelism = 100)
public static Object arguments() {
    return List.of("test"); // Only 1 argument!
}
```

### Clean Up Resources

Always clean up in @AfterAll, even with failures:

```java
@Verifyica.AfterAll
public void afterAll(ArgumentContext argumentContext) {
    TestContext context = (TestContext) argumentContext.getMap().get("testContext");
    if (context != null) {
        try {
            context.getResource().close();
        } catch (Exception e) {
            // Log but don't fail cleanup
            logger.warn("Cleanup failed", e);
        }
    }
}
```

## See Also

- [Configuration → Parallelism](../../configuration/parallelism/) - Basic parallelism configuration
- [Execution Model](../../core-concepts/execution-model/) - How parallel execution works
