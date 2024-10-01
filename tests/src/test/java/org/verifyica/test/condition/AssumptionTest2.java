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

package org.verifyica.test.condition;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.verifyica.api.Argument;
import org.verifyica.api.ArgumentContext;
import org.verifyica.api.Assumption;
import org.verifyica.api.Verifyica;

@SuppressWarnings("unchecked")
public class AssumptionTest2 {

    @Verifyica.ArgumentSupplier(parallelism = Integer.MAX_VALUE)
    public static Object arguments() {
        Collection<Argument<Integer>> collection = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            collection.add(Argument.ofInt(i));
        }
        return collection;
    }

    @Verifyica.BeforeAll
    public void beforeAll(ArgumentContext argumentContext) throws Throwable {
        Assumption.assumeTrue(() -> argumentContext.getTestArgument().getPayload(Integer.class) % 2 == 1);
    }

    @Verifyica.BeforeEach
    public void beforeEach(ArgumentContext argumentContext) throws Throwable {
        assertThat(argumentContext.getTestArgument().getPayload(Integer.class) % 2 == 1);
    }

    @Verifyica.Test
    public void test1(ArgumentContext argumentContext) throws Throwable {
        assertThat(argumentContext.getTestArgument().getPayload(Integer.class) % 2 == 1);

        System.out.printf(
                "test1(name[%s], payload[%s])%n",
                argumentContext.getTestArgument(),
                argumentContext.getTestArgument().getPayload());

        storeState(argumentContext, "test1");
        argumentContext.getStore().put("test1", "X");
    }

    @Verifyica.Test
    public void test2(ArgumentContext argumentContext) throws Throwable {
        assertThat(argumentContext.getTestArgument().getPayload(Integer.class) % 2 == 1);
        Assumption.assumeTrue(argumentContext.getStore().containsKey("test1"));

        System.out.printf(
                "test2(name[%s], payload[%s])%n",
                argumentContext.getTestArgument(),
                argumentContext.getTestArgument().getPayload());

        storeState(argumentContext, "test2");
    }

    @Verifyica.Test
    public void test3(ArgumentContext argumentContext) throws Throwable {
        assertThat(argumentContext.getTestArgument().getPayload(Integer.class) % 2 == 1);
        Assumption.assumeTrue(argumentContext.getStore().containsKey("test2"));

        System.out.printf(
                "test3(name[%s], payload[%s])%n",
                argumentContext.getTestArgument(),
                argumentContext.getTestArgument().getPayload());

        storeState(argumentContext, "test3");
    }

    @Verifyica.Test
    public void test4(ArgumentContext argumentContext) throws Throwable {
        assertThat(argumentContext.getTestArgument().getPayload(Integer.class) % 2 == 1);
        Assumption.assumeFalse(() -> argumentContext.getTestArgument().getPayload(Integer.class) % 2 == 0);

        System.out.printf(
                "test4(name[%s], payload[%s])%n",
                argumentContext.getTestArgument(),
                argumentContext.getTestArgument().getPayload());

        storeState(argumentContext, "test4");
    }

    @Verifyica.Test
    public void test5(ArgumentContext argumentContext) throws Throwable {
        assertThat(argumentContext.getTestArgument().getPayload(Integer.class) % 2 == 1);
        Assumption.assumeFalse(() -> argumentContext.getTestArgument().getPayload(Integer.class) % 2 == 0);

        System.out.printf(
                "test5(name[%s], payload[%s])%n",
                argumentContext.getTestArgument(),
                argumentContext.getTestArgument().getPayload());

        storeState(argumentContext, "test5");
    }

    private static void storeState(ArgumentContext argumentContext, String state) {
        List<String> list = (List<String>)
                argumentContext.getClassContext().getStore().computeIfAbsent("state", object -> new ArrayList<>());
        list.add(state);
    }
}
