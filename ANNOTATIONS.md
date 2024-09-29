# Annotations

All Verifyica annotations are defined in a container class `Verifyica`.

[Verifyica.java](api/src/main/java/org/verifyica/api/Verifyica.java)

---

### @Verifyica.ArgumentSupplier

All test classes must define a single method annotated with `@Verifyica.ArgumentSupplier`.

- required
- one method per class
- valid types:
  - `Collection`
  - `Enumeration`
  - `Iterable`
  - `Iterator`
  - `Stream`
  - `Object`
  - array
- must be public
- must be static
- must not define any parameters
- must return a non-null Object

**Notes**

- may return mixed types


- `Argument.EMPTY` may be used in scenarios where the argument is irrelevant

#### Parallelism

Test argument parallelism (parallel test argument testing) can be defined with an annotation property `parallelism`

- The default `parallelism` value is `1`


- `parallelism` will be constrained to `verifyica.engine.argument.parallelism` 

**Notes**

- If the `@Verifyica.ArgumentSupplier` method returns a `null` object, the test class will be ignored/not reported.


- `Argument.EMTPY` can be used when the argument is not being used
  - The payload for `Argument.EMPTY` is `null`

Examples:

```java
@Verifyica.ArgumentSupplier
public static String arguments() {
    return "test";
}
```

```java
@Verifyica.ArgumentSupplier
public static Argument<?> arguments() {
    return Argument.of("test", "test");
}
```

```java
@Verifyica.ArgumentSupplier
public static Collection<String> arguments() {
    Collection<String> collection = new ArrayList<>();
    collection.add("test");
    return collection;
}
```

```java
@Verifyica.ArgumentSupplier
public static Object[] arguments() {
    Object[] objects = new Object[2];
    objects[0] = "foo";
    objects[1] = "bar";
    return objects;
}
```

```java
// Test 2 arguments in parallel/execution submission in order 
@Verifyica.ArgumentSupplier(parallelism = 2)
public static Collection<Argument<String>> arguments() {
    Collection<Argument<String>> collection = new ArrayList<>();

    for (int i = 0; i < 10; i++) {
        collection.add(Argument.ofString("String " + i));
    }

    return collection;
}
```

```java
@Verifyica.ArgumentSupplier
public static Object arguments() {
    return Argument.empty();
}
```

---

### @Verifyica.Prepare / @Verifyica.Conclude

`@Verifyica.Prepare` methods are executed **once** before a test class is tested.

`@Verifyica.Conclude` methods are executed **once** after a test class is tested.

All methods annotated with `@Verifyica.Prepare` or `@Verifyica.Conclude`:

- optional
- one method per class
- must return `void`
- must be public
- may be static
- must define a single parameter [ClassContext](api/src/main/java/org/verifyica/api/ClassContext.java)
- may throw `Throwable`

**Notes**

When using test class inheritance, `@Verifyica.Prepare` and `@Verifyica.Conclude` are hierarchical.

- `@Verifyica.Prepare`
  - superclass method before subclass method


- `@Verifyica.Conclude`
  - subclass method before superclass method
 
---

### @Verifyica.BeforeAll / @Verifyica.AfterAll

`@Verifyica.BeforeAll` methods are executed **once for each test argument** before test methods.

`@Verifyica.AfterAll` methods are executed **once for each test argument** after test methods;

All methods annotated with `@Verifyica.BeforeAll` or `@Verifyica.AfterAll`:

- optional
- one method per class
- must return `void`
- must be public
- must not be static
- must defined a single parameter [ArgumentContext](api/src/main/java/org/verifyica/api/ArgumentContext.java)
- may throw `Throwable`

**Notes**

When using test class inheritance, `@Verifyica.BeforeAll` and `@Verifyica.AfterAll` are hierarchical.

- `@Verifyica.BeforeAll`
  - superclass method before subclass method


- `@Verifyica.AfterAll`
  - subclass method before superclass method

---

### @Verifyica.BeforeEach / @Verifyica.AfterEach

`@Verifyica.BeforeEach` methods are executed **once** before each `@Verifyica.Test` test method.

`@Verifyica.AfterEach` methods are executed **once after each `@Verifyica.Test` test method;

All methods annotated with `@Verifyica.BeforeEach` or `@Verifyica.AfterEach`:

- optional
- one method per class
- must return `void`
- must be public
- must not be static
- must defined a single parameter [ArgumentContext](api/src/main/java/org/verifyica/api/ArgumentContext.java)
- may throw `Throwable`

**Notes**

When using test class inheritance, `@Verifyica.BeforeEach` and `@Verifyica.AfterEach` are hierarchical.

- `@Verifyica.BeforeEach`
  - superclass method before subclass method


- `@Verifyica.AfterEach`
  - subclass method before superclass method

---

### @Verifyica.Test

All methods annotated with `@Verifyica.Test`:

- at least 1 `@Verifyica.Test` method is required for concrete classes
- must return `void`
- must be public
- must not be static
- must defined a single parameter [ArgumentContext](api/src/main/java/org/verifyica/api/ArgumentContext.java)
- may throw `Throwable`

**Notes**

- `@Verifyica.Test` methods are not hierarchical

---

### @Verifyica.Order

Used by Verifyica to order test classes / test methods.

- optional

**Notes**

- Order is relative to the class which declares the methods for the following annotations...
  - `@Verifyica.Prepare`
  - `@Verifyica.BeforeAll`
  - `@Verifyica.BeforeEach`
  - `@Verifyica.AfterEach`
  - `@Verifyica.AfterAll`
  - `@Verifyica.Conclude`


- `@Verifyica.Test` test method ordering is irrespective of which class (superclass / subclass) the method is defined.


- If `verifyica.engine.class.parallelism` is greater than `1`, orders test class **execution submission order**.
  - Test class execution will still be in parallel.

- A method with `@Verifyica.Order` and `@Verifyica.Step` will generate an error
  - use one or thr other

---

### @Verifyica.DisplayName

Used by Verifyica to set the test class / test method display name.

- optional

**Notes**

- Used for test class / test method ordering if `@Verifyica.Order` is not declared.

---

### @Verifyica.Tag

Repeatable annotation used to tag test classes for filtering.

- optional
- values are trimmed of leading / trailing whitespace
- tags with empty values are ignored

---

### @Verifyica.Disabled

Indicates the Verifyica that a test class / test method is disabled/do not test.

- optional

---

### @Verifyica.ClassInterceptorSupplier

Used to register a test class specific [ClassInterceptor](api/src/main/java/org/verifyica/api/interceptor/ClassInterceptor.java)

- optional
- may return a `Stream`, `Iterable`, `Collection`, arrays, or single `ClassInterceptor` instance
- must be public
- must be static
- must not define any parameters
- may throw `Throwable`

---

### @Verifyica.Step

Used to define _scenario_ test method execution order. The `id` -> `nextId` defines a DAG (Directed Acyclic Graph).

- optional
- `id` is required
  - must not effectively be blank
- `nextId` defines the next _step_ in the _scenario_
  - an empty `nextId` signals the end of the scenario 

**Notes**

- A test class can only have `id` -> `nextId` mappings that define a single DAG
  - multiple DAGs are not supported

- Only one test method can have an empty `nextId`

- A test method with `@Verifyica.Step` and `@Verifyica.Order` will generate an error
  - use one or thr other 

- Test methods with `@Verifyica.Disabled` are removed after the DAG is generated

### @Verifyica.IndependentTests

Used to indicate that test methods in a test class are independent of each other.

- for a test class
  - order the test methods
  - execute the test methods sequentially
  - all test methods are executed

---

### @Verifyica.Testable

Used to mark a class as having inner test classes when using IntelliJ.

---

Copyright (C) 2024 The Verifyica project authors
