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

import java.util.ArrayList;
import java.util.Collection;
import org.verifyica.api.Argument;
import org.verifyica.api.ArgumentContext;
import org.verifyica.api.ClassContext;
import org.verifyica.api.Verifyica;

public class ConcreteTest1 extends AbstractTest1 {

    @Verifyica.ArgumentSupplier
    public static Collection<Argument<String>> arguments() {
        Collection<Argument<String>> collection = new ArrayList<>();

        collection.add(Argument.ofString("String 0"));
        collection.add(Argument.ofString("String 1"));
        collection.add(Argument.ofString("String 3"));

        return collection;
    }

    @Verifyica.Prepare
    public static void prepare2(ClassContext classContext) {
        System.out.println("    ConcreteTest1 prepare2()");

        assertThat(classContext).isNotNull();
        assertThat(classContext.getMap()).isNotNull();
    }

    @Verifyica.BeforeAll
    public void beforeAll2(ArgumentContext argumentContext) {
        System.out.printf("    ConcreteTest1 beforeAll2(%s)%n", argumentContext.getTestArgument());

        assertThat(argumentContext).isNotNull();
        assertThat(argumentContext.getMap()).isNotNull();
        assertThat(argumentContext.getTestArgument()).isNotNull();
    }

    @Verifyica.BeforeEach
    public void beforeEach2(ArgumentContext argumentContext) {
        System.out.printf("    ConcreteTest1 beforeEach2(%s)%n", argumentContext.getTestArgument());

        assertThat(argumentContext).isNotNull();
        assertThat(argumentContext.getMap()).isNotNull();
        assertThat(argumentContext.getTestArgument()).isNotNull();
    }

    @Verifyica.Test
    public void test2(ArgumentContext argumentContext) {
        System.out.printf("    ConcreteTest1 test2(%s)%n", argumentContext.getTestArgument());

        assertThat(argumentContext).isNotNull();
        assertThat(argumentContext.getMap()).isNotNull();
        assertThat(argumentContext.getTestArgument()).isNotNull();
    }

    @Verifyica.AfterEach
    public void afterEach2(ArgumentContext argumentContext) {
        System.out.printf("    ConcreteTest1 afterEach2(%s)%n", argumentContext.getTestArgument());

        assertThat(argumentContext).isNotNull();
        assertThat(argumentContext.getMap()).isNotNull();
        assertThat(argumentContext.getTestArgument()).isNotNull();
    }

    @Verifyica.AfterAll
    public void afterAll2(ArgumentContext argumentContext) {
        System.out.printf("    ConcreteTest1 afterAll2(%s)%n", argumentContext.getTestArgument());

        assertThat(argumentContext).isNotNull();
        assertThat(argumentContext.getMap()).isNotNull();
        assertThat(argumentContext.getTestArgument()).isNotNull();
    }

    @Verifyica.Conclude
    public static void conclude2(ClassContext classContext) {
        System.out.println("    ConcreteTest1 conclude2()");

        assertThat(classContext).isNotNull();
        assertThat(classContext.getMap()).isNotNull();
    }
}
