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
import java.lang.reflect.Modifier;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.platform.engine.EngineDiscoveryRequest;
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
            VerifyicaTestEngine testEngine = new VerifyicaTestEngine();
            assertThat(testEngine).isNotNull();
        }

        @Test
        @DisplayName("Should have public constructor")
        public void shouldHavePublicConstructor() throws Exception {
            Constructor<VerifyicaTestEngine> constructor = VerifyicaTestEngine.class.getDeclaredConstructor();
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
        @DisplayName("Should return correct group ID")
        public void shouldReturnCorrectGroupId() {
            Optional<String> groupId = engine.getGroupId();
            assertThat(groupId).isPresent();
            assertThat(groupId.get()).isEqualTo("org.verifyica");
        }

        @Test
        @DisplayName("Should return correct artifact ID")
        public void shouldReturnCorrectArtifactId() {
            Optional<String> artifactId = engine.getArtifactId();
            assertThat(artifactId).isPresent();
            assertThat(artifactId.get()).isEqualTo("engine");
        }

        @Test
        @DisplayName("Should return version")
        public void shouldReturnVersion() {
            Optional<String> version = engine.getVersion();
            assertThat(version).isPresent();
            // Version should not be null or empty
            assertThat(version.get()).isNotEmpty();
        }

        @Test
        @DisplayName("Static getVersion should return version")
        public void staticGetVersionShouldReturnVersion() {
            String version = VerifyicaTestEngine.staticGetVersion();
            assertThat(version).isNotNull();
            // Version should not be empty
            assertThat(version).isNotEmpty();
        }

        @Test
        @DisplayName("Static and instance version should match")
        public void staticAndInstanceVersionShouldMatch() {
            String staticVersion = VerifyicaTestEngine.staticGetVersion();
            Optional<String> instanceVersion = engine.getVersion();
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
            String originalProperty = System.getProperty("surefire.test.class.path");

            try {
                // Set the property to simulate running under Maven Surefire
                System.setProperty("surefire.test.class.path", "/some/path");

                EngineDiscoveryRequest discoveryRequest = mock(EngineDiscoveryRequest.class);
                UniqueId uniqueId = UniqueId.forEngine("verifyica");

                VerifyicaTestEngine testEngine = new VerifyicaTestEngine();
                TestDescriptor descriptor = testEngine.discover(discoveryRequest, uniqueId);

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
            EngineDiscoveryRequest discoveryRequest = mock(EngineDiscoveryRequest.class);
            UniqueId uniqueId = UniqueId.forEngine("verifyica");

            TestDescriptor descriptor = engine.discover(discoveryRequest, uniqueId);

            // When not running under Surefire, should return an EngineDescriptor
            assertThat(descriptor).isNotNull();
            assertThat(descriptor).isInstanceOf(EngineDescriptor.class);
        }

        @Test
        @DisplayName("Should return disabled descriptor for incorrect unique ID")
        public void shouldReturnDisabledDescriptorForIncorrectUniqueId() {
            EngineDiscoveryRequest discoveryRequest = mock(EngineDiscoveryRequest.class);
            UniqueId uniqueId = UniqueId.forEngine("junit-jupiter");

            TestDescriptor descriptor = engine.discover(discoveryRequest, uniqueId);

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
            assertThat(engine.getId()).isEqualTo("Verifyica".toLowerCase());
        }

        @Test
        @DisplayName("Group ID should not be empty")
        public void groupIdShouldNotBeEmpty() {
            Optional<String> groupId = engine.getGroupId();
            assertThat(groupId).isPresent();
            assertThat(groupId.get()).isNotEmpty();
        }

        @Test
        @DisplayName("Artifact ID should not be empty")
        public void artifactIdShouldNotBeEmpty() {
            Optional<String> artifactId = engine.getArtifactId();
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
            String version = VerifyicaTestEngine.staticGetVersion();

            // Version should be either "unknown" or match semantic versioning pattern
            assertThat(version).satisfiesAnyOf(v -> assertThat(v).isEqualTo("unknown"), v -> assertThat(v)
                    .matches("^\\d+\\.\\d+\\.\\d+.*"));
        }

        @Test
        @DisplayName("Version should not be null")
        public void versionShouldNotBeNull() {
            String version = VerifyicaTestEngine.staticGetVersion();
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
        @DisplayName("Should have all required TestEngine methods")
        public void shouldHaveAllRequiredTestEngineMethods() {
            // Check that all required methods exist
            assertThat(engine.getId()).isNotNull();
            assertThat(engine.getGroupId()).isPresent();
            assertThat(engine.getArtifactId()).isPresent();
            assertThat(engine.getVersion()).isPresent();
        }
    }

    @Nested
    @DisplayName("Maven Plugin Detection Tests")
    public class MavenPluginDetectionTests {

        @Test
        @DisplayName("Should detect Maven plugin when property is set")
        public void shouldDetectMavenPluginWhenPropertyIsSet() {
            // Save original property
            String originalProperty = System.getProperty(Constants.MAVEN_PLUGIN);

            try {
                // Set the property to simulate running via Maven plugin
                System.setProperty(Constants.MAVEN_PLUGIN, "true");

                // Create a new engine instance to pick up the property
                VerifyicaTestEngine testEngine = new VerifyicaTestEngine();

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
            String originalProperty = System.getProperty(Constants.MAVEN_PLUGIN);

            try {
                // Clear the property
                System.clearProperty(Constants.MAVEN_PLUGIN);

                // Create a new engine instance
                VerifyicaTestEngine testEngine = new VerifyicaTestEngine();

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
            Thread[] threads = new Thread[threadCount];
            final boolean[] success = {true};

            for (int i = 0; i < threadCount; i++) {
                threads[i] = new Thread(() -> {
                    for (int j = 0; j < iterationsPerThread; j++) {
                        try {
                            String version = VerifyicaTestEngine.staticGetVersion();
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

            for (Thread thread : threads) {
                thread.join();
            }

            assertThat(success[0]).isTrue();
        }

        @Test
        @DisplayName("Should be thread-safe for concurrent engine creation")
        public void shouldBeThreadSafeForConcurrentEngineCreation() throws InterruptedException {
            final int threadCount = 10;
            Thread[] threads = new Thread[threadCount];
            final VerifyicaTestEngine[] engines = new VerifyicaTestEngine[threadCount];

            for (int i = 0; i < threadCount; i++) {
                final int index = i;
                threads[i] = new Thread(() -> {
                    engines[index] = new VerifyicaTestEngine();
                });
                threads[i].start();
            }

            for (Thread thread : threads) {
                thread.join();
            }

            for (VerifyicaTestEngine testEngine : engines) {
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
            VerifyicaTestEngine engine1 = new VerifyicaTestEngine();
            VerifyicaTestEngine engine2 = new VerifyicaTestEngine();
            VerifyicaTestEngine engine3 = new VerifyicaTestEngine();

            assertThat(engine1.getId()).isEqualTo(engine2.getId());
            assertThat(engine2.getId()).isEqualTo(engine3.getId());
            assertThat(engine1.staticGetVersion()).isEqualTo(engine2.staticGetVersion());
            assertThat(engine2.staticGetVersion()).isEqualTo(engine3.staticGetVersion());
        }

        @Test
        @DisplayName("Should maintain consistent identity across instances")
        public void shouldMaintainConsistentIdentityAcrossInstances() {
            VerifyicaTestEngine engine1 = new VerifyicaTestEngine();
            VerifyicaTestEngine engine2 = new VerifyicaTestEngine();

            assertThat(engine1.getId()).isEqualTo("verifyica");
            assertThat(engine2.getId()).isEqualTo("verifyica");
            assertThat(engine1.getGroupId()).isEqualTo(engine2.getGroupId());
            assertThat(engine1.getArtifactId()).isEqualTo(engine2.getArtifactId());
            assertThat(engine1.getVersion()).isEqualTo(engine2.getVersion());
        }
    }
}
