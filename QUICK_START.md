# Quick Start

# Configure the Maven Surefire Plugin

Configure the Maven Surefire Plugin in your `pom.xml` to include only JUnit tests and enable automatic detection of JUnit Jupiter extensions:

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <version>3.5.2</version>
    <configuration>
        <!-- Only include JUnit tests -->
        <includes>
            <include>%regex[.*org.junit.*]</include>
        </includes>
        <systemPropertyVariables>
            <junit.jupiter.extensions.autodetection.enabled>true
            </junit.jupiter.extensions.autodetection.enabled>
        </systemPropertyVariables>
    </configuration>
</plugin>
```

# Configure the Verifyica Maven Plugin

Configure the Verifyica Maven Plugin in your `pom.xml`. The plugin will only run Verifyica tests.

```xml
<plugin>
    <groupId>org.verifyica</groupId>
    <artifactId>verifyica-maven-plugin</artifactId>
    <version>VERSION</version>
    <executions>
        <execution>
            <phase>test</phase>
            <goals>
                <goal>test</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

# Add the Verifyica Dependencies

Add the Verifyica dependencies to your `pom.xml`.

```xml
<dependency>
    <groupId>org.verifyica</groupId>
    <artifactId>verifyica-api</artifactId>
    <version>VERSION</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.verifyica</groupId>
    <artifactId>verifyica-engine</artifactId>
    <version>VERSION</version>
    <scope>test</scope>
</dependency>
```

# Write a Verifyica Test

Create a Verifyica test class in your `src/test/java` directory. Here's an example of a simple Verifyica test:

Example of a Verifyica Test with Parallel Argument Execution of 2:

```java
package your.package.name;

import java.util.ArrayList;
import java.util.Collection;
import org.verifyica.api.ArgumentContext;
import org.verifyica.api.ClassContext;
import org.verifyica.api.Verifyica;
import org.verifyica.examples.support.Logger;

public class ParallelArgumentTest {

    private static final Logger LOGGER = Logger.createLogger(ParallelArgumentTest.class);

    // Run the arguments in parallel with a parallelism of 2
    @Verifyica.ArgumentSupplier(parallelism = 2)
    public static Object arguments() {
        Collection<String> collection = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            collection.add("string-" + i);
        }

        return collection;
    }

    @Verifyica.Prepare
    public void prepare(ClassContext classContext) {
        LOGGER.info("prepare()");
    }

    @Verifyica.BeforeAll
    public void beforeAll(ArgumentContext argumentContext) {
        LOGGER.info("beforeAll() argument [%s]", argumentContext.testArgument());
    }

    @Verifyica.BeforeEach
    public void beforeEach(ArgumentContext argumentContext) {
        LOGGER.info("beforeEach() argument [%s]", argumentContext.testArgument());
    }

    @Verifyica.Test
    public void test1(ArgumentContext argumentContext) {
        LOGGER.info("test1() argument [%s]", argumentContext.testArgument());
    }

    @Verifyica.Test
    public void test2(ArgumentContext argumentContext) {
        LOGGER.info("test2() argument [%s]", argumentContext.testArgument());
    }

    @Verifyica.Test
    public void test3(ArgumentContext argumentContext) {
        LOGGER.info("test3() argument [%s]", argumentContext.testArgument());
    }

    @Verifyica.AfterEach
    public void afterEach(ArgumentContext argumentContext) {
        LOGGER.info("afterEach() argument [%s]", argumentContext.testArgument());
    }

    @Verifyica.AfterAll
    public void afterAll(ArgumentContext argumentContext) {
        LOGGER.info("afterAll() argument [%s]", argumentContext.testArgument());
    }

    @Verifyica.Conclude
    public void conclude(ClassContext classContext) {
        LOGGER.info("conclude()");
    }
}
```

# Other Test Examples

For more information on the Verifyica API and how to write tests, refer to the tests and examples provided in the Verifyica repository.

- [Tests](https://github.com/verifyica-team/verifyica/tree/main/tests)
- [Examples](https://github.com/verifyica-team/verifyica/tree/main/examples)

# Annotations / API

For more information on the Verifyica API and available annotations, refer to the documentation below.

- [Verifyica Annotations](https://github.com/verifyica-team/verifyica/blob/main/ANNOTATIONS.md)
- [Verifyica API](https://github.com/verifyica-team/verifyica/blob/main/API.md)

---

Copyright (C) Verifyica project authors and contributors. All rights reserved.