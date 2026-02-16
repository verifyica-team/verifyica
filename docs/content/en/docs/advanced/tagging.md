---
title: "Test Tagging"
linkTitle: "Tagging"
weight: 4
description: >
  Organize and filter tests with @Tag
---

The `@Tag` annotation allows you to label tests for filtering and organization.

## Overview

Tags provide a flexible way to categorize tests so you can run specific subsets during different testing scenarios.

## Basic Usage

```java
import org.verifyica.api.Tag;
import org.verifyica.api.Verifyica;

@Tag("integration")
public class IntegrationTest {

    @Verifyica.ArgumentSupplier
    public static Object arguments() {
        return List.of("test-data");
    }

    @Verifyica.Test
    public void testDatabaseConnection(String argument) {
        // Tagged as "integration" from class level
    }
}
```

## Multiple Tags

Apply multiple tags to organize tests by different dimensions:

```java
@Tag("integration")
@Tag("database")
@Tag("slow")
public class DatabaseIntegrationTest {

    @Verifyica.Test
    public void testQuery(String argument) {
        // Has all three tags: integration, database, slow
    }
}
```

## Method-Level Tags

Override or extend class-level tags on individual methods:

```java
@Tag("integration")
public class MixedTest {

    @Verifyica.ArgumentSupplier
    public static Object arguments() {
        return List.of("test-data");
    }

    @Verifyica.Test
    @Tag("fast")
    public void quickTest(String argument) {
        // Tags: integration, fast
    }

    @Verifyica.Test
    @Tag("slow")
    public void slowTest(String argument) {
        // Tags: integration, slow
    }
}
```

## Common Tag Categories

### By Test Type

```java
@Tag("unit")        // Unit tests
@Tag("integration") // Integration tests
@Tag("e2e")         // End-to-end tests
@Tag("smoke")       // Smoke tests
```

### By Speed

```java
@Tag("fast")        // Quick tests
@Tag("slow")        // Slow tests
@Tag("overnight")   // Very long tests
```

### By Component

```java
@Tag("database")
@Tag("api")
@Tag("ui")
@Tag("security")
```

### By Environment

```java
@Tag("dev")
@Tag("staging")
@Tag("production")
```

## Example: Organizing Test Suites

```java
@Tag("integration")
@Tag("database")
public class DatabaseTest {

    @Verifyica.Test
    @Tag("fast")
    public void testConnection(String argument) {
        // Fast database connection test
    }

    @Verifyica.Test
    @Tag("slow")
    public void testComplexQuery(String argument) {
        // Slow complex query test
    }
}

@Tag("integration")
@Tag("api")
public class ApiTest {

    @Verifyica.Test
    @Tag("fast")
    public void testEndpoint(String argument) {
        // Fast API endpoint test
    }
}
```

## Filtering Tests by Tags

Configure filters in `verifyica.yaml`:

```yaml
# Run only fast integration tests
filters:
  include:
    tags:
      - integration
      - fast
```

```yaml
# Run all tests except slow ones
filters:
  exclude:
    tags:
      - slow
```

```yaml
# Run database tests but not overnight ones
filters:
  include:
    tags:
      - database
  exclude:
    tags:
      - overnight
```

See [Configuration → Filters](../../configuration/filters/) for complete filtering options.

## Tag Naming Conventions

### Use Lowercase

```java
// Good
@Tag("integration")
@Tag("database")

// Less ideal
@Tag("Integration")
@Tag("DATABASE")
```

### Use Descriptive Names

```java
// Good
@Tag("requires-network")
@Tag("writes-to-filesystem")

// Less ideal
@Tag("rn")
@Tag("fs")
```

### Avoid Spaces

```java
// Good
@Tag("end-to-end")
@Tag("smoke-test")

// Bad
@Tag("end to end")
@Tag("smoke test")
```

## Example: CI/CD Pipeline Tags

```java
@Tag("pr-check")
public class PullRequestTest {
    // Runs on every pull request
}

@Tag("nightly")
public class ComprehensiveTest {
    // Runs once per night
}

@Tag("release")
public class ReleaseValidationTest {
    // Runs before releases
}
```

Configure CI to run different tag sets:

```bash
# PR builds: Run fast smoke tests
mvn test -Dverifyica.filter.include.tags=pr-check,fast

# Nightly builds: Run comprehensive suite
mvn test -Dverifyica.filter.include.tags=nightly

# Release builds: Run release validation
mvn test -Dverifyica.filter.include.tags=release
```

## Best Practices

### Tag Strategically

```java
// Good: Meaningful categorization
@Tag("integration")
@Tag("database")
@Tag("slow")

// Less useful: Over-tagging
@Tag("test")
@Tag("java")
@Tag("class")
```

### Consistent Naming

Establish team conventions:

```java
// Good: Consistent naming
@Tag("integration")
@Tag("integration-api")
@Tag("integration-database")

// Inconsistent
@Tag("integration")
@Tag("apiIntegration")
@Tag("db_integration")
```

### Document Tag Meanings

Create a tag catalog in your project README:

```markdown
## Test Tags

- `fast` - Tests that run in < 1 second
- `slow` - Tests that take > 10 seconds
- `integration` - Tests requiring external systems
- `database` - Tests requiring database
- `pr-check` - Must pass before merging PRs
```

## See Also

- [Configuration → Filters](../../configuration/filters/) - Configure tag-based filtering
- [Examples](../../examples/) - See tagging in action
