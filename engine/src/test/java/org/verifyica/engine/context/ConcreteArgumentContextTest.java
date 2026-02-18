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

import org.junit.jupiter.api.*;
import org.verifyica.api.Argument;
import org.verifyica.api.ClassContext;
import org.verifyica.api.Configuration;

@DisplayName("ConcreteArgumentContext Tests")
public class ConcreteArgumentContextTest {

    private ClassContext mockClassContext;
    private Configuration mockConfiguration;
    private Argument<String> argument;
    private ConcreteArgumentContext context;

    @BeforeEach
    public void setUp() {
        mockClassContext = mock(ClassContext.class);
        mockConfiguration = mock(Configuration.class);
        when(mockClassContext.getConfiguration()).thenReturn(mockConfiguration);

        argument = Argument.of("test-arg", "test value");
        context = new ConcreteArgumentContext(mockClassContext, 0, argument);
    }

    @Nested
    @DisplayName("Constructor Tests")
    public class ConstructorTests {

        @Test
        @DisplayName("Should create context with valid parameters")
        public void shouldCreateContextWithValidParameters() {
            assertThat(context).isNotNull().satisfies(ctx -> {
                assertThat(ctx.getClassContext()).isEqualTo(mockClassContext);
                assertThat(ctx.getArgumentIndex()).isEqualTo(0);
                assertThat(ctx.getArgument()).isEqualTo(argument);
            });
        }

        @Test
        @DisplayName("Should handle various argument indices")
        public void shouldHandleVariousArgumentIndices() {
            ConcreteArgumentContext ctx0 = new ConcreteArgumentContext(mockClassContext, 0, argument);
            ConcreteArgumentContext ctx5 = new ConcreteArgumentContext(mockClassContext, 5, argument);
            ConcreteArgumentContext ctx100 = new ConcreteArgumentContext(mockClassContext, 100, argument);

            assertThat(ctx0.getArgumentIndex()).isEqualTo(0);
            assertThat(ctx5.getArgumentIndex()).isEqualTo(5);
            assertThat(ctx100.getArgumentIndex()).isEqualTo(100);
        }
    }

    @Nested
    @DisplayName("Class Context Tests")
    public class ClassContextTests {

        @Test
        @DisplayName("Should return correct public class context")
        public void shouldReturnCorrectClassContext() {
            assertThat(context.getClassContext()).isSameAs(mockClassContext);
        }
    }

    @Nested
    @DisplayName("Configuration Tests")
    public class ConfigurationTests {

        @Test
        @DisplayName("Should return configuration from public class context")
        public void shouldReturnConfigurationFromClassContext() {
            Configuration config = context.getConfiguration();

            assertThat(config).isSameAs(mockConfiguration);
            verify(mockClassContext).getConfiguration();
        }
    }

    @Nested
    @DisplayName("Argument Index Tests")
    public class ArgumentIndexTests {

        @Test
        @DisplayName("Should return correct argument index")
        public void shouldReturnCorrectArgumentIndex() {
            assertThat(context.getArgumentIndex()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Argument Tests")
    public class ArgumentTests {

        @Test
        @DisplayName("Should return correct argument")
        public void shouldReturnCorrectArgument() {
            Argument<?> retrieved = context.getArgument();

            assertThat(retrieved).isEqualTo(argument).satisfies(arg -> {
                assertThat(arg.getName()).isEqualTo("test-arg");
                assertThat(arg.getPayload()).isEqualTo("test value");
            });
        }

        @Test
        @DisplayName("Should cast argument to correct type")
        public void shouldCastArgumentToCorrectType() {
            Argument<String> typedArg = context.getArgumentAs(String.class);

            assertThat(typedArg).isNotNull().satisfies(arg -> {
                assertThat(arg.getName()).isEqualTo("test-arg");
                assertThat(arg.getPayload()).isEqualTo("test value");
            });
        }

        @Test
        @DisplayName("Should throw exception when casting to incompatible type")
        public void shouldThrowExceptionWhenCastingToIncompatibleType() {
            assertThatThrownBy(() -> context.getArgumentAs(Integer.class))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Cannot cast Argument<")
                    .hasMessageContaining("java.lang.String")
                    .hasMessageContaining("java.lang.Integer");
        }

        @Test
        @DisplayName("Should throw exception when type is null")
        public void shouldThrowExceptionWhenTypeIsNull() {
            assertThatThrownBy(() -> context.getArgumentAs(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("type is null");
        }

        @Test
        @DisplayName("Should handle argument with null payload")
        public void shouldHandleArgumentWithNullPayload() {
            Argument<String> nullArg = Argument.of("null-arg", null);
            ConcreteArgumentContext nullContext = new ConcreteArgumentContext(mockClassContext, 0, nullArg);

            Argument<String> retrieved = nullContext.getArgumentAs(String.class);

            assertThat(retrieved).isNotNull().satisfies(arg -> {
                assertThat(arg.getName()).isEqualTo("null-arg");
                assertThat(arg.getPayload()).isNull();
            });
        }

        @Test
        @DisplayName("Should handle argument with compatible subtype")
        public void shouldHandleArgumentWithCompatibleSubtype() {
            Argument<Integer> intArg = Argument.of("int-arg", 42);
            ConcreteArgumentContext intContext = new ConcreteArgumentContext(mockClassContext, 0, intArg);

            // Number is supertype of Integer, should work
            Argument<Number> numberArg = intContext.getArgumentAs(Number.class);

            assertThat(numberArg).isNotNull().satisfies(arg -> {
                assertThat(arg.getPayload()).isEqualTo(42);
            });
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
        @DisplayName("Should be equal to context with same values")
        public void shouldBeEqualToContextWithSameValues() {
            ConcreteArgumentContext other = new ConcreteArgumentContext(mockClassContext, 0, argument);

            assertThat(context).isEqualTo(other).hasSameHashCodeAs(other);
        }

        @Test
        @DisplayName("Should not be equal to context with different index")
        public void shouldNotBeEqualToContextWithDifferentIndex() {
            ConcreteArgumentContext other = new ConcreteArgumentContext(mockClassContext, 1, argument);

            assertThat(context).isNotEqualTo(other);
        }

        @Test
        @DisplayName("Should not be equal to context with different argument")
        public void shouldNotBeEqualToContextWithDifferentArgument() {
            Argument<String> differentArg = Argument.of("different", "different value");
            ConcreteArgumentContext other = new ConcreteArgumentContext(mockClassContext, 0, differentArg);

            assertThat(context).isNotEqualTo(other);
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
        @DisplayName("Should include argument information in toString")
        public void shouldIncludeArgumentInformationInToString() {
            String toString = context.toString();

            assertThat(toString)
                    .contains("ConcreteArgumentContext")
                    .contains("classContext")
                    .contains("argumentIndex")
                    .contains("argument");
        }
    }

    @Nested
    @DisplayName("Map Tests")
    public class MapTests {

        @Test
        @DisplayName("Should store and retrieve argument-level state")
        public void shouldStoreAndRetrieveArgumentLevelState() {
            context.getMap().put("argKey", "argValue");

            assertThat(context.getMap()).containsEntry("argKey", "argValue");
        }

        @Test
        @DisplayName("Should isolate state between different argument contexts")
        public void shouldIsolateStateBetweenDifferentArgumentContexts() {
            ConcreteArgumentContext context1 = new ConcreteArgumentContext(mockClassContext, 0, argument);
            ConcreteArgumentContext context2 = new ConcreteArgumentContext(mockClassContext, 1, argument);

            context1.getMap().put("key", "value1");
            context2.getMap().put("key", "value2");

            assertThat(context1.getMap().get("key")).isEqualTo("value1");
            assertThat(context2.getMap().get("key")).isEqualTo("value2");
        }
    }
}
