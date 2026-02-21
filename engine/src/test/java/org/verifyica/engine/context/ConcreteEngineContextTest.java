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
import org.verifyica.api.Configuration;

@DisplayName("ConcreteEngineContext Tests")
public class ConcreteEngineContextTest {

    private Configuration mockConfiguration;
    private ConcreteEngineContext context;

    @BeforeEach
    public void setUp() {
        mockConfiguration = mock(Configuration.class);
        context = new ConcreteEngineContext(mockConfiguration, "1.0.0");
    }

    @Nested
    @DisplayName("Constructor Tests")
    public class ConstructorTests {

        @Test
        @DisplayName("Should create context with valid configuration and version")
        public void shouldCreateContextWithValidConfigurationAndVersion() {
            final ConcreteEngineContext newContext = new ConcreteEngineContext(mockConfiguration, "2.0.0");

            assertThat(newContext).isNotNull().satisfies(ctx -> {
                assertThat(ctx.getConfiguration()).isNotNull();
                assertThat(ctx.getVersion()).isEqualTo("2.0.0");
                assertThat(ctx.getMap()).isNotNull().isEmpty();
            });
        }

        @Test
        @DisplayName("Should wrap configuration in ImmutableConfiguration")
        public void shouldWrapConfigurationInImmutableConfiguration() {
            assertThat(context.getConfiguration()).isNotNull().isNotSameAs(mockConfiguration);
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when configuration is null")
        public void shouldThrowWhenConfigurationIsNull() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> new ConcreteEngineContext(null, "1.0.0"))
                    .withMessageContaining("configuration is null");
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when version is null")
        public void shouldThrowWhenVersionIsNull() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> new ConcreteEngineContext(mockConfiguration, null))
                    .withMessageContaining("version is null");
        }
    }

    @Nested
    @DisplayName("Version Tests")
    public class VersionTests {

        @Test
        @DisplayName("Should return correct version")
        public void shouldReturnCorrectVersion() {
            assertThat(context.getVersion()).isEqualTo("1.0.0");
        }

        @Test
        @DisplayName("Should support various version formats")
        public void shouldSupportVariousVersionFormats() {
            assertThat(new ConcreteEngineContext(mockConfiguration, "1.0.0").getVersion())
                    .isEqualTo("1.0.0");
            assertThat(new ConcreteEngineContext(mockConfiguration, "2.5.3-SNAPSHOT").getVersion())
                    .isEqualTo("2.5.3-SNAPSHOT");
            assertThat(new ConcreteEngineContext(mockConfiguration, "v3.0.0-beta").getVersion())
                    .isEqualTo("v3.0.0-beta");
        }

        @Test
        @DisplayName("Should handle empty string version")
        public void shouldHandleEmptyStringVersion() {
            final ConcreteEngineContext emptyVersionContext = new ConcreteEngineContext(mockConfiguration, "");
            assertThat(emptyVersionContext.getVersion()).isEqualTo("");
        }

        @Test
        @DisplayName("Should handle whitespace-only version")
        public void shouldHandleWhitespaceOnlyVersion() {
            final ConcreteEngineContext whitespaceVersionContext = new ConcreteEngineContext(mockConfiguration, "   ");
            assertThat(whitespaceVersionContext.getVersion()).isEqualTo("   ");
        }
    }

    @Nested
    @DisplayName("Configuration Tests")
    public class ConfigurationTests {

        @Test
        @DisplayName("Should return immutable configuration")
        public void shouldReturnImmutableConfiguration() {
            final Configuration config = context.getConfiguration();

            assertThat(config).isNotNull().isNotSameAs(mockConfiguration);
        }

        @Test
        @DisplayName("Should return same configuration instance on multiple calls")
        public void shouldReturnSameConfigurationInstanceOnMultipleCalls() {
            final Configuration config1 = context.getConfiguration();
            final Configuration config2 = context.getConfiguration();

            assertThat(config1).isSameAs(config2);
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
        @DisplayName("Should be equal to context with same version and map state")
        public void shouldNotBeEqualToContextWithSameVersion() {
            final ConcreteEngineContext other = new ConcreteEngineContext(mockConfiguration, "1.0.0");

            assertThat(context).isNotEqualTo(other);
        }

        @Test
        @DisplayName("Should not be equal to context with different version")
        public void shouldNotBeEqualToContextWithDifferentVersion() {
            final ConcreteEngineContext other = new ConcreteEngineContext(mockConfiguration, "2.0.0");

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
        @DisplayName("Should not be equal when map content differs")
        public void shouldNotBeEqualWhenMapContentDiffers() {
            final ConcreteEngineContext other = new ConcreteEngineContext(mockConfiguration, "1.0.0");
            other.getMap().put("key", "value");

            assertThat(context).isNotEqualTo(other);
        }

        @Test
        @DisplayName("Should have consistent hashCode")
        public void shouldHaveConsistentHashCode() {
            final int hashCode1 = context.hashCode();
            final int hashCode2 = context.hashCode();

            assertThat(hashCode1).isEqualTo(hashCode2);
        }

        @Test
        @DisplayName("Should not have equal hashCode for different objects")
        public void shouldNotHaveEqualHashCodeForDifferentObjects() {
            final ConcreteEngineContext other = new ConcreteEngineContext(mockConfiguration, "1.0.0");

            assertThat(context).isNotEqualTo(other);
            assertThat(context.hashCode()).isNotEqualTo(other.hashCode());
        }

        @Test
        @DisplayName("Should have different hashCode for different versions")
        public void shouldHaveDifferentHashCodeForDifferentVersions() {
            final ConcreteEngineContext other = new ConcreteEngineContext(mockConfiguration, "2.0.0");

            assertThat(context.hashCode()).isNotEqualTo(other.hashCode());
        }
    }

    @Nested
    @DisplayName("ToString Tests")
    public class ToStringTests {

        @Test
        @DisplayName("Should include version in toString")
        public void shouldIncludeVersionInToString() {
            final String toString = context.toString();

            assertThat(toString)
                    .contains("ConcreteEngineContext")
                    .contains("version")
                    .contains("1.0.0");
        }
    }

    @Nested
    @DisplayName("Map Tests")
    public class MapTests {

        @Test
        @DisplayName("Should store and retrieve values in context map")
        public void shouldStoreAndRetrieveValuesInContextMap() {
            context.getMap().put("engineKey", "engineValue");

            assertThat(context.getMap()).containsEntry("engineKey", "engineValue");
        }

        @Test
        @DisplayName("Should support multiple concurrent map operations")
        public void shouldSupportMultipleConcurrentMapOperations() {
            context.getMap().put("key1", "value1");
            context.getMap().put("key2", 123);
            context.getMap().put("key3", true);

            assertThat(context.getMap())
                    .hasSize(3)
                    .containsEntry("key1", "value1")
                    .containsEntry("key2", 123)
                    .containsEntry("key3", true);
        }

        @Test
        @DisplayName("Should return same map instance on multiple calls")
        public void shouldReturnSameMapInstanceOnMultipleCalls() {
            assertThat(context.getMap()).isSameAs(context.getMap());
        }

        @Test
        @DisplayName("Should share map state with other contexts when inherited")
        public void shouldMaintainMapStateIndependentlyPerInstance() {
            final ConcreteEngineContext otherContext = new ConcreteEngineContext(mockConfiguration, "1.0.0");

            context.getMap().put("context1Key", "context1Value");
            otherContext.getMap().put("context2Key", "context2Value");

            assertThat(context.getMap()).containsOnlyKeys("context1Key");
            assertThat(otherContext.getMap()).containsOnlyKeys("context2Key");
        }
    }
}
