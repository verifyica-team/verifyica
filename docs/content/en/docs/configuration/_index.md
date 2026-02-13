---
title: "Configuration"
linkTitle: "Configuration"
weight: 3
description: >
  Configure Verifyica for your testing needs
---

Verifyica provides flexible configuration options to control test execution, parallelism, filtering, and behavior.

## Configuration Methods

Verifyica can be configured through:

1. **Properties File** - `verifyica.properties` in the classpath
2. **System Properties** - `-D` flags passed to the JVM
3. **Environment Variables** - Environment variables with `VERIFYICA_` prefix

## Quick Configuration Topics

### Properties

Configure engine behavior, parallelism, logging, and more through properties.

[Learn more about Properties →](properties/)

### Filters

Use YAML-based filters to include or exclude tests by class name, package, or tags.

[Learn more about Filters →](filters/)

### Parallelism

Control how many classes, arguments, and tests execute in parallel.

[Learn more about Parallelism →](parallelism/)

## Configuration Priority

When the same property is defined in multiple places, the priority is:

1. **System Properties** (highest priority)
2. **Environment Variables**
3. **Properties File** (lowest priority)

Example:

```bash
# Properties file: parallelism = 2
# System property overrides it
java -Dverifyica.engine.argument.parallelism=4 -jar test.jar

# Result: parallelism = 4
```

## Configuration File Location

The `verifyica.properties` file should be placed in:

```
src/test/resources/verifyica.properties
```

Example `verifyica.properties`:

```properties
# Class-level parallelism
verifyica.engine.class.parallelism=4

# Argument-level parallelism
verifyica.engine.argument.parallelism=2

# Thread type (virtual, platform, platform-ephemeral)
verifyica.engine.thread.type=virtual

# Filter definitions file
verifyica.engine.filter.definitions.filename=verifyica.engine.filter.definitions.yaml

# Logger configuration
verifyica.engine.logger.level=INFO
```

## Next Steps

- [Properties](properties/) - Complete properties reference
- [Filters](filters/) - Configure test filters
- [Parallelism](parallelism/) - Configure parallel execution
