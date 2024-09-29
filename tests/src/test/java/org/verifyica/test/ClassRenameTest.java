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

package org.verifyica.test;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.function.Predicate;
import org.verifyica.api.ArgumentContext;
import org.verifyica.api.Verifyica;
import org.verifyica.api.interceptor.EngineInterceptor;
import org.verifyica.api.interceptor.EngineInterceptorContext;
import org.verifyica.engine.api.ClassDefinition;

public class ClassRenameTest {

    @Verifyica.Autowired
    public static class DisplayNameEngineInterceptor implements EngineInterceptor {

        public Predicate<ClassDefinition> onTestDiscoveryPredicate() {
            return classDefinition -> classDefinition.getTestClass() == ClassRenameTest.class;
        }

        public void onTestDiscovery(EngineInterceptorContext engineInterceptorContext, ClassDefinition classDefinition)
                throws Throwable {
            assertThat(classDefinition.getTestClass()).isEqualTo(ClassRenameTest.class);

            String fullDisplayName = classDefinition.getDisplayName();
            String displayName = classDefinition.getTestClass().getSimpleName();
            String newDisplayName = displayName + "Renamed";
            System.out.printf("Renaming test class from [%s] to [%s]%n", fullDisplayName, newDisplayName);
            classDefinition.setDisplayName(newDisplayName);
        }
    }

    @Verifyica.ArgumentSupplier
    public static Object arguments() {
        return "test";
    }

    @Verifyica.Test
    public void test(ArgumentContext argumentContext) throws Throwable {
        assertThat(argumentContext).isNotNull();
        assertThat(argumentContext.getStore()).isNotNull();
        assertThat(argumentContext.getTestArgument()).isNotNull();
        assertThat(argumentContext.getTestArgument().getPayload()).isEqualTo("test");

        System.out.printf(
                "test(name[%s], payload[%s])%n",
                argumentContext.getTestArgument(),
                argumentContext.getTestArgument().getPayload());
    }
}
