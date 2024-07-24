# Locking

There is no `@Verifyica.ResourceLock` annotation be design, because complex locking strategies can't be implemented using a annoation-based approach.

Example complex locking tragedies

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


Example solution:

```java
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.antublue.verifyica.api.Argument;
import org.antublue.verifyica.api.ArgumentContext;
import org.antublue.verifyica.api.Verifyica;

/** Example test */
@SuppressWarnings("unchecked")
public class ClassContextLockTest {

    private static final String READ_LOCK_KEY = "readLockKey";
    private static final String WRITE_LOCK_KEY = "writeLockKey";

    @Verifyica.ArgumentSupplier(parallelism = 10)
    public static Collection<Argument<String>> arguments() {
        Collection<Argument<String>> collection = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            collection.add(Argument.ofString("String " + i));
        }

        return collection;
    }

    @Verifyica.BeforeAll
    public void beforeAll(ArgumentContext argumentContext) {
        lockReadLock(argumentContext, READ_LOCK_KEY);
    }

    @Verifyica.Test
    public void test1(ArgumentContext argumentContext) throws Throwable {
        // ... test 1 code ...

        Thread.sleep(1000);
    }

    @Verifyica.Test
    public void test2(ArgumentContext argumentContext) throws Throwable {
        // ... test 2 code ...

        Thread.sleep(1000);
    }

    @Verifyica.Test
    public void test3(ArgumentContext argumentContext) throws Throwable {
        // ... test 3 code ...

        try {
            lockWriteLock(argumentContext, WRITE_LOCK_KEY);
            Thread.sleep(1000);
        } finally {
            unlockWriteLock(argumentContext, WRITE_LOCK_KEY);
        }
    }

    @Verifyica.AfterAll
    public void afterAll(ArgumentContext argumentContext) {
        unlockReadLock(argumentContext, READ_LOCK_KEY);
    }

    private static void lockReadLock(ArgumentContext argumentContext, String lockKey) {
        ReentrantReadWriteLock.class.cast(argumentContext
                .getClassContext()
                .getStore()
                .computeIfAbsent(lockKey, o -> new ReentrantReadWriteLock(true))).readLock().lock();
    }

    private static void unlockReadLock(ArgumentContext argumentContext, String lockKey) {
        argumentContext.getClassContext().getStore().get(lockKey, ReentrantReadWriteLock.class).readLock().unlock();
    }

    private static void lockWriteLock(ArgumentContext argumentContext, String lockKey) {
        ReentrantReadWriteLock.class.cast(argumentContext
                .getClassContext()
                .getStore()
                .computeIfAbsent(lockKey, o -> new ReentrantReadWriteLock(true))).writeLock().lock();
    }

    private static void unlockWriteLock(ArgumentContext argumentContext, String lockKey) {
        argumentContext.getClassContext().getStore().get(lockKey, ReentrantReadWriteLock.class).writeLock().unlock();
    }
}
```

