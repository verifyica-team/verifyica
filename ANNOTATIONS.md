# Annotations

All Verifyica annotations are defined in a container class `Verifyica`.

[Verifyica.java](api/src/main/java/org/antublue/verifyica/api/Verifyica.java)

### @Verifyica.ArgumentSupplier

All test classes must define single method annotated with `@Verifyica.ArgumentSupplier`.

- must be public
- must be static
- must not define any parameters
- must return a non-null Object
- iterables, collections, arrays, etc. can contain mixed Object types

**Notes**

- If the `@Verifyica.ArgumentSupplier` method returns a `null` object, the test class will be ignored/not reported.

Examples:

```java
public static String arguments() {
    return "test";
}
```

```java
public static Argument<?> arguments() {
    return Argument.of("test", "test");
}
```

```java
import java.util.ArrayList;

public static Collection<String> arguments() {
    Collection<String> collection = new ArrayList<>();
    collection.add("test");
    return collection;
}
```

```java
import java.util.ArrayList;

public static Object[] arguments() {
    Object[] objects = new Object[2];
    objects[0] = "foo";
    objects[1] = "bar";
    return objects;
}
```

### @Verifyica.Prepare / @Verifyica.Conclude

`@Verifyica.Prepare` methods are executed **once** before a test class is tested.

`@Verifyica.Conclude` methods are executed **once** after a test class is tested.

All methods annotated with `@Verifyica.Prepare` or `@Verifyica.Conclude`:

- must return `void`
- must be public
- must be static
- must define a single parameter [ClassContext](api/src/main/java/org/antublue/verifyica/api/ClassContext.java)
- may throw `Throwable`

### @Verifyica.BeforeAll / @Verifyica.AfterAll

`@Verifyica.BeforeAll` methods are executed **once for each test argument** before test methods.

`@Verifyica.AfterAll` methods are executed **once for each test argument** after test methods;

All methods annotated with `@Verifyica.BeforeAll` or `@Verifyica.AfterAll`:

- must return `void`
- must be public
- must not be static
- must defined a single parameter [ArgumentContext](api/src/main/java/org/antublue/verifyica/api/ArgumentContext.java)
- may throw `Throwable`

### @Verifyica.BeforeEach / @Verifyica.AfterEach

`@Verifyica.BeforeEach` methods are executed **once** before each `@Verifyica.Test` test method.

`@Verifyica.AfterEach` methods are executed **once after each `@Verifyica.Test` test method;

All methods annotated with `@Verifyica.BeforeEach` or `@Verifyica.AfterEach`:

- must return `void`
- must be public
- must not be static
- must defined a single parameter [ArgumentContext](api/src/main/java/org/antublue/verifyica/api/ArgumentContext.java)
- may throw `Throwable`

### @Verifyica.Test

All methods annotated with `@Verifyica.Test`:

- must return `void`
- must be public
- must not be static
- must defined a single parameter [ArgumentContext](api/src/main/java/org/antublue/verifyica/api/ArgumentContext.java)
- may throw `Throwable`