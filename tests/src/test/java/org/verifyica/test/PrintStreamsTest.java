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

package org.verifyica.test;

import java.util.ArrayList;
import java.util.Collection;
import org.verifyica.api.Argument;
import org.verifyica.api.ArgumentContext;
import org.verifyica.api.Verifyica;
import org.verifyica.test.support.RandomSupport;

@Verifyica.Disabled
public class PrintStreamsTest {

    @Verifyica.ArgumentSupplier(parallelism = 10)
    public static Collection<Argument<String>> arguments() {
        Collection<Argument<String>> collection = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            collection.add(Argument.ofString("String " + i));
        }

        return collection;
    }

    @Verifyica.Test
    public void test1(ArgumentContext argumentContext) {
        sleep();
        System.out.println("test1() A");
        sleep();
        new RuntimeException().printStackTrace(System.err);
        sleep();
        System.out.println("test1() B");
        sleep();
        new IllegalArgumentException().printStackTrace(System.err);
    }

    private static void sleep() {
        try {
            Thread.sleep(RandomSupport.randomLong(0, 1000));
        } catch (InterruptedException e) {
            // INTENTIONALLY BLANK
        }
    }
}
