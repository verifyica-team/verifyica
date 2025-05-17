/*
 * Copyright (C) Verifyica project authors and contributors
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

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.concurrent.TimeUnit;

/** Class to implement SystemLoadAverageThrottle */
public class SystemLoadAverageThrottle implements Throttle {

    // Target maximum system load
    private static final double TARGET_MAXIMUM_LOAD_PERCENTAGE = 0.8;

    // Base sleep time in milliseconds for exponential backoff
    private static final long BASE_SLEEP_TIME_MS = 100;

    // Maximum sleep time in milliseconds
    private static final long MAXIMUM_SLEEP_TIME_MS = 5000;

    // Maximum system load allowed
    private final double maximumLoad;

    // Operating system management bean
    private final OperatingSystemMXBean operatingSystemMXBean;

    /**
     * Constructor
     *
     * @param targetMaximumLoad the maximum load percentage
     */
    private SystemLoadAverageThrottle(double targetMaximumLoad) {
        // Validate maxLoadPercentage input
        if (targetMaximumLoad <= 0 || targetMaximumLoad > 1.0) {
            throw new IllegalArgumentException("Max load percentage must be between 0 and 1");
        }

        // Get operating system management bean
        operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();

        // Calculate maximum allowed system load
        maximumLoad = operatingSystemMXBean.getAvailableProcessors() * targetMaximumLoad;
    }

    @Override
    public void throttle() throws InterruptedException {
        // Throttle with maximum sleep time
        throttleWithMaxSleep(MAXIMUM_SLEEP_TIME_MS);
    }

    /**
     * Method to throttle execution with maximum sleep time
     *
     * @param maxSleepTime the maximum sleep time
     * @throws InterruptedException if the thread is interrupted
     */
    private void throttleWithMaxSleep(long maxSleepTime) throws InterruptedException {
        // Get current system load average
        double currentLoad = operatingSystemMXBean.getSystemLoadAverage();

        // Check if current load exceeds maximum load
        if (currentLoad > maximumLoad) {

            // Calculate exponential sleep time
            long sleepTime = calculateExponentialSleepTime(currentLoad, maximumLoad);

            // Ensure sleep time does not exceed maximum sleep time
            sleepTime = Math.min(sleepTime, maxSleepTime);

            // Pause the thread for the calculated sleep time
            TimeUnit.MILLISECONDS.sleep(sleepTime);

            // Recursive call to throttle with remaining maximum sleep time
            throttleWithMaxSleep(maxSleepTime);
        }
    }

    /**
     * Method to calculate exponential sleep time.
     *
     * @param currentLoad the current load
     * @param maxLoad the max load
     * @return the exponential sleep time
     */
    private long calculateExponentialSleepTime(double currentLoad, double maxLoad) {
        // Calculate excess load
        double excessLoad = currentLoad - maxLoad;

        // Initialize sleep time with base sleep time
        long sleepTime = BASE_SLEEP_TIME_MS;

        // If we have excess load, calculate exponential sleep time
        if (excessLoad > 0) {
            // Calculate exponential sleep time
            sleepTime = (long) (BASE_SLEEP_TIME_MS * Math.pow(2, excessLoad));

            // Ensure sleep time does not exceed maximum sleep time
            sleepTime = Math.min(sleepTime, MAXIMUM_SLEEP_TIME_MS);
        }

        // Return calculated sleep time
        return sleepTime;
    }

    /**
     * Method to get singleton instance
     *
     * @return the singleton instance
     */
    public static SystemLoadAverageThrottle getInstance() {
        return Holder.INSTANCE;
    }

    /** Class to implement Holder */
    private static class Holder {

        /**
         * The singleton instance
         */
        private static final SystemLoadAverageThrottle INSTANCE =
                new SystemLoadAverageThrottle(TARGET_MAXIMUM_LOAD_PERCENTAGE);
    }
}
