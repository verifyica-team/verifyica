---
title: "Argument API"
linkTitle: "Argument API"
weight: 2
description: >
  Complete API reference for the Argument<T> interface
---

The `Argument<T>` interface provides type-safe containers for test data.

## Interface Definition

```java
public interface Argument<T> {
    String getName();
    T getPayload();
    boolean hasPayload();
    <V> V getPayloadAs(Class<V> type);
}
```

## Methods

### getName()

Returns the display name of this argument.

```java
Argument<String> arg = Argument.of("test-name", "value");
String name = arg.getName(); // Returns "test-name"
```

### getPayload()

Returns the payload value.

```java
Argument<Config> arg = Argument.of("config", new Config());
Config config = arg.getPayload();
```

### hasPayload()

Checks if the payload is non-null.

```java
Argument<String> arg = Argument.of("name", null);
boolean has = arg.hasPayload(); // Returns false
```

### getPayloadAs(Class<V>)

Casts the payload to a specific type.

```java
Argument<?> arg = Argument.of("name", new DatabaseConfig());
DatabaseConfig config = arg.getPayloadAs(DatabaseConfig.class);
```

## Factory Methods

### Generic Arguments

```java
Argument<T> of(String name, T payload)
```

Creates a named argument with a payload.

### Primitive Type Factory Methods

```java
Argument<Boolean> ofBoolean(boolean value)
Argument<Integer> ofInt(int value)
Argument<Long> ofLong(long value)
Argument<Double> ofDouble(double value)
Argument<String> ofString(String value)
```

### BigDecimal and BigInteger

```java
Argument<BigInteger> ofBigInteger(String value)
Argument<BigDecimal> ofBigDecimal(String value)
```

## See Also

- [Arguments](../../core-concepts/arguments/) - Using arguments in tests
- [Argument Suppliers](../../core-concepts/arguments/#argument-suppliers) - Providing arguments
