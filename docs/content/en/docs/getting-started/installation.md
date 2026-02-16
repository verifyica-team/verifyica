---
title: "Installation"
linkTitle: "Installation"
weight: 1
description: >
  Add Verifyica to your Maven project
---

## Overview

Verifyica is available on Maven Central and can be added to your project using Maven. You need to add two dependencies:

1. **verifyica-api** - Contains annotations and API classes
2. **verifyica-engine** - The JUnit Platform based TestEngine implementation

## Maven

Add the following dependencies to your `pom.xml`:

```xml
<dependencies>
    
    <!-- Verifyica API (compile scope for annotations) -->
    <dependency>
        <groupId>org.verifyica</groupId>
        <artifactId>verifyica-api</artifactId>
        <version>1.0.0</version>
    </dependency>

    <!-- Verifyica Engine (test scope) -->
    <dependency>
        <groupId>org.verifyica</groupId>
        <artifactId>verifyica-engine</artifactId>
        <version>1.0.0</version>
        <scope>test</scope>
    </dependency>

</dependencies>
```

### Maven Surefire Configuration

Configure the Maven Surefire plugin to **ignore** Verifyica tests (they will be run by the Verifyica Maven Plugin):

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-surefire-plugin</artifactId>
            <version>3.5.2</version>
            <configuration>
                <excludes>
                    <!-- Exclude Verifyica tests ... this should not match standard JUnit tests -->
                    <exclude>**/*</exclude>
                </excludes>
            </configuration>
        </plugin>
    </plugins>
</build>
```

### Verifyica Maven Plugin (Required)

Add the Verifyica Maven Plugin to run your tests:

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.verifyica</groupId>
            <artifactId>verifyica-maven-plugin</artifactId>
            <version>1.0.0</version>
            <executions>
                <execution>
                    <goals>
                        <goal>test</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

**Complete Build Configuration Example:**

```xml
<build>
    <plugins>
        <!-- Maven Surefire: Exclude Verifyica tests -->
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-surefire-plugin</artifactId>
            <version>3.5.2</version>
            <configuration>
                <excludes>
                    <!-- Exclude Verifyica tests ... this should not match standard JUnit tests -->
                    <exclude>**/*</exclude>
                </excludes>
            </configuration>
        </plugin>

        <!-- Verifyica Maven Plugin: Run Verifyica tests -->
        <plugin>
            <groupId>org.verifyica</groupId>
            <artifactId>verifyica-maven-plugin</artifactId>
            <version>1.0.0</version>
            <executions>
                <execution>
                    <goals>
                        <goal>test</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

## Verifying Installation

After adding the dependencies, verify the installation by running:

```bash
mvn clean test
```

If you don't have any tests yet, the build should succeed with "No tests found" or similar output.

## IDE Support

Verifyica tests work with any IDE that supports JUnit Platform:

### IntelliJ IDEA

- **Automatic:** IntelliJ IDEA 2017.3+ automatically detects JUnit Platform tests
- Run tests by clicking the green arrow next to test classes or methods
- View test results in the integrated test runner

### Eclipse

- **Requires:** Eclipse 4.7+ with JUnit 5 support
- Install the "JUnit 5" feature if not already present
- Run tests using "Run As > JUnit Test"

### Visual Studio Code

- **Extension:** Install the "Test Runner for Java" extension
- Tests appear in the Test Explorer sidebar
- Run tests by clicking the play button

## Dependencies and Plugins Overview

### Dependencies

| Artifact | Purpose | Scope |
|----------|---------|-------|
| `verifyica-api` | Annotations and API classes | compile |
| `verifyica-engine` | TestEngine implementation | test |

### Plugins

| Plugin | Purpose | Phase |
|--------|---------|-------|
| `verifyica-maven-plugin` | Runs Verifyica tests | test |
| `maven-surefire-plugin` | Configured to exclude Verifyica tests | test |

## Version Compatibility

| Verifyica Version | Java Version | JUnit Platform Version |
|-------------------|--------------|------------------------|
| 1.0.0             | 8+ | 1.10.0+ |

## Next Steps

Now that Verifyica is installed, proceed to:

- [Quick Start](../quick-start/) - Write your first test
- [First Test](../first-test/) - Understand test structure in depth
