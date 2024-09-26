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

package org.verifyica.engine.context;

import org.verifyica.api.Configuration;
import org.verifyica.api.EngineContext;
import org.verifyica.api.Store;
import org.verifyica.engine.common.Precondition;

/** Class to implement ConcreteEngineContext */
public class ConcreteEngineContext implements EngineContext {

    private final Configuration configuration;
    private final String version;
    private final Store store;

    /**
     * Constructor
     *
     * @param configuration configuration
     * @param version version
     */
    public ConcreteEngineContext(Configuration configuration, String version) {
        Precondition.notNull(configuration, "configuration is null");
        Precondition.notNull(version, "version is null");

        this.configuration = configuration;
        this.version = version;
        this.store = new ConcreteStore();
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public Configuration getConfiguration() {
        return configuration;
    }

    @Override
    public Store getStore() {
        return store;
    }

    @Override
    public String toString() {
        return "ConcreteEngineContext{"
                + "configuration="
                + configuration
                + ", store="
                + store
                + '}';
    }
}
