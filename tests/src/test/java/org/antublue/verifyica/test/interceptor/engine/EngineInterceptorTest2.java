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

package org.antublue.verifyica.test.interceptor.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import org.antublue.verifyica.api.ArgumentContext;
import org.antublue.verifyica.api.Verifyica;
import org.antublue.verifyica.api.interceptor.engine.ClassDefinition;
import org.antublue.verifyica.api.interceptor.engine.EngineInterceptor;
import org.antublue.verifyica.api.interceptor.engine.EngineInterceptorContext;
import org.antublue.verifyica.api.interceptor.engine.MethodDefinition;

public class EngineInterceptorTest2 implements EngineInterceptor {

    @Verifyica.Autowired
    public static class ReorderTestMethods implements EngineInterceptor {

        @Override
        public void onTestDiscovery(
                EngineInterceptorContext engineInterceptorContext,
                ClassDefinition classDefinition) {
            if (classDefinition.getTestClass() == EngineInterceptorTest2.class) {
                System.out.printf("re-ordering test methods%n");
                reverseOrder(classDefinition.getTestMethodDefinitions());
            }
        }
    }

    @Verifyica.ArgumentSupplier
    public static String arguments() {
        return "ignored";
    }

    @Verifyica.Test(order = 1)
    public void test1(ArgumentContext argumentContext) throws Throwable {
        System.out.printf("test1(%s)%n", argumentContext.getTestArgument().getPayload());
    }

    @Verifyica.Test(order = 2)
    public void test2(ArgumentContext argumentContext) throws Throwable {
        System.out.printf("test2(%s)%n", argumentContext.getTestArgument().getPayload());
    }

    @Verifyica.Test(order = 3)
    public void test3(ArgumentContext argumentContext) throws Throwable {
        System.out.printf("test3(%s)%n", argumentContext.getTestArgument().getPayload());
    }

    @Verifyica.Test(order = 4)
    public void test4(ArgumentContext argumentContext) throws Throwable {
        System.out.printf("test3(%s)%n", argumentContext.getTestArgument().getPayload());
    }

    private static void reverseOrder(Set<MethodDefinition> methodDefinitions) {
        ArrayList<MethodDefinition> list = new ArrayList<>(methodDefinitions);
        Collections.reverse(list);
        methodDefinitions.clear();
        methodDefinitions.addAll(list);
    }
}
