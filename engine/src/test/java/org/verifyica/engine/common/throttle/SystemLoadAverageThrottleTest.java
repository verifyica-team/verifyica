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

package org.verifyica.engine.common.throttle;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("SystemLoadAverageThrottle Tests")
public class SystemLoadAverageThrottleTest {

    @Test
    @DisplayName("Should return singleton instance")
    public void shouldReturnSingletonInstance() {
        final SystemLoadAverageThrottle instance1 = SystemLoadAverageThrottle.getInstance();
        final SystemLoadAverageThrottle instance2 = SystemLoadAverageThrottle.getInstance();

        assertThat(instance1).isNotNull();
        assertThat(instance2).isNotNull();
        assertThat(instance1).isSameAs(instance2);
    }

    @Test
    @DisplayName("Should implement Throttle interface")
    public void shouldImplementThrottleInterface() {
        final SystemLoadAverageThrottle instance = SystemLoadAverageThrottle.getInstance();

        assertThat(instance).isInstanceOf(Throttle.class);
    }

    @Test
    @DisplayName("Should complete throttle without exception")
    @Timeout(value = 70)
    public void shouldCompleteThrottleWithoutException() {
        final SystemLoadAverageThrottle instance = SystemLoadAverageThrottle.getInstance();

        assertThatCode(instance::throttle).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should complete throttle with max cumulative time")
    public void shouldCompleteThrottleWithMaxCumulativeTime() {
        final SystemLoadAverageThrottle instance = SystemLoadAverageThrottle.getInstance();

        assertThatCode(() -> instance.throttle(100)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should handle zero max cumulative time")
    public void shouldHandleZeroMaxCumulativeTime() {
        final SystemLoadAverageThrottle instance = SystemLoadAverageThrottle.getInstance();

        assertThatCode(() -> instance.throttle(0)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should handle multiple throttle calls")
    public void shouldHandleMultipleThrottleCalls() {
        final SystemLoadAverageThrottle instance = SystemLoadAverageThrottle.getInstance();

        assertThatCode(() -> {
                    for (int i = 0; i < 3; i++) {
                        instance.throttle(50);
                    }
                })
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should throw InterruptedException when thread is interrupted")
    public void shouldThrowInterruptedExceptionWhenThreadIsInterrupted() throws InterruptedException {
        final SystemLoadAverageThrottle instance = SystemLoadAverageThrottle.getInstance();

        final Thread testThread = new Thread(() -> {
            try {
                instance.throttle(5000);
            } catch (InterruptedException e) {
                // Expected
                Thread.currentThread().interrupt();
            }
        });

        testThread.start();
        testThread.interrupt();
        testThread.join(1000);

        assertThat(testThread.isAlive()).isFalse();
    }

    @Test
    @DisplayName("Should reject negative target maximum load")
    public void shouldRejectNegativeTargetMaximumLoad() throws Exception {
        final Constructor<SystemLoadAverageThrottle> constructor =
                SystemLoadAverageThrottle.class.getDeclaredConstructor(double.class, double.class);
        constructor.setAccessible(true);

        assertThatThrownBy(() -> constructor.newInstance(-0.1, 0.5))
                .isInstanceOf(InvocationTargetException.class)
                .hasCauseInstanceOf(IllegalArgumentException.class)
                .satisfies(ex -> {
                    final Throwable cause = ex.getCause();
                    assertThat(cause.getMessage()).containsIgnoringCase("target maximum load");
                });
    }

    @Test
    @DisplayName("Should reject zero target maximum load")
    public void shouldRejectZeroTargetMaximumLoad() throws Exception {
        final Constructor<SystemLoadAverageThrottle> constructor =
                SystemLoadAverageThrottle.class.getDeclaredConstructor(double.class, double.class);
        constructor.setAccessible(true);

        assertThatThrownBy(() -> constructor.newInstance(0.0, 0.5))
                .isInstanceOf(InvocationTargetException.class)
                .hasCauseInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Should reject target maximum load greater than 1.0")
    public void shouldRejectTargetMaximumLoadGreaterThanOne() throws Exception {
        final Constructor<SystemLoadAverageThrottle> constructor =
                SystemLoadAverageThrottle.class.getDeclaredConstructor(double.class, double.class);
        constructor.setAccessible(true);

        assertThatThrownBy(() -> constructor.newInstance(1.1, 0.5))
                .isInstanceOf(InvocationTargetException.class)
                .hasCauseInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Should reject negative target maximum CPU percentage")
    public void shouldRejectNegativeTargetMaximumCpuPercentage() throws Exception {
        final Constructor<SystemLoadAverageThrottle> constructor =
                SystemLoadAverageThrottle.class.getDeclaredConstructor(double.class, double.class);
        constructor.setAccessible(true);

        assertThatThrownBy(() -> constructor.newInstance(0.5, -0.1))
                .isInstanceOf(InvocationTargetException.class)
                .hasCauseInstanceOf(IllegalArgumentException.class)
                .satisfies(ex -> {
                    final Throwable cause = ex.getCause();
                    assertThat(cause.getMessage()).containsIgnoringCase("target maximum CPU percentage");
                });
    }

    @Test
    @DisplayName("Should reject zero target maximum CPU percentage")
    public void shouldRejectZeroTargetMaximumCpuPercentage() throws Exception {
        final Constructor<SystemLoadAverageThrottle> constructor =
                SystemLoadAverageThrottle.class.getDeclaredConstructor(double.class, double.class);
        constructor.setAccessible(true);

        assertThatThrownBy(() -> constructor.newInstance(0.5, 0.0))
                .isInstanceOf(InvocationTargetException.class)
                .hasCauseInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Should reject target maximum CPU percentage greater than 1.0")
    public void shouldRejectTargetMaximumCpuPercentageGreaterThanOne() throws Exception {
        final Constructor<SystemLoadAverageThrottle> constructor =
                SystemLoadAverageThrottle.class.getDeclaredConstructor(double.class, double.class);
        constructor.setAccessible(true);

        assertThatThrownBy(() -> constructor.newInstance(0.5, 1.1))
                .isInstanceOf(InvocationTargetException.class)
                .hasCauseInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @CsvSource({"0.1, 0.1", "0.5, 0.5", "0.8, 0.8", "1.0, 1.0", "0.1, 0.9", "0.9, 0.1"})
    @DisplayName("Should accept valid constructor parameters")
    public void shouldAcceptValidConstructorParameters(
            final double targetMaximumLoad, final double targetMaximumCpuPercentage) throws Exception {
        final Constructor<SystemLoadAverageThrottle> constructor =
                SystemLoadAverageThrottle.class.getDeclaredConstructor(double.class, double.class);
        constructor.setAccessible(true);

        assertThatCode(() -> constructor.newInstance(targetMaximumLoad, targetMaximumCpuPercentage))
                .doesNotThrowAnyException();
    }

    @ParameterizedTest
    @CsvSource({"0.5, 100", "1.0, 100", "1.5, 100", "2.0, 100", "3.0, 100"})
    @DisplayName("Should calculate exponential sleep time correctly")
    public void shouldCalculateExponentialSleepTimeCorrectly(final double excessRatio, final long expectedMinSleepTime)
            throws Exception {
        final Method method =
                SystemLoadAverageThrottle.class.getDeclaredMethod("calculateExponentialSleepTime", double.class);
        method.setAccessible(true);

        final SystemLoadAverageThrottle instance = SystemLoadAverageThrottle.getInstance();
        final long sleepTime = (Long) method.invoke(instance, excessRatio);

        if (excessRatio <= 1.0) {
            assertThat(sleepTime).isEqualTo(100L);
        } else {
            assertThat(sleepTime).isGreaterThanOrEqualTo(expectedMinSleepTime);
            assertThat(sleepTime).isLessThanOrEqualTo(5000L);
        }
    }

    @Test
    @DisplayName("Should return base sleep time when excess ratio is 1.0 or less")
    public void shouldReturnBaseSleepTimeWhenExcessRatioIsOneOrLess() throws Exception {
        final Method method =
                SystemLoadAverageThrottle.class.getDeclaredMethod("calculateExponentialSleepTime", double.class);
        method.setAccessible(true);

        final SystemLoadAverageThrottle instance = SystemLoadAverageThrottle.getInstance();

        assertThat((Long) method.invoke(instance, 0.0)).isEqualTo(100L);
        assertThat((Long) method.invoke(instance, 0.5)).isEqualTo(100L);
        assertThat((Long) method.invoke(instance, 1.0)).isEqualTo(100L);
    }

    @Test
    @DisplayName("Should cap sleep time at maximum")
    public void shouldCapSleepTimeAtMaximum() throws Exception {
        final Method method =
                SystemLoadAverageThrottle.class.getDeclaredMethod("calculateExponentialSleepTime", double.class);
        method.setAccessible(true);

        final SystemLoadAverageThrottle instance = SystemLoadAverageThrottle.getInstance();

        // Very high excess ratio should still return at most MAXIMUM_SLEEP_TIME_MS
        final long sleepTime = (Long) method.invoke(instance, 100.0);
        assertThat(sleepTime).isEqualTo(5000L);
    }

    @Test
    @DisplayName("Should floor sleep time at minimum")
    public void shouldFloorSleepTimeAtMinimum() throws Exception {
        final Method method =
                SystemLoadAverageThrottle.class.getDeclaredMethod("calculateExponentialSleepTime", double.class);
        method.setAccessible(true);

        final SystemLoadAverageThrottle instance = SystemLoadAverageThrottle.getInstance();

        // Just over 1.0 should return at least MINIMUM_SLEEP_TIME_MS
        final long sleepTime = (Long) method.invoke(instance, 1.01);
        assertThat(sleepTime).isGreaterThanOrEqualTo(10L);
    }

    @Test
    @DisplayName("Should read all lines from file correctly")
    public void shouldReadAllLinesFromFileCorrectly(@TempDir final Path tempDir) throws Exception {
        final Method method = SystemLoadAverageThrottle.class.getDeclaredMethod("readAllLines", Path.class);
        method.setAccessible(true);

        final Path testFile = tempDir.resolve("test.txt");
        final List<String> expectedLines = Arrays.asList("line1", "line2", "line3");
        Files.write(testFile, expectedLines);

        @SuppressWarnings("unchecked")
        final List<String> actualLines = (List<String>) method.invoke(null, testFile);

        assertThat(actualLines).isEqualTo(expectedLines);
    }

    @Test
    @DisplayName("Should read empty file correctly")
    public void shouldReadEmptyFileCorrectly(@TempDir final Path tempDir) throws Exception {
        final Method method = SystemLoadAverageThrottle.class.getDeclaredMethod("readAllLines", Path.class);
        method.setAccessible(true);

        final Path testFile = tempDir.resolve("empty.txt");
        Files.createFile(testFile);

        @SuppressWarnings("unchecked")
        final List<String> actualLines = (List<String>) method.invoke(null, testFile);

        assertThat(actualLines).isEmpty();
    }

    @Test
    @DisplayName("Should read file to string correctly")
    public void shouldReadFileToStringCorrectly(@TempDir final Path tempDir) throws Exception {
        final Method method = SystemLoadAverageThrottle.class.getDeclaredMethod("readFileToString", Path.class);
        method.setAccessible(true);

        final Path testFile = tempDir.resolve("test.txt");
        final String content = "Hello World";
        Files.write(testFile, Collections.singletonList(content));

        final String actualContent = (String) method.invoke(null, testFile);

        assertThat(actualContent).isEqualTo(content);
    }

    @Test
    @DisplayName("Should read multi-line file to single string")
    public void shouldReadMultiLineFileToSingleString(@TempDir final Path tempDir) throws Exception {
        final Method method = SystemLoadAverageThrottle.class.getDeclaredMethod("readFileToString", Path.class);
        method.setAccessible(true);

        final Path testFile = tempDir.resolve("test.txt");
        final List<String> lines = Arrays.asList("line1", "line2", "line3");
        Files.write(testFile, lines);

        final String actualContent = (String) method.invoke(null, testFile);

        assertThat(actualContent).isEqualTo("line1line2line3");
    }

    @Test
    @DisplayName("Should throw IOException for non-existent file when reading lines")
    public void shouldThrowIOExceptionForNonExistentFileWhenReadingLines() throws Exception {
        final Method method = SystemLoadAverageThrottle.class.getDeclaredMethod("readAllLines", Path.class);
        method.setAccessible(true);

        final Path nonExistentFile = Paths.get("/non/existent/file.txt");

        assertThatThrownBy(() -> method.invoke(null, nonExistentFile))
                .isInstanceOf(InvocationTargetException.class)
                .hasCauseInstanceOf(IOException.class);
    }

    @Test
    @DisplayName("Should throw IOException for non-existent file when reading string")
    public void shouldThrowIOExceptionForNonExistentFileWhenReadingString() throws Exception {
        final Method method = SystemLoadAverageThrottle.class.getDeclaredMethod("readFileToString", Path.class);
        method.setAccessible(true);

        final Path nonExistentFile = Paths.get("/non/existent/file.txt");

        assertThatThrownBy(() -> method.invoke(null, nonExistentFile))
                .isInstanceOf(InvocationTargetException.class)
                .hasCauseInstanceOf(IOException.class);
    }

    @Test
    @DisplayName("Should return valid CPU usage value")
    public void shouldReturnValidCpuUsageValue() throws Exception {
        final Method method = SystemLoadAverageThrottle.class.getDeclaredMethod("getInstantCpuUsage");
        method.setAccessible(true);

        final SystemLoadAverageThrottle instance = SystemLoadAverageThrottle.getInstance();
        final double cpuUsage = (Double) method.invoke(instance);

        // On Linux systems with /proc/stat, should return a value between 0 and 1
        // On non-Linux systems, should return -1
        assertThat(cpuUsage).satisfiesAnyOf(value -> assertThat(value).isBetween(0.0, 1.0), value -> assertThat(value)
                .isEqualTo(-1.0));
    }

    @Test
    @DisplayName("Should return null cgroup limit when cgroup files do not exist")
    public void shouldReturnNullCgroupLimitWhenCgroupFilesDoNotExist() throws Exception {
        final Method method = SystemLoadAverageThrottle.class.getDeclaredMethod("readCgroupCpuLimit");
        method.setAccessible(true);

        final SystemLoadAverageThrottle instance = SystemLoadAverageThrottle.getInstance();
        final Double cgroupLimit = (Double) method.invoke(instance);

        // When not in a container or cgroup files don't exist, should return null
        assertThat(cgroupLimit).isNull();
    }

    @Test
    @DisplayName("Should get effective processor count")
    public void shouldGetEffectiveProcessorCount() throws Exception {
        final Method method = SystemLoadAverageThrottle.class.getDeclaredMethod("getEffectiveProcessorCount");
        method.setAccessible(true);

        final SystemLoadAverageThrottle instance = SystemLoadAverageThrottle.getInstance();
        final int processorCount = (Integer) method.invoke(instance);

        assertThat(processorCount).isGreaterThanOrEqualTo(1);
    }

    @ParameterizedTest
    @ValueSource(longs = {1, 10, 100, 1000})
    @DisplayName("Should complete throttle within reasonable time for various max times")
    @Timeout(value = 10)
    public void shouldCompleteThrottleWithinReasonableTime(final long maxTimeMs) {
        final SystemLoadAverageThrottle instance = SystemLoadAverageThrottle.getInstance();

        assertThatCode(() -> instance.throttle(maxTimeMs)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should handle concurrent throttle calls")
    public void shouldHandleConcurrentThrottleCalls() throws InterruptedException {
        final SystemLoadAverageThrottle instance = SystemLoadAverageThrottle.getInstance();
        final int threadCount = 3;
        final Thread[] threads = new Thread[threadCount];

        for (int i = 0; i < threadCount; i++) {
            threads[i] = new Thread(() -> {
                try {
                    instance.throttle(50);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            threads[i].start();
        }

        for (final Thread thread : threads) {
            thread.join(2000);
            assertThat(thread.isAlive()).isFalse();
        }
    }
}
