/*
 * Copyright (C) Verifyica project authors and contributors
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

package org.verifyica.examples.dependency;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import org.verifyica.api.ArgumentContext;
import org.verifyica.api.ClassContext;
import org.verifyica.api.Verifyica;

@SuppressWarnings("unchecked")
public class DependsOnTest {

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
    @Verifyica.Tag("test1")
    public void test1(ArgumentContext argumentContext) {
        System.out.printf(
                "test1(name[%s], payload[%s])%n",
                argumentContext.getTestArgument(),
                argumentContext.getTestArgument().getPayload());

        argumentContext.classContext().map().getAs(LIST, List.class).add("test1");
    }

    @Verifyica.Test
    @Verifyica.Tag("test2")
    @Verifyica.DependsOn("test1")
    public void test2(ArgumentContext argumentContext) {
        System.out.printf(
                "test2(name[%s], payload[%s])%n",
                argumentContext.getTestArgument(),
                argumentContext.getTestArgument().getPayload());

        argumentContext.classContext().map().getAs(LIST, List.class).add("test2");
    }

    @Verifyica.Test
    @Verifyica.Tag("test3")
    public void test3(ArgumentContext argumentContext) {
        System.out.printf(
                "test3(name[%s], payload[%s])%n",
                argumentContext.getTestArgument(),
                argumentContext.getTestArgument().getPayload());

        argumentContext.classContext().map().getAs(LIST, List.class).add("test3");
    }

    @Verifyica.Test
    @Verifyica.Tag("test4")
    @Verifyica.DependsOn("test3")
    public void test4(ArgumentContext argumentContext) {
        System.out.printf(
                "test4(name[%s], payload[%s])%n",
                argumentContext.getTestArgument(),
                argumentContext.getTestArgument().getPayload());

        argumentContext.classContext().map().getAs(LIST, List.class).add("test4");
    }

    @Verifyica.Test
    @Verifyica.Tag("test5")
    @Verifyica.DependsOn("test2")
    @Verifyica.DependsOn("test4")
    public void test5(ArgumentContext argumentContext) {
        System.out.printf(
                "test5(name[%s], payload[%s])%n",
                argumentContext.getTestArgument(),
                argumentContext.getTestArgument().getPayload());

        argumentContext.classContext().map().getAs(LIST, List.class).add("test5");
    }

    @Verifyica.Test
    @Verifyica.Tag("test6")
    @Verifyica.DependsOn("test1")
    public void test6(ArgumentContext argumentContext) {
        System.out.printf(
                "test6(name[%s], payload[%s])%n",
                argumentContext.getTestArgument(),
                argumentContext.getTestArgument().getPayload());

        argumentContext.classContext().map().getAs(LIST, List.class).add("test6");
    }

    @Verifyica.Test
    public void test7(ArgumentContext argumentContext) {
        System.out.printf(
                "test7(name[%s], payload[%s])%n",
                argumentContext.getTestArgument(),
                argumentContext.getTestArgument().getPayload());

        argumentContext.classContext().map().getAs(LIST, List.class).add("test7");
    }

    @Verifyica.Conclude
    public void conclude(ClassContext classContext) {
        List<String> expected = new ArrayList<>();
        expected.add("test1");
        expected.add("test2");
        expected.add("test6");
        expected.add("test3");
        expected.add("test4");
        expected.add("test5");
        expected.add("test7");

        List<String> actual = classContext.map().getAs(LIST);

        assertThat(actual).isEqualTo(expected);
    }
}
