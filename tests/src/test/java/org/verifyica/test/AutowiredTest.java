/*
 * Copyright (C) 2024-present Verifyica project authors and contributors
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.verifyica.test.support.AssertionSupport.assertArgumentContext;
import static org.verifyica.test.support.TestSupport.logArgumentContext;

import org.verifyica.api.ArgumentContext;
import org.verifyica.api.Configuration;
import org.verifyica.api.EngineContext;
import org.verifyica.api.Verifyica;

public class AutowiredTest {

    @Verifyica.Autowired
    private static Configuration CONFIGURATION;

    @Verifyica.Autowired
    private static EngineContext ENGINE_CONTEXT;

    @Verifyica.Autowired
    private EngineContext engineContext;

    @Verifyica.Autowired
    private Configuration configuration;

    @Verifyica.ArgumentSupplier
    public static Object arguments() {
        return "test";
    }

    @Verifyica.Test
    public void test(ArgumentContext argumentContext) throws Throwable {
        logArgumentContext("test()", argumentContext);

        assertArgumentContext(argumentContext);

        assertThat(CONFIGURATION).isNotNull();
        assertThat(configuration).isNotNull();
        assertThat(CONFIGURATION).isSameAs(configuration);

        assertThat(engineContext).isNotNull();
        assertThat(ENGINE_CONTEXT).isNotNull();
        assertThat(engineContext).isSameAs(ENGINE_CONTEXT);
    }

    public static class NestedAutowiredTest {

        @Verifyica.Autowired
        private EngineContext engineContext;

        @Verifyica.Autowired
        private Configuration configuration;

        @Verifyica.ArgumentSupplier
        public static Object arguments() {
            return "test";
        }

        @Verifyica.Test
        public void test(ArgumentContext argumentContext) throws Throwable {
            logArgumentContext("test()", argumentContext);

            assertArgumentContext(argumentContext);
            assertThat(engineContext).isNotNull();
            assertThat(configuration).isNotNull();
        }
    }
}
