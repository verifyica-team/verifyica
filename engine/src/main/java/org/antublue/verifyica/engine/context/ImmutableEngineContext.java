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

package org.antublue.verifyica.engine.context;

import org.antublue.verifyica.api.Configuration;
import org.antublue.verifyica.api.EngineContext;
import org.antublue.verifyica.api.Store;
import org.antublue.verifyica.engine.configuration.DefaultConfiguration;

/** Class to implement ImmutableEngineContext */
public class ImmutableEngineContext implements EngineContext {

    private static final Configuration CONFIGURATION = DefaultConfiguration.getInstance();

    private final Store store;

    /** Constructor */
    private ImmutableEngineContext() {
        store = new Store();
    }

    @Override
    public Configuration getConfiguration() {
        return CONFIGURATION;
    }

    @Override
    public Store getStore() {
        return store;
    }

    /**
     * Method to get the singleton instance
     *
     * @return the singleton instance
     */
    public static ImmutableEngineContext getInstance() {
        return SingletonHolder.SINGLETON;
    }

    /** Class to hold the singleton instance */
    private static class SingletonHolder {

        /** The singleton instance */
        private static final ImmutableEngineContext SINGLETON = new ImmutableEngineContext();
    }
}