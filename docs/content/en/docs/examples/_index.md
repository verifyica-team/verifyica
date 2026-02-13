---
title: "Examples"
linkTitle: "Examples"
weight: 7
description: >
  Real-world examples and patterns
---

This section provides practical examples of Verifyica usage patterns.

## Examples

- [**Simple Tests**](simple-tests/) - Basic sequential and parallel tests
- [**Parallel Tests**](parallel-tests/) - Parallelism patterns
- [**Interceptor Examples**](interceptor-examples/) - ClassInterceptor usage
- [**Dependency Examples**](dependency-examples/) - Test dependency chains
- [**TestContainers Examples**](testcontainers-examples/) - Container integration

## Example Code Repository

Find complete, runnable examples in the Verifyica repository:

```bash
git clone https://github.com/verifyica-team/verifyica.git
cd verifyica/examples
```

## Running Examples

### Maven

```bash
cd examples
mvn clean test
```

### IDE

Import the examples module and run test classes directly.

## Example Structure

All examples follow consistent patterns:

1. **Clear argument suppliers** - Show how to provide test data
2. **Lifecycle usage** - Demonstrate setup/teardown
3. **Assertions** - Show test validation
4. **Comments** - Explain key concepts
