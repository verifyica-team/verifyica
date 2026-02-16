---
title: "Advanced Topics"
linkTitle: "Advanced"
weight: 5
description: >
  Advanced features and patterns for power users
---

This section covers advanced Verifyica features for sophisticated testing scenarios.

## Topics

- [**Parallelism**](parallelism/) - Advanced parallel execution patterns
- [**Dependencies**](dependencies/) - @DependsOn for test ordering
- [**Ordering**](ordering/) - @Order annotation usage
- [**Tagging**](tagging/) - @Tag for test organization
- [**Cleanup Executor**](cleanup-executor/) - Advanced cleanup patterns
- [**Keyed Concurrency**](keyed-concurrency/) - Concurrency utilities

## When to Use Advanced Features

These features are powerful but add complexity. Use them when:

- Standard lifecycle isn't sufficient
- Tests have complex dependencies
- Need fine-grained execution control
- Managing shared resources across tests

## Best Practices

- Start with simple patterns before adding complexity
- Document why advanced features are needed
- Test advanced patterns thoroughly
- Consider maintainability impact
