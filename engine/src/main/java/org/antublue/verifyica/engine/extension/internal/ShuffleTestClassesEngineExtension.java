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

package org.antublue.verifyica.engine.extension.internal;

import java.util.Collections;
import java.util.List;
import org.antublue.verifyica.api.Verifyica;
import org.antublue.verifyica.api.engine.EngineExtension;
import org.antublue.verifyica.api.engine.EngineExtensionContext;
import org.antublue.verifyica.api.engine.ExtensionResult;
import org.antublue.verifyica.engine.configuration.Constants;
import org.antublue.verifyica.engine.extension.InternalEngineExtension;
import org.antublue.verifyica.engine.logger.Logger;
import org.antublue.verifyica.engine.logger.LoggerFactory;

/** Class to implement ShuffleTestClassesEngineExtension */
@InternalEngineExtension
@Verifyica.Order(order = Integer.MAX_VALUE)
public class ShuffleTestClassesEngineExtension implements EngineExtension {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(ShuffleTestClassesEngineExtension.class);

    @Override
    public ExtensionResult onTestClassDiscovery(
            EngineExtensionContext engineExtensionContext, List<Class<?>> testClasses) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("onTestClassDiscovery()");
        }

        if (Constants.TRUE.equals(
                engineExtensionContext
                        .getEngineContext()
                        .getConfiguration()
                        .getOrDefault(Constants.ENGINE_TEST_CLASS_SHUFFLE, Constants.FALSE))) {
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("shuffling test class order ...");
            }

            Collections.shuffle(testClasses);
        }

        if (LOGGER.isTraceEnabled()) {
            // Print all test classes that were discovered
            testClasses.forEach(testClass -> LOGGER.trace("test class [%s]", testClass.getName()));
        }

        return ExtensionResult.PROCEED;
    }
}
