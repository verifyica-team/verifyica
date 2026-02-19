---
title: "Contexts"
linkTitle: "Contexts"
weight: 3
description: >
  Understanding EngineContext, ClassContext, and ArgumentContext
---

Verifyica provides three context objects that give you access to test execution state and metadata at different levels.

## Context Hierarchy

```
EngineContext (Engine level)
  └── ClassContext (Test class level)
        └── ArgumentContext (Argument level)
```

Each context provides access to:

- Its parent context
- Configuration and properties
- Test execution metadata
- Lifecycle state

## Context Types

### ArgumentContext

The most commonly used context, providing access to the current argument and test execution state.

#### Interface

```java
public interface ArgumentContext extends Context {
    ClassContext getClassContext();
    int getArgumentIndex();
    Argument<?> getArgument();
    <V> Argument<V> getArgumentAs(Class<V> type);
}
```

#### Usage in Test Methods

Inject `ArgumentContext` as a parameter to any lifecycle method:

```java
@Verifyica.BeforeAll
public void beforeAll(ArgumentContext argumentContext) {
    // Access the Argument object
    Argument<?> argument = argumentContext.getArgument();
    System.out.println("Argument name: " + argument.getName());
    System.out.println("Argument index: " + argumentContext.getArgumentIndex());

    // Access payload if needed
    String value = (String) argument.getPayload();
}

@Verifyica.Test
public void test(ArgumentContext argumentContext) {
    // Access argument metadata
    String name = argumentContext.getArgument().getName();
    int index = argumentContext.getArgumentIndex();

    // Access class context
    ClassContext classContext = argumentContext.getClassContext();
    Class<?> testClass = classContext.getTestClass();
}
```

#### Key Methods

| Method | Description |
|--------|-------------|
| `getArgument()` | Returns the current `Argument<?>` |
| `getArgumentAs(Class<V>)` | Returns the argument cast to specific type |
| `getArgumentIndex()` | Returns the argument's index (0-based) |
| `getClassContext()` | Returns the parent ClassContext |

#### Example: Using Argument Name

```java
@Verifyica.Test
public void test(ArgumentContext argumentContext) {
    String argumentName = argumentContext.getArgument().getName();

    // Use argument name for logging, reporting, or conditional logic
    logger.info("Testing: " + argumentName);

    if (argumentName.contains("production")) {
        // Special handling for production arguments
        enableProductionSafetyChecks();
    }
}
```

#### Example: Conditional Test Execution

```java
@Verifyica.BeforeAll
public void beforeAll(ArgumentContext argumentContext) {
    int index = argumentContext.getArgumentIndex();

    // Only run expensive setup for first argument
    if (index == 0) {
        expensiveGlobalSetup();
    }
}
```

### ClassContext

Provides access to test class-level information and state.

#### Interface

```java
public interface ClassContext extends Context {
    EngineContext getEngineContext();
    Class<?> getTestClass();
    Object getTestInstance();
    // Additional methods...
}
```

#### Usage

```java
@Verifyica.Prepare
public void prepare(ClassContext classContext) {
    Class<?> testClass = classContext.getTestClass();
    System.out.println("Preparing test class: " + testClass.getName());

    // Access engine context
    EngineContext engineContext = classContext.getEngineContext();
}
```

#### Key Methods

| Method | Description |
|--------|-------------|
| `getTestClass()` | Returns the test class (`Class<?>`) |
| `getTestInstance()` | Returns the test instance object |
| `getEngineContext()` | Returns the parent EngineContext |

#### Example: Class-Level State

```java
@Verifyica.Prepare
public void prepare(ClassContext classContext) {
    Class<?> testClass = classContext.getTestClass();

    // Check for class-level annotations
    if (testClass.isAnnotationPresent(RequiresDatabase.class)) {
        databaseManager.startDatabase();
    }

    // Store class-level metadata
    String testClassName = testClass.getSimpleName();
    reportGenerator.startTestClass(testClassName);
}
```

### EngineContext

Provides access to engine-level configuration and global state.

#### Interface

```java
public interface EngineContext extends Context {
    Configuration getConfiguration();
    // Additional methods...
}
```

#### Usage

```java
@Verifyica.Prepare
public void prepare(EngineContext engineContext) {
    Configuration config = engineContext.getConfiguration();

    // Access configuration properties
    String property = config.getProperty("custom.property");
    System.out.println("Property value: " + property);
}
```

#### Example: Global Configuration

```java
@Verifyica.Prepare
public void prepare(EngineContext engineContext) {
    Configuration config = engineContext.getConfiguration();

    // Check if running in CI environment
    boolean isCi = Boolean.parseBoolean(config.getProperty("verifyica.ci.mode", "false"));

    if (isCi) {
        // Adjust test behavior for CI
        timeouts.setMultiplier(2.0);
        logger.setLevel(Level.DEBUG);
    }
}
```

## Context Parameter Injection

Verifyica automatically injects context parameters into lifecycle methods based on their type.

### Supported Parameter Combinations

Based on the lifecycle annotation, different parameter patterns are valid:

#### For @Prepare and @Conclude

These annotations only support **no parameters** OR **ClassContext**:

```java
// No parameters
@Verifyica.Prepare
public void prepare() {
}

// ClassContext only
@Verifyica.Prepare
public void prepare(ClassContext classContext) {
    classContext.getMap().put("key", "value");
}

@Verifyica.Conclude
public void conclude(ClassContext classContext) {
    // Access class-level state
}
```

#### For @BeforeAll, @AfterAll, @BeforeEach, @AfterEach, @Test

These annotations support **unwrapped argument** OR **ArgumentContext**:

```java
// Pattern 1: Unwrapped argument (most common)
@Verifyica.Test
public void test(String argument) {
    // Use the argument directly
}

// Pattern 2: ArgumentContext (when you need metadata)
@Verifyica.Test
public void test(ArgumentContext argumentContext) {
    // Access the Argument object
    Argument<?> argument = argumentContext.getArgument();
    String name = argument.getName();
    Object payload = argument.getPayload();
}
```

**Important Rules:**

- ✅ **@Prepare/@Conclude**: No params OR ClassContext only
- ✅ **@BeforeAll/@AfterAll/@BeforeEach/@AfterEach/@Test**: Unwrapped argument OR ArgumentContext
- ❌ **NEVER** use EngineContext (not supported)
- ❌ **NEVER** mix context with unwrapped argument: `test(ArgumentContext ctx, String arg)`
- ❌ **NEVER** use multiple contexts: `test(ArgumentContext ctx, ClassContext ctx2)`

### @Prepare and @Conclude

These methods execute outside argument processing and only support **no parameters** OR **ClassContext**:

```java
@Verifyica.Prepare
public void prepare() {
    // No parameters
}

@Verifyica.Prepare
public void prepare(ClassContext classContext) {
    // ClassContext only (NOT EngineContext, NOT ArgumentContext)
    classContext.getMap().put("key", "value");
}

@Verifyica.Conclude
public void conclude() {
    // No parameters
}

@Verifyica.Conclude
public void conclude(ClassContext classContext) {
    // ClassContext only
}
```

## Common Patterns

### Conditional Test Execution Based on Argument

```java
@Verifyica.BeforeAll
public void beforeAll(ArgumentContext argumentContext) {
    String argumentName = argumentContext.getArgument().getName();

    if (argumentName.startsWith("skip-")) {
        throw new TestSkippedException("Skipping: " + argumentName);
    }
}
```

### Dynamic Test Configuration

Use a context class to encapsulate per-argument state:

```java
// Define a context class to hold per-argument state
public static class TestContext {
    private final Connection connection;
    private final int port;

    public TestContext(Connection connection, int port) {
        this.connection = connection;
        this.port = port;
    }

    public Connection getConnection() {
        return connection;
    }

    public int getPort() {
        return port;
    }
}

@Verifyica.BeforeAll
public void beforeAll(ArgumentContext argumentContext) {
    int index = argumentContext.getArgumentIndex();

    // Use different ports based on argument index to avoid conflicts
    int port = 5432 + index;

    // Get config from argument payload
    DatabaseConfig config = argumentContext.getArgument().getPayloadAs(DatabaseConfig.class);
    config.setPort(port);

    // Create connection and store in context
    Connection conn = database.connect(config);
    TestContext context = new TestContext(conn, port);

    // Store context in ArgumentContext map
    argumentContext.getMap().put("testContext", context);
}

@Verifyica.Test
public void test(ArgumentContext argumentContext) {
    TestContext context = (TestContext) argumentContext.getMap().get("testContext");
    Connection conn = context.getConnection();
    // Use connection...
}

@Verifyica.AfterAll
public void afterAll(ArgumentContext argumentContext) {
    TestContext context = (TestContext) argumentContext.getMap().get("testContext");
    if (context != null && context.getConnection() != null) {
        context.getConnection().close();
    }
}
```

### Aggregate Reporting

Store results in ClassContext map for aggregation:

```java
@Verifyica.AfterAll
public void afterAll(ArgumentContext argumentContext) {
    String argumentName = argumentContext.getArgument().getName();
    TestResult result = calculateResult();

    // Store result in ClassContext map (thread-safe)
    ClassContext classContext = argumentContext.getClassContext();
    @SuppressWarnings("unchecked")
    Map<String, TestResult> results = (Map<String, TestResult>)
        classContext.getMap().computeIfAbsent("results", k -> new ConcurrentHashMap<>());
    results.put(argumentName, result);
}

@Verifyica.Conclude
public void conclude(ClassContext classContext) {
    // Retrieve results from ClassContext map
    @SuppressWarnings("unchecked")
    Map<String, TestResult> results = (Map<String, TestResult>)
        classContext.getMap().get("results");

    if (results != null) {
        // Generate aggregate report from all argument results
        AggregateReport report = new AggregateReport(results);
        report.save(classContext.getTestClass().getSimpleName() + "-report.html");
    }
}
```

### Accessing Test Method Information

While contexts don't directly provide test method information, you can use interceptors for method-level access. See [Interceptors](../interceptors/).

## Context Best Practices

### Use the Right Context Level

```java
// Good: Use ArgumentContext for argument-specific logic
@Verifyica.Test
public void test(ArgumentContext argumentContext) {
    String name = argumentContext.getArgument().getName();
    logger.info("Testing: " + name);
}

// Bad: Don't pass context if you don't need it
@Verifyica.Test
public void test(ArgumentContext argumentContext) {
    // Don't use argumentContext
    assert config.isValid();
}
```

### Cache Expensive Context Lookups

```java
private String argumentName;

@Verifyica.BeforeAll
public void beforeAll(ArgumentContext argumentContext) {
    // Cache the argument name
    argumentName = argumentContext.getArgument().getName();
}

@Verifyica.Test
public void test(Config config) {
    // Use cached value instead of passing ArgumentContext to every test
    logger.info("Testing: " + argumentName);
}
```

### Don't Modify Context State

Contexts are read-only - don't try to modify them:

```java
// Good: Read from context
@Verifyica.Test
public void test(ArgumentContext argumentContext) {
    int index = argumentContext.getArgumentIndex();
    // Use index...
}

// Bad: Don't try to modify context
@Verifyica.Test
public void test(ArgumentContext argumentContext) {
    // This would throw an exception if it were possible
    // argumentContext.setArgumentIndex(5);
}
```

### Leverage Context Hierarchy

```java
@Verifyica.Test
public void test(ArgumentContext argumentContext) {
    // Navigate the context hierarchy
    ClassContext classContext = argumentContext.getClassContext();
    EngineContext engineContext = classContext.getEngineContext();

    // Access configuration from engine level
    String property = engineContext.getConfiguration().getProperty("key");

    // Access test class from class level
    Class<?> testClass = classContext.getTestClass();

    // Access argument from argument level
    Argument<?> argument = argumentContext.getArgument();
}
```

## Next Steps

- [Interceptors](../interceptors/) - Hook into lifecycle events with full context access
- [Execution Model](../execution-model/) - Understand how contexts flow through execution
- [API Reference → Context API](../../api-reference/context-api/) - Complete context API reference
