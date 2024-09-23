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

package org.antublue.verifyica.test.scenario.step;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.function.Predicate;
import org.antublue.verifyica.api.ArgumentContext;
import org.antublue.verifyica.api.Verifyica;
import org.antublue.verifyica.api.interceptor.engine.ClassDefinition;
import org.antublue.verifyica.api.interceptor.engine.EngineInterceptor;
import org.antublue.verifyica.api.interceptor.engine.EngineInterceptorContext;

@Verifyica.Disabled
@Verifyica.ScenarioTest
public class ScenarioTest2 {

    @Verifyica.ArgumentSupplier
    public static String arguments() {
        return "ignored";
    }

    @Verifyica.Autowired
    public static class StepMethodOrderEngineInterceptor implements EngineInterceptor {

        @Override
        public Predicate<ClassDefinition> onTestDiscoveryPredicate() {
            return classDefinition -> classDefinition.getTestClass() == ScenarioTest2.class;
        }

        @Override
        public void onTestDiscovery(
                EngineInterceptorContext engineInterceptorContext, ClassDefinition classDefinition)
                throws Throwable {
            assertThat(classDefinition.getTestClass()).isEqualTo(ScenarioTest2.class);

            new StepMethodOrderer().orderMethods(classDefinition.getTestMethodDefinitions());

            classDefinition
                    .getTestMethodDefinitions()
                    .forEach(
                            methodDefinition ->
                                    System.out.printf(
                                            "testMethod -> %s%n",
                                            methodDefinition.getMethod().getName()));
        }
    }

    @Verifyica.Test
    public void test(ArgumentContext argumentContext) {
        System.out.printf(
                "test(name[%s], payload[%s])%n",
                argumentContext.getTestArgument(), argumentContext.getTestArgument().getPayload());
    }

    @Verifyica.Test
    @Step(tag = "step0", nextTag = "step2")
    public void step0(ArgumentContext argumentContext) throws Throwable {
        System.out.printf(
                "step0(name[%s], payload[%s])%n",
                argumentContext.getTestArgument(), argumentContext.getTestArgument().getPayload());
    }

    @Verifyica.Test
    @Step(tag = "step2", nextTag = "step4")
    public void step2(ArgumentContext argumentContext) throws Throwable {
        System.out.printf(
                "step2(name[%s], payload[%s])%n",
                argumentContext.getTestArgument(), argumentContext.getTestArgument().getPayload());
    }

    @Verifyica.Test
    @Step(tag = "step4", nextTag = "step1")
    public void step4(ArgumentContext argumentContext) throws Throwable {
        System.out.printf(
                "step4(name[%s], payload[%s])%n",
                argumentContext.getTestArgument(), argumentContext.getTestArgument().getPayload());

        throw new AssertionError("Forced");
    }

    @Verifyica.Test
    @Step(tag = "step1", nextTag = "step3")
    public void step1(ArgumentContext argumentContext) throws Throwable {
        System.out.printf(
                "step1(name[%s], payload[%s])%n",
                argumentContext.getTestArgument(), argumentContext.getTestArgument().getPayload());
    }

    @Verifyica.Test
    @Step(tag = "step3", nextTag = "step5")
    public void step3(ArgumentContext argumentContext) throws Throwable {
        System.out.printf(
                "step3(name[%s], payload[%s])%n",
                argumentContext.getTestArgument(), argumentContext.getTestArgument().getPayload());
    }

    @Verifyica.Test
    @Step(tag = "step5")
    public void step5(ArgumentContext argumentContext) throws Throwable {
        System.out.printf(
                "step5(name[%s], payload[%s])%n",
                argumentContext.getTestArgument(), argumentContext.getTestArgument().getPayload());
    }
}
