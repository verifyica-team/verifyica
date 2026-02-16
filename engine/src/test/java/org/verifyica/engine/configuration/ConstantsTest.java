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

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import org.junit.jupiter.api.*;

@DisplayName("Constants Tests")
public class ConstantsTest {

    @Nested
    @DisplayName("Constructor Tests")
    public class ConstructorTests {

        @Test
        @DisplayName("Should have private constructor")
        public void shouldHavePrivateConstructor() throws Exception {
            Constructor<Constants> constructor = Constants.class.getDeclaredConstructor();
            assertThat(Modifier.isPrivate(constructor.getModifiers())).isTrue();
        }

        @Test
        @DisplayName("Should have final public class modifier")
        public void shouldHaveFinalClassModifier() {
            assertThat(Modifier.isFinal(Constants.class.getModifiers())).isTrue();
        }

        @Test
        @DisplayName("Private constructor should be accessible via reflection")
        public void privateConstructorShouldBeAccessibleViaReflection() throws Exception {
            Constructor<Constants> constructor = Constants.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            Constants instance = constructor.newInstance();
            assertThat(instance).isNotNull();
        }
    }

    @Nested
    @DisplayName("Basic Constant Value Tests")
    public class BasicConstantValueTests {

        @Test
        @DisplayName("Should have correct TRUE constant")
        public void shouldHaveCorrectTrueConstant() {
            assertThat(Constants.TRUE).isEqualTo("true");
        }

        @Test
        @DisplayName("Should have correct FALSE constant")
        public void shouldHaveCorrectFalseConstant() {
            assertThat(Constants.FALSE).isEqualTo("false");
        }

        @Test
        @DisplayName("Should have correct VIRTUAL constant")
        public void shouldHaveCorrectVirtualConstant() {
            assertThat(Constants.VIRTUAL).isEqualTo("virtual");
        }

        @Test
        @DisplayName("Should have correct PLATFORM constant")
        public void shouldHaveCorrectPlatformConstant() {
            assertThat(Constants.PLATFORM).isEqualTo("platform");
        }

        @Test
        @DisplayName("Should have correct PLATFORM_EPHEMERAL constant")
        public void shouldHaveCorrectPlatformEphemeralConstant() {
            assertThat(Constants.PLATFORM_EPHEMERAL).isEqualTo("platform-ephemeral");
        }
    }

    @Nested
    @DisplayName("Prefix Hierarchy Tests")
    public class PrefixHierarchyTests {

        @Test
        @DisplayName("Should have correct PREFIX constant")
        public void shouldHaveCorrectPrefixConstant() {
            assertThat(Constants.PREFIX).isEqualTo("verifyica");
        }

        @Test
        @DisplayName("Should have correct ENGINE constant")
        public void shouldHaveCorrectEngineConstant() {
            assertThat(Constants.ENGINE).isEqualTo("verifyica.engine");
            assertThat(Constants.ENGINE).isEqualTo(Constants.PREFIX + ".engine");
        }

        @Test
        @DisplayName("Should have correct ENGINE_THREAD constant")
        public void shouldHaveCorrectEngineThreadConstant() {
            assertThat(Constants.ENGINE_THREAD).isEqualTo("verifyica.engine.thread");
            assertThat(Constants.ENGINE_THREAD).isEqualTo(Constants.ENGINE + ".thread");
        }

        @Test
        @DisplayName("Should have correct ENGINE_THREAD_TYPE constant")
        public void shouldHaveCorrectEngineThreadTypeConstant() {
            assertThat(Constants.ENGINE_THREAD_TYPE).isEqualTo("verifyica.engine.thread.type");
            assertThat(Constants.ENGINE_THREAD_TYPE).isEqualTo(Constants.ENGINE_THREAD + ".type");
        }
    }

    @Nested
    @DisplayName("Logger Configuration Constants Tests")
    public class LoggerConfigurationConstantsTests {

        @Test
        @DisplayName("Should have correct ENGINE_LOGGER_REGEX constant")
        public void shouldHaveCorrectEngineLoggerRegexConstant() {
            assertThat(Constants.ENGINE_LOGGER_REGEX).isEqualTo("verifyica.engine.logger.regex");
            assertThat(Constants.ENGINE_LOGGER_REGEX).isEqualTo(Constants.ENGINE + ".logger.regex");
        }

        @Test
        @DisplayName("Should have correct ENGINE_LOGGER_LEVEL constant")
        public void shouldHaveCorrectEngineLoggerLevelConstant() {
            assertThat(Constants.ENGINE_LOGGER_LEVEL).isEqualTo("verifyica.engine.logger.level");
            assertThat(Constants.ENGINE_LOGGER_LEVEL).isEqualTo(Constants.ENGINE + ".logger.level");
        }
    }

    @Nested
    @DisplayName("Autowired Configuration Constants Tests")
    public class AutowiredConfigurationConstantsTests {

        @Test
        @DisplayName("Should have correct ENGINE_AUTOWIRED constant")
        public void shouldHaveCorrectEngineAutowiredConstant() {
            assertThat(Constants.ENGINE_AUTOWIRED).isEqualTo("verifyica.engine.autowired");
            assertThat(Constants.ENGINE_AUTOWIRED).isEqualTo(Constants.ENGINE + ".autowired");
        }

        @Test
        @DisplayName("Should have correct ENGINE_PRUNE_STACK_TRACE constant")
        public void shouldHaveCorrectEnginePruneStackTraceConstant() {
            assertThat(Constants.ENGINE_PRUNE_STACK_TRACE).isEqualTo("verifyica.engine.prune.stacktraces");
            assertThat(Constants.ENGINE_PRUNE_STACK_TRACE).isEqualTo(Constants.ENGINE + ".prune.stacktraces");
        }

        @Test
        @DisplayName("Should have correct ENGINE_AUTOWIRED_ENGINE constant")
        public void shouldHaveCorrectEngineAutowiredEngineConstant() {
            assertThat(Constants.ENGINE_AUTOWIRED_ENGINE).isEqualTo("verifyica.engine.autowired.engine");
            assertThat(Constants.ENGINE_AUTOWIRED_ENGINE).isEqualTo(Constants.ENGINE_AUTOWIRED + ".engine");
        }
    }

    @Nested
    @DisplayName("Engine Interceptors Configuration Constants Tests")
    public class EngineInterceptorsConfigurationConstantsTests {

        @Test
        @DisplayName("Should have correct ENGINE_AUTOWIRED_ENGINE_INTERCEPTORS constant")
        public void shouldHaveCorrectEngineAutowiredEngineInterceptorsConstant() {
            assertThat(Constants.ENGINE_AUTOWIRED_ENGINE_INTERCEPTORS)
                    .isEqualTo("verifyica.engine.autowired.engine.interceptors");
            assertThat(Constants.ENGINE_AUTOWIRED_ENGINE_INTERCEPTORS)
                    .isEqualTo(Constants.ENGINE_AUTOWIRED_ENGINE + ".interceptors");
        }

        @Test
        @DisplayName("Should have correct ENGINE_AUTOWIRED_ENGINE_INTERCEPTORS_INCLUDE constant")
        public void shouldHaveCorrectEngineAutowiredEngineInterceptorsIncludeConstant() {
            assertThat(Constants.ENGINE_AUTOWIRED_ENGINE_INTERCEPTORS_INCLUDE)
                    .isEqualTo("verifyica.engine.autowired.engine.interceptors.include");
            assertThat(Constants.ENGINE_AUTOWIRED_ENGINE_INTERCEPTORS_INCLUDE)
                    .isEqualTo(Constants.ENGINE_AUTOWIRED_ENGINE_INTERCEPTORS + ".include");
        }

        @Test
        @DisplayName("Should have correct ENGINE_AUTOWIRED_ENGINE_INTERCEPTORS_INCLUDE_REGEX constant")
        public void shouldHaveCorrectEngineAutowiredEngineInterceptorsIncludeRegexConstant() {
            assertThat(Constants.ENGINE_AUTOWIRED_ENGINE_INTERCEPTORS_INCLUDE_REGEX)
                    .isEqualTo("verifyica.engine.autowired.engine.interceptors.include.regex");
            assertThat(Constants.ENGINE_AUTOWIRED_ENGINE_INTERCEPTORS_INCLUDE_REGEX)
                    .isEqualTo(Constants.ENGINE_AUTOWIRED_ENGINE_INTERCEPTORS_INCLUDE + ".regex");
        }

        @Test
        @DisplayName("Should have correct ENGINE_AUTOWIRED_ENGINE_INTERCEPTORS_EXCLUDE constant")
        public void shouldHaveCorrectEngineAutowiredEngineInterceptorsExcludeConstant() {
            assertThat(Constants.ENGINE_AUTOWIRED_ENGINE_INTERCEPTORS_EXCLUDE)
                    .isEqualTo("verifyica.engine.autowired.engine.interceptors.exclude");
            assertThat(Constants.ENGINE_AUTOWIRED_ENGINE_INTERCEPTORS_EXCLUDE)
                    .isEqualTo(Constants.ENGINE_AUTOWIRED_ENGINE_INTERCEPTORS + ".exclude");
        }

        @Test
        @DisplayName("Should have correct ENGINE_AUTOWIRED_ENGINE_INTERCEPTORS_EXCLUDE_REGEX constant")
        public void shouldHaveCorrectEngineAutowiredEngineInterceptorsExcludeRegexConstant() {
            assertThat(Constants.ENGINE_AUTOWIRED_ENGINE_INTERCEPTORS_EXCLUDE_REGEX)
                    .isEqualTo("verifyica.engine.autowired.engine.interceptors.exclude.regex");
            assertThat(Constants.ENGINE_AUTOWIRED_ENGINE_INTERCEPTORS_EXCLUDE_REGEX)
                    .isEqualTo(Constants.ENGINE_AUTOWIRED_ENGINE_INTERCEPTORS_EXCLUDE + ".regex");
        }
    }

    @Nested
    @DisplayName("Class Configuration Constants Tests")
    public class ClassConfigurationConstantsTests {

        @Test
        @DisplayName("Should have correct ENGINE_CLASS constant")
        public void shouldHaveCorrectEngineClassConstant() {
            assertThat(Constants.ENGINE_CLASS).isEqualTo("verifyica.engine.class");
            assertThat(Constants.ENGINE_CLASS).isEqualTo(Constants.ENGINE + ".class");
        }

        @Test
        @DisplayName("Should have correct ENGINE_AUTOWIRED_CLASS constant")
        public void shouldHaveCorrectEngineAutowiredClassConstant() {
            assertThat(Constants.ENGINE_AUTOWIRED_CLASS).isEqualTo("verifyica.engine.autowired.class");
            assertThat(Constants.ENGINE_AUTOWIRED_CLASS).isEqualTo(Constants.ENGINE_AUTOWIRED + ".class");
        }

        @Test
        @DisplayName("Should have correct ENGINE_CLASS_PARALLELISM constant")
        public void shouldHaveCorrectEngineClassParallelismConstant() {
            assertThat(Constants.ENGINE_CLASS_PARALLELISM).isEqualTo("verifyica.engine.class.parallelism");
            assertThat(Constants.ENGINE_CLASS_PARALLELISM).isEqualTo(Constants.ENGINE_CLASS + ".parallelism");
        }

        @Test
        @DisplayName("Should have correct ENGINE_CLASS_STATE_MACHINE_THROTTLE constant")
        public void shouldHaveCorrectEngineClassStateMachineThrottleConstant() {
            assertThat(Constants.ENGINE_CLASS_STATE_MACHINE_THROTTLE)
                    .isEqualTo("verifyica.engine.class.state.machine.throttle");
            assertThat(Constants.ENGINE_CLASS_STATE_MACHINE_THROTTLE)
                    .isEqualTo(Constants.ENGINE_CLASS + ".state.machine.throttle");
        }
    }

    @Nested
    @DisplayName("Class Interceptors Configuration Constants Tests")
    public class ClassInterceptorsConfigurationConstantsTests {

        @Test
        @DisplayName("Should have correct ENGINE_AUTOWIRED_CLASS_INTERCEPTORS constant")
        public void shouldHaveCorrectEngineAutowiredClassInterceptorsConstant() {
            assertThat(Constants.ENGINE_AUTOWIRED_CLASS_INTERCEPTORS)
                    .isEqualTo("verifyica.engine.autowired.class.interceptors");
            assertThat(Constants.ENGINE_AUTOWIRED_CLASS_INTERCEPTORS)
                    .isEqualTo(Constants.ENGINE_AUTOWIRED_CLASS + ".interceptors");
        }

        @Test
        @DisplayName("Should have correct ENGINE_AUTOWIRED_CLASS_INTERCEPTORS_INCLUDE constant")
        public void shouldHaveCorrectEngineAutowiredClassInterceptorsIncludeConstant() {
            assertThat(Constants.ENGINE_AUTOWIRED_CLASS_INTERCEPTORS_INCLUDE)
                    .isEqualTo("verifyica.engine.autowired.class.interceptors.include");
            assertThat(Constants.ENGINE_AUTOWIRED_CLASS_INTERCEPTORS_INCLUDE)
                    .isEqualTo(Constants.ENGINE_AUTOWIRED_CLASS_INTERCEPTORS + ".include");
        }

        @Test
        @DisplayName("Should have correct ENGINE_AUTOWIRED_CLASS_INTERCEPTORS_INCLUDE_REGEX constant")
        public void shouldHaveCorrectEngineAutowiredClassInterceptorsIncludeRegexConstant() {
            assertThat(Constants.ENGINE_AUTOWIRED_CLASS_INTERCEPTORS_INCLUDE_REGEX)
                    .isEqualTo("verifyica.engine.autowired.class.interceptors.include.regex");
            assertThat(Constants.ENGINE_AUTOWIRED_CLASS_INTERCEPTORS_INCLUDE_REGEX)
                    .isEqualTo(Constants.ENGINE_AUTOWIRED_CLASS_INTERCEPTORS_INCLUDE + ".regex");
        }

        @Test
        @DisplayName("Should have correct ENGINE_AUTOWIRED_CLASS_INTERCEPTORS_EXCLUDE constant")
        public void shouldHaveCorrectEngineAutowiredClassInterceptorsExcludeConstant() {
            assertThat(Constants.ENGINE_AUTOWIRED_CLASS_INTERCEPTORS_EXCLUDE)
                    .isEqualTo("verifyica.engine.autowired.class.interceptors.exclude");
            assertThat(Constants.ENGINE_AUTOWIRED_CLASS_INTERCEPTORS_EXCLUDE)
                    .isEqualTo(Constants.ENGINE_AUTOWIRED_CLASS_INTERCEPTORS + ".exclude");
        }

        @Test
        @DisplayName("Should have correct ENGINE_AUTOWIRED_CLASS_INTERCEPTORS_EXCLUDE_REGEX constant")
        public void shouldHaveCorrectEngineAutowiredClassInterceptorsExcludeRegexConstant() {
            assertThat(Constants.ENGINE_AUTOWIRED_CLASS_INTERCEPTORS_EXCLUDE_REGEX)
                    .isEqualTo("verifyica.engine.autowired.class.interceptors.exclude.regex");
            assertThat(Constants.ENGINE_AUTOWIRED_CLASS_INTERCEPTORS_EXCLUDE_REGEX)
                    .isEqualTo(Constants.ENGINE_AUTOWIRED_CLASS_INTERCEPTORS_EXCLUDE + ".regex");
        }
    }

    @Nested
    @DisplayName("Filter Configuration Constants Tests")
    public class FilterConfigurationConstantsTests {

        @Test
        @DisplayName("Should have correct ENGINE_FILTER constant")
        public void shouldHaveCorrectEngineFilterConstant() {
            assertThat(Constants.ENGINE_FILTER).isEqualTo("verifyica.engine.filter");
            assertThat(Constants.ENGINE_FILTER).isEqualTo(Constants.ENGINE + ".filter");
        }

        @Test
        @DisplayName("Should have correct ENGINE_FILTER_DEFINITIONS constant")
        public void shouldHaveCorrectEngineFilterDefinitionsConstant() {
            assertThat(Constants.ENGINE_FILTER_DEFINITIONS).isEqualTo("verifyica.engine.filter.definitions");
            assertThat(Constants.ENGINE_FILTER_DEFINITIONS).isEqualTo(Constants.ENGINE_FILTER + ".definitions");
        }

        @Test
        @DisplayName("Should have correct ENGINE_FILTER_DEFINITIONS_FILENAME constant")
        public void shouldHaveCorrectEngineFilterDefinitionsFilenameConstant() {
            assertThat(Constants.ENGINE_FILTER_DEFINITIONS_FILENAME)
                    .isEqualTo("verifyica.engine.filter.definitions.filename");
            assertThat(Constants.ENGINE_FILTER_DEFINITIONS_FILENAME)
                    .isEqualTo(Constants.ENGINE_FILTER_DEFINITIONS + ".filename");
        }
    }

    @Nested
    @DisplayName("Argument Configuration Constants Tests")
    public class ArgumentConfigurationConstantsTests {

        @Test
        @DisplayName("Should have correct ENGINE_ARGUMENT constant")
        public void shouldHaveCorrectEngineArgumentConstant() {
            assertThat(Constants.ENGINE_ARGUMENT).isEqualTo("verifyica.engine.argument");
            assertThat(Constants.ENGINE_ARGUMENT).isEqualTo(Constants.ENGINE + ".argument");
        }

        @Test
        @DisplayName("Should have correct ENGINE_ARGUMENT_PARALLELISM constant")
        public void shouldHaveCorrectEngineArgumentParallelismConstant() {
            assertThat(Constants.ENGINE_ARGUMENT_PARALLELISM).isEqualTo("verifyica.engine.argument.parallelism");
            assertThat(Constants.ENGINE_ARGUMENT_PARALLELISM).isEqualTo(Constants.ENGINE_ARGUMENT + ".parallelism");
        }

        @Test
        @DisplayName("Should have correct ENGINE_ARGUMENT_STATE_MACHINE_THROTTLE constant")
        public void shouldHaveCorrectEngineArgumentStateMachineThrottleConstant() {
            assertThat(Constants.ENGINE_ARGUMENT_STATE_MACHINE_THROTTLE)
                    .isEqualTo("verifyica.engine.argument.state.machine.throttle");
            assertThat(Constants.ENGINE_ARGUMENT_STATE_MACHINE_THROTTLE)
                    .isEqualTo(Constants.ENGINE_ARGUMENT + ".state.machine.throttle");
        }
    }

    @Nested
    @DisplayName("Test Configuration Constants Tests")
    public class TestConfigurationConstantsTests {

        @Test
        @DisplayName("Should have correct ENGINE_TEST constant")
        public void shouldHaveCorrectEngineTestConstant() {
            assertThat(Constants.ENGINE_TEST).isEqualTo("verifyica.engine.test");
            assertThat(Constants.ENGINE_TEST).isEqualTo(Constants.ENGINE + ".test");
        }

        @Test
        @DisplayName("Should have correct ENGINE_TEST_STATE_MACHINE_THROTTLE constant")
        public void shouldHaveCorrectEngineTestStateMachineThrottleConstant() {
            assertThat(Constants.ENGINE_TEST_STATE_MACHINE_THROTTLE)
                    .isEqualTo("verifyica.engine.test.state.machine.throttle");
            assertThat(Constants.ENGINE_TEST_STATE_MACHINE_THROTTLE)
                    .isEqualTo(Constants.ENGINE_TEST + ".state.machine.throttle");
        }
    }

    @Nested
    @DisplayName("Maven Plugin Configuration Constants Tests")
    public class MavenPluginConfigurationConstantsTests {

        @Test
        @DisplayName("Should have correct MAVEN_PLUGIN constant")
        public void shouldHaveCorrectMavenPluginConstant() {
            assertThat(Constants.MAVEN_PLUGIN).isEqualTo("verifyica.maven.plugin");
            assertThat(Constants.MAVEN_PLUGIN).isEqualTo(Constants.PREFIX + ".maven.plugin");
        }

        @Test
        @DisplayName("Should have correct MAVEN_PLUGIN_MODE constant")
        public void shouldHaveCorrectMavenPluginModeConstant() {
            assertThat(Constants.MAVEN_PLUGIN_MODE).isEqualTo("verifyica.maven.plugin.mode");
            assertThat(Constants.MAVEN_PLUGIN_MODE).isEqualTo(Constants.MAVEN_PLUGIN + ".mode");
        }

        @Test
        @DisplayName("Should have correct MAVEN_PLUGIN_VERSION constant")
        public void shouldHaveCorrectMavenPluginVersionConstant() {
            assertThat(Constants.MAVEN_PLUGIN_VERSION).isEqualTo("verifyica.maven.plugin.version");
            assertThat(Constants.MAVEN_PLUGIN_VERSION).isEqualTo(Constants.MAVEN_PLUGIN + ".version");
        }

        @Test
        @DisplayName("Should have correct MAVEN_PLUGIN_LOG constant")
        public void shouldHaveCorrectMavenPluginLogConstant() {
            assertThat(Constants.MAVEN_PLUGIN_LOG).isEqualTo("verifyica.maven.plugin.log");
            assertThat(Constants.MAVEN_PLUGIN_LOG).isEqualTo(Constants.MAVEN_PLUGIN + ".log");
        }

        @Test
        @DisplayName("Should have correct MAVEN_PLUGIN_LOG_TESTS constant")
        public void shouldHaveCorrectMavenPluginLogTestsConstant() {
            assertThat(Constants.MAVEN_PLUGIN_LOG_TESTS).isEqualTo("verifyica.maven.plugin.log.tests");
            assertThat(Constants.MAVEN_PLUGIN_LOG_TESTS).isEqualTo(Constants.MAVEN_PLUGIN_LOG + ".tests");
        }

        @Test
        @DisplayName("Should have correct MAVEN_PLUGIN_LOG_TIMING_UNITS constant")
        public void shouldHaveCorrectMavenPluginLogTimingUnitsConstant() {
            assertThat(Constants.MAVEN_PLUGIN_LOG_TIMING_UNITS).isEqualTo("verifyica.maven.plugin.log.units");
            assertThat(Constants.MAVEN_PLUGIN_LOG_TIMING_UNITS).isEqualTo(Constants.MAVEN_PLUGIN_LOG + ".units");
        }
    }

    @Nested
    @DisplayName("Constant Prefix Pattern Tests")
    public class ConstantPrefixPatternTests {

        @Test
        @DisplayName("All engine constants should start with ENGINE prefix")
        public void allEngineConstantsShouldStartWithEnginePrefix() {
            assertThat(Constants.ENGINE_THREAD).startsWith(Constants.ENGINE);
            assertThat(Constants.ENGINE_LOGGER_REGEX).startsWith(Constants.ENGINE);
            assertThat(Constants.ENGINE_AUTOWIRED).startsWith(Constants.ENGINE);
            assertThat(Constants.ENGINE_CLASS).startsWith(Constants.ENGINE);
            assertThat(Constants.ENGINE_FILTER).startsWith(Constants.ENGINE);
            assertThat(Constants.ENGINE_ARGUMENT).startsWith(Constants.ENGINE);
            assertThat(Constants.ENGINE_TEST).startsWith(Constants.ENGINE);
        }

        @Test
        @DisplayName("All maven plugin constants should start with MAVEN_PLUGIN prefix")
        public void allMavenPluginConstantsShouldStartWithMavenPluginPrefix() {
            assertThat(Constants.MAVEN_PLUGIN_MODE).startsWith(Constants.MAVEN_PLUGIN);
            assertThat(Constants.MAVEN_PLUGIN_VERSION).startsWith(Constants.MAVEN_PLUGIN);
            assertThat(Constants.MAVEN_PLUGIN_LOG).startsWith(Constants.MAVEN_PLUGIN);
        }

        @Test
        @DisplayName("All autowired engine interceptors constants should have proper hierarchy")
        public void allAutowiredEngineInterceptorsConstantsShouldHaveProperHierarchy() {
            assertThat(Constants.ENGINE_AUTOWIRED_ENGINE_INTERCEPTORS_INCLUDE)
                    .startsWith(Constants.ENGINE_AUTOWIRED_ENGINE_INTERCEPTORS);
            assertThat(Constants.ENGINE_AUTOWIRED_ENGINE_INTERCEPTORS_EXCLUDE)
                    .startsWith(Constants.ENGINE_AUTOWIRED_ENGINE_INTERCEPTORS);
            assertThat(Constants.ENGINE_AUTOWIRED_ENGINE_INTERCEPTORS_INCLUDE_REGEX)
                    .startsWith(Constants.ENGINE_AUTOWIRED_ENGINE_INTERCEPTORS_INCLUDE);
            assertThat(Constants.ENGINE_AUTOWIRED_ENGINE_INTERCEPTORS_EXCLUDE_REGEX)
                    .startsWith(Constants.ENGINE_AUTOWIRED_ENGINE_INTERCEPTORS_EXCLUDE);
        }

        @Test
        @DisplayName("All autowired public class interceptors constants should have proper hierarchy")
        public void allAutowiredClassInterceptorsConstantsShouldHaveProperHierarchy() {
            assertThat(Constants.ENGINE_AUTOWIRED_CLASS_INTERCEPTORS_INCLUDE)
                    .startsWith(Constants.ENGINE_AUTOWIRED_CLASS_INTERCEPTORS);
            assertThat(Constants.ENGINE_AUTOWIRED_CLASS_INTERCEPTORS_EXCLUDE)
                    .startsWith(Constants.ENGINE_AUTOWIRED_CLASS_INTERCEPTORS);
            assertThat(Constants.ENGINE_AUTOWIRED_CLASS_INTERCEPTORS_INCLUDE_REGEX)
                    .startsWith(Constants.ENGINE_AUTOWIRED_CLASS_INTERCEPTORS_INCLUDE);
            assertThat(Constants.ENGINE_AUTOWIRED_CLASS_INTERCEPTORS_EXCLUDE_REGEX)
                    .startsWith(Constants.ENGINE_AUTOWIRED_CLASS_INTERCEPTORS_EXCLUDE);
        }
    }

    @Nested
    @DisplayName("Constant Immutability Tests")
    public class ConstantImmutabilityTests {

        @Test
        @DisplayName("All string constants should be non-null")
        public void allStringConstantsShouldBeNonNull() {
            assertThat(Constants.TRUE).isNotNull();
            assertThat(Constants.FALSE).isNotNull();
            assertThat(Constants.VIRTUAL).isNotNull();
            assertThat(Constants.PLATFORM).isNotNull();
            assertThat(Constants.PLATFORM_EPHEMERAL).isNotNull();
            assertThat(Constants.PREFIX).isNotNull();
            assertThat(Constants.ENGINE).isNotNull();
        }

        @Test
        @DisplayName("All constants should be final")
        public void allConstantsShouldBeFinal() throws Exception {
            assertThat(Modifier.isFinal(Constants.class.getDeclaredField("TRUE").getModifiers()))
                    .isTrue();
            assertThat(Modifier.isFinal(
                            Constants.class.getDeclaredField("FALSE").getModifiers()))
                    .isTrue();
            assertThat(Modifier.isFinal(
                            Constants.class.getDeclaredField("PREFIX").getModifiers()))
                    .isTrue();
            assertThat(Modifier.isFinal(
                            Constants.class.getDeclaredField("ENGINE").getModifiers()))
                    .isTrue();
        }

        @Test
        @DisplayName("All constants should be public")
        public void allConstantsShouldBePublic() throws Exception {
            assertThat(Modifier.isPublic(
                            Constants.class.getDeclaredField("TRUE").getModifiers()))
                    .isTrue();
            assertThat(Modifier.isPublic(
                            Constants.class.getDeclaredField("FALSE").getModifiers()))
                    .isTrue();
            assertThat(Modifier.isPublic(
                            Constants.class.getDeclaredField("PREFIX").getModifiers()))
                    .isTrue();
            assertThat(Modifier.isPublic(
                            Constants.class.getDeclaredField("ENGINE").getModifiers()))
                    .isTrue();
        }

        @Test
        @DisplayName("All constants should be static")
        public void allConstantsShouldBeStatic() throws Exception {
            assertThat(Modifier.isStatic(
                            Constants.class.getDeclaredField("TRUE").getModifiers()))
                    .isTrue();
            assertThat(Modifier.isStatic(
                            Constants.class.getDeclaredField("FALSE").getModifiers()))
                    .isTrue();
            assertThat(Modifier.isStatic(
                            Constants.class.getDeclaredField("PREFIX").getModifiers()))
                    .isTrue();
            assertThat(Modifier.isStatic(
                            Constants.class.getDeclaredField("ENGINE").getModifiers()))
                    .isTrue();
        }
    }
}
