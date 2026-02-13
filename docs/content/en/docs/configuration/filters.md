---
title: "Filters"
linkTitle: "Filters"
weight: 2
description: >
  Include or exclude tests using YAML-based filters
---

Verifyica provides a powerful YAML-based filtering system to control which tests execute based on class names, packages, tags, and patterns.

## Filter Configuration File

Filters are defined in a YAML file, by default `verifyica.engine.filter.definitions.yaml` in the classpath.

### Location

Place the filter file in:

```
src/test/resources/verifyica.engine.filter.definitions.yaml
```

### Custom Filename

Configure a custom filename via properties:

```properties
verifyica.engine.filter.definitions.filename=my-filters.yaml
```

## Filter Structure

The filter file has two main sections: `include` and `exclude`.

### Basic Structure

```yaml
include:
  - # Include filters

exclude:
  - # Exclude filters
```

### Filter Priority

1. **Exclude filters** are evaluated first
2. **Include filters** are evaluated second
3. If a test matches an exclude filter, it's excluded regardless of include filters
4. If no include filters are specified, all tests (not excluded) are included

## Filter Types

### Class Name Filters

Filter by exact class name:

```yaml
include:
  - className: "com.example.tests.DatabaseTest"
  - className: "com.example.tests.ApiTest"
```

### Package Filters

Filter by package name:

```yaml
include:
  - packageName: "com.example.tests.integration"
  - packageName: "com.example.tests.unit"
```

### Pattern Filters

Use regex patterns for flexible matching:

```yaml
include:
  - pattern: ".*IntegrationTest"
  - pattern: "com\\.example\\.tests\\..*Test"
```

### Tag Filters

Filter by `@Tag` annotations:

```yaml
include:
  - tag: "fast"
  - tag: "unit"

exclude:
  - tag: "slow"
  - tag: "integration"
```

Tests are tagged using the `@Tag` annotation:

```java
@Tag("fast")
@Tag("unit")
public class FastUnitTest {
    // Tests...
}
```

## Complete Filter Examples

### Example 1: Run Only Integration Tests

```yaml
include:
  - tag: "integration"
```

### Example 2: Exclude Slow Tests

```yaml
exclude:
  - tag: "slow"
```

### Example 3: Run Only Specific Package

```yaml
include:
  - packageName: "com.example.tests.critical"
```

### Example 4: Complex Filtering

```yaml
include:
  # Include all integration tests
  - tag: "integration"
  # Include all tests in critical package
  - packageName: "com.example.tests.critical"
  # Include tests matching pattern
  - pattern: ".*SmokeTest"

exclude:
  # Exclude slow tests
  - tag: "slow"
  # Exclude experimental tests
  - tag: "experimental"
  # Exclude specific class
  - className: "com.example.tests.BrokenTest"
```

### Example 5: Environment-Specific Filters

**CI environment (fast tests only):**

```yaml
include:
  - tag: "ci"
  - tag: "fast"

exclude:
  - tag: "slow"
  - tag: "manual"
```

**Local development (all except slow):**

```yaml
exclude:
  - tag: "slow"
```

**Production validation (critical tests only):**

```yaml
include:
  - tag: "critical"
  - tag: "smoke"
```

## Using Tags in Tests

### Single Tag

```java
import org.verifyica.api.Tag;

@Tag("integration")
public class IntegrationTest {
    // Tests...
}
```

### Multiple Tags

```java
@Tag("integration")
@Tag("database")
@Tag("slow")
public class DatabaseIntegrationTest {
    // Tests...
}
```

### Method-Level Tags

Tags can be placed on individual test methods:

```java
public class MixedTest {

    @Verifyica.Test
    @Tag("fast")
    public void fastTest(String argument) {
        // Fast test
    }

    @Verifyica.Test
    @Tag("slow")
    public void slowTest(String argument) {
        // Slow test
    }
}
```

**Note:** Method-level tags require class-level filtering to work correctly. If the class is excluded, method-level tags won't be evaluated.

## Filter Matching Logic

### Multiple Filters of Same Type (OR logic)

Multiple filters of the same type use OR logic:

```yaml
include:
  - tag: "unit"
  - tag: "integration"
```

This includes tests with tag "unit" **OR** "integration".

### Different Filter Types (AND logic)

Different filter types use AND logic:

```yaml
include:
  - packageName: "com.example.tests"
  - tag: "integration"
```

This includes tests in package "com.example.tests" **AND** with tag "integration".

### Exclude Takes Precedence

```yaml
include:
  - tag: "integration"

exclude:
  - className: "com.example.tests.BrokenIntegrationTest"
```

`BrokenIntegrationTest` is excluded even though it has the "integration" tag.

## Common Filter Patterns

### Run Only Unit Tests

```yaml
include:
  - tag: "unit"
```

### Run Everything Except Slow Tests

```yaml
exclude:
  - tag: "slow"
```

### Run Only Tests in Specific Package

```yaml
include:
  - packageName: "com.example.tests.smoke"
```

### Run Integration Tests Except Database Tests

```yaml
include:
  - tag: "integration"

exclude:
  - tag: "database"
```

### Run All Tests Ending with "SmokeTest"

```yaml
include:
  - pattern: ".*SmokeTest"
```

## Dynamic Filter Files

### Maven Profiles

Use Maven profiles to switch filter files:

```xml
<profiles>
    <profile>
        <id>ci</id>
        <build>
            <resources>
                <resource>
                    <directory>src/test/resources/filters</directory>
                    <includes>
                        <include>ci-filters.yaml</include>
                    </includes>
                    <targetPath>.</targetPath>
                    <filtering>false</filtering>
                </resource>
            </resources>
        </build>
        <properties>
            <verifyica.engine.filter.definitions.filename>ci-filters.yaml</verifyica.engine.filter.definitions.filename>
        </properties>
    </profile>
</profiles>
```

Run with: `mvn test -Pci`

### Environment-Based Configuration

Use system properties to switch filters:

```bash
# Use CI filters
mvn test -Dverifyica.engine.filter.definitions.filename=ci-filters.yaml

# Use integration test filters
mvn test -Dverifyica.engine.filter.definitions.filename=integration-filters.yaml
```

## Troubleshooting Filters

### No Tests Run

If no tests execute after adding filters:

1. **Check filter syntax** - Ensure YAML is valid
2. **Verify tags** - Ensure tests have matching tags
3. **Check exclusions** - Exclude filters may be too broad
4. **Enable debug logging:**

```properties
verifyica.engine.logger.level=DEBUG
```

### Unexpected Tests Run

If unexpected tests execute:

1. **Check include filters** - May be too broad
2. **Verify tag spelling** - Tags are case-sensitive
3. **Check pattern syntax** - Regex patterns must be escaped correctly

### Filter Not Applied

If filters aren't working:

1. **Verify file location** - Must be in classpath (src/test/resources)
2. **Check filename** - Default is `verifyica.engine.filter.definitions.yaml`
3. **Validate YAML syntax** - Use a YAML validator

## Best Practices

### Use Descriptive Tag Names

```java
// Good: Clear, descriptive tags
@Tag("database-integration")
@Tag("requires-docker")
@Tag("slow")

// Bad: Cryptic tags
@Tag("t1")
@Tag("x")
```

### Organize Tags by Category

```java
// Execution speed
@Tag("fast")  // < 1 second
@Tag("medium")  // 1-10 seconds
@Tag("slow")  // > 10 seconds

// Test type
@Tag("unit")
@Tag("integration")
@Tag("e2e")

// Dependencies
@Tag("requires-database")
@Tag("requires-docker")
@Tag("requires-network")
```

### Document Filter Files

```yaml
# CI Filter Configuration
# Runs fast, critical tests suitable for PR validation
include:
  - tag: "fast"
  - tag: "critical"

exclude:
  - tag: "slow"
  - tag: "flaky"  # Exclude flaky tests from CI
```

### Keep Filter Files Simple

Start with simple filters and add complexity only as needed:

```yaml
# Simple: Good starting point
include:
  - tag: "ci"

# Complex: Only if needed
include:
  - tag: "integration"
  - packageName: "com.example.critical"
  - pattern: ".*SmokeTest"
exclude:
  - tag: "slow"
  - tag: "flaky"
  - className: "com.example.BrokenTest"
```

## Next Steps

- [Properties](../properties/) - Configure other aspects of Verifyica
- [Parallelism](../parallelism/) - Configure parallel execution
- [Advanced → Tagging](../../advanced/tagging/) - Advanced tagging patterns
