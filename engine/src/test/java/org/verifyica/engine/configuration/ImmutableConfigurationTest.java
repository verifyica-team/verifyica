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

package org.verifyica.engine.configuration;

import static org.assertj.core.api.Assertions.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Properties;
import org.junit.jupiter.api.*;
import org.verifyica.api.Configuration;
import org.verifyica.engine.common.ImmutableProperties;

@DisplayName("ImmutableConfiguration Tests")
public class ImmutableConfigurationTest {

    // Java 8 compatible String repeat helper
    private static String repeat(String str, int count) {
        StringBuilder stringBuilder = new StringBuilder(str.length() * count);
        for (int i = 0; i < count; i++) {
            stringBuilder.append(str);
        }
        return stringBuilder.toString();
    }

    @Nested
    @DisplayName("Constructor Tests")
    public class ConstructorTests {

        @Test
        @DisplayName("Should create immutable configuration from configuration")
        public void shouldCreateImmutableConfigurationFromConfiguration() {
            Configuration source = createTestConfiguration("/path/to/config.properties");
            ImmutableConfiguration immutable = new ImmutableConfiguration(source);

            assertThat(immutable).isNotNull();
        }

        @Test
        @DisplayName("Should create immutable configuration with null path")
        public void shouldCreateImmutableConfigurationWithNullPath() {
            Configuration source = createTestConfiguration(null);
            ImmutableConfiguration immutable = new ImmutableConfiguration(source);

            assertThat(immutable).isNotNull();
            assertThat(immutable.getPropertiesPath()).isEmpty();
        }

        @Test
        @DisplayName("Should create immutable configuration with empty properties")
        public void shouldCreateImmutableConfigurationWithEmptyProperties() {
            Configuration source = createTestConfigurationWithProperties(null, new Properties());
            ImmutableConfiguration immutable = new ImmutableConfiguration(source);

            assertThat(immutable).isNotNull();
            assertThat(immutable.getProperties()).isEmpty();
        }

        @Test
        @DisplayName("Should copy properties from source configuration")
        public void shouldCopyPropertiesFromSourceConfiguration() {
            Properties sourceProps = new Properties();
            sourceProps.setProperty("key1", "value1");
            sourceProps.setProperty("key2", "value2");

            Configuration source = createTestConfigurationWithProperties("/path/to/config.properties", sourceProps);
            ImmutableConfiguration immutable = new ImmutableConfiguration(source);

            assertThat(immutable.getProperties().getProperty("key1")).isEqualTo("value1");
            assertThat(immutable.getProperties().getProperty("key2")).isEqualTo("value2");
        }
    }

    @Nested
    @DisplayName("Properties Path Delegation Tests")
    public class PropertiesPathDelegationTests {

        @Test
        @DisplayName("Should return empty Optional when path is null")
        public void shouldReturnEmptyOptionalWhenPathIsNull() {
            Configuration source = createTestConfiguration(null);
            ImmutableConfiguration immutable = new ImmutableConfiguration(source);

            Optional<Path> path = immutable.getPropertiesPath();

            assertThat(path).isEmpty();
        }

        @Test
        @DisplayName("Should return Optional with path when path exists")
        public void shouldReturnOptionalWithPathWhenPathExists() {
            String pathString = "/test/path/verifyica.properties";
            Configuration source = createTestConfiguration(pathString);
            ImmutableConfiguration immutable = new ImmutableConfiguration(source);

            Optional<Path> path = immutable.getPropertiesPath();

            assertThat(path).isPresent();
            assertThat(path.get().toString()).isEqualTo(pathString);
        }

        @Test
        @DisplayName("Should maintain path consistency across multiple calls")
        public void shouldMaintainPathConsistencyAcrossMultipleCalls() {
            Configuration source = createTestConfiguration("/test/config.properties");
            ImmutableConfiguration immutable = new ImmutableConfiguration(source);

            Optional<Path> path1 = immutable.getPropertiesPath();
            Optional<Path> path2 = immutable.getPropertiesPath();

            assertThat(path1.isPresent()).isEqualTo(path2.isPresent());
            if (path1.isPresent()) {
                assertThat(path1.get()).isEqualTo(path2.get());
            }
        }
    }

    @Nested
    @DisplayName("Properties Immutability Tests")
    public class PropertiesImmutabilityTests {

        @Test
        @DisplayName("Should return ImmutableProperties instance")
        public void shouldReturnImmutablePropertiesInstance() {
            Configuration source = createTestConfiguration("/test/config.properties");
            ImmutableConfiguration immutable = new ImmutableConfiguration(source);

            Properties properties = immutable.getProperties();

            assertThat(properties).isInstanceOf(ImmutableProperties.class);
        }

        @Test
        @DisplayName("Should throw exception when trying to modify properties")
        public void shouldThrowExceptionWhenTryingToModifyProperties() {
            Configuration source = createTestConfiguration("/test/config.properties");
            ImmutableConfiguration immutable = new ImmutableConfiguration(source);

            Properties properties = immutable.getProperties();

            assertThatThrownBy(() -> properties.setProperty("key", "value"))
                    .isInstanceOf(UnsupportedOperationException.class)
                    .hasMessage("Cannot modify an immutable properties instance.");
        }

        @Test
        @DisplayName("Should throw exception on put operation")
        public void shouldThrowExceptionOnPutOperation() {
            Configuration source = createTestConfiguration("/test/config.properties");
            ImmutableConfiguration immutable = new ImmutableConfiguration(source);

            Properties properties = immutable.getProperties();

            assertThatThrownBy(() -> properties.put("key", "value"))
                    .isInstanceOf(UnsupportedOperationException.class)
                    .hasMessage("Cannot modify an immutable properties instance.");
        }

        @Test
        @DisplayName("Should throw exception on remove operation")
        public void shouldThrowExceptionOnRemoveOperation() {
            Properties sourceProps = new Properties();
            sourceProps.setProperty("key1", "value1");

            Configuration source = createTestConfigurationWithProperties("/test/config.properties", sourceProps);
            ImmutableConfiguration immutable = new ImmutableConfiguration(source);

            Properties properties = immutable.getProperties();

            assertThatThrownBy(() -> properties.remove("key1"))
                    .isInstanceOf(UnsupportedOperationException.class)
                    .hasMessage("Cannot modify an immutable properties instance.");
        }

        @Test
        @DisplayName("Should throw exception on clear operation")
        public void shouldThrowExceptionOnClearOperation() {
            Configuration source = createTestConfiguration("/test/config.properties");
            ImmutableConfiguration immutable = new ImmutableConfiguration(source);

            Properties properties = immutable.getProperties();

            assertThatThrownBy(() -> properties.clear())
                    .isInstanceOf(UnsupportedOperationException.class)
                    .hasMessage("Cannot modify an immutable properties instance.");
        }
    }

    @Nested
    @DisplayName("Source Independence Tests")
    public class SourceIndependenceTests {

        @Test
        @DisplayName("Should not be affected by changes to source properties")
        public void shouldNotBeAffectedByChangesToSourceProperties() {
            Properties sourceProps = new Properties();
            sourceProps.setProperty("key1", "value1");

            Configuration source = createTestConfigurationWithProperties("/test/config.properties", sourceProps);
            ImmutableConfiguration immutable = new ImmutableConfiguration(source);

            sourceProps.setProperty("key2", "value2");

            assertThat(immutable.getProperties().getProperty("key1")).isEqualTo("value1");
            assertThat(immutable.getProperties().getProperty("key2")).isNull();
        }

        @Test
        @DisplayName("Should preserve initial property values")
        public void shouldPreserveInitialPropertyValues() {
            Properties sourceProps = new Properties();
            sourceProps.setProperty("key1", "originalValue");

            Configuration source = createTestConfigurationWithProperties("/test/config.properties", sourceProps);
            ImmutableConfiguration immutable = new ImmutableConfiguration(source);

            sourceProps.setProperty("key1", "modifiedValue");

            assertThat(immutable.getProperties().getProperty("key1")).isEqualTo("originalValue");
        }

        @Test
        @DisplayName("Should not reflect source property removal")
        public void shouldNotReflectSourcePropertyRemoval() {
            Properties sourceProps = new Properties();
            sourceProps.setProperty("key1", "value1");
            sourceProps.setProperty("key2", "value2");

            Configuration source = createTestConfigurationWithProperties("/test/config.properties", sourceProps);
            ImmutableConfiguration immutable = new ImmutableConfiguration(source);

            sourceProps.remove("key1");

            assertThat(immutable.getProperties().getProperty("key1")).isEqualTo("value1");
        }
    }

    @Nested
    @DisplayName("Properties Access Tests")
    public class PropertiesAccessTests {

        @Test
        @DisplayName("Should read property values correctly")
        public void shouldReadPropertyValuesCorrectly() {
            Properties sourceProps = new Properties();
            sourceProps.setProperty("key1", "value1");
            sourceProps.setProperty("key2", "value2");

            Configuration source = createTestConfigurationWithProperties("/test/config.properties", sourceProps);
            ImmutableConfiguration immutable = new ImmutableConfiguration(source);

            assertThat(immutable.getProperties().getProperty("key1")).isEqualTo("value1");
            assertThat(immutable.getProperties().getProperty("key2")).isEqualTo("value2");
        }

        @Test
        @DisplayName("Should return null for non-existent property")
        public void shouldReturnNullForNonExistentProperty() {
            Configuration source = createTestConfiguration("/test/config.properties");
            ImmutableConfiguration immutable = new ImmutableConfiguration(source);

            assertThat(immutable.getProperties().getProperty("nonexistent")).isNull();
        }

        @Test
        @DisplayName("Should return same properties instance on multiple calls")
        public void shouldReturnSamePropertiesInstanceOnMultipleCalls() {
            Configuration source = createTestConfiguration("/test/config.properties");
            ImmutableConfiguration immutable = new ImmutableConfiguration(source);

            Properties props1 = immutable.getProperties();
            Properties props2 = immutable.getProperties();

            assertThat(props1).isSameAs(props2);
        }

        @Test
        @DisplayName("Should handle empty property values")
        public void shouldHandleEmptyPropertyValues() {
            Properties sourceProps = new Properties();
            sourceProps.setProperty("emptyKey", "");

            Configuration source = createTestConfigurationWithProperties("/test/config.properties", sourceProps);
            ImmutableConfiguration immutable = new ImmutableConfiguration(source);

            assertThat(immutable.getProperties().getProperty("emptyKey")).isEqualTo("");
        }
    }

    @Nested
    @DisplayName("Multiple Properties Tests")
    public class MultiplePropertiesTests {

        @Test
        @DisplayName("Should preserve all properties from source")
        public void shouldPreserveAllPropertiesFromSource() {
            Properties sourceProps = new Properties();
            sourceProps.setProperty("key1", "value1");
            sourceProps.setProperty("key2", "value2");
            sourceProps.setProperty("key3", "value3");

            Configuration source = createTestConfigurationWithProperties("/test/config.properties", sourceProps);
            ImmutableConfiguration immutable = new ImmutableConfiguration(source);

            assertThat(immutable.getProperties()).hasSize(3);
            assertThat(immutable.getProperties().getProperty("key1")).isEqualTo("value1");
            assertThat(immutable.getProperties().getProperty("key2")).isEqualTo("value2");
            assertThat(immutable.getProperties().getProperty("key3")).isEqualTo("value3");
        }

        @Test
        @DisplayName("Should contain correct number of properties")
        public void shouldContainCorrectNumberOfProperties() {
            Properties sourceProps = new Properties();
            for (int i = 1; i <= 10; i++) {
                sourceProps.setProperty("key" + i, "value" + i);
            }

            Configuration source = createTestConfigurationWithProperties("/test/config.properties", sourceProps);
            ImmutableConfiguration immutable = new ImmutableConfiguration(source);

            assertThat(immutable.getProperties()).hasSize(10);
        }

        @Test
        @DisplayName("Should handle properties with special characters in keys")
        public void shouldHandlePropertiesWithSpecialCharactersInKeys() {
            Properties sourceProps = new Properties();
            sourceProps.setProperty("key.with.dots", "value1");
            sourceProps.setProperty("key-with-dashes", "value2");
            sourceProps.setProperty("key_with_underscores", "value3");

            Configuration source = createTestConfigurationWithProperties("/test/config.properties", sourceProps);
            ImmutableConfiguration immutable = new ImmutableConfiguration(source);

            assertThat(immutable.getProperties().getProperty("key.with.dots")).isEqualTo("value1");
            assertThat(immutable.getProperties().getProperty("key-with-dashes")).isEqualTo("value2");
            assertThat(immutable.getProperties().getProperty("key_with_underscores"))
                    .isEqualTo("value3");
        }
    }

    @Nested
    @DisplayName("Configuration Interface Compliance Tests")
    public class ConfigurationInterfaceComplianceTests {

        @Test
        @DisplayName("Should implement Configuration interface")
        public void shouldImplementConfigurationInterface() {
            Configuration source = createTestConfiguration("/test/config.properties");
            ImmutableConfiguration immutable = new ImmutableConfiguration(source);

            assertThat(immutable).isInstanceOf(Configuration.class);
        }

        @Test
        @DisplayName("Should provide getPropertiesPath method")
        public void shouldProvideGetPropertiesPathMethod() {
            Configuration source = createTestConfiguration("/test/config.properties");
            ImmutableConfiguration immutable = new ImmutableConfiguration(source);

            assertThatCode(() -> immutable.getPropertiesPath()).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should provide getProperties method")
        public void shouldProvideGetPropertiesMethod() {
            Configuration source = createTestConfiguration("/test/config.properties");
            ImmutableConfiguration immutable = new ImmutableConfiguration(source);

            assertThatCode(() -> immutable.getProperties()).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    public class EdgeCaseTests {

        @Test
        @DisplayName("Should handle configuration with very long path")
        public void shouldHandleConfigurationWithVeryLongPath() {
            String longPath = repeat("/very/long/path/", 20) + "verifyica.properties";
            Configuration source = createTestConfiguration(longPath);
            ImmutableConfiguration immutable = new ImmutableConfiguration(source);

            assertThat(immutable.getPropertiesPath()).isPresent();
            // Paths.get() normalizes the path, removing redundant slashes
            String expectedPath = Paths.get(longPath).toString();
            assertThat(immutable.getPropertiesPath().get().toString()).isEqualTo(expectedPath);
        }

        @Test
        @DisplayName("Should handle properties with very long values")
        public void shouldHandlePropertiesWithVeryLongValues() {
            Properties sourceProps = new Properties();
            String longValue = repeat("value", 1000);
            sourceProps.setProperty("longKey", longValue);

            Configuration source = createTestConfigurationWithProperties("/test/config.properties", sourceProps);
            ImmutableConfiguration immutable = new ImmutableConfiguration(source);

            assertThat(immutable.getProperties().getProperty("longKey")).isEqualTo(longValue);
        }

        @Test
        @DisplayName("Should handle properties with whitespace in values")
        public void shouldHandlePropertiesWithWhitespaceInValues() {
            Properties sourceProps = new Properties();
            sourceProps.setProperty("key1", "  value with spaces  ");
            sourceProps.setProperty("key2", "\tvalue with tabs\t");

            Configuration source = createTestConfigurationWithProperties("/test/config.properties", sourceProps);
            ImmutableConfiguration immutable = new ImmutableConfiguration(source);

            assertThat(immutable.getProperties().getProperty("key1")).isEqualTo("  value with spaces  ");
            assertThat(immutable.getProperties().getProperty("key2")).isEqualTo("\tvalue with tabs\t");
        }
    }

    @Nested
    @DisplayName("Thread Safety Tests")
    public class ThreadSafetyTests {

        @Test
        @DisplayName("Should be safe for concurrent reads")
        public void shouldBeSafeForConcurrentReads() throws InterruptedException {
            Properties sourceProps = new Properties();
            sourceProps.setProperty("key1", "value1");
            sourceProps.setProperty("key2", "value2");

            Configuration source = createTestConfigurationWithProperties("/test/config.properties", sourceProps);
            ImmutableConfiguration immutable = new ImmutableConfiguration(source);

            int threadCount = 10;
            Thread[] threads = new Thread[threadCount];
            boolean[] results = new boolean[threadCount * 100];

            for (int i = 0; i < threadCount; i++) {
                final int threadIndex = i;
                threads[i] = new Thread(() -> {
                    for (int j = 0; j < 100; j++) {
                        results[threadIndex * 100 + j] =
                                "value1".equals(immutable.getProperties().getProperty("key1"));
                    }
                });
                threads[i].start();
            }

            for (Thread thread : threads) {
                thread.join();
            }

            for (boolean result : results) {
                assertThat(result).isTrue();
            }
        }
    }

    // Helper methods

    private Configuration createTestConfiguration(String pathString) {
        return createTestConfigurationWithProperties(pathString, new Properties());
    }

    private Configuration createTestConfigurationWithProperties(String pathString, Properties props) {
        return new Configuration() {
            private final Path path = pathString != null ? Paths.get(pathString) : null;
            private final Properties properties = props;

            @Override
            public Optional<Path> getPropertiesPath() {
                return Optional.ofNullable(path);
            }

            @Override
            public Properties getProperties() {
                return properties;
            }
        };
    }
}
