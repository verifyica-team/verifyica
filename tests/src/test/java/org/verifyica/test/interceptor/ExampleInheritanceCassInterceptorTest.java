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

package org.verifyica.test.interceptor;

import static org.assertj.core.api.Assertions.assertThat;

import org.verifyica.api.ArgumentContext;
import org.verifyica.api.Verifyica;

public class ExampleInheritanceCassInterceptorTest implements ExampleInheritanceClassInterceptor {

    @Verifyica.ArgumentSupplier
    public static String arguments() {
        return "ignored";
    }

    @Verifyica.Test
    @Verifyica.Order(0)
    public void test1(ArgumentContext argumentContext) throws Throwable {
        System.out.printf("test1(%s)%n", argumentContext.getTestArgument().getPayload());

        assertThat(argumentContext).isNotNull();
        assertThat(argumentContext.getMap()).isNotNull();
        assertThat(argumentContext.getTestArgument()).isNotNull();
        assertThat(argumentContext.getClassContext().getEngineContext()
                        == argumentContext.getClassContext().getEngineContext())
                .isTrue();

        // Validate that the interceptor added a global String to the EngineContext Store
        assertThat(argumentContext
                        .getClassContext()
                        .getEngineContext()
                        .getMap()
                        .get(ExampleAutowiredEngineInterceptor1.KEY))
                .isNotNull();

        assertThat(argumentContext
                        .getClassContext()
                        .getEngineContext()
                        .getMap()
                        .get(ExampleAutowiredEngineInterceptor1.KEY))
                .isEqualTo(ExampleAutowiredEngineInterceptor1.VALUE);
    }

    @Verifyica.Test
    @Verifyica.Order(1)
    public void test2(ArgumentContext argumentContext) throws Throwable {
        System.out.printf("test2(%s)%n", argumentContext.getTestArgument().getPayload());

        assertThat(argumentContext).isNotNull();
        assertThat(argumentContext.getMap()).isNotNull();
        assertThat(argumentContext.getTestArgument()).isNotNull();
        assertThat(argumentContext.getClassContext().getEngineContext()
                        == argumentContext.getClassContext().getEngineContext())
                .isTrue();

        // Validate that the interceptor added a global String to the EngineContext Store
        assertThat(argumentContext
                        .getClassContext()
                        .getEngineContext()
                        .getMap()
                        .get(ExampleAutowiredEngineInterceptor1.KEY))
                .isNotNull();

        assertThat(argumentContext
                        .getClassContext()
                        .getEngineContext()
                        .getMap()
                        .get(ExampleAutowiredEngineInterceptor1.KEY))
                .isEqualTo(ExampleAutowiredEngineInterceptor1.VALUE);
    }

    @Verifyica.Test
    @Verifyica.Order(2)
    public void test3(ArgumentContext argumentContext) throws Throwable {
        System.out.printf("test3(%s)%n", argumentContext.getTestArgument().getPayload());

        assertThat(argumentContext).isNotNull();
        assertThat(argumentContext.getMap()).isNotNull();
        assertThat(argumentContext.getTestArgument()).isNotNull();
        assertThat(argumentContext.getClassContext().getEngineContext()
                        == argumentContext.getClassContext().getEngineContext())
                .isTrue();

        // Validate that the interceptor added a global String to the EngineContext Store
        assertThat(argumentContext
                        .getClassContext()
                        .getEngineContext()
                        .getMap()
                        .get(ExampleAutowiredEngineInterceptor1.KEY))
                .isNotNull();

        assertThat(argumentContext
                        .getClassContext()
                        .getEngineContext()
                        .getMap()
                        .get(ExampleAutowiredEngineInterceptor1.KEY))
                .isEqualTo(ExampleAutowiredEngineInterceptor1.VALUE);
    }
}
