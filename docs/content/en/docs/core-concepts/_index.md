---
title: "Core Concepts"
linkTitle: "Core Concepts"
weight: 2
description: >
  Understand the fundamental concepts that power Verifyica
---

This section explores the core concepts that make Verifyica a powerful testing framework.

## Key Concepts

### Arguments

At the heart of Verifyica is the concept of **arguments**. Instead of writing separate test methods for different scenarios, you write one test that executes multiple times with different arguments. The [`Argument<T>`](arguments/) interface provides type-safe, named test data containers.

Learn more: [Arguments →](arguments/)

### Lifecycle

Verifyica provides a complete **test lifecycle** that goes far beyond traditional setUp/tearDown patterns. Each argument gets its own lifecycle with Prepare, BeforeAll, BeforeEach, Test, AfterEach, AfterAll, and Conclude phases.

Learn more: [Lifecycle →](lifecycle/)

### Contexts

**Context objects** provide access to test execution state and metadata. Three context types (EngineContext, ClassContext, ArgumentContext) give you control over test execution at different levels.

Learn more: [Contexts →](contexts/)

### Interceptors

**Interceptors** allow you to hook into the test lifecycle for cross-cutting concerns like logging, metrics, or resource management. ClassInterceptor and EngineInterceptor provide flexible interception points.

Learn more: [Interceptors →](interceptors/)

### Execution Model

Understanding how Verifyica **executes tests** is crucial for writing efficient, parallel-friendly tests. The execution model defines how arguments are processed, how parallelism works, and how test methods are invoked.

Learn more: [Execution Model →](execution-model/)

## Why These Concepts Matter

Traditional parameterized testing frameworks often:

- Lack proper lifecycle management per parameter
- Don't support complex argument types well
- Have limited parallelism control
- Provide no interception or extension points

Verifyica addresses all these limitations through its core concepts, enabling you to:

- Write cleaner, more maintainable tests
- Test complex scenarios with minimal code
- Control execution parallelism at multiple levels
- Extend test behavior without modifying test code

## Next Steps

Start with [Arguments](arguments/) to understand how test data flows through Verifyica, then progress through the other concepts to build a complete mental model of the framework.
