/*
 * Copyright (C) 2024 The Verifyica project authors
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

package org.antublue.verifyica.engine.descriptor;

import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.EngineDescriptor;

/** Class to implement StatusEngineDescriptor */
public class StatusEngineDescriptor extends EngineDescriptor {

    private long testCount;
    private long failureCount;

    /**
     * Constructor
     *
     * @param uniqueId uniqueId
     * @param displayName displayName
     */
    public StatusEngineDescriptor(UniqueId uniqueId, String displayName) {
        super(uniqueId, displayName);
    }

    /**
     * Method to set the test descriptor count
     *
     * @param testCount testCount
     */
    public void setTestCount(long testCount) {
        this.testCount = testCount;
    }

    /**
     * Method to get the test descriptor count
     *
     * @return the test descriptor count
     */
    public long getTestCount() {
        return testCount;
    }

    /**
     * Method to set the failure count
     *
     * @param failureCount failureCount
     */
    public void setFailureCount(long failureCount) {
        this.failureCount = failureCount;
    }

    /**
     * Method to return the test descriptor failure count
     *
     * @return the test descriptor failure count
     */
    public long getFailureCount() {
        return failureCount;
    }
}
