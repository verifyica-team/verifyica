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

import java.util.function.Function;
import org.junit.platform.engine.TestDescriptor;

/** Class to implement ToExecutableTestDescriptor */
public class ToExecutableTestDescriptor
        implements Function<TestDescriptor, ExecutableTestDescriptor> {

    /** Singleton instance */
    public static final ToExecutableTestDescriptor INSTANCE = new ToExecutableTestDescriptor();

    /** Constructor */
    public ToExecutableTestDescriptor() {
        // INTENTIONALLY BLANK
    }

    @Override
    public ExecutableTestDescriptor apply(TestDescriptor testDescriptor) {
        return testDescriptor instanceof ExecutableTestDescriptor
                ? (ExecutableTestDescriptor) testDescriptor
                : null;
    }
}
