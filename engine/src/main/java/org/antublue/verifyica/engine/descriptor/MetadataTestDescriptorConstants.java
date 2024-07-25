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

/** Class to implement MetadataConstants */
public class MetadataTestDescriptorConstants {

    /** Constant */
    public static final String PASS = "PASS";

    /** Constant */
    public static final String FAIL = "FAIL";

    /** Constant */
    public static final String SKIP = "SKIP";

    /** Constant */
    public static final String TEST_CLASS = "testClass";

    /** Constant */
    public static final String TEST_CLASS_DISPLAY_NAME = "testClass.displayName";

    /** Constant */
    public static final String TEST_ARGUMENT = "testArgument";

    /** Constant */
    public static final String TEST_METHOD = "testMethod";

    /** Constant */
    public static final String TEST_METHOD_DISPLAY_NAME = "testMethod.displayName";

    /** Constant */
    public static final String TEST_DESCRIPTOR_DURATION = "testDescriptorDuration";

    /** Constructor */
    private MetadataTestDescriptorConstants() {
        // INTENTIONALLY BLANK
    }
}
