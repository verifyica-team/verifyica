# Verifyica

[![Build](https://github.com/antublue/verifyica/actions/workflows/build.yml/badge.svg)](https://github.com/antublue/verifyica/actions/workflows/build.yml) [![Codacy Badge](https://app.codacy.com/project/badge/Grade/016a73debd6041cb91e53abda16e76ac)](https://app.codacy.com/gh/antublue/verifyica/dashboard?utm_source=gh&utm_medium=referral&utm_content=&utm_campaign=Badge_grade)

A Java annotation-based, multi-threaded test engine for integration testing built on JUnit platform.

# Purpose

Parameterized unit testing is traditionally test first oriented ...

- Given a set of tests (methods), execute each test using a set of arguments.

```
for (Test test : Tests) {
   for (TestArgument testArgument : TestArguments) {
      execute(test, testArgument)
   }
}
```

Verifyica swaps the paradigm to be test arguments first oriented ...

- Given a set of arguments, execute each test with the arguments

```
for (TestArgument testArgument : TestArguments) {
   for (Test test : Tests) {
      execute(test, testArgument)
   }
}
```

# Basic example

Basic example test that will execute a set of tests against an environment.

In this example, the environment is ...

- initialized
- tested (test1, test2, ...)
- destroyed

```java
import java.io.IOException;
import java.util.Properties;
import org.antublue.verifyica.api.Argument;
import org.antublue.verifyica.api.ArgumentContext;
import org.antublue.verifyica.api.Verifyica;

/** Example */
public class EnvironmentTest {

  @Verifyica.ArgumentSupplier
  public static Argument<Environment> arguments() {
    Properties properties = new Properties();

    // ... configure properties for the environment ...

    String name = "Test Environment";
    Environment environment = new Environment(properties);

    return Argument.of(name, environment);
  }

  @Verifyica.BeforeAll
  public void beforeAll(ArgumentContext argumentContext) throws Throwable {
    argumentContext.getArgument(Environment.class).getPayload().initialize();
  }

  @Verifyica.Test
  public void test1(ArgumentContext argumentContext) throws Throwable {
    HttpClient httpClient =
            argumentContext.getArgument(Environment.class).getPayload().getHttpClient();

    // ... test code ...
  }

  @Verifyica.Test
  public void test2(ArgumentContext argumentContext) throws Throwable {
    HttpClient httpClient =
            argumentContext.getArgument(Environment.class).getPayload().getHttpClient();

    // ... test code ...
  }

  // ... more tests

  @Verifyica.AfterAll
  public void afterAll(ArgumentContext argumentContext) {
    argumentContext.getArgument(Environment.class).getPayload().destroy();
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
     * @throws Throwable
     */
    public void initialize() throws Throwable {
      // code to initialize the external environment
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

# Examples using test containers

Example tests using [testcontainers-java](https://java.testcontainers.org/)

- [KafkaTest.java](tests/src/test/java/org/antublue/verifyica/test/testcontainers/KafkaTest.java)
- [MongoDBTest.java](tests/src/test/java/org/antublue/verifyica/test/testcontainers/MongoDBTest.java)

# Features

- Purpose built for integration testing using [testcontainers-java](https://java.testcontainers.org/)
    - i.e. for each argument, execute a set of test methods
- Annotation based
- Multi-threaded support
- Virtual thread support (Java 21+)
- Properties file driven configuration
- IntelliJ support

# Base Requirements

- Java 8 (Java 21+ preferred for virtual threads)
- Maven

# Ancillary Requirements

To perform integration testing, you need a test environment. Typically this is created using [testcontainers-java](https://java.testcontainers.org/) and [Docker](https://www.docker.com/) (or another compatible container environment.)

Alternatively, tests can be performed on an external environment. Ideally the external environment would have an API to initialize and destroy it.
# Contributing

See [Contributing](CONTRIBUTING.md) for details.

# Licensing

[Apache-2.0](LICENSE.txt) licensed.

# Support

![YourKit logo](https://www.yourkit.com/images/yklogo.png)

[YourKit](https://www.yourkit.com/) supports open source projects with innovative and intelligent tools for monitoring and profiling Java and .NET applications.

YourKit is the creator of <a href="https://www.yourkit.com/java/profiler/">YourKit Java Profiler</a>,
<a href="https://www.yourkit.com/dotnet-profiler/">YourKit .NET Profiler</a>,
and <a href="https://www.yourkit.com/youmonitor/">YourKit YouMonitor</a>.