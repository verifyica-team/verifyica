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

import java.io.BufferedReader;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Class to implement SystemLoadAverageThrottle
 */
public class SystemLoadAverageThrottle implements Throttle {

    // Target maximum system load percentage
    private static final double TARGET_MAXIMUM_LOAD_PERCENTAGE = 0.8;

    // Base sleep time in milliseconds for exponential backoff
    private static final long BASE_SLEEP_TIME_MS = 100;

    // Maximum sleep time in milliseconds
    private static final long MAXIMUM_SLEEP_TIME_MS = 5_000;

    // Minimum sleep time in milliseconds
    private static final long MINIMUM_SLEEP_TIME_MS = 10;

    // Maximum cumulative sleep time to prevent indefinite throttling
    private static final long MAXIMUM_CUMULATIVE_SLEEP_TIME_MS = 60_000;

    // Path to /proc/stat for CPU usage calculation
    private static final Path PROC_STAT = Paths.get("/proc/stat");

    // Maximum system load allowed
    private final double maximumLoad;

    // Target maximum CPU percentage (0.0 - 1.0)
    private final double targetMaximumCpuPercentage;

    // Operating system management bean
    private final OperatingSystemMXBean operatingSystemMXBean;

    // Previous CPU stats for calculating instantaneous usage
    private final AtomicLong prevUser = new AtomicLong(0);
    private final AtomicLong prevSystem = new AtomicLong(0);
    private final AtomicLong prevIdle = new AtomicLong(0);
    private final AtomicLong prevIoWait = new AtomicLong(0);

    /**
     * Constructor
     *
     * @param targetMaximumLoad the maximum load percentage (0.0 - 1.0)
     * @param targetMaximumCpuPercentage the maximum CPU percentage (0.0 - 1.0)
     */
    private SystemLoadAverageThrottle(double targetMaximumLoad, double targetMaximumCpuPercentage) {
        // Validate targetMaximumLoad input
        if (targetMaximumLoad <= 0 || targetMaximumLoad > 1.0) {
            throw new IllegalArgumentException("Target maximum load must be between 0 and 1");
        }

        // Validate targetMaximumCpuPercentage input
        if (targetMaximumCpuPercentage <= 0 || targetMaximumCpuPercentage > 1.0) {
            throw new IllegalArgumentException("Target maximum CPU percentage must be between 0 and 1");
        }

        this.targetMaximumCpuPercentage = targetMaximumCpuPercentage;

        // Get operating system management bean
        operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();

        // Calculate maximum allowed system load based on available processors
        // or cgroup limits if running in a container
        int effectiveProcessors = getEffectiveProcessorCount();
        maximumLoad = effectiveProcessors * targetMaximumLoad;

        // Initialize CPU stats
        readCpuStats();
    }

    @Override
    public void throttle() throws InterruptedException {
        throttle(MAXIMUM_CUMULATIVE_SLEEP_TIME_MS);
    }

    /**
     * Method to throttle execution with maximum cumulative sleep time
     *
     * @param maxCumulativeSleepTimeMs the maximum cumulative sleep time in milliseconds
     * @throws InterruptedException if the thread is interrupted
     */
    public void throttle(long maxCumulativeSleepTimeMs) throws InterruptedException {
        long cumulativeSleepTime = 0;

        while (cumulativeSleepTime < maxCumulativeSleepTimeMs) {
            double currentLoad = operatingSystemMXBean.getSystemLoadAverage();
            double currentCpuUsage = getInstantCpuUsage();

            // Check if we need to throttle
            boolean loadExceeded = currentLoad > 0 && currentLoad > maximumLoad;
            boolean cpuExceeded = currentCpuUsage > targetMaximumCpuPercentage;

            if (!loadExceeded && !cpuExceeded) {
                // System is within limits, exit throttling
                return;
            }

            // Calculate sleep time based on which metric is more exceeded
            double excessRatio;
            if (loadExceeded && cpuExceeded) {
                excessRatio = Math.max(currentLoad / maximumLoad, currentCpuUsage / targetMaximumCpuPercentage);
            } else if (loadExceeded) {
                excessRatio = currentLoad / maximumLoad;
            } else {
                excessRatio = currentCpuUsage / targetMaximumCpuPercentage;
            }

            long sleepTime = calculateExponentialSleepTime(excessRatio);

            // Ensure we don't exceed cumulative limit
            long remainingTime = maxCumulativeSleepTimeMs - cumulativeSleepTime;
            sleepTime = Math.min(sleepTime, remainingTime);

            if (sleepTime < MINIMUM_SLEEP_TIME_MS) {
                sleepTime = MINIMUM_SLEEP_TIME_MS;
            }

            TimeUnit.MILLISECONDS.sleep(sleepTime);
            cumulativeSleepTime += sleepTime;
        }
    }

    /**
     * Method to calculate exponential sleep time.
     *
     * @param excessRatio the ratio of current to target (1.0 = at target)
     * @return the exponential sleep time
     */
    private long calculateExponentialSleepTime(double excessRatio) {
        // excessRatio > 1 means we're over the target
        if (excessRatio <= 1.0) {
            return BASE_SLEEP_TIME_MS;
        }

        // Calculate how much we're over (e.g., 1.5 = 50% over)
        double excess = excessRatio - 1.0;

        // Exponential backoff: double sleep time for each unit of excess
        long sleepTime = (long) (BASE_SLEEP_TIME_MS * Math.pow(2, excess * 5));

        // Clamp to valid range
        return Math.max(MINIMUM_SLEEP_TIME_MS, Math.min(sleepTime, MAXIMUM_SLEEP_TIME_MS));
    }

    /**
     * Gets instantaneous CPU usage from /proc/stat (Linux only)
     *
     * @return CPU usage as a percentage (0.0 - 1.0), or -1 if unavailable
     */
    @SuppressWarnings("PMD.NPathComplexity")
    private double getInstantCpuUsage() {
        if (!Files.exists(PROC_STAT)) {
            return -1; // Not Linux, can't read /proc/stat
        }

        try {
            List<String> lines = readAllLines(PROC_STAT);
            String cpuLine = null;
            for (String line : lines) {
                if (line.startsWith("cpu ")) {
                    cpuLine = line;
                    break;
                }
            }

            if (cpuLine == null) {
                return -1;
            }

            String[] parts = cpuLine.trim().split("\\s+");
            if (parts.length < 5) {
                return -1;
            }

            long user = Long.parseLong(parts[1]);
            long nice = Long.parseLong(parts[2]);
            long system = Long.parseLong(parts[3]);
            long idle = Long.parseLong(parts[4]);
            long iowait = parts.length > 5 ? Long.parseLong(parts[5]) : 0;

            long prevUserVal = prevUser.get();
            long prevSystemVal = prevSystem.get();
            long prevIdleVal = prevIdle.get();
            long prevIoWaitVal = prevIoWait.get();

            // Store current values for next call
            prevUser.set(user);
            prevSystem.set(system);
            prevIdle.set(idle);
            prevIoWait.set(iowait);

            // First call - no delta available
            if (prevUserVal == 0 && prevSystemVal == 0) {
                return -1;
            }

            long userDelta = user - prevUserVal;
            long systemDelta = system - prevSystemVal;
            long idleDelta = idle - prevIdleVal;
            long iowaitDelta = iowait - prevIoWaitVal;

            long totalDelta = userDelta + nice + systemDelta + idleDelta + iowaitDelta;
            long usedDelta = userDelta + systemDelta;

            if (totalDelta == 0) {
                return 0.0;
            }

            return (double) usedDelta / totalDelta;
        } catch (IOException | NumberFormatException e) {
            return -1;
        }
    }

    /**
     * Reads CPU stats to initialize previous values
     */
    @SuppressWarnings("PMD.EmptyCatchBlock")
    private void readCpuStats() {
        if (!Files.exists(PROC_STAT)) {
            return;
        }

        try {
            List<String> lines = Files.readAllLines(PROC_STAT);
            for (String line : lines) {
                if (line.startsWith("cpu ")) {
                    String[] parts = line.trim().split("\\s+");
                    if (parts.length >= 5) {
                        prevUser.set(Long.parseLong(parts[1]));
                        prevSystem.set(Long.parseLong(parts[3]));
                        prevIdle.set(Long.parseLong(parts[4]));
                        prevIoWait.set(parts.length > 5 ? Long.parseLong(parts[5]) : 0);
                    }
                    break;
                }
            }
        } catch (IOException | NumberFormatException e) {
            // Ignore, will fall back to load average
        }
    }

    /**
     * Gets effective processor count considering cgroups limits
     *
     * @return effective number of processors
     */
    private int getEffectiveProcessorCount() {
        // Try to detect cgroup CPU limits (container environments)
        Double cgroupLimit = readCgroupCpuLimit();
        if (cgroupLimit != null) {
            return Math.max(1, cgroupLimit.intValue());
        }

        return operatingSystemMXBean.getAvailableProcessors();
    }

    /**
     * Reads CPU limit from cgroups (v1 or v2)
     *
     * @return CPU limit as a double, or null if not in a container/no limit
     */
    @SuppressWarnings("PMD.EmptyCatchBlock")
    private Double readCgroupCpuLimit() {
        // Try cgroups v2 first
        Path cpuMax = Paths.get("/sys/fs/cgroup/cpu.max");
        if (Files.exists(cpuMax)) {
            try {
                String content = readFileToString(cpuMax).trim();
                String[] parts = content.split(" ");
                if (parts.length == 2) {
                    long quota = Long.parseLong(parts[0]);
                    long period = Long.parseLong(parts[1]);
                    if (quota > 0 && period > 0) {
                        return (double) quota / period;
                    }
                }
            } catch (IOException | NumberFormatException e) {
                // Fall through to cgroups v1 - this is expected fallback behavior
            }
        }

        // Try cgroups v1
        Path quotaPath = Paths.get("/sys/fs/cgroup/cpu/cpu.cfs_quota_us");
        Path periodPath = Paths.get("/sys/fs/cgroup/cpu/cpu.cfs_period_us");

        if (Files.exists(quotaPath)) {
            try {
                long quota = Long.parseLong(readFileToString(quotaPath).trim());
                if (quota > 0) {
                    long period = Long.parseLong(readFileToString(periodPath).trim());
                    if (period > 0) {
                        return (double) quota / period;
                    }
                }
            } catch (IOException | NumberFormatException e) {
                // Ignore - cgroup limit not readable, use available processors
            }
        }

        return null;
    }

    /**
     * Java 8 compatible method to read all lines from a file
     *
     * @param path the file path
     * @return list of lines
     * @throws IOException if an I/O error occurs
     */
    private static List<String> readAllLines(Path path) throws IOException {
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        }
        return lines;
    }

    /**
     * Java 8 compatible method to read a file to string
     *
     * @param path the file path
     * @return file content as string
     * @throws IOException if an I/O error occurs
     */
    private static String readFileToString(Path path) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        return sb.toString();
    }

    /**
     * Method to get singleton instance
     *
     * @return the singleton instance
     */
    public static SystemLoadAverageThrottle getInstance() {
        return Holder.INSTANCE;
    }

    /**
     * Class to implement Holder
     */
    private static class Holder {

        /**
         * The singleton instance
         */
        private static final SystemLoadAverageThrottle INSTANCE =
                new SystemLoadAverageThrottle(TARGET_MAXIMUM_LOAD_PERCENTAGE, TARGET_MAXIMUM_LOAD_PERCENTAGE);
    }
}
