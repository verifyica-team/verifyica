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

package org.antublue.verifyica.test.inheritance;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;

import org.antublue.verifyica.api.ArgumentContext;
import org.antublue.verifyica.api.ClassContext;
import org.antublue.verifyica.api.Verifyica;

/** Example test */
public abstract class AbstractTest1 {

    @Verifyica.Prepare
    public static void prepare(ClassContext classContext) {
        System.out.println(format("AbstractTest1 prepare()"));

        assertThat(classContext).isNotNull();
        assertThat(classContext.getStore()).isNotNull();

        System.out.println(format("class(%s)", classContext.getTestClass().getName()));
        System.out.println(format("instance(%s)", classContext.getTestInstance()));
    }

    @Verifyica.BeforeAll
    public final void beforeAll(ArgumentContext argumentContext) {
        System.out.println(
                format("AbstractTest1 beforeAll(%s)", argumentContext.getTestArgument()));

        assertThat(argumentContext).isNotNull();
        assertThat(argumentContext.getStore()).isNotNull();
        assertThat(argumentContext.getTestArgument()).isNotNull();

        System.out.println(
                format("  class(%s)", argumentContext.getClassContext().getTestClass().getName()));
        System.out.println(
                format("  instance(%s)", argumentContext.getClassContext().getTestInstance()));
        System.out.println(format("  argument(%s)", argumentContext.getTestArgument().getName()));
    }

    @Verifyica.BeforeEach
    public final void beforeEach(ArgumentContext argumentContext) {
        System.out.println(
                format("AbstractTest1 beforeEach(%s)", argumentContext.getTestArgument()));

        assertThat(argumentContext).isNotNull();
        assertThat(argumentContext.getStore()).isNotNull();
        assertThat(argumentContext.getTestArgument()).isNotNull();
    }

    @Verifyica.Test
    public final void test1(ArgumentContext argumentContext) {
        System.out.println(format("AbstractTest1 test1(%s)", argumentContext.getTestArgument()));

        assertThat(argumentContext).isNotNull();
        assertThat(argumentContext.getStore()).isNotNull();
        assertThat(argumentContext.getTestArgument()).isNotNull();
    }

    @Verifyica.AfterEach
    public final void afterEach(ArgumentContext argumentContext) {
        System.out.println(
                format("AbstractTest1 afterEach(%s)", argumentContext.getTestArgument()));

        assertThat(argumentContext).isNotNull();
        assertThat(argumentContext.getStore()).isNotNull();
        assertThat(argumentContext.getTestArgument()).isNotNull();
    }

    @Verifyica.AfterAll
    public final void afterAll(ArgumentContext argumentContext) {
        System.out.println(format("AbstractTest1 afterAll(%s)", argumentContext.getTestArgument()));

        assertThat(argumentContext).isNotNull();
        assertThat(argumentContext.getStore()).isNotNull();
        assertThat(argumentContext.getTestArgument()).isNotNull();
    }

    @Verifyica.Conclude
    public static void conclude(ClassContext classContext) {
        System.out.println(format("AbstractTest1 conclude()"));

        assertThat(classContext).isNotNull();
        assertThat(classContext.getStore()).isNotNull();
    }
}
