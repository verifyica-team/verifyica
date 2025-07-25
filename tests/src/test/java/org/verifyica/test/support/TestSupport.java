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

package org.verifyica.test.support;

import org.verifyica.api.ArgumentContext;
import org.verifyica.api.ClassContext;

public class TestSupport {

    private TestSupport() {
        // INTENTIONALLY EMPTY
    }

    /**
     * Method to log an ArgumentContext name
     *
     * @param testMethodName testMethodName
     * @param classContext classContext
     */
    public static void logArgumentContext(String testMethodName, ClassContext classContext) {
        System.out.printf(
                "%s:: ClassContext testClass [%s]%n",
                testMethodName, classContext.getTestClass().getName());
    }

    /**
     * Method to log an ArgumentContext name
     *
     * @param testMethodName testMethodName
     * @param argumentContext argumentContext
     */
    public static void logArgumentContext(String testMethodName, ArgumentContext argumentContext) {
        String argumentName = null;
        String argumentValue = null;

        if (argumentContext.getTestArgument() != null) {
            argumentName = argumentContext.getTestArgument().getName();
            argumentValue = argumentContext.getTestArgument().getPayload() != null
                    ? argumentContext.getTestArgument().getPayload().toString()
                    : null;
        }

        System.out.printf(
                "%s::%s ArgumentContext name [%s] payload [%s]%n",
                argumentContext.getClassContext().getTestClass().getName(),
                testMethodName,
                argumentName,
                argumentValue);
    }
}
