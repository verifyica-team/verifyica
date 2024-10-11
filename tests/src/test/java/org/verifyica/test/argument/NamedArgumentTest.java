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

public class NamedArgumentTest {

    @Verifyica.ArgumentSupplier
    public static Collection<Object> arguments() {
        Collection<Object> collection = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            collection.add(new CustomArgument(i));
        }
        return collection;
    }

    @Verifyica.Prepare
    public void prepare(ClassContext classContext) {
        System.out.println("prepare()");
        assertThat(classContext).isNotNull();
    }

    @Verifyica.BeforeAll
    public void beforeAll(CustomArgument argument) {
        System.out.println(format("beforeAll(%s)", argument));
    }

    @Verifyica.BeforeEach
    public void beforeEach(CustomArgument argument) {
        System.out.println(format("beforeEach(%s)", argument));
    }

    @Verifyica.Test
    @Verifyica.Order(1)
    public void testDirectArgument(CustomArgument argument) {
        System.out.printf("testDirectArgument(%s)%n", argument);
    }

    @Verifyica.Test
    @Verifyica.Order(3)
    public void testArgument(Argument<CustomArgument> argument) {
        System.out.printf("testArgument(name[%s], payload[%s])%n", argument.getName(), argument.getPayload());
    }

    @Verifyica.Test
    @Verifyica.Order(4)
    public void testArgumentContext(ArgumentContext argumentContext) {
        Argument<CustomArgument> argument = argumentContext.getTestArgument(CustomArgument.class);
        System.out.printf("testArgumentContext(name[%s], payload[%s])%n", argument.getName(), argument.getPayload());
    }

    @Verifyica.AfterEach
    public void afterEach(CustomArgument argument) {
        System.out.println(format("afterEach(%s)", argument));
    }

    @Verifyica.AfterAll
    public void afterAll(CustomArgument argument) {
        System.out.println(format("afterAll(%s)", argument));
    }

    @Verifyica.Conclude
    public void conclude(ClassContext classContext) {
        System.out.println("conclude()");
        assertThat(classContext).isNotNull();
    }

    private static class CustomArgument implements Named {

        private final int value;

        public CustomArgument(int value) {
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
