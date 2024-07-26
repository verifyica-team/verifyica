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

    private boolean hasFailures;

    /**
     * Constructor
     *
     * @param uniqueId uniqueId
     * @param displayName displayName
     */
    public StatusEngineDescriptor(UniqueId uniqueId, String displayName) {
        super(uniqueId, displayName);
    }

    /** Method to mark the test descriptor as having failures */
    public void setHasFailures() {
        this.hasFailures = true;
    }

    /**
     * Method to return if the test descriptor has failures
     *
     * @return true if the test descriptor has failures, else false
     */
    public boolean hasFailures() {
        return hasFailures;
    }
}
