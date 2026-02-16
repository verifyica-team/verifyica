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

package com.example.tests;

import java.util.ArrayList;
import java.util.Collection;
import org.verifyica.api.Verifyica;

public class MyFirstTest {

    // Arguments are tested in parallel by default
    @Verifyica.ArgumentSupplier
    public static Object arguments() {
        Collection<String> collection = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            collection.add("argument-" + i);
        }
        return collection;
    }

    /**
     * The actual test.
     */
    @Verifyica.Test
    public void testWithArgument(String argument) {
        System.out.println("Testing with: " + argument);
        assert argument != null;
        assert argument.startsWith("argument-");
    }
}
