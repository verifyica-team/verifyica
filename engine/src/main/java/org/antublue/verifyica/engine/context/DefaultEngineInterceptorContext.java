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
import org.antublue.verifyica.api.interceptor.EngineInterceptorContext;
import org.antublue.verifyica.engine.configuration.DefaultConfiguration;
import org.junit.platform.commons.util.Preconditions;

/** Class to implement DefaultEngineInterceptorContext */
public class DefaultEngineInterceptorContext implements EngineInterceptorContext {

    private static final Configuration CONFIGURATION =
            DefaultConfiguration.getInstance().asImmutable();

    private final EngineContext engineContext;
    private final EngineInterceptorContext immutableEngineInterceptorContext;

    private EngineInterceptorContext.LifeCycle lifeCycle;

    /**
     * Constructor
     *
     * @param engineContext engineContext
     */
    public DefaultEngineInterceptorContext(EngineContext engineContext) {
        Preconditions.notNull(engineContext, "engineContext is null");

        this.engineContext = engineContext;
        this.immutableEngineInterceptorContext = new ImmutableEngineInterceptorContext(this);
    }

    @Override
    public Configuration getConfiguration() {
        return CONFIGURATION;
    }

    /**
     * Method to set the LifeCycle
     *
     * @param lifeCycle lifeCycle
     * @return DefaultEngineInterceptorContext DefaultEngineInterceptorContext
     */
    public DefaultEngineInterceptorContext setLifeCycle(
            EngineInterceptorContext.LifeCycle lifeCycle) {
        this.lifeCycle = lifeCycle;
        return this;
    }

    @Override
    public EngineInterceptorContext.LifeCycle getLifeCycle() {
        return lifeCycle;
    }

    @Override
    public EngineContext getEngineContext() {
        return engineContext;
    }

    /**
     * Method to get an immutable version
     *
     * @return an immutable version
     */
    public EngineInterceptorContext asImmutable() {
        return immutableEngineInterceptorContext;
    }
}
