# Configuration

## Parallelism

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


- `@Verifyica.ArgumentSupplier(parallelism = X)` is still honored

## ExecutorService Thread type

Verifyica designed to use both platform threads and virtual threads automatically.

- Java 21+
  - virtual threads


- Java 8 through Java 20
  - platform threads

