---
title: "Glossary"
linkTitle: "Glossary"
weight: 100
date: 2026-02-15
lastmod: 2026-02-15
description: >
  Definitions of key Verifyica terminology and concepts
---

## Glossary

### Argument
A test parameter that drives test execution. In Verifyica, tests run multiple times with different arguments. Arguments can be simple types (String, Integer) or complex objects wrapped in `Argument<T>`.

**See Also:** [Arguments](../core-concepts/arguments/)

### Argument Supplier
A static method annotated with `@Verifyica.ArgumentSupplier` that provides test arguments. This method returns a Collection, array, Stream, or `Argument<T>` instances that define what data tests will run against.

**See Also:** [Argument Supplier](../core-concepts/arguments/#argument-suppliers)

### ArgumentContext
A context object that provides access to information about the current argument being executed, including the argument itself, metadata, and a thread-safe map for sharing state within an argument's lifecycle.

**See Also:** [Context API](../api-reference/context-api/)

### ClassInterceptor
An interceptor that hooks into test class lifecycle events, allowing cross-cutting concerns (logging, metrics, resource management) to be applied across all tests in a class.

**See Also:** [Interceptors](../core-concepts/interceptors/)

### EngineInterceptor
An interceptor that hooks into engine-level lifecycle events, enabling global cross-cutting concerns across all test classes.

**See Also:** [Interceptors](../core-concepts/interceptors/)

### Execute (Test Phase)
The `@Verifyica.Test` method execution phase where the actual test logic runs. Each test method executes once per argument.

### Parallelism
The degree to which tests can run concurrently. Verifyica supports parallelism at class level, argument level, and can use virtual threads (Java 21+) or platform threads.

**See Also:** [Parallelism Configuration](../configuration/parallelism/)

### Prepare
A lifecycle phase annotated with `@Verifyica.Prepare` that runs once before any arguments are processed. Used for global test setup.

**See Also:** [Lifecycle](../core-concepts/lifecycle/)

### Conclude
A lifecycle phase annotated with `@Verifyica.Conclude` that runs once after all arguments are processed. Used for global test cleanup.

**See Also:** [Lifecycle](../core-concepts/lifecycle/)

### TestEngine
The JUnit Platform component that discovers and executes Verifyica tests. Verifyica provides its own TestEngine implementation that integrates with the JUnit Platform.

### JUnit Platform
The foundation upon which Verifyica is built. The JUnit Platform provides the infrastructure for discovering and executing tests, and Verifyica implements this platform as a custom TestEngine.

### Type-Safe Argument
An argument wrapped in the `Argument<T>` interface that provides compile-time type safety, a descriptive name, and better test reporting.

**See Also:** [Argument API](../api-reference/argument-api/)

### Verifyica Maven Plugin
The Maven plugin (`verifyica-maven-plugin`) that executes Verifyica tests during the Maven test phase. Required because Maven Surefire doesn't natively support custom TestEngines.

**See Also:** [Installation](../getting-started/installation/)

### EngineContext
A context object providing access to engine-level configuration and information available across all test classes.

**See Also:** [Context API](../api-reference/context-api/)

### ClassContext
A context object providing access to class-level information and configuration shared across all arguments in a test class.

**See Also:** [Context API](../api-reference/context-api/)

### Filter
A YAML-based configuration for including or excluding tests by class name, package, or tags. Filters allow selective test execution.

**See Also:** [Filters](../configuration/filters/)

### Tag
A marker annotation (`@Tag`) that categorizes tests for filtering purposes. Tests can be included or excluded based on tags.

**See Also:** [Tagging](../advanced/tagging/)

### Order
An annotation (`@Order`) that controls the execution order of test methods within a test class. Lower values execute first.

**See Also:** [Ordering](../advanced/ordering/)

### DependsOn
An annotation (`@DependsOn`) that specifies test method dependencies, ensuring tests execute in the correct order and handling failures appropriately.

**See Also:** [Dependencies](../advanced/dependencies/)

### @BeforeAll
A lifecycle annotation that marks methods to run once per argument before its test methods execute. Used for argument-specific setup.

**See Also:** [Lifecycle](../core-concepts/lifecycle/)

### @AfterAll
A lifecycle annotation that marks methods to run once per argument after its test methods execute. Used for argument-specific cleanup.

**See Also:** [Lifecycle](../core-concepts/lifecycle/)

### @BeforeEach
A lifecycle annotation that marks methods to run before each test method for each argument. Used for test-specific setup.

**See Also:** [Lifecycle](../core-concepts/lifecycle/)

### @AfterEach
A lifecycle annotation that marks methods to run after each test method for each argument. Used for test-specific cleanup.

**See Also:** [Lifecycle](../core-concepts/lifecycle/)

### Argument-Driven Testing
A testing paradigm where the same test logic executes multiple times with different input data (arguments). This approach reduces code duplication and improves test coverage.

### Test Lifecycle
The complete set of phases a test goes through, from preparation through execution to cleanup. Verifyica provides a rich lifecycle with Prepare, BeforeAll, BeforeEach, Test, AfterEach, AfterAll, and Conclude phases.

**See Also:** [Lifecycle](../core-concepts/lifecycle/)

### Interceptor Pattern
A design pattern that allows cross-cutting concerns to be applied to test execution by intercepting lifecycle events.

### Virtual Thread
A lightweight thread introduced in Java 21 that Verifyica can use for parallel test execution, enabling high levels of concurrency with minimal resource overhead.

**See Also:** [Thread Configuration](../configuration/properties/)

### Thread Type
The type of threads used for parallel execution: `virtual` (Java 21+ virtual threads), `platform` (traditional threads), or `platform-ephemeral` (disposable platform threads).

**See Also:** [Thread Configuration](../configuration/properties/)

### Test Discovery
The process by which the Verifyica TestEngine finds and registers test classes and methods for execution.

### TestDescriptor
A JUnit Platform concept representing a node in the test tree. Verifyica creates TestDescriptors for test classes, arguments, and test methods.

### CleanupExecutor
A utility class that provides advanced cleanup patterns with support for timeouts and exception handling.

**See Also:** [Utilities](../api-reference/utilities/)

