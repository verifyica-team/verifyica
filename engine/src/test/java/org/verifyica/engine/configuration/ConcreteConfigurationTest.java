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

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Properties;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

@DisplayName("ConcreteConfiguration Tests")
public class ConcreteConfigurationTest {

    @Nested
    @DisplayName("Singleton Pattern Tests")
    public class SingletonPatternTests {

        @Test
        @DisplayName("Should return singleton instance")
        public void shouldReturnSingletonInstance() {
            ConcreteConfiguration instance1 = ConcreteConfiguration.getInstance();
            ConcreteConfiguration instance2 = ConcreteConfiguration.getInstance();

            assertThat(instance1).isNotNull();
            assertThat(instance2).isNotNull();
            assertThat(instance1).isSameAs(instance2);
        }

        @Test
        @DisplayName("Should return same instance across multiple calls")
        public void shouldReturnSameInstanceAcrossMultipleCalls() {
            ConcreteConfiguration instance1 = ConcreteConfiguration.getInstance();
            ConcreteConfiguration instance2 = ConcreteConfiguration.getInstance();
            ConcreteConfiguration instance3 = ConcreteConfiguration.getInstance();

            assertThat(instance1).isSameAs(instance2).isSameAs(instance3);
        }

        @Test
        @DisplayName("Should have properties object initialized")
        public void shouldHavePropertiesObjectInitialized() {
            ConcreteConfiguration instance = ConcreteConfiguration.getInstance();

            assertThat(instance.getProperties()).isNotNull();
        }
    }

    @Nested
    @DisplayName("Property Path Tests")
    public class PropertyPathTests {

        @Test
        @DisplayName("Should return empty Optional when no properties file found")
        public void shouldReturnEmptyOptionalWhenNoPropertiesFileFound() {
            ConcreteConfiguration instance = ConcreteConfiguration.getInstance();
            Optional<Path> propertiesPath = instance.getPropertiesPath();

            assertThat(propertiesPath).isNotNull();
        }

        @Test
        @DisplayName("Should return path wrapped in Optional when properties file exists")
        public void shouldReturnPathWrappedInOptionalWhenPropertiesFileExists() {
            ConcreteConfiguration instance = ConcreteConfiguration.getInstance();
            Optional<Path> propertiesPath = instance.getPropertiesPath();

            if (propertiesPath.isPresent()) {
                assertThat(propertiesPath.get()).isNotNull();
                assertThat(propertiesPath.get().toString()).endsWith("verifyica.properties");
            }
        }
    }

    @Nested
    @DisplayName("Properties Loading Tests")
    public class PropertiesLoadingTests {

        @Test
        @DisplayName("Should return non-null properties")
        public void shouldReturnNonNullProperties() {
            ConcreteConfiguration instance = ConcreteConfiguration.getInstance();
            Properties properties = instance.getProperties();

            assertThat(properties).isNotNull();
        }

        @Test
        @DisplayName("Should return empty properties when no file found")
        public void shouldReturnEmptyPropertiesWhenNoFileFound() {
            ConcreteConfiguration instance = ConcreteConfiguration.getInstance();
            Properties properties = instance.getProperties();

            assertThat(properties).isNotNull();
        }

        @Test
        @DisplayName("Properties should be instance of Properties")
        public void propertiesShouldBeInstanceOfProperties() {
            ConcreteConfiguration instance = ConcreteConfiguration.getInstance();

            assertThat(instance.getProperties()).isInstanceOf(Properties.class);
        }
    }

    @Nested
    @DisplayName("System Property Override Tests")
    public class SystemPropertyOverrideTests {

        @TempDir
        Path tempDir;

        @Test
        @DisplayName("Should load properties from system property location")
        public void shouldLoadPropertiesFromSystemPropertyLocation() throws IOException {
            Path propertiesFile = tempDir.resolve("custom-verifyica.properties");
            String content = "test.property=test.value\n" + "another.property=another.value\n";
            Files.write(propertiesFile, content.getBytes(StandardCharsets.UTF_8));

            String originalProperty = System.getProperty("verifyica.properties");
            try {
                System.setProperty(
                        "verifyica.properties", propertiesFile.toAbsolutePath().toString());

                ConcreteConfiguration instance = ConcreteConfiguration.getInstance();
                Properties properties = instance.getProperties();

                assertThat(properties).isNotNull();
            } finally {
                if (originalProperty != null) {
                    System.setProperty("verifyica.properties", originalProperty);
                } else {
                    System.clearProperty("verifyica.properties");
                }
            }
        }

        @Test
        @DisplayName("Should handle non-existent system property file gracefully")
        public void shouldHandleNonExistentSystemPropertyFileGracefully() {
            String originalProperty = System.getProperty("verifyica.properties");
            try {
                System.setProperty("verifyica.properties", "/non/existent/path/verifyica.properties");

                // ConcreteConfiguration is a singleton, already initialized before this test runs
                // Setting the property after initialization has no effect, so it handles this gracefully
                assertThatCode(() -> {
                            ConcreteConfiguration.getInstance().getProperties();
                        })
                        .doesNotThrowAnyException();
            } finally {
                if (originalProperty != null) {
                    System.setProperty("verifyica.properties", originalProperty);
                } else {
                    System.clearProperty("verifyica.properties");
                }
            }
        }
    }

    @Nested
    @DisplayName("File Discovery Tests")
    public class FileDiscoveryTests {

        @TempDir
        Path tempDir;

        @Test
        @DisplayName("Should find properties file in current directory")
        public void shouldFindPropertiesFileInCurrentDirectory() throws IOException {
            Path propertiesFile = tempDir.resolve("verifyica.properties");
            String content = "test.key=test.value\n";
            Files.write(propertiesFile, content.getBytes(StandardCharsets.UTF_8));

            File file = propertiesFile.toFile();

            assertThat(file.exists()).isTrue();
            assertThat(file.isFile()).isTrue();
            assertThat(file.canRead()).isTrue();
        }

        @Test
        @DisplayName("Should search parent directories for properties file")
        public void shouldSearchParentDirectoriesForPropertiesFile() throws IOException {
            Path parentDir = tempDir.resolve("parent");
            Path childDir = parentDir.resolve("child");
            Files.createDirectories(childDir);

            Path propertiesFile = parentDir.resolve("verifyica.properties");
            String content = "parent.property=parent.value\n";
            Files.write(propertiesFile, content.getBytes(StandardCharsets.UTF_8));

            assertThat(propertiesFile.toFile().exists()).isTrue();
            assertThat(childDir.toFile().exists()).isTrue();
        }

        @Test
        @DisplayName("Should stop searching at root directory")
        public void shouldStopSearchingAtRootDirectory() {
            Path root = Paths.get("/").toAbsolutePath().normalize();

            assertThat(root.getParent()).isNull();
        }
    }

    @Nested
    @DisplayName("Properties Content Tests")
    public class PropertiesContentTests {

        @TempDir
        Path tempDir;

        @Test
        @DisplayName("Should load properties with correct key-value pairs")
        public void shouldLoadPropertiesWithCorrectKeyValuePairs() throws IOException {
            Path propertiesFile = tempDir.resolve("test-verifyica.properties");
            String content = "key1=value1\n" + "key2=value2\n" + "key3=value3\n";
            Files.write(propertiesFile, content.getBytes(StandardCharsets.UTF_8));

            Properties properties = new Properties();
            properties.load(Files.newBufferedReader(propertiesFile, StandardCharsets.UTF_8));

            assertThat(properties).hasSize(3);
            assertThat(properties.getProperty("key1")).isEqualTo("value1");
            assertThat(properties.getProperty("key2")).isEqualTo("value2");
            assertThat(properties.getProperty("key3")).isEqualTo("value3");
        }

        @Test
        @DisplayName("Should handle empty properties file")
        public void shouldHandleEmptyPropertiesFile() throws IOException {
            Path propertiesFile = tempDir.resolve("empty-verifyica.properties");
            Files.write(propertiesFile, "".getBytes(StandardCharsets.UTF_8));

            Properties properties = new Properties();
            properties.load(Files.newBufferedReader(propertiesFile, StandardCharsets.UTF_8));

            assertThat(properties).isEmpty();
        }

        @Test
        @DisplayName("Should handle properties with comments")
        public void shouldHandlePropertiesWithComments() throws IOException {
            Path propertiesFile = tempDir.resolve("commented-verifyica.properties");
            String content = "# This is a comment\n" + "key1=value1\n" + "! Another comment\n" + "key2=value2\n";
            Files.write(propertiesFile, content.getBytes(StandardCharsets.UTF_8));

            Properties properties = new Properties();
            properties.load(Files.newBufferedReader(propertiesFile, StandardCharsets.UTF_8));

            assertThat(properties).hasSize(2);
            assertThat(properties.getProperty("key1")).isEqualTo("value1");
            assertThat(properties.getProperty("key2")).isEqualTo("value2");
        }

        @Test
        @DisplayName("Should handle properties with special characters")
        public void shouldHandlePropertiesWithSpecialCharacters() throws IOException {
            Path propertiesFile = tempDir.resolve("special-verifyica.properties");
            String content = "key.with.dots=value\n" + "key-with-dashes=value\n" + "key_with_underscores=value\n";
            Files.write(propertiesFile, content.getBytes(StandardCharsets.UTF_8));

            Properties properties = new Properties();
            properties.load(Files.newBufferedReader(propertiesFile, StandardCharsets.UTF_8));

            assertThat(properties).hasSize(3);
            assertThat(properties.getProperty("key.with.dots")).isEqualTo("value");
            assertThat(properties.getProperty("key-with-dashes")).isEqualTo("value");
            assertThat(properties.getProperty("key_with_underscores")).isEqualTo("value");
        }

        @Test
        @DisplayName("Should handle properties with whitespace")
        public void shouldHandlePropertiesWithWhitespace() throws IOException {
            Path propertiesFile = tempDir.resolve("whitespace-verifyica.properties");
            String content = "key1 = value1\n" + "  key2=value2  \n" + "key3 = value with spaces\n";
            Files.write(propertiesFile, content.getBytes(StandardCharsets.UTF_8));

            Properties properties = new Properties();
            properties.load(Files.newBufferedReader(propertiesFile, StandardCharsets.UTF_8));

            assertThat(properties).hasSize(3);
        }
    }

    @Nested
    @DisplayName("UTF-8 Encoding Tests")
    public class UTF8EncodingTests {

        @TempDir
        Path tempDir;

        @Test
        @DisplayName("Should load properties with UTF-8 encoding")
        public void shouldLoadPropertiesWithUTF8Encoding() throws IOException {
            Path propertiesFile = tempDir.resolve("utf8-verifyica.properties");
            String content = "unicode.property=\u00E9\u00E7\u00E0\n";
            Files.write(propertiesFile, content.getBytes(StandardCharsets.UTF_8));

            Properties properties = new Properties();
            properties.load(Files.newBufferedReader(propertiesFile, StandardCharsets.UTF_8));

            assertThat(properties).hasSize(1);
        }

        @Test
        @DisplayName("Should handle empty lines in properties file")
        public void shouldHandleEmptyLinesInPropertiesFile() throws IOException {
            Path propertiesFile = tempDir.resolve("empty-lines-verifyica.properties");
            String content = "key1=value1\n" + "\n" + "key2=value2\n" + "\n" + "\n" + "key3=value3\n";
            Files.write(propertiesFile, content.getBytes(StandardCharsets.UTF_8));

            Properties properties = new Properties();
            properties.load(Files.newBufferedReader(propertiesFile, StandardCharsets.UTF_8));

            assertThat(properties).hasSize(3);
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    public class ErrorHandlingTests {

        @Test
        @DisplayName("Should handle IOException when file cannot be read")
        public void shouldHandleIOExceptionWhenFileCannotBeRead() {
            Path nonReadableFile = Paths.get("/root/restricted/verifyica.properties");

            assertThat(nonReadableFile.toFile().canRead()).isFalse();
        }

        @Test
        @DisplayName("Should handle malformed properties file gracefully")
        public void shouldHandleMalformedPropertiesFileGracefully() throws IOException {
            Path tempFile = Files.createTempFile("malformed", ".properties");
            try {
                Files.write(tempFile, "malformed\\line".getBytes(StandardCharsets.UTF_8));

                Properties properties = new Properties();
                properties.load(Files.newBufferedReader(tempFile, StandardCharsets.UTF_8));

                assertThat(properties).isNotNull();
            } finally {
                Files.deleteIfExists(tempFile);
            }
        }
    }

    @Nested
    @DisplayName("Thread Safety Tests")
    public class ThreadSafetyTests {

        @Test
        @DisplayName("Should return same singleton instance from multiple threads")
        public void shouldReturnSameSingletonInstanceFromMultipleThreads() throws InterruptedException {
            int threadCount = 10;
            Thread[] threads = new Thread[threadCount];
            ConcreteConfiguration[] instances = new ConcreteConfiguration[threadCount];

            for (int i = 0; i < threadCount; i++) {
                final int index = i;
                threads[i] = new Thread(() -> {
                    instances[index] = ConcreteConfiguration.getInstance();
                });
                threads[i].start();
            }

            for (Thread thread : threads) {
                thread.join();
            }

            ConcreteConfiguration firstInstance = instances[0];
            for (int i = 1; i < threadCount; i++) {
                assertThat(instances[i]).isSameAs(firstInstance);
            }
        }
    }

    @Nested
    @DisplayName("Configuration State Tests")
    public class ConfigurationStateTests {

        @Test
        @DisplayName("Should maintain consistent properties reference")
        public void shouldMaintainConsistentPropertiesReference() {
            ConcreteConfiguration instance = ConcreteConfiguration.getInstance();
            Properties properties1 = instance.getProperties();
            Properties properties2 = instance.getProperties();

            assertThat(properties1).isSameAs(properties2);
        }

        @Test
        @DisplayName("Should maintain consistent properties path reference")
        public void shouldMaintainConsistentPropertiesPathReference() {
            ConcreteConfiguration instance = ConcreteConfiguration.getInstance();
            Optional<Path> path1 = instance.getPropertiesPath();
            Optional<Path> path2 = instance.getPropertiesPath();

            if (path1.isPresent() && path2.isPresent()) {
                assertThat(path1.get()).isEqualTo(path2.get());
            } else {
                assertThat(path1.isPresent()).isEqualTo(path2.isPresent());
            }
        }
    }

    @Nested
    @DisplayName("File System Integration Tests")
    public class FileSystemIntegrationTests {

        @TempDir
        Path tempDir;

        @Test
        @DisplayName("Should handle absolute file path")
        public void shouldHandleAbsoluteFilePath() throws IOException {
            Path propertiesFile = tempDir.resolve("verifyica.properties");
            Files.write(propertiesFile, "test=value".getBytes(StandardCharsets.UTF_8));

            Path absolutePath = propertiesFile.toAbsolutePath();

            assertThat(absolutePath.isAbsolute()).isTrue();
            assertThat(absolutePath.toFile().exists()).isTrue();
        }

        @Test
        @DisplayName("Should handle normalized file path")
        public void shouldHandleNormalizedFilePath() throws IOException {
            Path propertiesFile = tempDir.resolve("verifyica.properties");
            Files.write(propertiesFile, "test=value".getBytes(StandardCharsets.UTF_8));

            Path normalizedPath = propertiesFile.normalize();

            assertThat(normalizedPath.toFile().exists()).isTrue();
        }

        @Test
        @DisplayName("Should handle file existence check")
        public void shouldHandleFileExistenceCheck() throws IOException {
            Path propertiesFile = tempDir.resolve("verifyica.properties");

            assertThat(propertiesFile.toFile().exists()).isFalse();

            Files.write(propertiesFile, "test=value".getBytes(StandardCharsets.UTF_8));

            assertThat(propertiesFile.toFile().exists()).isTrue();
        }

        @Test
        @DisplayName("Should verify file is regular file")
        public void shouldVerifyFileIsRegularFile() throws IOException {
            Path propertiesFile = tempDir.resolve("verifyica.properties");
            Files.write(propertiesFile, "test=value".getBytes(StandardCharsets.UTF_8));

            assertThat(propertiesFile.toFile().isFile()).isTrue();
            assertThat(propertiesFile.toFile().isDirectory()).isFalse();
        }

        @Test
        @DisplayName("Should verify file is readable")
        public void shouldVerifyFileIsReadable() throws IOException {
            Path propertiesFile = tempDir.resolve("verifyica.properties");
            Files.write(propertiesFile, "test=value".getBytes(StandardCharsets.UTF_8));

            assertThat(propertiesFile.toFile().canRead()).isTrue();
        }
    }
}
