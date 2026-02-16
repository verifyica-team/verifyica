/*
 * Copyright (C) Verifyica project authors and contributors. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.verifyica.engine.context;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.*;
import org.verifyica.api.Configuration;
import org.verifyica.api.EngineContext;

@DisplayName("ConcreteClassContext Tests")
public class ConcreteClassContextTest {

    private EngineContext mockEngineContext;
    private Configuration mockConfiguration;
    private AtomicReference<Object> testInstanceRef;
    private ConcreteClassContext context;
    private Set<String> testTags;

    @BeforeEach
    public void setUp() {
        mockEngineContext = mock(EngineContext.class);
        mockConfiguration = mock(Configuration.class);
        when(mockEngineContext.getConfiguration()).thenReturn(mockConfiguration);

        testTags = new HashSet<>();
        testTags.add("tag1");
        testTags.add("tag2");

        testInstanceRef = new AtomicReference<>();

        context = new ConcreteClassContext(
                mockEngineContext,
                ConcreteClassContextTest.class,
                "Test Class Display Name",
                testTags,
                4,
                testInstanceRef);
    }

    @Nested
    @DisplayName("Constructor Tests")
    public class ConstructorTests {

        @Test
        @DisplayName("Should create context with valid parameters")
        public void shouldCreateContextWithValidParameters() {
            assertThat(context).isNotNull().satisfies(ctx -> {
                assertThat(ctx.getEngineContext()).isEqualTo(mockEngineContext);
                assertThat(ctx.getTestClass()).isEqualTo(ConcreteClassContextTest.class);
                assertThat(ctx.getTestClassDisplayName()).isEqualTo("Test Class Display Name");
                assertThat(ctx.getTestClassTags()).containsExactlyInAnyOrder("tag1", "tag2");
                assertThat(ctx.getTestArgumentParallelism()).isEqualTo(4);
            });
        }

        @Test
        @DisplayName("Should handle empty tags set")
        public void shouldHandleEmptyTagsSet() {
            ConcreteClassContext emptyTagsContext = new ConcreteClassContext(
                    mockEngineContext, String.class, "Display Name", new HashSet<>(), 1, new AtomicReference<>());

            assertThat(emptyTagsContext.getTestClassTags()).isEmpty();
        }

        @Test
        @DisplayName("Should handle parallelism of 1")
        public void shouldHandleParallelismOfOne() {
            ConcreteClassContext sequentialContext = new ConcreteClassContext(
                    mockEngineContext, String.class, "Display Name", new HashSet<>(), 1, new AtomicReference<>());

            assertThat(sequentialContext.getTestArgumentParallelism()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should handle high parallelism value")
        public void shouldHandleHighParallelismValue() {
            ConcreteClassContext highParallelismContext = new ConcreteClassContext(
                    mockEngineContext,
                    String.class,
                    "Display Name",
                    new HashSet<>(),
                    Integer.MAX_VALUE,
                    new AtomicReference<>());

            assertThat(highParallelismContext.getTestArgumentParallelism()).isEqualTo(Integer.MAX_VALUE);
        }
    }

    @Nested
    @DisplayName("Configuration Tests")
    public class ConfigurationTests {

        @Test
        @DisplayName("Should return configuration from engine context")
        public void shouldReturnConfigurationFromEngineContext() {
            Configuration config = context.getConfiguration();

            assertThat(config).isSameAs(mockConfiguration);
            verify(mockEngineContext).getConfiguration();
        }
    }

    @Nested
    @DisplayName("Test Instance Tests")
    public class TestInstanceTests {

        @Test
        @DisplayName("Should throw exception when instance not yet created")
        public void shouldThrowExceptionWhenInstanceNotYetCreated() {
            assertThatThrownBy(() -> context.getTestInstance())
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("The class instance has not yet been instantiated");
        }

        @Test
        @DisplayName("Should return test instance after it is set")
        public void shouldReturnTestInstanceAfterItIsSet() {
            Object testInstance = new Object();
            testInstanceRef.set(testInstance);

            Object retrieved = context.getTestInstance();

            assertThat(retrieved).isSameAs(testInstance);
        }

        @Test
        @DisplayName("Should cast test instance to specific type")
        public void shouldCastTestInstanceToSpecificType() {
            String testInstance = "test instance";
            testInstanceRef.set(testInstance);

            String retrieved = context.getTestInstanceAs(String.class);

            assertThat(retrieved).isEqualTo("test instance");
        }

        @Test
        @DisplayName("Should throw exception when casting to wrong type")
        public void shouldThrowExceptionWhenCastingToWrongType() {
            testInstanceRef.set("string instance");

            assertThatThrownBy(() -> context.getTestInstanceAs(Integer.class)).isInstanceOf(ClassCastException.class);
        }

        @Test
        @DisplayName("Should throw exception when getting typed instance before creation")
        public void shouldThrowExceptionWhenGettingTypedInstanceBeforeCreation() {
            assertThatThrownBy(() -> context.getTestInstanceAs(String.class))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("The class instance has not yet been instantiated");
        }
    }

    @Nested
    @DisplayName("Test Class Tests")
    public class TestClassTests {

        @Test
        @DisplayName("Should return correct test class")
        public void shouldReturnCorrectTestClass() {
            assertThat(context.getTestClass()).isEqualTo(ConcreteClassContextTest.class);
        }

        @Test
        @DisplayName("Should return correct display name")
        public void shouldReturnCorrectDisplayName() {
            assertThat(context.getTestClassDisplayName()).isEqualTo("Test Class Display Name");
        }

        @Test
        @DisplayName("Should return unmodifiable tags set")
        public void shouldReturnUnmodifiableTagsSet() {
            Set<String> tags = context.getTestClassTags();

            assertThat(tags).containsExactlyInAnyOrder("tag1", "tag2");
        }
    }

    @Nested
    @DisplayName("Parallelism Tests")
    public class ParallelismTests {

        @Test
        @DisplayName("Should return correct parallelism value")
        public void shouldReturnCorrectParallelismValue() {
            assertThat(context.getTestArgumentParallelism()).isEqualTo(4);
        }
    }

    @Nested
    @DisplayName("Equals and HashCode Tests")
    public class EqualsAndHashCodeTests {

        @Test
        @DisplayName("Should be equal to itself")
        public void shouldBeEqualToItself() {
            assertThat(context).isEqualTo(context);
        }

        @Test
        @DisplayName("Should not be equal to null")
        public void shouldNotBeEqualToNull() {
            assertThat(context).isNotEqualTo(null);
        }

        @Test
        @DisplayName("Should not be equal to different class")
        public void shouldNotBeEqualToDifferentClass() {
            assertThat(context).isNotEqualTo("not a context");
        }

        @Test
        @DisplayName("Should have consistent hashCode")
        public void shouldHaveConsistentHashCode() {
            int hashCode1 = context.hashCode();
            int hashCode2 = context.hashCode();

            assertThat(hashCode1).isEqualTo(hashCode2);
        }
    }

    @Nested
    @DisplayName("ToString Tests")
    public class ToStringTests {

        @Test
        @DisplayName("Should include public class information in toString")
        public void shouldIncludeClassInformationInToString() {
            String toString = context.toString();

            assertThat(toString)
                    .contains("ConcreteClassContext")
                    .contains("testClass")
                    .contains("testClassDisplayName")
                    .contains("testArgumentParallelism");
        }
    }

    @Nested
    @DisplayName("Map Tests")
    public class MapTests {

        @Test
        @DisplayName("Should store and retrieve class-level state")
        public void shouldStoreAndRetrieveClassLevelState() {
            context.getMap().put("classKey", "classValue");

            assertThat(context.getMap()).containsEntry("classKey", "classValue");
        }
    }
}
