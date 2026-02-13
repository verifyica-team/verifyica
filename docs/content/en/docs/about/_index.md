---
title: "About Verifyica"
linkTitle: "About"
weight: 10
---

## What is Verifyica?

Verifyica is a JUnit Platform based TestEngine that provides argument-driven testing with advanced lifecycle management. It enables developers to write cleaner, more maintainable tests by executing the same test logic across multiple test arguments with complete control over setup and teardown.

## Key Features

- **Argument-Driven Testing** - Write tests once, execute across multiple arguments
- **Rich Lifecycle** - Prepare, BeforeAll, BeforeEach, Test, AfterEach, AfterAll, Conclude phases
- **Parallel Execution** - Fine-grained control at class, argument, and test levels
- **Type-Safe Arguments** - `Argument<T>` interface with compile-time type checking
- **Interceptors** - Hook into lifecycle for cross-cutting concerns
- **JUnit Platform Integration** - Works with existing JUnit tooling and IDEs
- **Minimal Dependencies** - Lightweight with no transitive dependencies
- **Java 8+ Compatible** - Works with Java 8 through Java 21+

## Architecture

```mermaid
graph TD;
    subgraph "API Module"
        A[Annotations<br/>@Verifyica.*]
        B[Argument API]
        C[Context API]
        D[Interceptor API]
        E[Utilities]
    end

    subgraph "Engine Module"
        F[VerifyicaTestEngine]
        G[Test Discovery]
        H[Execution Engine]
        I[Configuration]
        J[Filter System]
    end

    subgraph "Maven Plugin"
        K[VerifyicaMavenPlugin]
    end

    A --> F
    B --> F
    C --> F
    D --> F
    E --> F
    F --> G
    G --> H
    F --> I
    F --> J
    K --> F
```

## Project Information

- **GitHub:** https://github.com/verifyica-team/verifyica
- **Maven Central:** https://central.sonatype.com/artifact/org.verifyica/verifyica-api
- **License:** Apache License 2.0
- **Current Version:** 1.0.0

## Module Structure

| Module | Purpose | Artifact ID |
|--------|---------|-------------|
| API | Annotations and interfaces | verifyica-api |
| Engine | TestEngine implementation | verifyica-engine |
| Maven Plugin | Maven integration | verifyica-maven-plugin |
| Tests | Integration tests | verifyica-tests |
| Examples | Example tests | verifyica-examples |

## Why Verifyica?

Traditional parameterized testing frameworks often lack:
- Proper lifecycle management per parameter
- Support for complex argument types
- Fine-grained parallelism control
- Extension and interception points

Verifyica addresses these limitations while maintaining compatibility with the JUnit Platform ecosystem.

## Contributing

Contributions are welcome! See the [GitHub repository](https://github.com/verifyica-team/verifyica) for:
- Filing issues
- Submitting pull requests
- Joining discussions
- Reviewing documentation

## Community

- **Discussions:** https://github.com/verifyica-team/verifyica/discussions
- **Issues:** https://github.com/verifyica-team/verifyica/issues

## Versioning

Verifyica follows semantic versioning (MAJOR.MINOR.PATCH):
- **MAJOR:** Breaking API changes
- **MINOR:** New features, backward compatible
- **PATCH:** Bug fixes, backward compatible
