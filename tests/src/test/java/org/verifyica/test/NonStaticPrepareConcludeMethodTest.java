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

package org.verifyica.test;

import static org.assertj.core.api.Assertions.assertThat;

import org.verifyica.api.ArgumentContext;
import org.verifyica.api.ClassContext;
import org.verifyica.api.Verifyica;

public class NonStaticPrepareConcludeMethodTest {

    @Verifyica.ArgumentSupplier
    public static Object arguments() {
        return "test";
    }

    @Verifyica.Prepare
    public void prepare(ClassContext classContext) {
        System.out.println("prepare()");

        assertThat(classContext).isNotNull();
        assertThat(classContext.getMap()).isNotNull();
        assertThat(classContext.getConfiguration())
                .isSameAs(classContext.getEngineContext().getConfiguration());
        assertThat(classContext.getConfiguration().getPropertiesFilename())
                .isEqualTo(classContext.getEngineContext().getConfiguration().getPropertiesFilename());
        assertThat(classContext.getTestInstance()).isNotNull();
    }

    @Verifyica.Test
    public void test(ArgumentContext argumentContext) throws Throwable {
        System.out.printf(
                "test(name[%s], payload[%s])%n",
                argumentContext.getTestArgument(),
                argumentContext.getTestArgument().getPayload());

        assertThat(argumentContext).isNotNull();
        assertThat(argumentContext.getMap()).isNotNull();
        assertThat(argumentContext.getTestArgument()).isNotNull();
        assertThat(argumentContext.getTestArgument().getPayload()).isEqualTo("test");
    }

    @Verifyica.Conclude
    public void conclude(ClassContext classContext) {
        System.out.println("conclude()");

        assertThat(classContext).isNotNull();
        assertThat(classContext.getMap()).isNotNull();
        assertThat(classContext.getConfiguration())
                .isSameAs(classContext.getEngineContext().getConfiguration());
        assertThat(classContext.getConfiguration().getPropertiesFilename())
                .isEqualTo(classContext.getEngineContext().getConfiguration().getPropertiesFilename());
        assertThat(classContext.getTestInstance()).isNotNull();
    }
}
