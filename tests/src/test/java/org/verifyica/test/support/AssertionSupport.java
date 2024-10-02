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

package org.verifyica.test.support;

import static org.assertj.core.api.Assertions.assertThat;

import org.verifyica.api.ArgumentContext;
import org.verifyica.api.ClassContext;
import org.verifyica.api.EngineContext;

public class AssertionSupport {

    private AssertionSupport() {
        // INTENTIONALLY BLANK
    }

    public static void assertEngineContext(EngineContext engineContext) {
        assertThat(engineContext).isNotNull();
        assertThat(engineContext.getConfiguration()).isNotNull();
        assertThat(engineContext.getVersion()).isNotBlank();
        assertThat(engineContext.getMap()).isNotNull();
    }

    public static void assertClassContext(ClassContext classContext) {
        assertThat(classContext).isNotNull();
        assertThat(classContext.getConfiguration()).isNotNull();
        assertThat(classContext.getMap()).isNotNull();
        assertThat(classContext.getTestClass()).isNotNull();
        assertThat(classContext.getTestInstance()).isNotNull();
        assertThat(classContext.getTestArgumentParallelism()).isGreaterThanOrEqualTo(1);
        assertThat(classContext.getTestClassDisplayName()).isNotBlank();

        assertEngineContext(classContext.getEngineContext());
    }

    public static void assertArgumentContext(ArgumentContext argumentContext) {
        assertThat(argumentContext).isNotNull();
        assertThat(argumentContext.getConfiguration()).isNotNull();
        assertThat(argumentContext.getMap()).isNotNull();
        assertThat(argumentContext.getTestArgument()).isNotNull();
        assertThat(argumentContext.getTestArgument(Object.class)).isNotNull();

        assertClassContext(argumentContext.getClassContext());
    }
}
