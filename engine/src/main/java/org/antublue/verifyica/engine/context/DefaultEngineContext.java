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
import org.antublue.verifyica.engine.Version;
import org.antublue.verifyica.engine.configuration.DefaultConfiguration;

/** Class to implement DefaultConfiguration */
public class DefaultEngineContext implements EngineContext {

    // private final Map<String, String> map;
    private final Configuration configuration;
    private final Store store;

    /** Constructor */
    private DefaultEngineContext() {
        configuration = new DefaultConfiguration();
        store = new DefaultStore();
    }

    @Override
    public String getVersion() {
        return Version.version();
    }

    @Override
    public Configuration getConfiguration() {
        return configuration;
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
    public static DefaultEngineContext getInstance() {
        return SingletonHolder.SINGLETON;
    }

    /** Class to hold the singleton instance */
    private static class SingletonHolder {

        /** The singleton instance */
        private static final DefaultEngineContext SINGLETON = new DefaultEngineContext();
    }
}
