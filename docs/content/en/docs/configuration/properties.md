---
title: "Properties"
linkTitle: "Properties"
weight: 1
description: >
  Complete reference for all Verifyica configuration properties
---

This page provides a complete reference for all Verifyica configuration properties.

## Property Format

Properties can be set in three ways:

### 1. Properties File

Create `src/test/resources/verifyica.properties`:

```properties
verifyica.engine.argument.parallelism=4
verifyica.engine.thread.type=virtual
```

### 2. System Properties

Pass as JVM arguments:

```bash
java -Dverifyica.engine.argument.parallelism=4 -jar test.jar
```

### 3. Environment Variables

Set environment variables (replace `.` with `_` and uppercase):

```bash
export VERIFYICA_ENGINE_ARGUMENT_PARALLELISM=4
```

## Parallelism Properties

### verifyica.engine.class.parallelism

Controls how many test classes execute in parallel.

- **Type:** Integer
- **Default:** `1` (sequential)
- **Example:** `verifyica.engine.class.parallelism=4`

**Usage:**
```properties
# Run 4 test classes in parallel
verifyica.engine.class.parallelism=4
```

### verifyica.engine.argument.parallelism

Controls how many arguments within a class execute in parallel.

- **Type:** Integer
- **Default:** `1` (sequential)
- **Example:** `verifyica.engine.argument.parallelism=2`

**Note:** This is a default value. Individual `@ArgumentSupplier` annotations can override this:

```java
@Verifyica.ArgumentSupplier(parallelism = 4)  // Overrides default
public static Collection<String> arguments() {
    return generateArguments();
}
```

**Usage:**
```properties
# Run 2 arguments in parallel by default
verifyica.engine.argument.parallelism=2
```

## Thread Configuration

### verifyica.engine.thread.type

Configures the type of threads used for parallel execution.

- **Type:** String
- **Values:** `virtual`, `platform`, `platform-ephemeral`
- **Default:** `platform`
- **Example:** `verifyica.engine.thread.type=virtual`

**Options:**

| Value | Description | Best For |
|-------|-------------|----------|
| `virtual` | Java 21+ virtual threads | High concurrency, I/O-bound tests |
| `platform` | Platform threads (traditional) | CPU-bound tests, Java 8+ |
| `platform-ephemeral` | Platform threads that are discarded after use | Tests with thread-local state |

**Usage:**
```properties
# Use virtual threads (requires Java 21+)
verifyica.engine.thread.type=virtual

# Use platform threads (default)
verifyica.engine.thread.type=platform

# Use ephemeral platform threads
verifyica.engine.thread.type=platform-ephemeral
```

**Virtual Threads Example:**

Virtual threads are ideal for I/O-bound tests with high concurrency:

```properties
verifyica.engine.thread.type=virtual
verifyica.engine.argument.parallelism=100  # Can handle many more with virtual threads
```

## State Machine Throttling

These properties control how fast state machines transition, useful for debugging.

### verifyica.engine.class.state.machine.throttle

Throttle class-level state machine transitions.

- **Type:** Two integers (min, max) in milliseconds
- **Default:** `0, 1000`
- **Example:** `verifyica.engine.class.state.machine.throttle=100, 500`

### verifyica.engine.argument.state.machine.throttle

Throttle argument-level state machine transitions.

- **Type:** Two integers (min, max) in milliseconds
- **Default:** `0, 1000`
- **Example:** `verifyica.engine.argument.state.machine.throttle=100, 500`

### verifyica.engine.test.state.machine.throttle

Throttle test-level state machine transitions.

- **Type:** Two integers (min, max) in milliseconds
- **Default:** `0, 1000`
- **Example:** `verifyica.engine.test.state.machine.throttle=100, 500`

**Usage:**

Useful for debugging timing issues or simulating slow operations:

```properties
# Add random delay between 0-1000ms to state transitions
verifyica.engine.argument.state.machine.throttle=0, 1000
```

## Logging Properties

### verifyica.engine.logger.level

Sets the logging level for Verifyica's internal logger.

- **Type:** String
- **Values:** `ALL`, `TRACE`, `DEBUG`, `INFO`, `WARN`, `ERROR`, `OFF`
- **Default:** `INFO`
- **Example:** `verifyica.engine.logger.level=DEBUG`

**Usage:**
```properties
# Enable debug logging
verifyica.engine.logger.level=DEBUG

# Disable all logging
verifyica.engine.logger.level=OFF

# Only errors
verifyica.engine.logger.level=ERROR
```

### verifyica.engine.logger.regex

Filter log messages by regex pattern.

- **Type:** Regular expression
- **Default:** (none - all messages logged)
- **Example:** `verifyica.engine.logger.regex=.*TestContainer.*`

**Usage:**
```properties
# Only log messages containing "TestContainer"
verifyica.engine.logger.regex=.*TestContainer.*

# Only log messages from specific package
verifyica.engine.logger.regex=com\\.example\\.tests\\..*
```

## Stack Trace Configuration

### verifyica.engine.prune.stacktraces

Controls whether stack traces are pruned to remove framework internals.

- **Type:** Boolean
- **Default:** `false`
- **Example:** `verifyica.engine.prune.stacktraces=true`

**Usage:**
```properties
# Prune stack traces for cleaner output
verifyica.engine.prune.stacktraces=true
```

**Effect:**

**Without pruning:**
```
at com.example.MyTest.testMethod(MyTest.java:42)
at org.verifyica.engine.internal.TestExecutor.execute(TestExecutor.java:123)
at org.verifyica.engine.internal.ArgumentProcessor.process(ArgumentProcessor.java:456)
at org.verifyica.engine.internal.ClassProcessor.process(ClassProcessor.java:789)
... 50 more framework lines
```

**With pruning:**
```
at com.example.MyTest.testMethod(MyTest.java:42)
```

## Filter Configuration

### verifyica.engine.filter.definitions.filename

Specifies the YAML file containing filter definitions.

- **Type:** String (filename)
- **Default:** `verifyica.engine.filter.definitions.yaml`
- **Example:** `verifyica.engine.filter.definitions.filename=my-filters.yaml`

**Usage:**
```properties
# Use custom filter file
verifyica.engine.filter.definitions.filename=my-filters.yaml
```

See [Filters](../filters/) for complete filter documentation.

## Interceptor Configuration

These properties control which interceptors are automatically discovered and loaded.

### verifyica.engine.autowired.engine.interceptors.exclude.regex

Exclude EngineInterceptors by regex pattern.

- **Type:** Regular expression
- **Default:** (none)
- **Example:** `verifyica.engine.autowired.engine.interceptors.exclude.regex=.*Debug.*`

### verifyica.engine.autowired.engine.interceptors.include.regex

Include only EngineInterceptors matching regex pattern.

- **Type:** Regular expression
- **Default:** (none - all included)
- **Example:** `verifyica.engine.autowired.engine.interceptors.include.regex=com\\.example\\..*`

### verifyica.engine.autowired.class.interceptors.exclude.regex

Exclude ClassInterceptors by regex pattern.

- **Type:** Regular expression
- **Default:** (none)
- **Example:** `verifyica.engine.autowired.class.interceptors.exclude.regex=.*Debug.*`

### verifyica.engine.autowired.class.interceptors.include.regex

Include only ClassInterceptors matching regex pattern.

- **Type:** Regular expression
- **Default:** (none - all included)
- **Example:** `verifyica.engine.autowired.class.interceptors.include.regex=com\\.example\\..*`

**Usage:**
```properties
# Only load interceptors from specific package
verifyica.engine.autowired.class.interceptors.include.regex=com\\.example\\.interceptors\\..*

# Exclude debug interceptors in production
verifyica.engine.autowired.class.interceptors.exclude.regex=.*Debug.*
```

## Maven Plugin Properties

These properties configure the Maven plugin behavior.

### verifyica.maven.plugin.log.tests

Controls whether the Maven plugin logs test execution.

- **Type:** Boolean
- **Default:** `false`
- **Example:** `verifyica.maven.plugin.log.tests=true`

**Usage:**
```properties
# Enable test logging in Maven plugin
verifyica.maven.plugin.log.tests=true
```

### verifyica.maven.plugin.log.timing.units

Sets the time unit for Maven plugin timing logs.

- **Type:** String
- **Values:** `nanoseconds`, `microseconds`, `milliseconds`, `seconds`
- **Default:** `milliseconds`
- **Example:** `verifyica.maven.plugin.log.timing.units=seconds`

**Usage:**
```properties
# Log timing in seconds
verifyica.maven.plugin.log.timing.units=seconds
```

## Complete Configuration Example

Here's a comprehensive `verifyica.properties` file:

```properties
# Parallelism Configuration
verifyica.engine.class.parallelism=4
verifyica.engine.argument.parallelism=2

# Thread Configuration
verifyica.engine.thread.type=virtual

# Logging Configuration
verifyica.engine.logger.level=INFO
verifyica.engine.logger.regex=

# Stack Trace Configuration
verifyica.engine.prune.stacktraces=true

# Filter Configuration
verifyica.engine.filter.definitions.filename=verifyica.engine.filter.definitions.yaml

# Interceptor Configuration
verifyica.engine.autowired.class.interceptors.include.regex=com\\.example\\.interceptors\\..*
verifyica.engine.autowired.class.interceptors.exclude.regex=.*Debug.*

# Maven Plugin Configuration
verifyica.maven.plugin.log.tests=true
verifyica.maven.plugin.log.timing.units=milliseconds
```

## Environment-Specific Configuration

### Development

```properties
verifyica.engine.argument.parallelism=1  # Sequential for easier debugging
verifyica.engine.logger.level=DEBUG
verifyica.engine.prune.stacktraces=false  # Full stack traces
```

### CI/CD

```properties
verifyica.engine.class.parallelism=4
verifyica.engine.argument.parallelism=4
verifyica.engine.thread.type=virtual
verifyica.engine.logger.level=INFO
verifyica.engine.prune.stacktraces=true
```

### Production Integration Tests

```properties
verifyica.engine.argument.parallelism=1  # Sequential for stability
verifyica.engine.logger.level=WARN
verifyica.engine.prune.stacktraces=true
```

## Next Steps

- [Filters](../filters/) - Configure test filtering with YAML
- [Parallelism](../parallelism/) - Deep dive into parallelism configuration
- [Advanced → Parallelism](../../advanced/parallelism/) - Advanced parallel execution patterns
