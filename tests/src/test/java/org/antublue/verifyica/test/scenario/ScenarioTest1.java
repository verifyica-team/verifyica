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

package org.antublue.verifyica.test.scenario;

import org.antublue.verifyica.api.ArgumentContext;
import org.antublue.verifyica.api.Verifyica;

@Verifyica.ScenarioTest
public class ScenarioTest1 {

    @Verifyica.ArgumentSupplier
    public static String arguments() {
        return "ignored";
    }

    @Verifyica.Test(order = 1)
    public void step0(ArgumentContext argumentContext) throws Throwable {
        System.out.printf(
                "step0(name[%s], payload[%s])%n",
                argumentContext.getTestArgument(), argumentContext.getTestArgument().getPayload());
    }

    @Verifyica.Test(order = 2)
    public void step2(ArgumentContext argumentContext) throws Throwable {
        System.out.printf(
                "step2(name[%s], payload[%s])%n",
                argumentContext.getTestArgument(), argumentContext.getTestArgument().getPayload());
    }

    @Verifyica.Test(order = 3)
    public void step4(ArgumentContext argumentContext) throws Throwable {
        System.out.printf(
                "step4(name[%s], payload[%s])%n",
                argumentContext.getTestArgument(), argumentContext.getTestArgument().getPayload());
    }

    @Verifyica.Test(order = 4)
    public void step1(ArgumentContext argumentContext) throws Throwable {
        System.out.printf(
                "step1(name[%s], payload[%s])%n",
                argumentContext.getTestArgument(), argumentContext.getTestArgument().getPayload());
    }

    @Verifyica.Test(order = 5)
    public void step3(ArgumentContext argumentContext) throws Throwable {
        System.out.printf(
                "step3(name[%s], payload[%s])%n",
                argumentContext.getTestArgument(), argumentContext.getTestArgument().getPayload());
    }

    @Verifyica.Test(order = 6)
    public void step5(ArgumentContext argumentContext) throws Throwable {
        System.out.printf(
                "step5(name[%s], payload[%s])%n",
                argumentContext.getTestArgument(), argumentContext.getTestArgument().getPayload());
    }
}
