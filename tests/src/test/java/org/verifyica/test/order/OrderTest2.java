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

package org.verifyica.test.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.verifyica.test.support.AssertionSupport.assertArgumentContext;

import java.util.ArrayList;
import java.util.List;
import org.verifyica.api.ArgumentContext;
import org.verifyica.api.Verifyica;

public class OrderTest2 implements AutoCloseable {

    private List<String> actual = new ArrayList<>();

    @Verifyica.ArgumentSupplier
    public static String arguments() {
        return "test";
    }

    @Verifyica.Test
    @Verifyica.Order(3)
    public void test1(ArgumentContext argumentContext) {
        System.out.printf("test1(%s)%n", argumentContext.getTestArgument());

        assertArgumentContext(argumentContext);
        actual.add("test1");
    }

    @Verifyica.Test
    @Verifyica.Order(2)
    public void test2(ArgumentContext argumentContext) {
        System.out.printf("test2(%s)%n", argumentContext.getTestArgument());

        assertArgumentContext(argumentContext);
        actual.add("test2");
    }

    @Verifyica.Test
    @Verifyica.Order(1)
    public void test3(ArgumentContext argumentContext) {
        System.out.printf("test3(%s)%n", argumentContext.getTestArgument());

        assertArgumentContext(argumentContext);
        actual.add("test3");
    }

    @Override
    public void close() {
        List<String> expected = new ArrayList<>();
        expected.add("test3");
        expected.add("test2");
        expected.add("test1");

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
