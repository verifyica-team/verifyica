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

package org.verifyica.test.step;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import org.verifyica.api.ArgumentContext;
import org.verifyica.api.ClassContext;
import org.verifyica.api.Verifyica;

@Verifyica.Disabled
public class StepTest5 {

    private static List<String> actual = new ArrayList<>();

    @Verifyica.ArgumentSupplier
    public static String arguments() {
        return "ignored";
    }

    @Verifyica.Test
    public void test(ArgumentContext argumentContext) {
        System.out.printf(
                "test(name[%s], payload[%s])%n",
                argumentContext.getTestArgument(),
                argumentContext.getTestArgument().getPayload());

        actual.add("test");
    }

    @Verifyica.Test
    @Verifyica.Step(id = "step0", nextId = "step2")
    public void test0(ArgumentContext argumentContext) throws Throwable {
        System.out.printf(
                "test0(name[%s], payload[%s])%n",
                argumentContext.getTestArgument(),
                argumentContext.getTestArgument().getPayload());

        actual.add("test0");
    }

    @Verifyica.Test
    @Verifyica.Step(id = "step2", nextId = "step4")
    public void test2(ArgumentContext argumentContext) throws Throwable {
        System.out.printf(
                "test2(name[%s], payload[%s])%n",
                argumentContext.getTestArgument(),
                argumentContext.getTestArgument().getPayload());

        actual.add("test2");
    }

    @Verifyica.Test
    @Verifyica.Step(id = "step4", nextId = "step1")
    public void test4(ArgumentContext argumentContext) throws Throwable {
        System.out.printf(
                "test4(name[%s], payload[%s])%n",
                argumentContext.getTestArgument(),
                argumentContext.getTestArgument().getPayload());

        actual.add("test4");
    }

    @Verifyica.Test
    @Verifyica.Step(id = "step1", nextId = "step3")
    public void test1(ArgumentContext argumentContext) throws Throwable {
        System.out.printf(
                "test1(name[%s], payload[%s])%n",
                argumentContext.getTestArgument(),
                argumentContext.getTestArgument().getPayload());

        throw new AssertionError("Forced");
    }

    @Verifyica.Test
    @Verifyica.Step(id = "step3", nextId = "step5")
    public void test3(ArgumentContext argumentContext) throws Throwable {
        System.out.printf(
                "test3(name[%s], payload[%s])%n",
                argumentContext.getTestArgument(),
                argumentContext.getTestArgument().getPayload());

        actual.add("test3");
    }

    @Verifyica.Test
    @Verifyica.Step(id = "step5", nextId = "")
    public void test5(ArgumentContext argumentContext) throws Throwable {
        System.out.printf(
                "test5(name[%s], payload[%s])%n",
                argumentContext.getTestArgument(),
                argumentContext.getTestArgument().getPayload());

        actual.add("test5");
    }

    @Verifyica.Conclude
    public void conclude(ClassContext classContext) {
        List<String> expected = new ArrayList<>();

        expected.add("test0");
        expected.add("test2");
        expected.add("test4");

        assertThat(actual.size()).isEqualTo(expected.size());

        int pad = pad(expected);

        for (int i = 0; i < expected.size(); i++) {
            System.out.printf("expected [%-" + pad + "s] actual [%-" + pad + "s]%n", expected.get(i), actual.get(i));

            assertThat(actual.get(i)).isEqualTo(expected.get(i));
        }
    }

    private static int pad(List<String> strings) {
        int pad = 0;

        for (String string : strings) {
            pad = Math.max(string.length(), pad);
        }

        return pad;
    }
}
