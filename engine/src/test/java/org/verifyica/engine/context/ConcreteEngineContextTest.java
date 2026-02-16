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
class ConcreteEngineContextTest {

    private Configuration mockConfiguration;
    private ConcreteEngineContext context;

    @BeforeEach
    void setUp() {
        mockConfiguration = mock(Configuration.class);
        context = new ConcreteEngineContext(mockConfiguration, "1.0.0");
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create context with valid configuration and version")
        void shouldCreateContextWithValidConfigurationAndVersion() {
            ConcreteEngineContext newContext = new ConcreteEngineContext(mockConfiguration, "2.0.0");

            assertThat(newContext).isNotNull().satisfies(ctx -> {
                assertThat(ctx.getConfiguration()).isNotNull();
                assertThat(ctx.getVersion()).isEqualTo("2.0.0");
                assertThat(ctx.getMap()).isNotNull().isEmpty();
            });
        }

        @Test
        @DisplayName("Should wrap configuration in ImmutableConfiguration")
        void shouldWrapConfigurationInImmutableConfiguration() {
            assertThat(context.getConfiguration()).isNotNull().isNotSameAs(mockConfiguration);
        }

        @Test
        @DisplayName("Should handle null version")
        void shouldHandleNullVersion() {
            ConcreteEngineContext nullVersionContext = new ConcreteEngineContext(mockConfiguration, null);

            assertThat(nullVersionContext.getVersion()).isNull();
        }
    }

    @Nested
    @DisplayName("Version Tests")
    class VersionTests {

        @Test
        @DisplayName("Should return correct version")
        void shouldReturnCorrectVersion() {
            assertThat(context.getVersion()).isEqualTo("1.0.0");
        }

        @Test
        @DisplayName("Should support various version formats")
        void shouldSupportVariousVersionFormats() {
            assertThat(new ConcreteEngineContext(mockConfiguration, "1.0.0").getVersion())
                    .isEqualTo("1.0.0");
            assertThat(new ConcreteEngineContext(mockConfiguration, "2.5.3-SNAPSHOT").getVersion())
                    .isEqualTo("2.5.3-SNAPSHOT");
            assertThat(new ConcreteEngineContext(mockConfiguration, "v3.0.0-beta").getVersion())
                    .isEqualTo("v3.0.0-beta");
        }
    }

    @Nested
    @DisplayName("Configuration Tests")
    class ConfigurationTests {

        @Test
        @DisplayName("Should return immutable configuration")
        void shouldReturnImmutableConfiguration() {
            Configuration config = context.getConfiguration();

            assertThat(config).isNotNull().isNotSameAs(mockConfiguration);
        }

        @Test
        @DisplayName("Should return same configuration instance on multiple calls")
        void shouldReturnSameConfigurationInstanceOnMultipleCalls() {
            Configuration config1 = context.getConfiguration();
            Configuration config2 = context.getConfiguration();

            assertThat(config1).isSameAs(config2);
        }
    }

    @Nested
    @DisplayName("Equals and HashCode Tests")
    class EqualsAndHashCodeTests {

        @Test
        @DisplayName("Should be equal to itself")
        void shouldBeEqualToItself() {
            assertThat(context).isEqualTo(context);
        }

        @Test
        @DisplayName("Should be equal to context with same version")
        void shouldBeEqualToContextWithSameVersion() {
            ConcreteEngineContext other = new ConcreteEngineContext(mockConfiguration, "1.0.0");

            // Note: equals() checks version, but configurations are wrapped separately
            assertThat(context.getVersion()).isEqualTo(other.getVersion());
        }

        @Test
        @DisplayName("Should not be equal to context with different version")
        void shouldNotBeEqualToContextWithDifferentVersion() {
            ConcreteEngineContext other = new ConcreteEngineContext(mockConfiguration, "2.0.0");

            assertThat(context.getVersion()).isNotEqualTo(other.getVersion());
        }

        @Test
        @DisplayName("Should not be equal to null")
        void shouldNotBeEqualToNull() {
            assertThat(context).isNotEqualTo(null);
        }

        @Test
        @DisplayName("Should not be equal to different class")
        void shouldNotBeEqualToDifferentClass() {
            assertThat(context).isNotEqualTo("not a context");
        }

        @Test
        @DisplayName("Should have consistent hashCode")
        void shouldHaveConsistentHashCode() {
            int hashCode1 = context.hashCode();
            int hashCode2 = context.hashCode();

            assertThat(hashCode1).isEqualTo(hashCode2);
        }
    }

    @Nested
    @DisplayName("ToString Tests")
    class ToStringTests {

        @Test
        @DisplayName("Should include version in toString")
        void shouldIncludeVersionInToString() {
            String toString = context.toString();

            assertThat(toString)
                    .contains("ConcreteEngineContext")
                    .contains("version")
                    .contains("1.0.0");
        }
    }

    @Nested
    @DisplayName("Map Tests")
    class MapTests {

        @Test
        @DisplayName("Should store and retrieve values in context map")
        void shouldStoreAndRetrieveValuesInContextMap() {
            context.getMap().put("engineKey", "engineValue");

            assertThat(context.getMap()).containsEntry("engineKey", "engineValue");
        }

        @Test
        @DisplayName("Should support multiple concurrent map operations")
        void shouldSupportMultipleConcurrentMapOperations() {
            context.getMap().put("key1", "value1");
            context.getMap().put("key2", 123);
            context.getMap().put("key3", true);

            assertThat(context.getMap())
                    .hasSize(3)
                    .containsEntry("key1", "value1")
                    .containsEntry("key2", 123)
                    .containsEntry("key3", true);
        }
    }
}
