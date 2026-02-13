---
title: "Simple Tests"
linkTitle: "Simple Tests"
weight: 1
description: >
  Basic test examples with sequential execution
---

Basic test patterns using sequential argument execution.

## Sequential Argument Test

Sequential execution processes one argument at a time.

### Example Code

```java
package org.verifyica.examples.simple;

import java.util.ArrayList;
import java.util.Collection;
import org.verifyica.api.Verifyica;

public class SequentialArgumentTest {

    @Verifyica.ArgumentSupplier
    public static Object arguments() {
        Collection<String> collection = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            collection.add("string-" + i);
        }

        return collection;
    }

    @Verifyica.Prepare
    public void prepare() {
        // Called once before all arguments
        System.out.println("prepare()");
    }

    @Verifyica.BeforeAll
    public void beforeAll(String argument) {
        System.out.println("beforeAll() argument [" + argument + "]");
    }

    @Verifyica.BeforeEach
    public void beforeEach(String argument) {
        System.out.println("beforeEach() argument [" + argument + "]");
    }

    @Verifyica.Test
    public void test1(String argument) {
        System.out.println("test1() argument [" + argument + "]");
    }

    @Verifyica.Test
    public void test2(String argument) {
        System.out.println("test2() argument [" + argument + "]");
    }

    @Verifyica.Test
    public void test3(String argument) {
        System.out.println("test3() argument [" + argument + "]");
    }

    @Verifyica.AfterEach
    public void afterEach(String argument) {
        System.out.println("afterEach() argument [" + argument + "]");
    }

    @Verifyica.AfterAll
    public void afterAll(String argument) {
        System.out.println("afterAll() argument [" + argument + "]");
    }

    @Verifyica.Conclude
    public void conclude() {
        // Called once after all arguments
        System.out.println("conclude()");
    }
}
```

### Execution Flow

For 10 arguments, the execution flow is:

1. `prepare()` - Once
2. For each argument (string-0 through string-9):
   - `beforeAll(argument)`
   - For each test method:
     - `beforeEach(argument)`
     - `test1/2/3(argument)`
     - `afterEach(argument)`
   - `afterAll(argument)`
3. `conclude()` - Once

### Key Features

**Sequential Processing**
- Arguments are processed one at a time
- No parallelism (default behavior)
- Predictable execution order

**Unwrapped Arguments**
- Test methods receive `String argument` directly
- No need for `ArgumentContext` wrapper
- Simpler method signatures

**Complete Lifecycle**
- All 7 lifecycle phases demonstrated
- Prepare and Conclude run once per test class
- BeforeAll/AfterAll run once per argument
- BeforeEach/AfterEach run for each test method

## When to Use Sequential Tests

Sequential tests are appropriate when:

- Arguments have dependencies on each other
- Shared resources don't support concurrent access
- Test execution order matters
- Debugging parallel execution issues
- Resource constraints prevent parallelism

## See Also

- [Parallel Tests](../parallel-tests/) - Parallel execution patterns
- [Core Concepts → Lifecycle](../../core-concepts/lifecycle/) - Lifecycle phases
- [Core Concepts → Arguments](../../core-concepts/arguments/) - Working with arguments
