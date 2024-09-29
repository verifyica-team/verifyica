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

package org.verifyica.test.interceptor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import org.verifyica.api.ArgumentContext;
import org.verifyica.api.Verifyica;
import org.verifyica.api.interceptor.ClassDefinition;
import org.verifyica.api.interceptor.EngineInterceptor;
import org.verifyica.api.interceptor.EngineInterceptorContext;
import org.verifyica.api.interceptor.MethodDefinition;

@Verifyica.Disabled
public class EngineInterceptorTest5 implements EngineInterceptor {

    @Verifyica.Autowired
    public static class ReverseTestMethodOrder implements EngineInterceptor {

        public void onTestDiscovery(
                EngineInterceptorContext engineInterceptorContext, ClassDefinition classDefinition) {
            if (classDefinition.getTestClass() == EngineInterceptorTest5.class) {
                System.out.println("reversing test method order");
                shuffle(classDefinition.getTestMethodDefinitions());
            }
        }
    }

    @Verifyica.ArgumentSupplier
    public static String arguments() {
        return "ignored";
    }

    @Verifyica.Test
    @Verifyica.Order(order = 1)
    public void test1(ArgumentContext argumentContext) throws Throwable {
        System.out.printf("test1(%s)%n", argumentContext.getTestArgument().getPayload());
    }

    @Verifyica.Test
    @Verifyica.Order(order = 2)
    public void test2(ArgumentContext argumentContext) throws Throwable {
        System.out.printf("test2(%s)%n", argumentContext.getTestArgument().getPayload());
    }

    @Verifyica.Test
    @Verifyica.Order(order = 3)
    public void test3(ArgumentContext argumentContext) throws Throwable {
        System.out.printf("test3(%s)%n", argumentContext.getTestArgument().getPayload());
    }

    @Verifyica.Test
    @Verifyica.Order(order = 4)
    public void test4(ArgumentContext argumentContext) throws Throwable {
        System.out.printf("test3(%s)%n", argumentContext.getTestArgument().getPayload());
    }

    private static void shuffle(Set<MethodDefinition> methodDefinitions) {
        ArrayList<MethodDefinition> list = new ArrayList<>(methodDefinitions);
        Collections.shuffle(list);
        methodDefinitions.clear();
        methodDefinitions.addAll(list);
    }
}
