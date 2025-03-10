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

package org.verifyica.engine.common.throttle;

/** Class to implement NoopThrottle */
@SuppressWarnings("PMD.EmptyCatchBlock")
public class NoopThrottle implements Throttle {

    /**
     * Constructor
     */
    private NoopThrottle() {
        // INTENTIONALLY BLANK
    }

    @Override
    public void throttle() {
        // INTENTIONALLY BLANK
    }

    /**
     * Method to get the singleton instance
     *
     * @return the singleton instance
     */
    public static NoopThrottle getInstance() {
        return Holder.INSTANCE;
    }

    /** Class to implement Holder */
    private static class Holder {

        /**
         * The singleton instance
         */
        private static final NoopThrottle INSTANCE = new NoopThrottle();
    }
}
