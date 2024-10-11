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
  - i.e. `Argument<?>` 


- `Argument.EMPTY` may be used in scenarios where the argument is irrelevant


- returning a `null` object will result in the test being completely ignore
  - will not run
  - will not be reported

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
- may throw `Throwable`

**Notes**

- `@Verifyica.Test` methods are not hierarchical

- Default ordering is by test method name (or `@verifyica.DisplayName` if used)

- Test methods with an `@Verifyica.Order` annotation are ordered before test methods without an `@Verifyica.Order` annotation

---

### @Verifyica.Independent

By default, test method execution is scenario-based.

- test methods are ordered
- direction selection of a test method executes all test methods that are ordered before the selected test method

Annotating a test method as `@Verifyica.Independent` allows execution of the test method in IntelliJ **without** executing previous test methods in the sequence.

**Notes**

- Use cautiously since it could break scenario requirements
  - test method 2 depends on test method 1, but test method 1 isn't executed
- IntelliJ has test method selection issues if multiple test methods are selected 
  - In these scenarios, all test methods will be executed 

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

- Test methods with an `@Verifyica.Order` annotation are ordered before test methods without an `@Verifyica.Order` annotation 

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
- may return a `Stream`, `Iterable`, `Collection`, array of `ClassInterceptor`, or a single `ClassInterceptor` instance
- must be public
- must be static
- must not define any parameters
- may throw `Throwable`

---

### @Verifyica.Testable

Used to mark a class as having inner test classes when using IntelliJ.

---

Copyright (C) 2024-present Verifyica project authors and contributors
