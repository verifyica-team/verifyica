# Configuration

Verifyica configuration is defined in a properties files `verifyica.properties`.

The properties file is searched recursively from the current execution directory, up to the root directory. The first file found will be used.

Example:

Given a project location ...

```
/home/user/github/project/
```

The search path will be ...

```
/home/user/github/project/verifyica.properties
/home/user/github/verifyica.properties
/home/user/verifyica.properties
/home/verifyica.properties
/verifyica.properties
```

**Notes**

- The first file found, regardless of content will be used

## Execution Parallelism

Verifyica has the ability to control both test class and test argument execution parallelism.

### Test class execution parallelism

Verifyica uses a distinct `ExecutorService` to execute test classes.

The default maximum number of threads is equal to the number of processors as reported by `Runtime.getRuntime().availableProcessors()`.

You can override the default value by defining a property in `verifyica.properties`

Example:

```properties
verifyica.engine.class.parallelism=20
```

### Test argument execution parallelism

Verifyica uses a distinct `ExecutorService` to execute test class arguments.

The default maximum number of threads is equal to the number of processors as reported by `Runtime.getRuntime().availableProcessors()`.

You can override the default value by defining a property in `verifyica.properties`

Example:

```properties
verifyica.engine.argument.parallelism=20
```

**Notes**

- `verifyica.engine.argument.parallelism` must be greater than or equal to `verifyica.engine.class.parallelism`


- if `verifyica.engine.argument.parallelism` is less than `verifyica.engine.class.parallelism`...
  - `verifyica.engine.argument.parallelism` will be set to `verifyica.engine.class.parallelism`
  - a `WARN` log print will be printed 


- `@Verifyica.ArgumentSupplier(parallelism = X)` is constrained to a maximum upper limit of `verifyica.engine.argument.parallelism`
  - overall test argument execution parallelism (for all test classes) is `verifyica.engine.argument.parallelism`

## Parallelism Thread Type

Verifyica is designed to use either platform threads or virtual threads depending on the Java version being used.

- Java 21+
  - virtual threads


- Java 8 through Java 20
  - platform threads

**Notes**

- `verifyica.engine.threads=platform` can be used to force use of platform threads on a Java version that supports virtual threads.

- When using platform threads, `verifyica.engine.threads.platform=ephemeral` can be use to create ephemeral threads;
  - new thread per class
  - new thread per argument (when argument supplier parallelism is greater than 1)

- When use virtual threads, threads are automatically ephemeral

## Engine Throttling

By design, Verifyica will execute as fast as possible based on engine / argument parallelism. For some scenarios, you may want to throttle execution.

The configuration values...

- `verifyica.engine.class.throttle`
- `verifyica.engine.argument.throttle`
- `verifyica.engine.argument.test.throttle`

... can be defined to throttle the execution of the engine's state machines.

Example:

```
verifyica.engine.argument.throttle=0, 1000
```

- Throttles execution of argument related methods randomly between `0` and `1000` milliseconds

## Logging

## Configuration Logging

Verifyica configuration logging is enabled by setting an environment variable.

- `VERIFYICA_CONFIGURATION_TRACE=true`

## General Logging

Verifyica engine logging can be enabled using two properties in `verifyica.properties`.

```properties
verifyica.engine.logger.level=ALL
verifyica.engine.logger.regex=
```

- `verifyica.engine.logger.level` controls whether logging is enabled.
  - default level is `INFO`
  - supported levels are `INFO`, `WARN`, `ERROR`, `TRACE`, `ALL`


- `verifyica.engine.logger.regex` controls which classes are enabled.
  - an empty value "" is mapped to `.*`

---

Copyright (C) 2024-present Verifyica project authors and contributors