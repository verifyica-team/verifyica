---
title: "Documentation"
linkTitle: "Documentation"
weight: 10
type: docs
outputs: ["HTML"]
---

Welcome to the Verifyica documentation! Verifyica is a JUnit Platform based TestEngine that provides argument-driven testing with advanced lifecycle management.

## What is Verifyica?

Verifyica extends the JUnit Platform to provide powerful parameterized testing capabilities that go beyond standard approaches. Instead of writing multiple test methods or using simple parameter lists, Verifyica lets you:

- **Write tests once, run them many times** across different arguments
- **Control parallelism** at class, argument, and test method levels
- **Manage complex lifecycles** with Prepare, BeforeAll, BeforeEach, Test, AfterEach, AfterAll, and Conclude phases
- **Define test dependencies** and execution order
- **Hook into execution** with interceptors for cross-cutting concerns

## Documentation Sections

### Getting Started

**New to Verifyica?** Start here to get up and running quickly. Learn how to add Verifyica to your project, write your first test, and understand the basic workflow.

[Explore Getting Started →](getting-started/)

**Key Topics:**
- Installation and setup
- Quick start guide
- Writing your first test
- Basic examples

---

### Core Concepts

**Master the fundamentals** of Verifyica. Understand arguments, lifecycle management, contexts, interceptors, and the execution model that powers the framework.

[Explore Core Concepts →](core-concepts/)

**Key Topics:**
- Arguments and argument suppliers
- Test lifecycle phases
- Context objects (EngineContext, ClassContext, ArgumentContext)
- Interceptors for cross-cutting concerns
- Execution model and parallelism

---

### Configuration

**Configure Verifyica** to match your testing needs. Learn about properties, filters, and parallelism configuration options.

[Explore Configuration →](configuration/)

**Key Topics:**
- verifyica.properties reference
- YAML filters for test selection
- Parallelism configuration
- Thread types and concurrency

---

### API Reference

**Complete API documentation** for all Verifyica annotations, interfaces, and utilities. Reference material for the entire API surface.

[Explore API Reference →](api-reference/)

**Key Topics:**
- @Verifyica annotations
- Argument<T> API
- Context interfaces
- Utility classes and helpers

---

### Advanced Topics

**Go deeper** with advanced patterns and techniques. Learn about test dependencies, ordering, tagging, and advanced parallelism strategies.

[Explore Advanced Topics →](advanced/)

**Key Topics:**
- Test dependencies with @DependsOn
- Execution ordering with @Order
- Test organization with @Tag
- Advanced parallelism patterns

---

### Examples

**See Verifyica in action** with practical examples. From simple sequential tests to complex parallel scenarios and TestContainers integration.

[Explore Examples →](examples/)

**Key Topics:**
- Simple test examples
- Parallel execution patterns
- TestContainers integration
- Real-world scenarios

---

### Troubleshooting

**Solve common issues** and find answers to frequently asked questions. Debug test discovery, parallelism, and integration problems.

[Explore Troubleshooting →](troubleshooting/)

**Key Topics:**
- Common issues and solutions
- FAQ
- Debugging tips
- Performance optimization

---

## Quick Links

### Most Popular Pages

- [Installation](getting-started/installation/) - Add Verifyica to your project
- [Quick Start](getting-started/quick-start/) - Your first test in 5 minutes
- [Lifecycle](core-concepts/lifecycle/) - Understanding test phases
- [Annotations](api-reference/annotations/) - Complete annotation reference
- [Parallelism](configuration/parallelism/) - Configure parallel execution

### Need Help?

- **Questions?** Ask in [GitHub Discussions](https://github.com/verifyica-team/verifyica/discussions)
- **Found a bug?** Report in [GitHub Issues](https://github.com/verifyica-team/verifyica/issues)
- **Contributing?** See the [Contributing Guide](https://github.com/verifyica-team/verifyica/blob/main/CONTRIBUTING.md)
