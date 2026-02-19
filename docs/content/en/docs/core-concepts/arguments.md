---
title: "Arguments"
linkTitle: "Arguments"
weight: 1
description: >
  Understanding the Argument<T> interface and argument suppliers
---

Arguments are the foundation of Verifyica testing. They define the test data that your tests will execute against.

## The Argument&lt;T&gt; Interface

The `Argument<T>` interface is a type-safe container that associates a name with a payload:

```java
public interface Argument<T> {
    String getName();         // Display name for test reporting
    T getPayload();          // The actual test data
    boolean hasPayload();    // Check if payload is non-null
    <V> V getPayloadAs(Class<V> type); // Cast payload to specific type
}
```

### Why Use Argument&lt;T&gt;?

While you can use simple types like `String` or `Integer` directly, `Argument<T>` provides:

1. **Named arguments** - Better test reporting with descriptive names
2. **Type safety** - Compile-time type checking
3. **Complex payloads** - Wrap any type of test data
4. **Better debugging** - Clear argument identification in logs and reports

### Creating Arguments

Use the static factory methods:

#### Generic Arguments

```java
Argument<String> arg = Argument.of("test-name", "payload-value");
Argument<Config> config = Argument.of("prod-config", new DatabaseConfig());
```

#### Primitive Type Arguments

Verifyica provides convenience methods for primitives:

```java
Argument<Boolean> bool = Argument.ofBoolean(true);        // "true"
Argument<Integer> num = Argument.ofInt(42);               // "42"
Argument<Long> lng = Argument.ofLong(100L);               // "100"
Argument<Double> dbl = Argument.ofDouble(3.14);           // "3.14"
Argument<String> str = Argument.ofString("hello");        // "hello"
```

#### Special Handling

String arguments have special null/empty handling:

```java
Argument.ofString(null);      // Name: "String=/null/", Payload: null
Argument.ofString("");        // Name: "String=/empty/", Payload: ""
Argument.ofString("value");   // Name: "value", Payload: "value"
```

#### BigInteger and BigDecimal

```java
Argument<BigInteger> big = Argument.ofBigInteger("12345678901234567890");
Argument<BigDecimal> dec = Argument.ofBigDecimal("3.141592653589793");
```

## Argument Suppliers

The `@ArgumentSupplier` method provides arguments to your tests.

### Basic Structure

```java
@Verifyica.ArgumentSupplier
public static Object arguments() {
    return /* Collection, array, Stream, or Argument<T> instances */;
}
```

### Requirements

- Must be `static`
- Must be `public`
- Return type must be one of:
  - `Collection<?>`
  - Array (e.g., `String[]`, `Object[]`)
  - `Stream<?>`
  - Single or multiple `Argument<T>` objects

### Return Type Examples

#### Collection (Most Common)

```java
@Verifyica.ArgumentSupplier
public static Collection<String> arguments() {
    return Arrays.asList("test1", "test2", "test3");
}
```

#### Argument&lt;T&gt; Collection

```java
@Verifyica.ArgumentSupplier
public static Collection<Argument<DatabaseConfig>> arguments() {
    return Arrays.asList(
        Argument.of("h2-memory", new DatabaseConfig("jdbc:h2:mem:test")),
        Argument.of("postgresql", new DatabaseConfig("jdbc:postgresql://localhost/test")),
        Argument.of("mysql", new DatabaseConfig("jdbc:mysql://localhost/test"))
    );
}
```

#### Array

```java
@Verifyica.ArgumentSupplier
public static String[] arguments() {
    return new String[] {"arg1", "arg2", "arg3"};
}
```

#### Stream (For Large Datasets)

```java
@Verifyica.ArgumentSupplier
public static Stream<Integer> arguments() {
    return IntStream.range(0, 1000).boxed();
}
```

Streams are useful for:

- Large datasets that don't fit in memory
- Lazy evaluation of arguments
- Dynamic argument generation

### Parallelism Configuration

Control how many arguments execute concurrently:

```java
@Verifyica.ArgumentSupplier(parallelism = 4)
public static Collection<String> arguments() {
    return generateArguments();
}
```

- `parallelism = 1` (default) - Sequential execution
- `parallelism = 2+` - Number of arguments executing in parallel
- `parallelism = 0` - Uses configured default parallelism

See [Configuration → Parallelism](../../configuration/parallelism/) for more details.

## Using Arguments in Tests

### Direct Type Usage

When your argument supplier returns simple types:

```java
@Verifyica.ArgumentSupplier
public static Collection<String> arguments() {
    return Arrays.asList("test1", "test2");
}

@Verifyica.Test
public void test(String argument) {
    // argument is directly the String value
    System.out.println("Testing: " + argument);
}
```

### Argument&lt;T&gt; Usage

When using `Argument<T>`:

```java
@Verifyica.ArgumentSupplier
public static Collection<Argument<Config>> arguments() {
    return Arrays.asList(
        Argument.of("dev", new Config("dev")),
        Argument.of("prod", new Config("prod"))
    );
}

@Verifyica.Test
public void test(Config config) {
    // config is the unwrapped payload
    System.out.println("Testing config: " + config.getName());
}
```

Verifyica automatically unwraps `Argument<T>` to provide the payload to your test methods.

### Accessing Argument Name

To access the argument name in your test, use the `ArgumentContext`:

```java
@Verifyica.Test
public void test(ArgumentContext argumentContext) {
    String name = argumentContext.getTestArgument().getName();
    System.out.println("Argument name: " + name);
    System.out.println("Config: " + config);
}
```

## Complex Argument Patterns

### Multiple Argument Types

Create a wrapper class for multiple parameters:

```java
public class TestData {

    private final String name;
    private final int port;
    private final boolean ssl;

    public TestData(String name, int port, boolean ssl) {
        this.name = name;
        this.port = port;
        this.ssl = ssl;
    }

    // Getters...
}

@Verifyica.ArgumentSupplier
public static Collection<Argument<TestData>> arguments() {
    return Arrays.asList(
        Argument.of("http-local", new TestData("localhost", 8080, false)),
        Argument.of("https-local", new TestData("localhost", 8443, true)),
        Argument.of("http-remote", new TestData("remote.com", 80, false))
    );
}

@Verifyica.Test
public void test(TestData data) {
    Connection conn = new Connection(data.getName(), data.getPort(), data.isSsl());
    // Test logic...
}
```

### Resource-Based Arguments

Create arguments that manage their own resources:

```java
public class DatabaseArgument implements AutoCloseable {
    private final String name;
    private final Connection connection;

    public DatabaseArgument(String url) {
        this.name = extractName(url);
        this.connection = DriverManager.getConnection(url);
    }

    public Connection getConnection() {
        return connection;
    }

    @Override
    public void close() throws Exception {
        connection.close();
    }
}

@Verifyica.ArgumentSupplier
public static Collection<Argument<DatabaseArgument>> arguments() {
    return Arrays.asList(
        Argument.of("h2", new DatabaseArgument("jdbc:h2:mem:test")),
        Argument.of("postgres", new DatabaseArgument("jdbc:postgresql://localhost/test"))
    );
}

@Verifyica.AfterAll
public void afterAll(DatabaseArgument db) throws Exception {
    db.close(); // Clean up per-argument resources
}
```

### Dynamic Argument Generation

Generate arguments based on configuration files, environment variables, or external sources:

```java
@Verifyica.ArgumentSupplier
public static Collection<Argument<ServerConfig>> arguments() throws IOException {
    List<Argument<ServerConfig>> args = new ArrayList<>();

    // Load from file
    Path configFile = Paths.get("test-configs.json");
    if (Files.exists(configFile)) {
        List<ServerConfig> configs = loadConfigsFromFile(configFile);
        for (ServerConfig config : configs) {
            args.add(Argument.of(config.getName(), config));
        }
    }

    // Add environment-specific configs
    String env = System.getenv("TEST_ENV");
    if ("full".equals(env)) {
        args.addAll(loadFullTestSuite());
    }

    return args;
}
```

### Conditional Arguments

Filter arguments based on runtime conditions:

```java
@Verifyica.ArgumentSupplier
public static Collection<Argument<Database>> arguments() {
    List<Argument<Database>> all = Arrays.asList(
        Argument.of("h2", Database.H2),
        Argument.of("postgres", Database.POSTGRES),
        Argument.of("mysql", Database.MYSQL),
        Argument.of("oracle", Database.ORACLE)
    );

    // Filter based on available drivers
    return all.stream()
        .filter(arg -> isDriverAvailable(arg.getPayload()))
        .collect(Collectors.toList());
}

private static boolean isDriverAvailable(Database db) {
    try {
        Class.forName(db.getDriverClass());
        return true;
    } catch (ClassNotFoundException e) {
        return false;
    }
}
```

## Best Practices

### Use Meaningful Names

```java
// Good: Descriptive names
Argument.of("prod-database-ssl-enabled", config)
Argument.of("dev-database-no-ssl", config)

// Bad: Generic names
Argument.of("config1", config)
Argument.of("config2", config)
```

### Keep Arguments Immutable

Arguments should be immutable to avoid side effects between tests:

```java
// Good: Immutable argument
public class Config {

    private final String url;
    private final int timeout;

    public Config(String url, int timeout) {
        this.url = url;
        this.timeout = timeout;
    }

    // Only getters, no setters
}

// Bad: Mutable argument
public class Config {

    private String url;
    private int timeout;

    // Setters allow modification
    public void setUrl(String url) { this.url = url; }
}
```

### Validate Arguments Early

Validate in the argument supplier to fail fast:

```java
@Verifyica.ArgumentSupplier
public static Collection<Argument<Config>> arguments() {
    List<Config> configs = loadConfigs();

    // Validate all configs before returning
    for (Config config : configs) {
        if (config.getUrl() == null) {
            throw new IllegalStateException("Config URL cannot be null");
        }
    }

    return configs.stream()
        .map(c -> Argument.of(c.getName(), c))
        .collect(Collectors.toList());
}
```

### Limit Argument Count

Too many arguments can make tests slow:

```java
// Good: Focused test scope
@Verifyica.ArgumentSupplier
public static Collection<String> arguments() {
    return Arrays.asList("critical-case-1", "critical-case-2", "edge-case");
}

// Bad: Excessive arguments
@Verifyica.ArgumentSupplier
public static Collection<Integer> arguments() {
    return IntStream.range(0, 10000).boxed().collect(Collectors.toList());
}
```

For large datasets, consider:

- Using filters to run subsets
- Splitting into multiple test classes
- Using `parallelism` to speed up execution

## Next Steps

- [Lifecycle](../lifecycle/) - Understand how arguments flow through the test lifecycle
- [Contexts](../contexts/) - Access argument metadata via ArgumentContext
- [Configuration → Parallelism](../../configuration/parallelism/) - Configure parallel argument execution
