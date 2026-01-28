/*
 * Copyright (C) Verifyica project authors and contributors. All rights reserved.
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

package org.verifyica.test.inheritance;

import static org.assertj.core.api.Assertions.assertThat;

import org.verifyica.api.ArgumentContext;
import org.verifyica.api.ClassContext;
import org.verifyica.api.Verifyica;

public abstract class AbstractTest1 {

    @Verifyica.Prepare
    public static void prepare(ClassContext classContext) {
        System.out.println("AbstractTest1 prepare()");

        assertThat(classContext).isNotNull();
        assertThat(classContext.getMap()).isNotNull();

        System.out.printf("class(%s)%n", classContext.getTestClass().getName());
        System.out.printf("instance(%s)%n", classContext.getTestInstance());
    }

    @Verifyica.BeforeAll
    public final void beforeAll(ArgumentContext argumentContext) {
        System.out.printf("AbstractTest1 beforeAll(%s)%n", argumentContext.getTestArgument());

        assertThat(argumentContext).isNotNull();
        assertThat(argumentContext.getMap()).isNotNull();
        assertThat(argumentContext.getTestArgument()).isNotNull();

        System.out.printf(
                "  class(%s)%n",
                argumentContext.getClassContext().getTestClass().getName());
        System.out.printf("  instance(%s)%n", argumentContext.getClassContext().getTestInstance());
        System.out.printf("  argument(%s)%n", argumentContext.getTestArgument().getName());
    }

    @Verifyica.BeforeEach
    public final void beforeEach(ArgumentContext argumentContext) {
        System.out.printf("AbstractTest1 beforeEach(%s)%n", argumentContext.getTestArgument());

        assertThat(argumentContext).isNotNull();
        assertThat(argumentContext.getMap()).isNotNull();
        assertThat(argumentContext.getTestArgument()).isNotNull();
    }

    @Verifyica.Test
    public final void test1(ArgumentContext argumentContext) {
        System.out.printf("AbstractTest1 test1(%s)%n", argumentContext.getTestArgument());

        assertThat(argumentContext).isNotNull();
        assertThat(argumentContext.getMap()).isNotNull();
        assertThat(argumentContext.getTestArgument()).isNotNull();
    }

    @Verifyica.AfterEach
    public final void afterEach(ArgumentContext argumentContext) {
        System.out.printf("AbstractTest1 afterEach(%s)%n", argumentContext.getTestArgument());

        assertThat(argumentContext).isNotNull();
        assertThat(argumentContext.getMap()).isNotNull();
        assertThat(argumentContext.getTestArgument()).isNotNull();
    }

    @Verifyica.AfterAll
    public final void afterAll(ArgumentContext argumentContext) {
        System.out.printf("AbstractTest1 afterAll(%s)%n", argumentContext.getTestArgument());

        assertThat(argumentContext).isNotNull();
        assertThat(argumentContext.getMap()).isNotNull();
        assertThat(argumentContext.getTestArgument()).isNotNull();
    }

    @Verifyica.Conclude
    public static void conclude(ClassContext classContext) {
        System.out.println("AbstractTest1 conclude()");

        assertThat(classContext).isNotNull();
        assertThat(classContext.getMap()).isNotNull();
    }
}
