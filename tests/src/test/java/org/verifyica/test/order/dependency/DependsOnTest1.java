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

package org.verifyica.test.order.dependency;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import org.verifyica.api.ArgumentContext;
import org.verifyica.api.ClassContext;
import org.verifyica.api.Verifyica;

@SuppressWarnings("unchecked")
public class DependsOnTest1 {

    private static final String LIST = "list";

    @Verifyica.ArgumentSupplier
    public static Object arguments() {
        return "test";
    }

    @Verifyica.Prepare
    public void prepare(ClassContext classContext) {
        classContext.map().put(LIST, new ArrayList<String>());
    }

    @Verifyica.Test
    public void test1(ArgumentContext argumentContext) throws Throwable {
        System.out.printf(
                "test1(name[%s], payload[%s])%n",
                argumentContext.getTestArgument(),
                argumentContext.getTestArgument().getPayload());

        argumentContext.classContext().map().getAs(LIST, List.class).add("test1");
    }

    @Verifyica.Test
    @Verifyica.Tag("test2")
    public void test2(ArgumentContext argumentContext) throws Throwable {
        System.out.printf(
                "test2(name[%s], payload[%s])%n",
                argumentContext.getTestArgument(),
                argumentContext.getTestArgument().getPayload());

        argumentContext.classContext().map().getAs(LIST, List.class).add("test2");
    }

    @Verifyica.Test
    @Verifyica.Tag("test3")
    @Verifyica.Experimental.DependsOn("test2")
    public void test3(ArgumentContext argumentContext) throws Throwable {
        System.out.printf(
                "test3(name[%s], payload[%s])%n",
                argumentContext.getTestArgument(),
                argumentContext.getTestArgument().getPayload());

        argumentContext.classContext().map().getAs(LIST, List.class).add("test3");
    }

    @Verifyica.Test
    @Verifyica.Experimental.DependsOn("test2")
    public void test4(ArgumentContext argumentContext) throws Throwable {
        System.out.printf(
                "test4(name[%s], payload[%s])%n",
                argumentContext.getTestArgument(),
                argumentContext.getTestArgument().getPayload());

        argumentContext.classContext().map().getAs(LIST, List.class).add("test4");
    }

    @Verifyica.Test
    public void test5(ArgumentContext argumentContext) throws Throwable {
        System.out.printf(
                "test5(name[%s], payload[%s])%n",
                argumentContext.getTestArgument(),
                argumentContext.getTestArgument().getPayload());

        argumentContext.classContext().map().getAs(LIST, List.class).add("test5");
    }

    @Verifyica.Conclude
    public void conclude(ClassContext classContext) {
        List<String> expected = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            expected.add("test" + i);
        }

        List<String> actual = classContext.map().getAs(LIST);

        assertThat(actual).isEqualTo(expected);
    }
}
