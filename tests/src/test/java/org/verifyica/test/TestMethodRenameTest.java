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

package org.verifyica.test;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.function.Consumer;
import org.verifyica.api.ArgumentContext;
import org.verifyica.api.EngineContext;
import org.verifyica.api.EngineInterceptor;
import org.verifyica.api.Verifyica;
import org.verifyica.engine.api.ClassDefinition;
import org.verifyica.engine.api.MethodDefinition;

public class TestMethodRenameTest {

    @Verifyica.Autowired
    public static class DisplayNameEngineInterceptor implements EngineInterceptor {

        public void onTestDiscovery(EngineContext engineContext, List<ClassDefinition> classDefinitions)
                throws Throwable {
            classDefinitions.stream()
                    .filter(classDefinition -> classDefinition.getTestClass() == TestMethodRenameTest.class)
                    .forEach(classDefinition -> {
                        classDefinition.getTestMethodDefinitions().forEach(new Consumer<MethodDefinition>() {
                            @Override
                            public void accept(MethodDefinition methodDefinition) {
                                String displayName =
                                        methodDefinition.getMethod().getName();
                                String newDisplayName = displayName + "Renamed";
                                System.out.printf(
                                        "Renaming test class from [%s] to" + " [%s]%n", displayName, newDisplayName);
                                methodDefinition.setDisplayName(newDisplayName);
                            }
                        });
                    });
        }
    }

    @Verifyica.ArgumentSupplier
    public static Object arguments() {
        return "test";
    }

    @Verifyica.Test
    public void test(ArgumentContext argumentContext) throws Throwable {
        assertThat(argumentContext).isNotNull();
        assertThat(argumentContext.getMap()).isNotNull();
        assertThat(argumentContext.getTestArgument()).isNotNull();
        assertThat(argumentContext.getTestArgument().getPayload()).isEqualTo("test");

        System.out.printf(
                "test(name[%s], payload[%s])%n",
                argumentContext.getTestArgument(),
                argumentContext.getTestArgument().getPayload());
    }
}
