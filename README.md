[![Build](https://github.com/antublue/verifyica/actions/workflows/build.yml/badge.svg)](https://github.com/antublue/verifyica/actions/workflows/build.yml) [![Codacy Badge](https://app.codacy.com/project/badge/Grade/016a73debd6041cb91e53abda16e76ac)](https://app.codacy.com/gh/antublue/verifyica/dashboard?utm_source=gh&utm_medium=referral&utm_content=&utm_campaign=Badge_grade) ![JDK Compatibility](https://img.shields.io/badge/JDK%20compatibility-8+-blue.svg) ![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)

|              |   |
|--------------|---|
| API          | [![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.antublue.verifyica/api/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.antublue.verifyica/api) |
| Engine       | [![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.antublue.verifyica/engine/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.antublue.verifyica/engine)  |
| Maven Plugin | [![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.antublue.verifyica/maven-plugin/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.antublue.verifyica/maven-plugin)  |

![Verifyica](assets/verifyica.png)

A Java annotation driven, multithreaded test engine for integration testing based on [JUnit](https://junit.org/junit5/) Platform.

# Purpose

Parameterized testing is traditionally test method oriented ...

- For each test method, execute the test method with the list of test arguments.

```
for (TestMethod testMethod : TestMethods) {
   for (TestArgument testArgument : TestArguments) {
      testMethod(testArgument)
   }   
}
```

Verifyica swaps the paradigm to be test argument oriented ...

- For each test argument, execute the list of test methods with the test argument.

```
for (TestArgument testArgument : TestArguments) {
   for (TestMethod testMethod : TestMethods) {
      testMethod(testArgument)
   }
}
```

# Features

- Purpose built for integration testing using [testcontainers-java](https://java.testcontainers.org/)
  - i.e. for each test argument, execute a set of test methods
- Annotation driven
- Multithreaded test class / test argument testing
- Virtual thread support (Java 21+)
- Properties file driven configuration
- Test class / test method filtering
- Test class tag filtering
- Test class interceptors (extensions)
- Engine interceptors (extensions)
- Test class / test argument parallel testing with configurable constraints
- Cleanup of `AutoClosable` test classes / test arguments
- Cleanup of `AutoClosable` objects in a [Store](api/src/main/java/org/antublue/verifyica/api/Store.java)
- Object / resource sharing via contexts
  - [EngineContext](api/src/main/java/org/antublue/verifyica/api/EngineContext.java)
  - [ClassContext](api/src/main/java/org/antublue/verifyica/api/ClassContext.java)
  - [ArgumentContext](api/src/main/java/org/antublue/verifyica/api/ArgumentContext.java) 
- LockManager for globally exclusive locks
- [IntelliJ](https://www.jetbrains.com/idea/) support
- [Maven](https://maven.apache.org/) support
- [JUnit5 ConsoleLauncher](https://junit.org/junit5/docs/5.0.0-M5/user-guide/#running-tests-console-launcher) support

# High Level Design

```
for (TestClass testClass : TestClasses) {

    instantiate test class instance

    execute @Verifyica.Prepare methods (superclass then subclass)

    for (TestArgument testArgument : TestArguments) {
    
       execute @Verifyica.BeforeAll methods (superclass then subclass)
      
       for (test method : Test methods) {
       
          execute @Verifyica.BeforeEach methods (superclass then subclass)
          
              execute test method
          
          execute @Verifyica.AfterEach methods (subclass then superclass)
       }
       
       execute @Verifyica.AfterAll methods (subclass then superclass)
    }

    execute @Verificya.Conclude methods (subclass then superclass)
   
    destroy test class instance
}
```

# Basic example

Basic example test class that will execute a set of test methods against an environment.

### Test Class

```java
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;
import org.antublue.verifyica.api.Argument;
import org.antublue.verifyica.api.ArgumentContext;
import org.antublue.verifyica.api.ClassContext;
import org.antublue.verifyica.api.Verifyica;

/** Example */
public class EnvironmentTest {

  @Verifyica.ArgumentSupplier
  public static Collection<Argument<Environment>> arguments() {
    Collection<Argument<Environment>> environments = new ArrayList<>();

    // Create environment 1

    Properties properties = new Properties();
    // ... configure properties for the environment ...
    String name = "Test Environment 1";
    Environment environment = new Environment(properties);

    environments.add(Argument.of(name, environment));

    // Create environment 2

    properties = new Properties();
    // ... configure properties for the environment ...
    name = "Test Environment 2";
    environment = new Environment(properties);

    environments.add(Argument.of(name, environment));

    // ... more environments ...

    return environments;
  }

  @Verifyica.Prepare
  public static void prepare(ClassContext classContext) {
    // ... initialize any static values ...
  }

  @Verifyica.BeforeAll
  public void beforeAll(ArgumentContext argumentContext) throws Throwable {
    Argument<Environment> argument = argumentContext.getTestArgument(Environment.class);

    System.out.println("[" + argument.getName() + "] initializing ...");

    argument.getPayload().initialize();

    System.out.println("[" + argument.getName() + "] initialized");
  }

  @Verifyica.Test
  public void test1(ArgumentContext argumentContext) throws Throwable {
    Argument<Environment> argument = argumentContext.getTestArgument(Environment.class);

    System.out.println("[" + argument.getName() + "] test1");

    HttpClient httpClient =
            argumentContext.getTestArgument(Environment.class).getPayload().getHttpClient();

    // ... test code ...
  }

  @Verifyica.Test
  public void test2(ArgumentContext argumentContext) throws Throwable {
    Argument<Environment> argument = argumentContext.getTestArgument(Environment.class);

    System.out.println("[" + argument.getName() + "] test2");

    HttpClient httpClient =
            argumentContext.getTestArgument(Environment.class).getPayload().getHttpClient();

    // ... test code ...
  }

  // ... more tests

  @Verifyica.AfterAll
  public void afterAll(ArgumentContext argumentContext) {
    Argument<Environment> argument = argumentContext.getTestArgument(Environment.class);

    System.out.println("[" + argument.getName() + "] destroying ...");

    argument.getPayload().destroy();

    System.out.println("[" + argument.getName() + "] destroyed");
  }

  @Verifyica.Conclude
  public static void conclude(ClassContext classContext) {
    // ... destroy/cleanup any static values ...
  }

  /** Class to represent an environment */
  public static class Environment {

    private final Properties properties;
    private final HttpClient httpClient;

    /**
     * Constructor
     *
     * @param properties properties
     */
    public Environment(Properties properties) {
      this.properties = properties;
      this.httpClient = new HttpClient(properties);
    }

    /**
     * Initialize the environment
     *
     * @throws Throwable Throwable
     */
    public void initialize() throws Throwable {
      // code to initialize the environment
    }

    /**
     * Method to get an HttpClient for the environment
     *
     * @return an HttpClient for the environment
     */
    public HttpClient getHttpClient() {
      return httpClient;
    }

    /** Method to destroy the environment */
    public void destroy() {
      // code to destroy the environment
    }
  }

  /** Mock HTTP client */
  public static class HttpClient {

    private final Properties properties;

    /** Constructor */
    public HttpClient(Properties properties) {
      this.properties = properties;

      // ... code to initialize the HTTP client ...
    }

    /**
     * Method to send a GET request
     *
     * @param path path
     * @return the response
     * @throws IOException IOException
     */
    public String doGet(String path) throws IOException {

      // ... code to perform the HTTP GET request ...

      return "success";
    }
  }
}

```

### Output

```text
[Test Environment 1] initializing ...
[Test Environment 1] initialized
[Test Environment 1] test1
[Test Environment 1] test2
[Test Environment 1] destroying ...
[Test Environment 1] destroyed
[Test Environment 2] initializing ...
[Test Environment 2] initialized
[Test Environment 2] test1
[Test Environment 2] test2
[Test Environment 2] destroying ...
[Test Environment 2] destroyed
```

# Test Containers Examples

Example tests using [testcontainers-java](https://java.testcontainers.org/)

- [TestContainerTest.java](tests/src/test/java/org/antublue/verifyica/test/testcontainers/TestContainerTest.java)
  - Generic example showing multithreaded argument testing

Examples using real test containers ...

- [KafkaTest.java](tests/src/test/java/org/antublue/verifyica/test/testcontainers/KafkaTest.java)
- [MongoDBTest.java](tests/src/test/java/org/antublue/verifyica/test/testcontainers/MongoDBTest.java)

# Core Requirements

### Java

Verifyica is compiled against/targets Java 8, but Java 21+ is recommended for virtual thread support.

### Maven

When using Maven, the Verifyica Maven plugin is required.

#### Plugin XML

```xml
<plugin>
    <groupId>org.antublue.verifyica</groupId>
    <artifactId>maven-plugin</artifactId>
    <version>VERSION</version>
    <executions>
        <execution>
            <phase>test</phase>
            <goals>
                <goal>test</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

#### Dependency XML

```xml
<dependency>
    <groupId>org.antublue.verifyica</groupId>
    <artifactId>api</artifactId>
    <version>VERSION</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.antublue.verifyica</groupId>
    <artifactId>engine</artifactId>
    <version>VERSION</version>
    <scope>test</scope>
</dependency>
```

# Ancillary Requirements

To perform integration testing, you need a test context/environment. Typically this is created using [testcontainers-java](https://java.testcontainers.org/) and [Docker](https://www.docker.com/) (or another compatible container environment.)

Alternatively, tests can be performed on an external environment/resource. Ideally the external environment/resource would have an API to initialize and destroy it.

An [EngineInterceptor](api/src/main/java/org/antublue/verifyica/api/interceptor/engine/EngineInterceptor.java) can be used to initialize and destroy an external environment/resource before and after execution of tests.

# Documentation

- [Annotations](ANNOTATIONS.md)
- [API](API.md)
- [Interceptors](INTERCEPTORS.md)
- [Locking](LOCKING.md)
- [Configuration](CONFIGURATION.md)
- [Filters](FILTERS.md)
- [FAQ](https://github.com/antublue/verifyica/wiki/FAQ)

**Notes**

- **Documentation is specific to a release**

# Contributing

See [Contributing](CONTRIBUTING.md) for details.

# License

Apache License 2.0, see [LICENSE](LICENSE).

# Code of Conduct

See [Code of Conduct](CODE_OF_CONDUCT.md) for details.

# DCO

See [DCO](DCO.md) for details.

# Support

![YourKit logo](https://www.yourkit.com/images/yklogo.png)

[YourKit](https://www.yourkit.com/) supports open source projects with innovative and intelligent tools for monitoring and profiling Java and .NET applications.

YourKit is the creator of <a href="https://www.yourkit.com/java/profiler/">YourKit Java Profiler</a>,
<a href="https://www.yourkit.com/dotnet-profiler/">YourKit .NET Profiler</a>,
and <a href="https://www.yourkit.com/youmonitor/">YourKit YouMonitor</a>.

---

Copyright (C) 2024 The Verifyica project authors
