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

package org.antublue.verifyica.engine.extension.internal.engine;

import java.util.Collections;
import java.util.List;
import org.antublue.verifyica.api.Verifyica;
import org.antublue.verifyica.api.extension.TestClassDefinition;
import org.antublue.verifyica.api.extension.engine.EngineExtensionContext;
import org.antublue.verifyica.engine.configuration.Constants;
import org.antublue.verifyica.engine.logger.Logger;
import org.antublue.verifyica.engine.logger.LoggerFactory;

/** Class to implement ShuffleTestClassesEngineExtension */
@Verifyica.Order(order = 1)
public class ShuffleTestClassesEngineExtension implements InternalEngineExtension {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(ShuffleTestClassesEngineExtension.class);

    /** Constructor */
    public ShuffleTestClassesEngineExtension() {
        // INTENTIONALLY BLANK
    }

    @Override
    public void onTestDiscovery(
            EngineExtensionContext engineExtensionContext,
            List<TestClassDefinition> testClassDefinitions) {
        LOGGER.trace("onTestDiscovery()");

        boolean shuffleTestClasses =
                engineExtensionContext
                        .getEngineContext()
                        .getConfiguration()
                        .getOptional(Constants.ENGINE_TEST_CLASS_SHUFFLE)
                        .map(Constants.TRUE::equals)
                        .orElse(false);

        if (shuffleTestClasses) {
            LOGGER.trace("shuffling test class order");
            Collections.shuffle(testClassDefinitions);
        }
    }
}
