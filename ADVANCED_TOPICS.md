# Shared State

Every test method accepts a scoped [Context](api/src/main/java/org/antublue/verifyica/api/Context.java) which has a [Store](api/src/main/java/org/antublue/verifyica/api/Store.java).

- [EngineContext](api/src/main/java/org/antublue/verifyica/api/EngineContext.java)
  - globally scoped to the engine 


- [ClassContext](api/src/main/java/org/antublue/verifyica/api/ClassContext.java)
  - scoped to the test instance

- [ArgumentContext](api/src/main/java/org/antublue/verifyica/api/ArgumentContext.java)
  - scoped to the test argument

The [Store](api/src/main/java/org/antublue/verifyica/api/Store.java) should be used to maintain any test stateTracker to allow parallel test argument execution.

`AutoCloseable` object values in a [Store](api/src/main/java/org/antublue/verifyica/api/Store.java) are automatically closed.

# Concurrency

## ConcurrencySupport

There is no `@Verifyica.ResourceLock` annotation be design, because complex locking strategies can't be implemented using an annotation-based approach.

[ConcurrencySupport.java](api/src/main/java/org/antublue/verifyica/api/concurrency/ConcurrencySupport.java) provides programmatic execute code in a Lock or Semaphore

### Simple method locking

Example solution:

- test 1 is not in a lock boundary
- test 2 is in write lock boundary

```java
import org.antublue.verifyica.api.Argument;
import org.antublue.verifyica.api.ArgumentContext;
import org.antublue.verifyica.api.concurrency.ConcurrencySupport;
import org.antublue.verifyica.api.Verifyica;

import java.util.ArrayList;
import java.util.Collection;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;

/** Example test */
public class LocksTest2 {

  private static final String LOCK_KEY = LocksTest2.class.getName() + ".lockKey";

  @Verifyica.ArgumentSupplier(parallelism = 10)
  public static Collection<Argument<String>> arguments() {
    Collection<Argument<String>> collection = new ArrayList<>();

    for (int i = 0; i < 10; i++) {
      collection.add(Argument.ofString("String " + i));
    }

    return collection;
  }

  @Verifyica.Test
  public void test1(ArgumentContext argumentContext) throws Throwable {
    System.out.println(format("test1(%s)", argumentContext.getTestArgument()));

    assertThat(argumentContext).isNotNull();
    assertThat(argumentContext.getStore()).isNotNull();
    assertThat(argumentContext.getTestArgument()).isNotNull();
  }

  @Verifyica.Test
  public void test2(ArgumentContext argumentContext) throws Throwable {
    ConcurrencySupport.executeWithLock(
            argumentContext.getClassContext(),
            (Callable<Void>)
                    () -> {
                      System.out.println(
                              format("test2(%s) locked", argumentContext.getTestArgument()));

                      System.out.println(
                              format("test2(%s)", argumentContext.getTestArgument()));

                      assertThat(argumentContext).isNotNull();
                      assertThat(argumentContext.getStore()).isNotNull();
                      assertThat(argumentContext.getTestArgument()).isNotNull();

                      Thread.sleep(500);

                      System.out.println(
                              format("test2(%s) unlocked", argumentContext.getTestArgument()));
                      
                      return null;
                    });
  }

  @Verifyica.Test
  public void test3(ArgumentContext argumentContext) throws Throwable {
    System.out.println(format("test3(%s)", argumentContext.getTestArgument()));

    assertThat(argumentContext).isNotNull();
    assertThat(argumentContext.getStore()).isNotNull();
    assertThat(argumentContext.getTestArgument()).isNotNull();
  }
}
```

----

Example complex locking strategy

- locking a set of methods (complex lock boundary)
  - lock read
    - test 1
      - lock write
        - test 2
      - unlock write
    - test 2
  - unlock read

- dynamically named locks
  - argument-based

---

Example solution:

- test 1 and test 2 are in a read lock boundary scoped to the class
- test 3 is in the read lock boundary and a write lock boundary

```java
import org.antublue.verifyica.api.Argument;
import org.antublue.verifyica.api.ArgumentContext;
import org.antublue.verifyica.api.Verifyica;
import org.antublue.verifyica.api.concurrency.ConcurrencySupport;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Callable;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;

/** Example test */
public class ReadWriteLockTest1 {

  @Verifyica.ArgumentSupplier(parallelism = 10)
  public static Collection<Argument<String>> arguments() {
    Collection<Argument<String>> collection = new ArrayList<>();

    for (int i = 0; i < 10; i++) {
      collection.add(Argument.ofString("String " + i));
    }

    return collection;
  }

  @Verifyica.Test
  public void test1(ArgumentContext argumentContext) throws Throwable {
    argumentContext.getClassContext().getReadWriteLock().readLock().lock();

    System.out.println(format("test1(%s) read locked", argumentContext.getTestArgument()));

    assertThat(argumentContext).isNotNull();
    assertThat(argumentContext.getStore()).isNotNull();
    assertThat(argumentContext.getTestArgument()).isNotNull();
  }

  @Verifyica.Test
  public void test2(ArgumentContext argumentContext) throws Throwable {
    ConcurrencySupport.executeWithLock(
            "writeLock",
            (Callable<Void>)
                    () -> {
                      System.out.println(
                              format("test2(%s) write locked", argumentContext.getTestArgument()));

                      System.out.println(
                              format("test2(%s)", argumentContext.getTestArgument()));

                      assertThat(argumentContext).isNotNull();
                      assertThat(argumentContext.getStore()).isNotNull();
                      assertThat(argumentContext.getTestArgument()).isNotNull();

                      Thread.sleep(500);

                      System.out.println(
                              format(
                                      "test2(%s) write unlocked",
                                      argumentContext.getTestArgument()));

                      return null;
                    });
  }

  @Verifyica.Test
  public void test3(ArgumentContext argumentContext) throws Throwable {
    try {
      System.out.println(format("test3(%s) read locked", argumentContext.getTestArgument()));

      assertThat(argumentContext).isNotNull();
      assertThat(argumentContext.getStore()).isNotNull();
      assertThat(argumentContext.getTestArgument()).isNotNull();

      System.out.println(format("test1(%s) read unlocked", argumentContext.getTestArgument()));
    } finally {
      argumentContext.getClassContext().getReadWriteLock().readLock().unlock();
    }
  }
}
```

## Semaphores

Maybe you don't need a lock, but a semaphore because a test method generates significant load

---

Example solution:

- test 3 is in a semaphore boundary scoped to the class

```java
import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.Semaphore;
import org.antublue.verifyica.api.Argument;
import org.antublue.verifyica.api.ArgumentContext;
import org.antublue.verifyica.api.Verifyica;
import org.antublue.verifyica.api.concurrency.ConcurrencySupport;

/** Example test */
public class StoreSemaphoreTest {

  @Verifyica.ArgumentSupplier(parallelism = 10)
  public static Collection<Argument<String>> arguments() {
    Collection<Argument<String>> collection = new ArrayList<>();

    for (int i = 0; i < 10; i++) {
      collection.add(Argument.ofString("String " + i));
    }

    return collection;
  }

  @Verifyica.Test
  public void test1(ArgumentContext argumentContext) throws Throwable {
    System.out.println(format("test1(%s)", argumentContext.getTestArgument()));

    assertThat(argumentContext).isNotNull();
    assertThat(argumentContext.getStore()).isNotNull();
    assertThat(argumentContext.getTestArgument()).isNotNull();
  }

  @Verifyica.Test
  public void test2(ArgumentContext argumentContext) throws Throwable {
    Semaphore semaphore = getSemaphore(argumentContext);

    ConcurrencySupport.executeInSemaphore(
            semaphore,
            (Callable<Void>) () -> {
              System.out.println(format("test2(%s)", argumentContext.getTestArgument()));

              assertThat(argumentContext).isNotNull();
              assertThat(argumentContext.getStore()).isNotNull();
              assertThat(argumentContext.getTestArgument()).isNotNull();

              Thread.sleep(500);

              return null;
            });
  }

  @Verifyica.Test
  public void test3(ArgumentContext argumentContext) throws Throwable {
    System.out.println(format("test3(%s)", argumentContext.getTestArgument()));

    assertThat(argumentContext).isNotNull();
    assertThat(argumentContext.getStore()).isNotNull();
    assertThat(argumentContext.getTestArgument()).isNotNull();
  }

  /**
   * Method to get or create a class level Semaphore
   *
   * @param argumentContext argumentContext
   * @return a Semaphore
   * @throws Throwable Throwable
   */
  private Semaphore getSemaphore(ArgumentContext argumentContext) throws Throwable {
    return argumentContext
            .getClassContext()
            .getStore()
            .computeIfAbsent("semaphore", key -> new Semaphore(1), Semaphore.class);
  }
}
```
