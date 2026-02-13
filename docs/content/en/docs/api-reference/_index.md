---
title: "API Reference"
linkTitle: "API Reference"
weight: 4
description: >
  Complete API reference for Verifyica annotations, interfaces, and utilities
---

This section provides detailed API documentation for all Verifyica components.

## Core APIs

- [**Annotations**](annotations/) - All `@Verifyica.*` annotations
- [**Argument API**](argument-api/) - `Argument<T>` interface and factory methods
- [**Context API**](context-api/) - EngineContext, ClassContext, ArgumentContext
- [**Interceptor API**](interceptor-api/) - ClassInterceptor and EngineInterceptor

## Utility APIs

- [**Utilities**](utilities/) - CleanupExecutor, TemporaryDirectory, RandomUtil
- [**Concurrency**](concurrency/) - KeyedMutexManager, KeyedLatchManager, KeyedSemaphoreManager

## Quick Reference

### Lifecycle Annotations

| Annotation | When It Runs | Receives Argument |
|------------|--------------|-------------------|
| `@Prepare` | Once before all arguments | No |
| `@BeforeAll` | Once per argument, before tests | Yes |
| `@BeforeEach` | Before each test method | Yes |
| `@Test` | Test execution | Yes |
| `@AfterEach` | After each test method | Yes |
| `@AfterAll` | Once per argument, after tests | Yes |
| `@Conclude` | Once after all arguments | No |

### Key Interfaces

| Interface | Purpose |
|-----------|---------|
| `Argument<T>` | Type-safe argument container |
| `ArgumentContext` | Access to current argument execution state |
| `ClassContext` | Access to test class execution state |
| `EngineContext` | Access to engine-level configuration |
| `ClassInterceptor` | Hook into test lifecycle |
| `EngineInterceptor` | Hook into engine lifecycle |

## Javadoc

For complete Javadoc, see the Maven Central repository or generate locally:

```bash
mvn javadoc:javadoc
```

## Next Steps

Browse the API reference sections to learn about specific components.
