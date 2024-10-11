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

package org.verifyica.test.argument;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Collection;
import org.verifyica.api.Argument;
import org.verifyica.api.ArgumentContext;
import org.verifyica.api.ClassContext;
import org.verifyica.api.Named;
import org.verifyica.api.Verifyica;

public class InheritedArgumentTest {

    @Verifyica.ArgumentSupplier
    public static Collection<Object> arguments() {
        Collection<Object> collection = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            collection.add(new ConcreteCustomArgument(i));
        }
        return collection;
    }

    @Verifyica.Prepare
    public void prepare(ClassContext classContext) {
        System.out.println("prepare()");
        assertThat(classContext).isNotNull();
    }

    @Verifyica.BeforeAll
    public void beforeAll(ConcreteCustomArgument argument) {
        System.out.println(format("beforeAll(%s)", argument));
    }

    @Verifyica.BeforeEach
    public void beforeEach(ConcreteCustomArgument argument) {
        System.out.println(format("beforeEach(%s)", argument));
    }

    @Verifyica.Test
    @Verifyica.Order(1)
    public void testDirectArgument(ConcreteCustomArgument argument) {
        System.out.printf("testDirectArgument(%s)%n", argument);
    }

    @Verifyica.Test
    @Verifyica.Order(3)
    public void testArgument(Argument<ConcreteCustomArgument> argument) {
        System.out.printf("testArgument(name[%s], payload[%s])%n", argument.getName(), argument.getPayload());
    }

    @Verifyica.Test
    @Verifyica.Order(4)
    public void testArgumentContext(ArgumentContext argumentContext) {
        Argument<ConcreteCustomArgument> argument = argumentContext.getTestArgument(ConcreteCustomArgument.class);
        System.out.printf("testArgumentContext(name[%s], payload[%s])%n", argument.getName(), argument.getPayload());

        argumentContext.getTestArgument(AbstractCustomArgument.class);
        argumentContext.getTestArgumentPayload(AbstractCustomArgument.class);
    }

    @Verifyica.AfterEach
    public void afterEach(ConcreteCustomArgument argument) {
        System.out.println(format("afterEach(%s)", argument));
    }

    @Verifyica.AfterAll
    public void afterAll(ConcreteCustomArgument argument) {
        System.out.println(format("afterAll(%s)", argument));
    }

    @Verifyica.Conclude
    public void conclude(ClassContext classContext) {
        System.out.println("conclude()");
        assertThat(classContext).isNotNull();
    }

    public static class ConcreteCustomArgument extends AbstractCustomArgument {

        public ConcreteCustomArgument(int value) {
            super(value);
        }
    }

    private abstract static class AbstractCustomArgument implements Named {

        private final int value;

        protected AbstractCustomArgument(int value) {
            this.value = value;
        }

        @Override
        public String getName() {
            return "CustomArgument(" + value + ")";
        }

        @Override
        public String toString() {
            return "CustomArgument{" + "value=" + value + '}';
        }
    }
}
