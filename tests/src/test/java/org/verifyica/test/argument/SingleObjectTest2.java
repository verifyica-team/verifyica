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

package org.verifyica.test.argument;

import java.util.ArrayList;
import java.util.Collection;
import org.verifyica.api.Argument;
import org.verifyica.api.ArgumentContext;
import org.verifyica.api.ClassContext;
import org.verifyica.api.Verifyica;

public class SingleObjectTest2 {

    @Verifyica.ArgumentSupplier(parallelism = Integer.MAX_VALUE)
    public static Collection<Object> arguments() {
        Collection<Object> collection = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            collection.add(i);
        }
        return collection;
    }

    @Verifyica.Prepare
    public void prepare(ClassContext classContext) {
        System.out.println("prepare()");
    }

    @Verifyica.BeforeAll
    public void beforeAll(Integer integer) {
        System.out.printf("beforeAll(%d)%n", integer);
    }

    @Verifyica.BeforeEach
    public void beforeEach(Integer integer) {
        System.out.printf("beforeEach(%d)%n", integer);
    }

    @Verifyica.Test
    @Verifyica.Order(1)
    public void testDirectArgument(Integer integer) {
        System.out.printf("testDirectArgument(%s)%n", integer);
    }

    @Verifyica.Test
    @Verifyica.Order(2)
    public void testArgument(Argument<Integer> argument) {
        System.out.printf("testArgument(name[%s], payload[%s])%n", argument.getName(), argument.getPayload());
    }

    @Verifyica.Test
    @Verifyica.Order(3)
    public void testArgumentContext(ArgumentContext argumentContext) {
        Argument<Integer> argument = argumentContext.getArgumentAs(Integer.class);
        System.out.printf("testArgumentContext(name[%s], payload[%s])%n", argument.getName(), argument.getPayload());
    }

    @Verifyica.AfterEach
    public void afterEach(Integer integer) {
        System.out.printf("afterEach(%d)%n", integer);
    }

    @Verifyica.AfterAll
    public void afterAll(Integer integer) {
        System.out.printf("afterAll(%d)%n", integer);
    }

    @Verifyica.Conclude
    public void conclude(ClassContext classContext) {
        System.out.println("conclude()");
    }
}
