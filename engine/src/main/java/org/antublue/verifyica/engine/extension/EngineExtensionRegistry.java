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

package org.antublue.verifyica.engine.extension;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.antublue.verifyica.api.engine.EngineExtension;
import org.antublue.verifyica.api.engine.EngineExtensionContext;
import org.antublue.verifyica.api.engine.ExtensionResult;
import org.antublue.verifyica.engine.configuration.Constants;
import org.antublue.verifyica.engine.context.DefaultEngineContext;
import org.antublue.verifyica.engine.discovery.Predicates;
import org.antublue.verifyica.engine.exception.EngineException;
import org.antublue.verifyica.engine.logger.Logger;
import org.antublue.verifyica.engine.logger.LoggerFactory;
import org.antublue.verifyica.engine.support.ClassPathSupport;
import org.antublue.verifyica.engine.support.ObjectSupport;
import org.antublue.verifyica.engine.support.OrderSupport;

/** Class to implement EngineExtensionRegistry */
public class EngineExtensionRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(EngineExtensionRegistry.class);

    private static final DefaultEngineContext DEFAULT_ENGINE_CONTEXT =
            DefaultEngineContext.getInstance();

    private final Lock lock;
    private final List<EngineExtension> engineExtensions;
    private boolean initialized;

    /** Constructor */
    private EngineExtensionRegistry() {
        lock = new ReentrantLock(true);
        engineExtensions = new ArrayList<>();
    }

    /**
     * Method to invoke engine extensions
     *
     * @param engineExtensionContext engineExtensionContext
     * @return the ExtensionResult
     * @throws Throwable Throwable
     */
    public ExtensionResult onInitialize(EngineExtensionContext engineExtensionContext)
            throws Throwable {
        load();

        ExtensionResult extensionResult = ExtensionResult.PROCEED;

        for (EngineExtension engineExtension : engineExtensions) {
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace(
                        "engine extension [%s] onInitialize()",
                        engineExtension.getClass().getName());
            }

            extensionResult = engineExtension.onInitialize(engineExtensionContext);

            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace(
                        "engine extension [%s] onInitialize() result [%s]",
                        engineExtension.getClass().getName(), extensionResult);
            }

            if (extensionResult != ExtensionResult.PROCEED) {
                break;
            }
        }

        return extensionResult;
    }

    /**
     * Method to invoke engine extensions
     *
     * @param engineExtensionContext engineExtensionContext
     * @param testClasses testClasses
     * @return the ExtensionResult
     * @throws Throwable Throwable
     */
    public ExtensionResult onTestClassDiscovery(
            EngineExtensionContext engineExtensionContext, List<Class<?>> testClasses)
            throws Throwable {
        load();

        ExtensionResult extensionResult = ExtensionResult.PROCEED;

        for (EngineExtension engineExtension : engineExtensions) {
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace(
                        "engine extension [%s] onTestClassDiscovery()",
                        engineExtension.getClass().getName());
            }

            extensionResult =
                    engineExtension.onTestClassDiscovery(engineExtensionContext, testClasses);

            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace(
                        "engine extension [%s] onTestClassDiscovery() result [%s]",
                        engineExtension.getClass().getName(), extensionResult);
            }

            if (extensionResult != ExtensionResult.PROCEED) {
                break;
            }
        }

        return extensionResult;
    }

    /**
     * Method to invoke engine extensions
     *
     * @param engineExtensionContext engineExtensionContext
     * @param testClass testClass
     * @param testMethods testMethods
     * @return the ExtensionResult
     * @throws Throwable Throwable
     */
    public ExtensionResult onTestClassTestClassMethodDiscovery(
            EngineExtensionContext engineExtensionContext,
            Class<?> testClass,
            List<Method> testMethods)
            throws Throwable {
        load();

        ExtensionResult extensionResult = ExtensionResult.PROCEED;

        for (EngineExtension engineExtension : engineExtensions) {
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace(
                        "engine extension [%s] onTestClassTestClassMethodDiscovery()",
                        engineExtension.getClass().getName());
            }

            extensionResult =
                    engineExtension.onTestClassTestMethodDiscovery(
                            engineExtensionContext, testClass, testMethods);

            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace(
                        "engine extension [%s] onTestClassTestClassMethodDiscovery() result"
                                + " [%s]",
                        engineExtension.getClass().getName(), extensionResult);
            }

            if (extensionResult != ExtensionResult.PROCEED) {
                break;
            }
        }

        return extensionResult;
    }

    /**
     * Method to invoke engine extensions
     *
     * @param engineExtensionContext engineExtensionContext
     * @return the ExtensionResult
     * @throws Throwable Throwable
     */
    public ExtensionResult onExecute(EngineExtensionContext engineExtensionContext)
            throws Throwable {
        ExtensionResult extensionResult = ExtensionResult.PROCEED;

        for (EngineExtension engineExtension : engineExtensions) {
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace(
                        "engine extension [%s] onExecute()", engineExtension.getClass().getName());
            }

            extensionResult = engineExtension.onExecute(engineExtensionContext);

            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace(
                        "engine extension [%s] onExecute() result [%s]",
                        engineExtension.getClass().getName(), extensionResult);
            }

            if (extensionResult != ExtensionResult.PROCEED) {
                break;
            }
        }

        return extensionResult;
    }

    /**
     * Method to invoke all engine extensions (capturing any Throwable exceptions)
     *
     * @param engineExtensionContext engineExtensionContext
     * @return a List of Throwables
     */
    public List<Throwable> onDestroy(EngineExtensionContext engineExtensionContext) {
        load();

        List<Throwable> throwables = new ArrayList<>();

        List<EngineExtension> engineExtensions = new ArrayList<>(this.engineExtensions);
        Collections.reverse(engineExtensions);

        for (EngineExtension engineExtension : engineExtensions) {
            try {
                if (LOGGER.isTraceEnabled()) {
                    LOGGER.trace(
                            "engine extension [%s] onDestroy()",
                            engineExtension.getClass().getName());
                }
                engineExtension.onDestroy(engineExtensionContext);
            } catch (Throwable t) {
                throwables.add(t);
            }
        }

        return throwables;
    }

    /**
     * Method to get a singleton instance
     *
     * @return the singleton instance
     */
    public static EngineExtensionRegistry getInstance() {
        return SingletonHolder.SINGLETON;
    }

    /**
     * Method to filter engine extensions
     *
     * @param classes classes
     */
    private static void filter(List<Class<?>> classes) {
        Optional.ofNullable(
                        DEFAULT_ENGINE_CONTEXT
                                .getConfiguration()
                                .get(Constants.ENGINE_EXTENSIONS_INCLUDE_REGEX))
                .ifPresent(
                        regex -> {
                            if (LOGGER.isTraceEnabled()) {
                                LOGGER.trace(
                                        "%s [%s]",
                                        Constants.ENGINE_EXTENSIONS_INCLUDE_REGEX, regex);
                            }

                            Pattern pattern = Pattern.compile(regex);
                            Matcher matcher = pattern.matcher("");

                            Iterator<Class<?>> iterator = classes.iterator();
                            while (iterator.hasNext()) {
                                Class<?> clazz = iterator.next();
                                matcher.reset(clazz.getName());
                                if (!matcher.find()) {
                                    if (LOGGER.isTraceEnabled()) {
                                        LOGGER.trace(
                                                "removing engine extension [%s]", clazz.getName());
                                    }
                                    iterator.remove();
                                }
                            }
                        });

        Optional.ofNullable(
                        DEFAULT_ENGINE_CONTEXT
                                .getConfiguration()
                                .get(Constants.ENGINE_EXTENSIONS_EXCLUDE_REGEX))
                .ifPresent(
                        regex -> {
                            if (LOGGER.isTraceEnabled()) {
                                LOGGER.trace(
                                        "%s [%s]",
                                        Constants.ENGINE_EXTENSIONS_EXCLUDE_REGEX, regex);
                            }

                            Pattern pattern = Pattern.compile(regex);
                            Matcher matcher = pattern.matcher("");

                            Iterator<Class<?>> iterator = classes.iterator();
                            while (iterator.hasNext()) {
                                Class<?> clazz = iterator.next();
                                matcher.reset(clazz.getName());
                                if (matcher.find()) {
                                    if (LOGGER.isTraceEnabled()) {
                                        LOGGER.trace(
                                                "removing engine extension [%s]", clazz.getName());
                                    }
                                    iterator.remove();
                                }
                            }
                        });
    }

    /** Method to load test engine extensions */
    private void load() {
        try {
            lock.lock();

            if (!initialized) {
                if (LOGGER.isTraceEnabled()) {
                    LOGGER.trace("load()");
                }

                // Load internal engine extensions
                List<Class<?>> internalEngineExtensionsClasses =
                        new ArrayList<>(
                                ClassPathSupport.findClasses(
                                        Predicates.ENGINE_INTERNAL_EXTENSION_CLASS));

                // Load external engine extensions
                List<Class<?>> externalEngineExtensionsClasses =
                        new ArrayList<>(
                                ClassPathSupport.findClasses(
                                        Predicates.ENGINE_EXTERNAL_EXTENSION_CLASS));

                // Filter external engine extensions
                filter(externalEngineExtensionsClasses);

                // Order engine extensions
                OrderSupport.order(internalEngineExtensionsClasses);
                OrderSupport.order(externalEngineExtensionsClasses);

                // Combine both internal and external engine extensions
                List<Class<?>> engineExtensionClasses =
                        new ArrayList<>(internalEngineExtensionsClasses);
                engineExtensionClasses.addAll(externalEngineExtensionsClasses);

                if (LOGGER.isTraceEnabled()) {
                    LOGGER.trace("engine extension count [%d]", engineExtensions.size());
                }

                for (Class<?> engineExtensionClass : engineExtensionClasses) {
                    if (LOGGER.isTraceEnabled()) {
                        engineExtensions.forEach(
                                engineExtension ->
                                        LOGGER.trace(
                                                "engine extension [%s]",
                                                engineExtension.getClass().getName()));
                    }

                    try {
                        Object object = ObjectSupport.createObject(engineExtensionClass);
                        engineExtensions.add((EngineExtension) object);
                    } catch (EngineException e) {
                        throw e;
                    } catch (Throwable t) {
                        throw new EngineException(t);
                    }
                }

                initialized = true;
            }
        } finally {
            lock.unlock();
        }
    }

    /** Class to hold the singleton instance */
    private static class SingletonHolder {

        /** The singleton instance */
        private static final EngineExtensionRegistry SINGLETON = new EngineExtensionRegistry();
    }
}
