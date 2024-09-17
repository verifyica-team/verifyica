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

package org.antublue.verifyica.engine;

import static java.lang.String.format;

import java.util.concurrent.ExecutorService;
import org.antublue.verifyica.api.Configuration;
import org.antublue.verifyica.api.EngineContext;
import org.antublue.verifyica.api.interceptor.engine.EngineInterceptorContext;
import org.antublue.verifyica.engine.common.FairExecutorService;
import org.antublue.verifyica.engine.common.Precondition;
import org.antublue.verifyica.engine.configuration.ConcreteConfiguration;
import org.antublue.verifyica.engine.configuration.Constants;
import org.antublue.verifyica.engine.context.ConcreteEngineContext;
import org.antublue.verifyica.engine.context.ConcreteEngineInterceptorContext;
import org.antublue.verifyica.engine.exception.EngineConfigurationException;
import org.antublue.verifyica.engine.interceptor.ClassInterceptorManager;
import org.antublue.verifyica.engine.interceptor.EngineInterceptorManager;
import org.antublue.verifyica.engine.logger.Logger;
import org.antublue.verifyica.engine.logger.LoggerFactory;
import org.antublue.verifyica.engine.support.ExecutorSupport;
import org.junit.platform.engine.EngineExecutionListener;

/** Class to implement ExecutionContext */
public class ExecutionContext {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExecutionContext.class);

    private final Configuration configuration;

    private final EngineInterceptorManager engineInterceptorManager;

    private final ClassInterceptorManager classInterceptorManager;

    private final EngineContext engineContext;

    private final EngineInterceptorContext engineInterceptorContext;

    private final ExecutorService classExecutorService;

    private final ExecutorService argumentExecutorService;

    private EngineExecutionListener engineExecutionListener;

    /** Constructor */
    ExecutionContext() {
        configuration = ConcreteConfiguration.getInstance();

        engineInterceptorManager = new EngineInterceptorManager();

        engineContext =
                new ConcreteEngineContext(configuration, VerifyicaTestEngine.staticGetVersion());

        engineInterceptorContext = new ConcreteEngineInterceptorContext(engineContext);

        classInterceptorManager = new ClassInterceptorManager(engineInterceptorContext);

        classExecutorService =
                ExecutorSupport.newExecutorService(getEngineClassParallelism(configuration));

        argumentExecutorService =
                new FairExecutorService(getEngineArgumentParallelism(configuration));
    }

    /**
     * Method to get Configuration
     *
     * @return the Configuration
     */
    public Configuration getConfiguration() {
        return configuration;
    }

    /**
     * Method to get the EngineInterceptorManager
     *
     * @return the EngineInterceptorManager
     */
    public EngineInterceptorManager getEngineInterceptorManager() {
        return engineInterceptorManager;
    }

    /**
     * Method to get the ClassInterceptorManager
     *
     * @return the ClassInterceptorManager
     */
    public ClassInterceptorManager getClassInterceptorManager() {
        return classInterceptorManager;
    }

    /**
     * Method to get the ClassExecutorService
     *
     * @return the ClassExecutorService
     */
    public ExecutorService getClassExecutorService() {
        return classExecutorService;
    }

    /**
     * Method to get the ArgumentExecutorService
     *
     * @return the ArgumentExecutorService
     */
    public ExecutorService getArgumentExecutorService() {
        return argumentExecutorService;
    }

    /**
     * Method to get the EngineInterceptorContext
     *
     * @return the EngineInterceptorContext
     */
    public EngineInterceptorContext getEngineInterceptorContext() {
        return engineInterceptorContext;
    }

    /**
     * Method to get the EngineContext
     *
     * @return the EngineContext
     */
    public EngineContext getEngineContext() {
        return engineContext;
    }

    /**
     * Method to set the EngineExecutionListener
     *
     * @param engineExecutionListener engineExecutionListener
     */
    public void setEngineExecutionListener(EngineExecutionListener engineExecutionListener) {
        Precondition.notNull(engineExecutionListener, "engineExecutionListener is null");

        this.engineExecutionListener = engineExecutionListener;
    }

    /**
     * Method to get the EngineExecutionListener
     *
     * @return the EngineExecutionListener
     */
    public EngineExecutionListener getEngineExecutionListener() {
        return engineExecutionListener;
    }

    /**
     * Method to get the engine class parallelism configuration value
     *
     * @return the engine parallelism value
     */
    private static int getEngineClassParallelism(Configuration configuration) {
        LOGGER.trace("getEngineClassParallelism()");

        int engineClassParallelism =
                configuration
                        .getOptional(Constants.ENGINE_CLASS_PARALLELISM)
                        .map(
                                value -> {
                                    int intValue;
                                    try {
                                        intValue = Integer.parseInt(value);
                                        if (intValue < 1) {
                                            throw new EngineConfigurationException(
                                                    format(
                                                            "Invalid %s value [%d]",
                                                            Constants.ENGINE_CLASS_PARALLELISM,
                                                            intValue));
                                        }
                                        return intValue;
                                    } catch (NumberFormatException e) {
                                        throw new EngineConfigurationException(
                                                format(
                                                        "Invalid %s value [%s]",
                                                        Constants.ENGINE_CLASS_PARALLELISM, value),
                                                e);
                                    }
                                })
                        .orElse(Runtime.getRuntime().availableProcessors());

        LOGGER.trace("engineClassParallelism [%d]", engineClassParallelism);

        return engineClassParallelism;
    }

    /**
     * Method to get the engine argument parallelism configuration value
     *
     * @return the engine parallelism value
     */
    private static int getEngineArgumentParallelism(Configuration configuration) {
        LOGGER.trace("getEngineArgumentParallelism()");

        int engineClassParallelism = getEngineClassParallelism(configuration);

        int engineArgumentParallelism =
                configuration
                        .getOptional(Constants.ENGINE_ARGUMENT_PARALLELISM)
                        .map(
                                value -> {
                                    int intValue;
                                    try {
                                        intValue = Integer.parseInt(value);
                                        if (intValue < 1) {
                                            throw new EngineConfigurationException(
                                                    format(
                                                            "Invalid %s value [%d]",
                                                            Constants.ENGINE_ARGUMENT_PARALLELISM,
                                                            intValue));
                                        }
                                        return intValue;
                                    } catch (NumberFormatException e) {
                                        throw new EngineConfigurationException(
                                                format(
                                                        "Invalid %s value [%s]",
                                                        Constants.ENGINE_ARGUMENT_PARALLELISM,
                                                        value),
                                                e);
                                    }
                                })
                        .orElse(engineClassParallelism);

        if (engineArgumentParallelism < engineClassParallelism) {
            LOGGER.warn(
                    "[%s] is less than [%s], setting [%s] to [%d]",
                    Constants.ENGINE_ARGUMENT_PARALLELISM,
                    Constants.ENGINE_CLASS_PARALLELISM,
                    Constants.ENGINE_ARGUMENT_PARALLELISM,
                    engineClassParallelism);

            engineArgumentParallelism = engineClassParallelism;
        }

        LOGGER.trace("engineArgumentParallelism [%d]", engineArgumentParallelism);

        return engineArgumentParallelism;
    }
}
