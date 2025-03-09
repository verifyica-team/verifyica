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

public class SystemLoadThrottle implements Throttle {

    private final OperatingSystemMXBean operatingSystemMXBean;
    private final double maximumLoad;
    private final long sleepMilliseconds;

    public SystemLoadThrottle(long sleepMilliseconds, double maximumLoad) {
        this.operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
        this.maximumLoad = maximumLoad * operatingSystemMXBean.getAvailableProcessors();
        this.sleepMilliseconds = sleepMilliseconds;
    }

    @Override
    public void throttle() {
        double load = operatingSystemMXBean.getSystemLoadAverage();
        // If we can't get the load, or it's greater than the maximum load, sleep
        if (load < 0 || load > maximumLoad) {
            try {
                Thread.sleep(sleepMilliseconds);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
