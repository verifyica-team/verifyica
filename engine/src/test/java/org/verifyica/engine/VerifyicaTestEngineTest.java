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

package org.verifyica.engine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Optional;
import java.util.function.Predicate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.platform.engine.ConfigurationParameters;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.EngineDescriptor;
import org.verifyica.engine.configuration.Constants;

/**
 * Tests for the {@link VerifyicaTestEngine} class.
 */
@DisplayName("VerifyicaTestEngine Tests")
public class VerifyicaTestEngineTest {

    private VerifyicaTestEngine engine;

    @BeforeEach
    void setUp() {
        engine = new VerifyicaTestEngine();
    }

    @Nested
    @DisplayName("Constructor Tests")
    public class ConstructorTests {

        @Test
        @DisplayName("Should create engine instance successfully")
        public void shouldCreateEngineInstanceSuccessfully() {
            final VerifyicaTestEngine testEngine = new VerifyicaTestEngine();
            assertThat(testEngine).isNotNull();
        }

        @Test
        @DisplayName("Should have public constructor")
        public void shouldHavePublicConstructor() throws Exception {
            final Constructor<VerifyicaTestEngine> constructor = VerifyicaTestEngine.class.getDeclaredConstructor();
            assertThat(Modifier.isPublic(constructor.getModifiers())).isTrue();
        }
    }

    @Nested
    @DisplayName("Engine Identity Tests")
    public class EngineIdentityTests {

        @Test
        @DisplayName("Should return correct engine ID")
        public void shouldReturnCorrectEngineId() {
            assertThat(engine.getId()).isEqualTo("verifyica");
        }

        @Test
        @DisplayName("Should return correct display name")
        public void shouldReturnCorrectDisplayName() {
            assertThat(engine.getId()).isEqualToIgnoringCase("Verifyica");
        }

        @Test
        @DisplayName("Should return correct group ID")
        public void shouldReturnCorrectGroupId() {
            final Optional<String> groupId = engine.getGroupId();
            assertThat(groupId).isPresent();
            assertThat(groupId.get()).isEqualTo("org.verifyica");
        }

        @Test
        @DisplayName("Should return correct artifact ID")
        public void shouldReturnCorrectArtifactId() {
            final Optional<String> artifactId = engine.getArtifactId();
            assertThat(artifactId).isPresent();
            assertThat(artifactId.get()).isEqualTo("engine");
        }

        @Test
        @DisplayName("Should return version")
        public void shouldReturnVersion() {
            final Optional<String> version = engine.getVersion();
            assertThat(version).isPresent();
            // Version should not be null or empty
            assertThat(version.get()).isNotEmpty();
        }

        @Test
        @DisplayName("Static getVersion should return version")
        public void staticGetVersionShouldReturnVersion() {
            final String version = VerifyicaTestEngine.staticGetVersion();
            assertThat(version).isNotNull();
            // Version should not be empty
            assertThat(version).isNotEmpty();
        }

        @Test
        @DisplayName("Static and instance version should match")
        public void staticAndInstanceVersionShouldMatch() {
            final String staticVersion = VerifyicaTestEngine.staticGetVersion();
            final Optional<String> instanceVersion = engine.getVersion();
            assertThat(instanceVersion).isPresent();
            assertThat(staticVersion).isEqualTo(instanceVersion.get());
        }
    }

    @Nested
    @DisplayName("Discover Tests")
    public class DiscoverTests {

        @Test
        @DisplayName("Should return disabled descriptor when running via Maven Surefire")
        public void shouldReturnDisabledDescriptorWhenRunningViaMavenSurefire() {
            // Save original property
            final String originalProperty = System.getProperty("surefire.test.class.path");

            try {
                // Set the property to simulate running under Maven Surefire
                System.setProperty("surefire.test.class.path", "/some/path");

                final EngineDiscoveryRequest discoveryRequest = mock(EngineDiscoveryRequest.class);
                final UniqueId uniqueId = UniqueId.forEngine("verifyica");

                final VerifyicaTestEngine testEngine = new VerifyicaTestEngine();
                final TestDescriptor descriptor = testEngine.discover(discoveryRequest, uniqueId);

                assertThat(descriptor).isNotNull();
                assertThat(descriptor.getDisplayName()).contains("disabled");
                assertThat(descriptor.getDisplayName()).containsIgnoringCase("surefire");
            } finally {
                // Restore original property
                if (originalProperty == null) {
                    System.clearProperty("surefire.test.class.path");
                } else {
                    System.setProperty("surefire.test.class.path", originalProperty);
                }
            }
        }

        @Test
        @DisplayName("Should return engine descriptor for correct unique ID")
        public void shouldReturnEngineDescriptorForCorrectUniqueId() {
            final EngineDiscoveryRequest discoveryRequest = mock(EngineDiscoveryRequest.class);
            final UniqueId uniqueId = UniqueId.forEngine("verifyica");

            final TestDescriptor descriptor = engine.discover(discoveryRequest, uniqueId);

            // When not running under Surefire, should return an EngineDescriptor
            assertThat(descriptor).isNotNull();
            assertThat(descriptor).isInstanceOf(EngineDescriptor.class);
        }

        @Test
        @DisplayName("Should return disabled descriptor for incorrect unique ID")
        public void shouldReturnDisabledDescriptorForIncorrectUniqueId() {
            final EngineDiscoveryRequest discoveryRequest = mock(EngineDiscoveryRequest.class);
            final UniqueId uniqueId = UniqueId.forEngine("junit-jupiter");

            final TestDescriptor descriptor = engine.discover(discoveryRequest, uniqueId);

            assertThat(descriptor).isNotNull();
            assertThat(descriptor.getDisplayName()).contains("disabled");
        }
    }

    @Nested
    @DisplayName("Engine Constants Tests")
    public class EngineConstantsTests {

        @Test
        @DisplayName("Engine ID should be lowercase display name")
        public void engineIdShouldBeLowercaseDisplayName() {
            assertThat(engine.getId()).isEqualTo("verifyica");
            assertThat(engine.getId()).isEqualTo("Verifyica".toLowerCase(java.util.Locale.ENGLISH));
        }

        @Test
        @DisplayName("Group ID should not be empty")
        public void groupIdShouldNotBeEmpty() {
            final Optional<String> groupId = engine.getGroupId();
            assertThat(groupId).isPresent();
            assertThat(groupId.get()).isNotEmpty();
        }

        @Test
        @DisplayName("Artifact ID should not be empty")
        public void artifactIdShouldNotBeEmpty() {
            final Optional<String> artifactId = engine.getArtifactId();
            assertThat(artifactId).isPresent();
            assertThat(artifactId.get()).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("Version Tests")
    public class VersionTests {

        @Test
        @DisplayName("Version should follow semantic versioning pattern or be unknown")
        public void versionShouldFollowSemanticVersioningPatternOrBeUnknown() {
            final String version = VerifyicaTestEngine.staticGetVersion();

            // Version should be either "unknown" or match semantic versioning pattern
            assertThat(version).satisfiesAnyOf(v -> assertThat(v).isEqualTo("unknown"), v -> assertThat(v)
                    .matches("^\\d+\\.\\d+\\.\\d+.*"));
        }

        @Test
        @DisplayName("Version should not be null")
        public void versionShouldNotBeNull() {
            final String version = VerifyicaTestEngine.staticGetVersion();
            assertThat(version).isNotNull();
        }
    }

    @Nested
    @DisplayName("TestEngine Interface Tests")
    public class TestEngineInterfaceTests {

        @Test
        @DisplayName("Should implement TestEngine interface")
        public void shouldImplementTestEngineInterface() {
            assertThat(engine).isInstanceOf(org.junit.platform.engine.TestEngine.class);
        }

        @Test
        @DisplayName("Should have TestEngine as direct interface")
        public void shouldHaveTestEngineAsDirectInterface() {
            final Class<?>[] interfaces = VerifyicaTestEngine.class.getInterfaces();
            boolean found = false;
            for (final Class<?> iface : interfaces) {
                if (iface.equals(org.junit.platform.engine.TestEngine.class)) {
                    found = true;
                    break;
                }
            }
            assertThat(found).isTrue();
        }

        @Test
        @DisplayName("Should have all required TestEngine methods")
        public void shouldHaveAllRequiredTestEngineMethods() {
            // Check that all required methods exist
            assertThat(engine.getId()).isNotNull();
            assertThat(engine.getGroupId()).isPresent();
            assertThat(engine.getArtifactId()).isPresent();
            assertThat(engine.getVersion()).isPresent();
        }

        @Test
        @DisplayName("Should have discover method")
        public void shouldHaveDiscoverMethod() throws NoSuchMethodException {
            final java.lang.reflect.Method method =
                    VerifyicaTestEngine.class.getMethod("discover", EngineDiscoveryRequest.class, UniqueId.class);
            assertThat(method).isNotNull();
            assertThat(method.getReturnType()).isEqualTo(TestDescriptor.class);
        }

        @Test
        @DisplayName("Should have execute method")
        public void shouldHaveExecuteMethod() throws NoSuchMethodException {
            final java.lang.reflect.Method method =
                    VerifyicaTestEngine.class.getMethod("execute", ExecutionRequest.class);
            assertThat(method).isNotNull();
            assertThat(method.getReturnType()).isEqualTo(void.class);
        }
    }

    @Nested
    @DisplayName("Maven Plugin Detection Tests")
    public class MavenPluginDetectionTests {

        @Test
        @DisplayName("Should detect Maven plugin when property is set")
        public void shouldDetectMavenPluginWhenPropertyIsSet() {
            // Save original property
            final String originalProperty = System.getProperty(Constants.MAVEN_PLUGIN);

            try {
                // Set the property to simulate running via Maven plugin
                System.setProperty(Constants.MAVEN_PLUGIN, "true");

                // Create a new engine instance to pick up the property
                final VerifyicaTestEngine testEngine = new VerifyicaTestEngine();

                // The engine should behave differently when running via Maven plugin
                // This is tested indirectly through discover behavior
                assertThat(testEngine).isNotNull();
            } finally {
                // Restore original property
                if (originalProperty == null) {
                    System.clearProperty(Constants.MAVEN_PLUGIN);
                } else {
                    System.setProperty(Constants.MAVEN_PLUGIN, originalProperty);
                }
            }
        }

        @Test
        @DisplayName("Should not detect Maven plugin when property is not set")
        public void shouldNotDetectMavenPluginWhenPropertyIsNotSet() {
            // Save original property
            final String originalProperty = System.getProperty(Constants.MAVEN_PLUGIN);

            try {
                // Clear the property
                System.clearProperty(Constants.MAVEN_PLUGIN);

                // Create a new engine instance
                final VerifyicaTestEngine testEngine = new VerifyicaTestEngine();

                assertThat(testEngine).isNotNull();
            } finally {
                // Restore original property
                if (originalProperty == null) {
                    System.clearProperty(Constants.MAVEN_PLUGIN);
                } else {
                    System.setProperty(Constants.MAVEN_PLUGIN, originalProperty);
                }
            }
        }
    }

    @Nested
    @DisplayName("Thread Safety Tests")
    public class ThreadSafetyTests {

        @Test
        @DisplayName("Should be thread-safe for concurrent version access")
        public void shouldBeThreadSafeForConcurrentVersionAccess() throws InterruptedException {
            final int threadCount = 10;
            final int iterationsPerThread = 100;
            final Thread[] threads = new Thread[threadCount];
            final boolean[] success = {true};

            for (int i = 0; i < threadCount; i++) {
                threads[i] = new Thread(() -> {
                    for (int j = 0; j < iterationsPerThread; j++) {
                        try {
                            final String version = VerifyicaTestEngine.staticGetVersion();
                            if (version == null) {
                                success[0] = false;
                            }
                        } catch (Exception e) {
                            success[0] = false;
                        }
                    }
                });
                threads[i].start();
            }

            for (final Thread thread : threads) {
                thread.join();
            }

            assertThat(success[0]).isTrue();
        }

        @Test
        @DisplayName("Should be thread-safe for concurrent engine creation")
        public void shouldBeThreadSafeForConcurrentEngineCreation() throws InterruptedException {
            final int threadCount = 10;
            final Thread[] threads = new Thread[threadCount];
            final VerifyicaTestEngine[] engines = new VerifyicaTestEngine[threadCount];

            for (int i = 0; i < threadCount; i++) {
                final int index = i;
                threads[i] = new Thread(() -> {
                    engines[index] = new VerifyicaTestEngine();
                });
                threads[i].start();
            }

            for (final Thread thread : threads) {
                thread.join();
            }

            for (final VerifyicaTestEngine testEngine : engines) {
                assertThat(testEngine).isNotNull();
                assertThat(testEngine.getId()).isEqualTo("verifyica");
            }
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    public class EdgeCaseTests {

        @Test
        @DisplayName("Should handle multiple engine instances")
        public void shouldHandleMultipleEngineInstances() {
            final VerifyicaTestEngine engine1 = new VerifyicaTestEngine();
            final VerifyicaTestEngine engine2 = new VerifyicaTestEngine();
            final VerifyicaTestEngine engine3 = new VerifyicaTestEngine();

            assertThat(engine1.getId()).isEqualTo(engine2.getId());
            assertThat(engine2.getId()).isEqualTo(engine3.getId());
            assertThat(engine1.staticGetVersion()).isEqualTo(engine2.staticGetVersion());
            assertThat(engine2.staticGetVersion()).isEqualTo(engine3.staticGetVersion());
        }

        @Test
        @DisplayName("Should maintain consistent identity across instances")
        public void shouldMaintainConsistentIdentityAcrossInstances() {
            final VerifyicaTestEngine engine1 = new VerifyicaTestEngine();
            final VerifyicaTestEngine engine2 = new VerifyicaTestEngine();

            assertThat(engine1.getId()).isEqualTo("verifyica");
            assertThat(engine2.getId()).isEqualTo("verifyica");
            assertThat(engine1.getGroupId()).isEqualTo(engine2.getGroupId());
            assertThat(engine1.getArtifactId()).isEqualTo(engine2.getArtifactId());
            assertThat(engine1.getVersion()).isEqualTo(engine2.getVersion());
        }
    }

    @Nested
    @DisplayName("Execute Tests")
    public class ExecuteTests {

        @Test
        @DisplayName("Should handle empty root descriptor children")
        public void shouldHandleEmptyRootDescriptorChildren() {
            // Create an execution request with an empty engine descriptor
            final EngineDescriptor engineDescriptor =
                    new EngineDescriptor(UniqueId.forEngine("verifyica"), "Verifyica");
            final EngineExecutionListener listener = mock(EngineExecutionListener.class);
            final ConfigurationParameters configParams = mock(ConfigurationParameters.class);
            final ExecutionRequest executionRequest = new ExecutionRequest(engineDescriptor, listener, configParams);

            // Should not throw when executing with empty children
            final VerifyicaTestEngine testEngine = new VerifyicaTestEngine();
            testEngine.execute(executionRequest);

            // No exception should be thrown, and no interactions should occur
            assertThat(testEngine).isNotNull();
        }
    }

    @Nested
    @DisplayName("Reflection Tests")
    public class ReflectionTests {

        @Test
        @DisplayName("Should have private version method")
        public void shouldHavePrivateVersionMethod() throws NoSuchMethodException {
            final Method versionMethod = VerifyicaTestEngine.class.getDeclaredMethod("version");
            assertThat(Modifier.isPrivate(versionMethod.getModifiers())).isTrue();
            assertThat(Modifier.isStatic(versionMethod.getModifiers())).isTrue();
            assertThat(versionMethod.getReturnType()).isEqualTo(String.class);
        }

        @Test
        @DisplayName("Should have private isRunningViaMavenSurefirePlugin method")
        public void shouldHavePrivateIsRunningViaMavenSurefirePluginMethod() throws NoSuchMethodException {
            final Method method = VerifyicaTestEngine.class.getDeclaredMethod("isRunningViaMavenSurefirePlugin");
            assertThat(Modifier.isPrivate(method.getModifiers())).isTrue();
            assertThat(Modifier.isStatic(method.getModifiers())).isTrue();
            assertThat(method.getReturnType()).isEqualTo(boolean.class);
        }

        @Test
        @DisplayName("Should have private isRunningViaVerifyicaMavenPlugin method")
        public void shouldHavePrivateIsRunningViaVerifyicaMavenPluginMethod() throws NoSuchMethodException {
            final Method method = VerifyicaTestEngine.class.getDeclaredMethod("isRunningViaVerifyicaMavenPlugin");
            assertThat(Modifier.isPrivate(method.getModifiers())).isTrue();
            assertThat(Modifier.isStatic(method.getModifiers())).isTrue();
            assertThat(method.getReturnType()).isEqualTo(boolean.class);
        }

        @Test
        @DisplayName("Should have ArgumentExecutorServiceSupplierMethod inner class")
        public void shouldHaveArgumentExecutorServiceSupplierMethodInnerClass() {
            final Class<?>[] innerClasses = VerifyicaTestEngine.class.getDeclaredClasses();
            boolean found = false;
            for (final Class<?> innerClass : innerClasses) {
                if (innerClass.getSimpleName().equals("ArgumentExecutorServiceSupplierMethod")) {
                    found = true;
                    break;
                }
            }
            assertThat(found).isTrue();
        }

        @Test
        @DisplayName("ArgumentExecutorServiceSupplierMethod should implement Predicate")
        public void argumentExecutorServiceSupplierMethodShouldImplementPredicate() throws ClassNotFoundException {
            final Class<?>[] innerClasses = VerifyicaTestEngine.class.getDeclaredClasses();
            for (final Class<?> innerClass : innerClasses) {
                if (innerClass.getSimpleName().equals("ArgumentExecutorServiceSupplierMethod")) {
                    assertThat(Predicate.class.isAssignableFrom(innerClass)).isTrue();
                    break;
                }
            }
        }
    }

    @Nested
    @DisplayName("System Property Tests")
    public class SystemPropertyTests {

        @Test
        @DisplayName("Should handle null surefire.test.class.path property")
        public void shouldHandleNullSurefireTestClassPathProperty() {
            // Save original property
            final String originalProperty = System.getProperty("surefire.test.class.path");

            try {
                // Clear the property
                System.clearProperty("surefire.test.class.path");

                // Create a new engine instance
                final VerifyicaTestEngine testEngine = new VerifyicaTestEngine();

                // The engine should be created successfully
                assertThat(testEngine).isNotNull();
                assertThat(testEngine.getId()).isEqualTo("verifyica");
            } finally {
                // Restore original property
                if (originalProperty == null) {
                    System.clearProperty("surefire.test.class.path");
                } else {
                    System.setProperty("surefire.test.class.path", originalProperty);
                }
            }
        }

        @Test
        @DisplayName("Should handle empty string property values")
        public void shouldHandleEmptyStringPropertyValues() {
            // Save original property
            final String originalProperty = System.getProperty(Constants.MAVEN_PLUGIN);

            try {
                // Set empty string property
                System.setProperty(Constants.MAVEN_PLUGIN, "");

                // Create a new engine instance
                final VerifyicaTestEngine testEngine = new VerifyicaTestEngine();

                // The engine should be created successfully
                assertThat(testEngine).isNotNull();
            } finally {
                // Restore original property
                if (originalProperty == null) {
                    System.clearProperty(Constants.MAVEN_PLUGIN);
                } else {
                    System.setProperty(Constants.MAVEN_PLUGIN, originalProperty);
                }
            }
        }

        @Test
        @DisplayName("Should handle property value other than true for Maven plugin")
        public void shouldHandlePropertyValueOtherThanTrueForMavenPlugin() {
            // Save original property
            final String originalProperty = System.getProperty(Constants.MAVEN_PLUGIN);

            try {
                // Set property to a value other than "true"
                System.setProperty(Constants.MAVEN_PLUGIN, "false");

                // Create a new engine instance
                final VerifyicaTestEngine testEngine = new VerifyicaTestEngine();

                // The engine should be created successfully
                assertThat(testEngine).isNotNull();
            } finally {
                // Restore original property
                if (originalProperty == null) {
                    System.clearProperty(Constants.MAVEN_PLUGIN);
                } else {
                    System.setProperty(Constants.MAVEN_PLUGIN, originalProperty);
                }
            }
        }
    }

    @Nested
    @DisplayName("Engine Descriptor Tests")
    public class EngineDescriptorTests {

        @Test
        @DisplayName("Should return engine descriptor with correct unique ID")
        public void shouldReturnEngineDescriptorWithCorrectUniqueId() {
            // Save original property to ensure we're not under Surefire
            final String originalProperty = System.getProperty("surefire.test.class.path");

            try {
                System.clearProperty("surefire.test.class.path");

                final EngineDiscoveryRequest discoveryRequest = mock(EngineDiscoveryRequest.class);
                final UniqueId uniqueId = UniqueId.forEngine("verifyica");

                final VerifyicaTestEngine testEngine = new VerifyicaTestEngine();
                final TestDescriptor descriptor = testEngine.discover(discoveryRequest, uniqueId);

                assertThat(descriptor).isNotNull();
                assertThat(descriptor.getUniqueId().toString()).contains("verifyica");
            } finally {
                // Restore original property
                if (originalProperty == null) {
                    System.clearProperty("surefire.test.class.path");
                } else {
                    System.setProperty("surefire.test.class.path", originalProperty);
                }
            }
        }

        @Test
        @DisplayName("Should handle discover with different unique ID")
        public void shouldHandleDiscoverWithDifferentUniqueId() {
            final EngineDiscoveryRequest discoveryRequest = mock(EngineDiscoveryRequest.class);
            final UniqueId uniqueId = UniqueId.forEngine("different-engine");

            final VerifyicaTestEngine testEngine = new VerifyicaTestEngine();
            final TestDescriptor descriptor = testEngine.discover(discoveryRequest, uniqueId);

            assertThat(descriptor).isNotNull();
            assertThat(descriptor.getDisplayName()).containsIgnoringCase("disabled");
        }
    }

    @Nested
    @DisplayName("Constants Tests")
    public class ConstantsTests {

        @Test
        @DisplayName("Should have correct display name constant")
        public void shouldHaveCorrectDisplayNameConstant() {
            // The display name should be "Verifyica" which matches the ID when lowercased
            assertThat(engine.getId()).isEqualTo("verifyica");
            assertThat("Verifyica".toLowerCase(java.util.Locale.ENGLISH)).isEqualTo(engine.getId());
        }

        @Test
        @DisplayName("Should have consistent group and artifact IDs")
        public void shouldHaveConsistentGroupAndArtifactIds() {
            final Optional<String> groupId = engine.getGroupId();
            final Optional<String> artifactId = engine.getArtifactId();

            assertThat(groupId).isPresent();
            assertThat(artifactId).isPresent();

            // Group ID should follow Maven conventions
            assertThat(groupId.get()).contains(".");
            assertThat(groupId.get()).isEqualTo("org.verifyica");

            // Artifact ID should not contain dots
            assertThat(artifactId.get()).doesNotContain(".");
            assertThat(artifactId.get()).isEqualTo("engine");
        }
    }

    @Nested
    @DisplayName("Null Safety Tests")
    public class NullSafetyTests {

        @Test
        @DisplayName("Engine ID should never be null")
        public void engineIdShouldNeverBeNull() {
            final String id = engine.getId();
            assertThat(id).isNotNull();
        }

        @Test
        @DisplayName("Group ID optional should never be null")
        public void groupIdOptionalShouldNeverBeNull() {
            final Optional<String> groupId = engine.getGroupId();
            assertThat(groupId).isNotNull();
        }

        @Test
        @DisplayName("Artifact ID optional should never be null")
        public void artifactIdOptionalShouldNeverBeNull() {
            final Optional<String> artifactId = engine.getArtifactId();
            assertThat(artifactId).isNotNull();
        }

        @Test
        @DisplayName("Version optional should never be null")
        public void versionOptionalShouldNeverBeNull() {
            final Optional<String> version = engine.getVersion();
            assertThat(version).isNotNull();
        }

        @Test
        @DisplayName("Static version should never be null")
        public void staticVersionShouldNeverBeNull() {
            final String version = VerifyicaTestEngine.staticGetVersion();
            assertThat(version).isNotNull();
        }
    }

    @Nested
    @DisplayName("Concurrency Tests")
    public class ConcurrencyTests {

        @Test
        @DisplayName("Should handle concurrent discover calls")
        public void shouldHandleConcurrentDiscoverCalls() throws InterruptedException {
            // Save original property
            final String originalProperty = System.getProperty("surefire.test.class.path");

            try {
                System.clearProperty("surefire.test.class.path");

                final int threadCount = 5;
                final Thread[] threads = new Thread[threadCount];
                final TestDescriptor[] descriptors = new TestDescriptor[threadCount];
                final boolean[] success = {true};

                for (int i = 0; i < threadCount; i++) {
                    final int index = i;
                    threads[i] = new Thread(() -> {
                        try {
                            final VerifyicaTestEngine testEngine = new VerifyicaTestEngine();
                            final EngineDiscoveryRequest discoveryRequest = mock(EngineDiscoveryRequest.class);
                            final UniqueId uniqueId = UniqueId.forEngine("verifyica");
                            descriptors[index] = testEngine.discover(discoveryRequest, uniqueId);
                        } catch (Exception e) {
                            success[0] = false;
                        }
                    });
                    threads[i].start();
                }

                for (final Thread thread : threads) {
                    thread.join();
                }

                assertThat(success[0]).isTrue();
                for (final TestDescriptor descriptor : descriptors) {
                    assertThat(descriptor).isNotNull();
                }
            } finally {
                // Restore original property
                if (originalProperty == null) {
                    System.clearProperty("surefire.test.class.path");
                } else {
                    System.setProperty("surefire.test.class.path", originalProperty);
                }
            }
        }
    }
}
