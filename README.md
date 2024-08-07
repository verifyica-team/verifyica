[![Build](https://github.com/antublue/verifyica/actions/workflows/build.yml/badge.svg)](https://github.com/antublue/verifyica/actions/workflows/build.yml) [![Codacy Badge](https://app.codacy.com/project/badge/Grade/016a73debd6041cb91e53abda16e76ac)](https://app.codacy.com/gh/antublue/verifyica/dashboard?utm_source=gh&utm_medium=referral&utm_content=&utm_campaign=Badge_grade)

|              |   |
|--------------|---|
| API          | [![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.antublue.verifyica/api/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.antublue.verifyica/api) |
| Engine       | [![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.antublue.verifyica/engine/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.antublue.verifyica/engine)  |
| Maven Plugin | [![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.antublue.verifyica/maven-plugin/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.antublue.verifyica/maven-plugin)  |

![Verifyica](assets/verifyica.png)

A Java annotation-based, multithreaded test engine for integration testing built on [JUnit](https://junit.org/junit5/) Platform.

# Purpose

Parameterized testing is traditionally test first oriented ...

- Given a set of tests (methods), execute each test using a set of arguments.

```
for (Test test : Tests) {
   for (TestArgument testArgument : TestArguments) {
      test1(testArgument)
   }   
   for (TestArgument testArgument : TestArguments) {
      test2(testArgument)
   }
}
```

Verifyica swaps the paradigm to be test argument first oriented ...

- Given a set of arguments, execute each test with the arguments.

```
for (TestArgument testArgument : TestArguments) {
   for (Test test : Tests) {
      test1(testArgument)
      test2(testArgument);
   }
}
```

# Features

- Purpose built for integration testing using [testcontainers-java](https://java.testcontainers.org/)
  - i.e. for each argument, execute a set of test methods
- Annotation based
- Multithreaded class / argument testing support
- Virtual thread support (Java 21+)
- Properties file driven configuration
- Test class / test method filtering
- Test class tag filtering
- Test class interceptors (extensions)
- Engine interceptors (extensions)
- Class / argument parallelism constraints
- Store cleanup of `AutoClosable` objects
- [IntelliJ](https://www.jetbrains.com/idea/) support

# High Level Design

```
for (TestClass testClass : TestClasses) {

    instantiate test class instance {

        execute @Verifyica.Prepare methods {
    
            for (TestArgument testArgument : TestArguments) {
            
               execute @Verifyica.BeforeAll methods
              
               for (test method : Test methods) {
               
                  execute @Verifyica.BeforeEach methods
                  
                      execute test method
                  
                  execute @Verifyica.AfterEach methods
               }
               
               execute @Verifyica.AfterAll methods
            }
        }
    
        execute @Verificya.Conclude methods
    }
    
    destroy test class instance
}
```

# Basic example

Basic example test that will execute a set of tests against an environment.

In this example, the environment is ...

- initialized
- tested (test1, test2, ...)
- destroyed

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

# Examples using test containers

Example tests using [testcontainers-java](https://java.testcontainers.org/)

- [TestContainerTest.java](tests/src/test/java/org/antublue/verifyica/test/testcontainers/TestContainerTest.java)
  - Generic example showing multi-threaded argument testing

Examples using real test containers ...

- [KafkaTest.java](tests/src/test/java/org/antublue/verifyica/test/testcontainers/KafkaTest.java)
- [MongoDBTest.java](tests/src/test/java/org/antublue/verifyica/test/testcontainers/MongoDBTest.java)

# Base Requirements

- Java 8 (Java 21+ preferred for virtual threads)
- Maven

# Ancillary Requirements

To perform integration testing, you need a test context/environment. Typically this is created using [testcontainers-java](https://java.testcontainers.org/) and [Docker](https://www.docker.com/) (or another compatible container environment.)

Alternatively, tests can be performed on an external environment. Ideally the external environment would have an API to initialize and destroy it.
# Contributing

See [Contributing](CONTRIBUTING.md) for details.

# License

Apache License 2.0, see [LICENSE](LICENSE).

# Code of Conduct

[Code of Conduct](CODE_OF_CONDUCT.md)

# DCO

[DCO](DCO.md)

# Support

![YourKit logo](https://www.yourkit.com/images/yklogo.png)

[YourKit](https://www.yourkit.com/) supports open source projects with innovative and intelligent tools for monitoring and profiling Java and .NET applications.

YourKit is the creator of <a href="https://www.yourkit.com/java/profiler/">YourKit Java Profiler</a>,
<a href="https://www.yourkit.com/dotnet-profiler/">YourKit .NET Profiler</a>,
and <a href="https://www.yourkit.com/youmonitor/">YourKit YouMonitor</a>.