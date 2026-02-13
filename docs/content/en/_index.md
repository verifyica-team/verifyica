---
title: "Verifyica"
linkTitle: "Home"
---

{{< blocks/cover title="Verifyica" image_anchor="top" height="full" >}}
<a class="btn btn-lg btn-primary me-3 mb-4" href="/docs/">
  Learn More <i class="fas fa-arrow-alt-circle-right ms-2"></i>
</a>
<a class="btn btn-lg btn-secondary me-3 mb-4" href="/docs/getting-started/">
  Get Started <i class="fab fa-github ms-2"></i>
</a>
<p class="lead mt-5">JUnit Platform based TestEngine for argument-driven testing with advanced lifecycle management</p>
{{< /blocks/cover >}}

{{% blocks/lead color="primary" %}}
Verifyica provides a powerful framework for parameterized testing that goes beyond traditional approaches. Write tests once, run them across multiple arguments with complete lifecycle control, advanced parallelism, and sophisticated dependency management.
{{% /blocks/lead %}}

{{% blocks/section color="dark" type="row" %}}

{{% blocks/feature icon="fa-lightbulb" title="Argument-Driven Testing" %}}
Write tests that execute across multiple arguments with type-safe `Argument<T>` support and flexible suppliers.
{{% /blocks/feature %}}

{{% blocks/feature icon="fa-cogs" title="Advanced Lifecycle" %}}
Complete lifecycle management with Prepare, BeforeAll, BeforeEach, Test, AfterEach, AfterAll, and Conclude phases for each argument.
{{% /blocks/feature %}}

{{% blocks/feature icon="fa-rocket" title="Parallel Execution" %}}
Fine-grained parallelism control at class, argument, and test levels with configurable thread pools.
{{% /blocks/feature %}}

{{% blocks/feature icon="fa-link" title="Test Dependencies" %}}
Define explicit test dependencies and execution order with `@DependsOn` and `@Order` annotations.
{{% /blocks/feature %}}

{{% blocks/feature icon="fa-plug" title="Interceptors" %}}
Hook into test lifecycle with ClassInterceptor and EngineInterceptor for cross-cutting concerns.
{{% /blocks/feature %}}

{{% blocks/feature icon="fa-shield-alt" title="Production Ready" %}}
Used in production with comprehensive test coverage, minimal dependencies, and Java 8+ compatibility.
{{% /blocks/feature %}}

{{% /blocks/section %}}

{{% blocks/section %}}

## Quick Example

```java
public class ParallelArgumentTest {

    @Verifyica.ArgumentSupplier(parallelism = 2)
    public static Object arguments() {
        Collection<String> collection = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            collection.add("string-" + i);
        }
        return collection;
    }

    @Verifyica.BeforeAll
    public void beforeAll(String argument) {
        // Setup for this argument
    }

    @Verifyica.Test
    public void test1(String argument) {
        // Test with this argument
    }

    @Verifyica.AfterAll
    public void afterAll(String argument) {
        // Cleanup for this argument
    }
}
```

Each test method runs 10 times (once per argument) with 2 arguments executing in parallel.

{{% /blocks/section %}}

{{% blocks/section type="row" %}}

{{% blocks/feature icon="fab fa-github" title="Contributions Welcome!" url="https://github.com/verifyica-team/verifyica" %}}
We welcome contributions via **Pull Requests** on [GitHub](https://github.com/verifyica-team/verifyica)!
{{% /blocks/feature %}}

{{% blocks/feature icon="fab fa-maven" title="Download from Maven Central" url="https://central.sonatype.com/artifact/org.verifyica/verifyica-api" %}}
Production releases available on Maven Central
{{% /blocks/feature %}}

{{% blocks/feature icon="fa-comments" title="Join the Discussion" url="https://github.com/verifyica-team/verifyica/discussions" %}}
Have questions? Join our community discussions!
{{% /blocks/feature %}}

{{% /blocks/section %}}
