# Verifyica

[![Build](https://github.com/antublue/verifyica/actions/workflows/build.yml/badge.svg)](https://github.com/antublue/verifyica/actions/workflows/build.yml)

A Java multi-threaded, annotation-based test engine for integration testing.

# Purpose

Unit testing is traditionally oriented tests (methods) ...

- Given a set of tests (methods), execute each test using a set of arguments.

```
for (Test test : Tests) {
   for (TestArgument testArgument : TestArguments) {
      execute(test, testArgument)
   }
}
```

Verifyica swaps the paradigm to be arguments first ...

- Given a set of arguments, execute each test with the arguments

```
for (TestArgument testArgument : TestArguments) {
   for (Test test : Tests) {
      execute(test, testArgument)
   }
}
```

# But why?

Integration testing typically requires resources that need to be reused (Docker network, Docker containers, etc.)

Creating, using, and destroying these resources is burdensome and doesn't allow multiple tests against the resources, resulting in large tests that execute a specific test flow.

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

Alternatively, tests can be performed on an external environment, though integration testing can be tricky.

# Support

![YourKit logo](https://www.yourkit.com/images/yklogo.png)

[YourKit](https://www.yourkit.com/) supports open source projects with innovative and intelligent tools for monitoring and profiling Java and .NET applications.

YourKit is the creator of <a href="https://www.yourkit.com/java/profiler/">YourKit Java Profiler</a>,
<a href="https://www.yourkit.com/dotnet-profiler/">YourKit .NET Profiler</a>,
and <a href="https://www.yourkit.com/youmonitor/">YourKit YouMonitor</a>.