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
import java.util.Set;
import java.util.function.Function;
import org.junit.jupiter.api.*;
import org.junit.platform.engine.ConfigurationParameters;
import org.verifyica.api.Configuration;

@DisplayName("ConcreteConfigurationParameters Tests")
public class ConcreteConfigurationParametersTest {

    // Java 8 compatible String repeat helper
    private static String repeat(String str, int count) {
        StringBuilder sb = new StringBuilder(str.length() * count);
        for (int i = 0; i < count; i++) {
            sb.append(str);
        }
        return sb.toString();
    }

    @Nested
    @DisplayName("Constructor Tests")
    public class ConstructorTests {

        @Test
        @DisplayName("Should create instance with valid configuration")
        public void shouldCreateInstanceWithValidConfiguration() {
            Configuration config = createTestConfiguration();
            ConcreteConfigurationParameters params = new ConcreteConfigurationParameters(config);

            assertThat(params).isNotNull();
        }

        @Test
        @DisplayName("Should throw exception when configuration is null")
        public void shouldThrowExceptionWhenConfigurationIsNull() {
            assertThatThrownBy(() -> new ConcreteConfigurationParameters(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("configuration is null");
        }

        @Test
        @DisplayName("Should implement ConfigurationParameters interface")
        public void shouldImplementConfigurationParametersInterface() {
            Configuration config = createTestConfiguration();
            ConcreteConfigurationParameters params = new ConcreteConfigurationParameters(config);

            assertThat(params).isInstanceOf(ConfigurationParameters.class);
        }
    }

    @Nested
    @DisplayName("Get String Value Tests")
    public class GetStringValueTests {

        @Test
        @DisplayName("Should retrieve existing property value")
        public void shouldRetrieveExistingPropertyValue() {
            Properties props = new Properties();
            props.setProperty("test.key", "test.value");

            Configuration config = createTestConfigurationWithProperties(props);
            ConcreteConfigurationParameters params = new ConcreteConfigurationParameters(config);

            Optional<String> result = params.get("test.key");

            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo("test.value");
        }

        @Test
        @DisplayName("Should return empty Optional for non-existent property")
        public void shouldReturnEmptyOptionalForNonExistentProperty() {
            Configuration config = createTestConfiguration();
            ConcreteConfigurationParameters params = new ConcreteConfigurationParameters(config);

            Optional<String> result = params.get("nonexistent.key");

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should trim key before lookup")
        public void shouldTrimKeyBeforeLookup() {
            Properties props = new Properties();
            props.setProperty("test.key", "test.value");

            Configuration config = createTestConfigurationWithProperties(props);
            ConcreteConfigurationParameters params = new ConcreteConfigurationParameters(config);

            Optional<String> result = params.get("  test.key  ");

            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo("test.value");
        }

        @Test
        @DisplayName("Should throw exception when key is null")
        public void shouldThrowExceptionWhenKeyIsNull() {
            Configuration config = createTestConfiguration();
            ConcreteConfigurationParameters params = new ConcreteConfigurationParameters(config);

            assertThatThrownBy(() -> params.get(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("key is null");
        }

        @Test
        @DisplayName("Should throw exception when key is blank")
        public void shouldThrowExceptionWhenKeyIsBlank() {
            Configuration config = createTestConfiguration();
            ConcreteConfigurationParameters params = new ConcreteConfigurationParameters(config);

            assertThatThrownBy(() -> params.get("   "))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("key is blank");
        }

        @Test
        @DisplayName("Should throw exception when key is empty")
        public void shouldThrowExceptionWhenKeyIsEmpty() {
            Configuration config = createTestConfiguration();
            ConcreteConfigurationParameters params = new ConcreteConfigurationParameters(config);

            assertThatThrownBy(() -> params.get(""))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("key is blank");
        }
    }

    @Nested
    @DisplayName("Get Boolean Value Tests")
    public class GetBooleanValueTests {

        @Test
        @DisplayName("Should return true for string value 'true'")
        public void shouldReturnTrueForStringValueTrue() {
            Properties props = new Properties();
            props.setProperty("boolean.key", "true");

            Configuration config = createTestConfigurationWithProperties(props);
            ConcreteConfigurationParameters params = new ConcreteConfigurationParameters(config);

            Optional<Boolean> result = params.getBoolean("boolean.key");

            assertThat(result).isPresent();
            assertThat(result.get()).isTrue();
        }

        @Test
        @DisplayName("Should return false for string value 'false'")
        public void shouldReturnFalseForStringValueFalse() {
            Properties props = new Properties();
            props.setProperty("boolean.key", "false");

            Configuration config = createTestConfigurationWithProperties(props);
            ConcreteConfigurationParameters params = new ConcreteConfigurationParameters(config);

            Optional<Boolean> result = params.getBoolean("boolean.key");

            assertThat(result).isPresent();
            assertThat(result.get()).isFalse();
        }

        @Test
        @DisplayName("Should return false for non-true value")
        public void shouldReturnFalseForNonTrueValue() {
            Properties props = new Properties();
            props.setProperty("boolean.key", "yes");

            Configuration config = createTestConfigurationWithProperties(props);
            ConcreteConfigurationParameters params = new ConcreteConfigurationParameters(config);

            Optional<Boolean> result = params.getBoolean("boolean.key");

            assertThat(result).isPresent();
            assertThat(result.get()).isFalse();
        }

        @Test
        @DisplayName("Should return false for non-existent property")
        public void shouldReturnFalseForNonExistentProperty() {
            Configuration config = createTestConfiguration();
            ConcreteConfigurationParameters params = new ConcreteConfigurationParameters(config);

            Optional<Boolean> result = params.getBoolean("nonexistent.key");

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should trim key before boolean lookup")
        public void shouldTrimKeyBeforeBooleanLookup() {
            Properties props = new Properties();
            props.setProperty("boolean.key", "true");

            Configuration config = createTestConfigurationWithProperties(props);
            ConcreteConfigurationParameters params = new ConcreteConfigurationParameters(config);

            Optional<Boolean> result = params.getBoolean("  boolean.key  ");

            assertThat(result).isPresent();
            assertThat(result.get()).isTrue();
        }

        @Test
        @DisplayName("Should throw exception when key is null for boolean")
        public void shouldThrowExceptionWhenKeyIsNullForBoolean() {
            Configuration config = createTestConfiguration();
            ConcreteConfigurationParameters params = new ConcreteConfigurationParameters(config);

            assertThatThrownBy(() -> params.getBoolean(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("key is null");
        }

        @Test
        @DisplayName("Should throw exception when key is blank for boolean")
        public void shouldThrowExceptionWhenKeyIsBlankForBoolean() {
            Configuration config = createTestConfiguration();
            ConcreteConfigurationParameters params = new ConcreteConfigurationParameters(config);

            assertThatThrownBy(() -> params.getBoolean("   "))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("key is blank");
        }

        @Test
        @DisplayName("Should return false for empty string value")
        public void shouldReturnFalseForEmptyStringValue() {
            Properties props = new Properties();
            props.setProperty("boolean.key", "");

            Configuration config = createTestConfigurationWithProperties(props);
            ConcreteConfigurationParameters params = new ConcreteConfigurationParameters(config);

            Optional<Boolean> result = params.getBoolean("boolean.key");

            assertThat(result).isPresent();
            assertThat(result.get()).isFalse();
        }
    }

    @Nested
    @DisplayName("Get With Transformer Tests")
    public class GetWithTransformerTests {

        @Test
        @DisplayName("Should transform property value using function")
        public void shouldTransformPropertyValueUsingFunction() {
            Properties props = new Properties();
            props.setProperty("number.key", "42");

            Configuration config = createTestConfigurationWithProperties(props);
            ConcreteConfigurationParameters params = new ConcreteConfigurationParameters(config);

            Optional<Integer> result = params.get("number.key", Integer::parseInt);

            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(42);
        }

        @Test
        @DisplayName("Should return empty Optional when property not found")
        public void shouldReturnEmptyOptionalWhenPropertyNotFound() {
            Configuration config = createTestConfiguration();
            ConcreteConfigurationParameters params = new ConcreteConfigurationParameters(config);

            Optional<Integer> result = params.get("nonexistent.key", Integer::parseInt);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should apply custom transformation")
        public void shouldApplyCustomTransformation() {
            Properties props = new Properties();
            props.setProperty("text.key", "hello");

            Configuration config = createTestConfigurationWithProperties(props);
            ConcreteConfigurationParameters params = new ConcreteConfigurationParameters(config);

            Optional<String> result = params.get("text.key", String::toUpperCase);

            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo("HELLO");
        }

        @Test
        @DisplayName("Should handle transformation returning null")
        public void shouldHandleTransformationReturningNull() {
            Properties props = new Properties();
            props.setProperty("test.key", "value");

            Configuration config = createTestConfigurationWithProperties(props);
            ConcreteConfigurationParameters params = new ConcreteConfigurationParameters(config);

            Function<String, String> nullReturningFunction = s -> null;
            Optional<String> result = params.get("test.key", nullReturningFunction);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should trim key before transformer lookup")
        public void shouldTrimKeyBeforeTransformerLookup() {
            Properties props = new Properties();
            props.setProperty("number.key", "100");

            Configuration config = createTestConfigurationWithProperties(props);
            ConcreteConfigurationParameters params = new ConcreteConfigurationParameters(config);

            Optional<Integer> result = params.get("  number.key  ", Integer::parseInt);

            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(100);
        }

        @Test
        @DisplayName("Should throw exception when key is null for transformer")
        public void shouldThrowExceptionWhenKeyIsNullForTransformer() {
            Configuration config = createTestConfiguration();
            ConcreteConfigurationParameters params = new ConcreteConfigurationParameters(config);

            assertThatThrownBy(() -> params.get(null, String::toUpperCase))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("key is null");
        }

        @Test
        @DisplayName("Should throw exception when key is blank for transformer")
        public void shouldThrowExceptionWhenKeyIsBlankForTransformer() {
            Configuration config = createTestConfiguration();
            ConcreteConfigurationParameters params = new ConcreteConfigurationParameters(config);

            assertThatThrownBy(() -> params.get("   ", String::toUpperCase))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("key is blank");
        }

        @Test
        @DisplayName("Should throw exception when transformer is null")
        public void shouldThrowExceptionWhenTransformerIsNull() {
            Configuration config = createTestConfiguration();
            ConcreteConfigurationParameters params = new ConcreteConfigurationParameters(config);

            assertThatThrownBy(() -> params.get("test.key", null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("transformer is null");
        }

        @Test
        @DisplayName("Should handle complex transformations")
        public void shouldHandleComplexTransformations() {
            Properties props = new Properties();
            props.setProperty("csv.key", "1,2,3,4,5");

            Configuration config = createTestConfigurationWithProperties(props);
            ConcreteConfigurationParameters params = new ConcreteConfigurationParameters(config);

            Optional<Integer> result = params.get("csv.key", value -> {
                String[] parts = value.split(",");
                return parts.length;
            });

            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(5);
        }
    }

    @Nested
    @DisplayName("Size Tests")
    public class SizeTests {

        @Test
        @DisplayName("Should return zero for empty properties")
        public void shouldReturnZeroForEmptyProperties() {
            Configuration config = createTestConfiguration();
            ConcreteConfigurationParameters params = new ConcreteConfigurationParameters(config);

            int size = params.size();

            assertThat(size).isZero();
        }

        @Test
        @DisplayName("Should return correct size for single property")
        public void shouldReturnCorrectSizeForSingleProperty() {
            Properties props = new Properties();
            props.setProperty("key1", "value1");

            Configuration config = createTestConfigurationWithProperties(props);
            ConcreteConfigurationParameters params = new ConcreteConfigurationParameters(config);

            int size = params.size();

            assertThat(size).isEqualTo(1);
        }

        @Test
        @DisplayName("Should return correct size for multiple properties")
        public void shouldReturnCorrectSizeForMultipleProperties() {
            Properties props = new Properties();
            props.setProperty("key1", "value1");
            props.setProperty("key2", "value2");
            props.setProperty("key3", "value3");

            Configuration config = createTestConfigurationWithProperties(props);
            ConcreteConfigurationParameters params = new ConcreteConfigurationParameters(config);

            int size = params.size();

            assertThat(size).isEqualTo(3);
        }

        @Test
        @DisplayName("Should return consistent size across multiple calls")
        public void shouldReturnConsistentSizeAcrossMultipleCalls() {
            Properties props = new Properties();
            props.setProperty("key1", "value1");
            props.setProperty("key2", "value2");

            Configuration config = createTestConfigurationWithProperties(props);
            ConcreteConfigurationParameters params = new ConcreteConfigurationParameters(config);

            int size1 = params.size();
            int size2 = params.size();

            assertThat(size1).isEqualTo(size2);
        }
    }

    @Nested
    @DisplayName("KeySet Tests")
    public class KeySetTests {

        @Test
        @DisplayName("Should return empty set for empty properties")
        public void shouldReturnEmptySetForEmptyProperties() {
            Configuration config = createTestConfiguration();
            ConcreteConfigurationParameters params = new ConcreteConfigurationParameters(config);

            Set<String> keySet = params.keySet();

            assertThat(keySet).isEmpty();
        }

        @Test
        @DisplayName("Should return all property names")
        public void shouldReturnAllPropertyNames() {
            Properties props = new Properties();
            props.setProperty("key1", "value1");
            props.setProperty("key2", "value2");
            props.setProperty("key3", "value3");

            Configuration config = createTestConfigurationWithProperties(props);
            ConcreteConfigurationParameters params = new ConcreteConfigurationParameters(config);

            Set<String> keySet = params.keySet();

            assertThat(keySet).hasSize(3);
            assertThat(keySet).contains("key1", "key2", "key3");
        }

        @Test
        @DisplayName("Should return only string property names")
        public void shouldReturnOnlyStringPropertyNames() {
            Properties props = new Properties();
            props.setProperty("string.key1", "value1");
            props.setProperty("string.key2", "value2");

            Configuration config = createTestConfigurationWithProperties(props);
            ConcreteConfigurationParameters params = new ConcreteConfigurationParameters(config);

            Set<String> keySet = params.keySet();

            assertThat(keySet).allMatch(key -> key instanceof String);
        }

        @Test
        @DisplayName("Should return consistent keySet across multiple calls")
        public void shouldReturnConsistentKeySetAcrossMultipleCalls() {
            Properties props = new Properties();
            props.setProperty("key1", "value1");
            props.setProperty("key2", "value2");

            Configuration config = createTestConfigurationWithProperties(props);
            ConcreteConfigurationParameters params = new ConcreteConfigurationParameters(config);

            Set<String> keySet1 = params.keySet();
            Set<String> keySet2 = params.keySet();

            assertThat(keySet1).isEqualTo(keySet2);
        }

        @Test
        @DisplayName("Should match size with keySet size")
        public void shouldMatchSizeWithKeySetSize() {
            Properties props = new Properties();
            props.setProperty("key1", "value1");
            props.setProperty("key2", "value2");
            props.setProperty("key3", "value3");

            Configuration config = createTestConfigurationWithProperties(props);
            ConcreteConfigurationParameters params = new ConcreteConfigurationParameters(config);

            assertThat(params.keySet()).hasSize(params.size());
        }
    }

    @Nested
    @DisplayName("Integration Tests")
    public class IntegrationTests {

        @Test
        @DisplayName("Should work with complex configuration")
        public void shouldWorkWithComplexConfiguration() {
            Properties props = new Properties();
            props.setProperty("verifyica.engine.thread.type", "virtual");
            props.setProperty("verifyica.engine.class.parallelism", "4");
            props.setProperty("verifyica.engine.prune.stacktraces", "true");

            Configuration config = createTestConfigurationWithProperties(props);
            ConcreteConfigurationParameters params = new ConcreteConfigurationParameters(config);

            assertThat(params.get("verifyica.engine.thread.type")).isPresent();
            assertThat(params.get("verifyica.engine.thread.type").get()).isEqualTo("virtual");

            assertThat(params.get("verifyica.engine.class.parallelism", Integer::parseInt))
                    .isPresent();
            assertThat(params.get("verifyica.engine.class.parallelism", Integer::parseInt)
                            .get())
                    .isEqualTo(4);

            assertThat(params.getBoolean("verifyica.engine.prune.stacktraces")).isPresent();
            assertThat(params.getBoolean("verifyica.engine.prune.stacktraces").get())
                    .isTrue();
        }

        @Test
        @DisplayName("Should handle mixed property access patterns")
        public void shouldHandleMixedPropertyAccessPatterns() {
            Properties props = new Properties();
            props.setProperty("string.prop", "value");
            props.setProperty("int.prop", "42");
            props.setProperty("bool.prop", "true");

            Configuration config = createTestConfigurationWithProperties(props);
            ConcreteConfigurationParameters params = new ConcreteConfigurationParameters(config);

            assertThat(params.get("string.prop")).isPresent();
            assertThat(params.get("int.prop", Integer::parseInt)).isPresent();
            assertThat(params.getBoolean("bool.prop")).isPresent();
            assertThat(params.size()).isEqualTo(3);
            assertThat(params.keySet()).hasSize(3);
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    public class EdgeCaseTests {

        @Test
        @DisplayName("Should handle properties with very long keys")
        public void shouldHandlePropertiesWithVeryLongKeys() {
            String longKey = repeat("very.long.key.", 20) + "property";
            Properties props = new Properties();
            props.setProperty(longKey, "value");

            Configuration config = createTestConfigurationWithProperties(props);
            ConcreteConfigurationParameters params = new ConcreteConfigurationParameters(config);

            Optional<String> result = params.get(longKey);

            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo("value");
        }

        @Test
        @DisplayName("Should handle properties with very long values")
        public void shouldHandlePropertiesWithVeryLongValues() {
            String longValue = repeat("value", 1000);
            Properties props = new Properties();
            props.setProperty("key", longValue);

            Configuration config = createTestConfigurationWithProperties(props);
            ConcreteConfigurationParameters params = new ConcreteConfigurationParameters(config);

            Optional<String> result = params.get("key");

            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(longValue);
        }

        @Test
        @DisplayName("Should handle properties with special characters")
        public void shouldHandlePropertiesWithSpecialCharacters() {
            Properties props = new Properties();
            props.setProperty("key.with.dots", "value1");
            props.setProperty("key-with-dashes", "value2");
            props.setProperty("key_with_underscores", "value3");

            Configuration config = createTestConfigurationWithProperties(props);
            ConcreteConfigurationParameters params = new ConcreteConfigurationParameters(config);

            assertThat(params.get("key.with.dots")).isPresent();
            assertThat(params.get("key-with-dashes")).isPresent();
            assertThat(params.get("key_with_underscores")).isPresent();
        }

        @Test
        @DisplayName("Should handle empty string values")
        public void shouldHandleEmptyStringValues() {
            Properties props = new Properties();
            props.setProperty("empty.key", "");

            Configuration config = createTestConfigurationWithProperties(props);
            ConcreteConfigurationParameters params = new ConcreteConfigurationParameters(config);

            Optional<String> result = params.get("empty.key");

            assertThat(result).isPresent();
            assertThat(result.get()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Thread Safety Tests")
    public class ThreadSafetyTests {

        @Test
        @DisplayName("Should handle concurrent access safely")
        public void shouldHandleConcurrentAccessSafely() throws InterruptedException {
            Properties props = new Properties();
            props.setProperty("key1", "value1");
            props.setProperty("key2", "value2");

            Configuration config = createTestConfigurationWithProperties(props);
            ConcreteConfigurationParameters params = new ConcreteConfigurationParameters(config);

            int threadCount = 10;
            Thread[] threads = new Thread[threadCount];
            boolean[] results = new boolean[threadCount * 100];

            for (int i = 0; i < threadCount; i++) {
                final int threadIndex = i;
                threads[i] = new Thread(() -> {
                    for (int j = 0; j < 100; j++) {
                        results[threadIndex * 100 + j] = params.get("key1").isPresent();
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

    private Configuration createTestConfiguration() {
        return createTestConfigurationWithProperties(new Properties());
    }

    private Configuration createTestConfigurationWithProperties(Properties props) {
        return new Configuration() {
            private final Properties properties = props;

            @Override
            public Optional<Path> getPropertiesPath() {
                return Optional.of(Paths.get("/test/verifyica.properties"));
            }

            @Override
            public Properties getProperties() {
                return properties;
            }
        };
    }
}
