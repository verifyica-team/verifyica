/*
 * Copyright (C) 2025-present Verifyica project authors and contributors
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

package org.verifyica.engine.common;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/** Class to implement SystemLoadAverageThrottle */
public class SystemLoadAverageThrottle implements Throttle {

    private static final long MONITOR_INTERVAL_MILLISECONDS = 500;
    private static final long THROTTLE_MILLISECONDS = 500;

    private static final OperatingSystemMXBean OPERATING_SYSTEM_MX_BEAN = ManagementFactory.getOperatingSystemMXBean();
    private static final AtomicReference<Double> SYSTEM_LOAD_AVERAGE = new AtomicReference<>(0.0);

    private static final double TARGET_SYSTEM_LOAD_AVERAGE = 0.75 * OPERATING_SYSTEM_MX_BEAN.getAvailableProcessors();

    static {
        // Create a thread to monitor the system load average
        Thread systemLoadAverageThread = new Thread(() -> {
            while (true) {
                // Get the system load
                double systemLoadAverage = OPERATING_SYSTEM_MX_BEAN.getSystemLoadAverage();

                // Set the system load
                SYSTEM_LOAD_AVERAGE.set(systemLoadAverage);

                try {
                    // Sleep for the monitor interval
                    TimeUnit.MILLISECONDS.sleep(MONITOR_INTERVAL_MILLISECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });

        // Set the name of the thread
        systemLoadAverageThread.setName("verifyica-system-load-average");

        // Set the thread as a daemon
        systemLoadAverageThread.setDaemon(true);

        // Start the thread
        systemLoadAverageThread.start();
    }

    /**
     * Constructor
     */
    private SystemLoadAverageThrottle() {
        // Private constructor to prevent instantiation
    }

    @Override
    public void throttle() {
        // Get the system load
        double systemLoadAverage = SYSTEM_LOAD_AVERAGE.get();

        // If we can't get the load, or it's greater than the maximum load, sleep
        if (systemLoadAverage < 0 || systemLoadAverage > TARGET_SYSTEM_LOAD_AVERAGE) {
            try {
                Thread.sleep(THROTTLE_MILLISECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Method to get the singleton instance
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

        // The singleton instance
        private static final SystemLoadAverageThrottle INSTANCE = new SystemLoadAverageThrottle();
    }
}
