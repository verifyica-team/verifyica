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

package org.verifyica.test;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.verifyica.test.support.AssertionSupport.assertArgumentContext;
import static org.verifyica.test.support.TestSupport.logArgumentContext;

import org.verifyica.api.ArgumentContext;
import org.verifyica.api.Verifyica;

public class ConfigurationPropertiesMutationTest {

    @Verifyica.ArgumentSupplier
    public static Object arguments() {
        return "test";
    }

    @Verifyica.Test
    public void test(ArgumentContext argumentContext) throws Throwable {
        logArgumentContext("test()", argumentContext);

        assertArgumentContext(argumentContext);

        assertThatExceptionOfType(UnsupportedOperationException.class)
                .isThrownBy(
                        () -> argumentContext.getConfiguration().getProperties().clear());

        assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(() -> argumentContext
                .getClassContext()
                .getConfiguration()
                .getProperties()
                .clear());

        assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(() -> argumentContext
                .getClassContext()
                .getEngineContext()
                .getConfiguration()
                .getProperties()
                .clear());
    }

    public static class NestedConfigurationPropertiesMutationTest {

        @Verifyica.ArgumentSupplier
        public static Object arguments() {
            return "test";
        }

        @Verifyica.Test
        public void test(ArgumentContext argumentContext) throws Throwable {
            logArgumentContext("test()", argumentContext);

            assertArgumentContext(argumentContext);

            assertThatExceptionOfType(UnsupportedOperationException.class)
                    .isThrownBy(() ->
                            argumentContext.getConfiguration().getProperties().clear());

            assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(() -> argumentContext
                    .getClassContext()
                    .getConfiguration()
                    .getProperties()
                    .clear());

            assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(() -> argumentContext
                    .getClassContext()
                    .getEngineContext()
                    .getConfiguration()
                    .getProperties()
                    .clear());
        }
    }
}
