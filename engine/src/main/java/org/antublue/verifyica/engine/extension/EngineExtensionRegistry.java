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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.antublue.verifyica.api.extension.engine.ClassDefinition;
import org.antublue.verifyica.api.extension.engine.EngineExtension;
import org.antublue.verifyica.api.extension.engine.EngineExtensionContext;
import org.antublue.verifyica.engine.configuration.Constants;
import org.antublue.verifyica.engine.context.DefaultEngineContext;
import org.antublue.verifyica.engine.discovery.Predicates;
import org.antublue.verifyica.engine.exception.EngineException;
import org.antublue.verifyica.engine.logger.Logger;
import org.antublue.verifyica.engine.logger.LoggerFactory;
import org.antublue.verifyica.engine.support.ArgumentSupport;
import org.antublue.verifyica.engine.support.ClassPathSupport;
import org.antublue.verifyica.engine.support.ObjectSupport;
import org.antublue.verifyica.engine.support.OrderSupport;

/** Class to implement EngineExtensionRegistry */
@SuppressWarnings("PMD.EmptyCatchBlock")
public class EngineExtensionRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(EngineExtensionRegistry.class);

    private static final DefaultEngineContext DEFAULT_ENGINE_CONTEXT =
            DefaultEngineContext.getInstance();

    private final ReentrantReadWriteLock readWriteLock;
    private final List<EngineExtension> engineExtensions;
    private boolean initialized;

    /** Constructor */
    private EngineExtensionRegistry() {
        readWriteLock = new ReentrantReadWriteLock(true);
        engineExtensions = new ArrayList<>();

        loadEngineExtensions();
    }

    /**
     * Method to register an engine extension
     *
     * @param engineExtension engineExtension
     * @return this EngineExtensionRegistry
     */
    public EngineExtensionRegistry register(EngineExtension engineExtension) {
        ArgumentSupport.notNull(engineExtension, "engineExtension is null");

        try {
            getReadWriteLock().writeLock().lock();
            engineExtensions.add(engineExtension);
        } finally {
            getReadWriteLock().writeLock().unlock();
        }

        return this;
    }

    /**
     * Method to unregister an engine extension
     *
     * @param engineExtension engineExtension
     * @return this EngineExtensionRegistry
     */
    public EngineExtensionRegistry unregister(EngineExtension engineExtension) {
        ArgumentSupport.notNull(engineExtension, "testClass is null");

        try {
            getReadWriteLock().writeLock().lock();
            engineExtensions.remove(engineExtension);
        } finally {
            getReadWriteLock().writeLock().unlock();
        }

        return this;
    }

    /**
     * Method to get the number of engine extensions
     *
     * @return the number of class extensions
     */
    public int size() {
        try {
            getReadWriteLock().readLock().lock();
            return engineExtensions.size();
        } finally {
            getReadWriteLock().readLock().unlock();
        }
    }

    /**
     * Method to call engine extensions
     *
     * @param engineExtensionContext engineExtensionContext
     * @throws Throwable Throwable
     */
    public void onInitialize(EngineExtensionContext engineExtensionContext) throws Throwable {
        ArgumentSupport.notNull(engineExtensionContext, "engineExtensionContext is null");

        for (EngineExtension engineExtension : getEngineExtensions()) {
            LOGGER.trace(
                    "engine extension [%s] onInitialize()", engineExtension.getClass().getName());

            engineExtension.onInitialize(engineExtensionContext);

            LOGGER.trace(
                    "engine extension [%s] onInitialize() success",
                    engineExtension.getClass().getName());
        }
    }

    /**
     * Method to call engine extensions
     *
     * @param engineExtensionContext engineExtensionContext
     * @param classDefinitions classDefinitions
     * @throws Throwable Throwable
     */
    public void onTestDiscovery(
            EngineExtensionContext engineExtensionContext, List<ClassDefinition> classDefinitions)
            throws Throwable {
        ArgumentSupport.notNull(engineExtensionContext, "engineExtensionContext is null");
        ArgumentSupport.notNull(classDefinitions, "classDefinitions is null");

        for (EngineExtension engineExtension : getEngineExtensions()) {
            LOGGER.trace(
                    "engine extension [%s] onTestDiscovery()",
                    engineExtension.getClass().getName());

            engineExtension.onTestDiscovery(engineExtensionContext, classDefinitions);

            LOGGER.trace(
                    "engine extension [%s] onTestDiscovery()" + " success",
                    engineExtension.getClass().getName());
        }
    }

    /**
     * Method to call engine extensions
     *
     * @param engineExtensionContext engineExtensionContext
     * @param classDefinition classDefinition
     * @throws Throwable Throwable
     */
    public void onTestDiscovery(
            EngineExtensionContext engineExtensionContext, ClassDefinition classDefinition)
            throws Throwable {
        ArgumentSupport.notNull(engineExtensionContext, "engineExtensionContext is null");
        ArgumentSupport.notNull(classDefinition, "classDefinition is null");

        for (EngineExtension engineExtension : getEngineExtensions()) {
            LOGGER.trace(
                    "engine extension [%s] onTestDiscovery() classDefinition testClass [%s]",
                    engineExtension.getClass().getName(), classDefinition.getTestClass().getName());

            engineExtension.onTestDiscovery(engineExtensionContext, classDefinition);

            LOGGER.trace(
                    "engine extension [%s] onTestDiscovery() classDefinition testClass [%s]"
                            + " success",
                    engineExtension.getClass().getName(), classDefinition.getTestClass().getName());
        }
    }

    /**
     * Method to call engine extension
     *
     * @param engineExtensionContext engineExtensionContext
     * @throws Throwable Throwable
     */
    public void beforeExecute(EngineExtensionContext engineExtensionContext) throws Throwable {
        ArgumentSupport.notNull(engineExtensionContext, "engineExtensionContext is null");

        for (EngineExtension engineExtension : getEngineExtensions()) {
            LOGGER.trace(
                    "engine extension [%s] beforeExecute()", engineExtension.getClass().getName());

            engineExtension.beforeExecute(engineExtensionContext);

            LOGGER.trace(
                    "engine extension [%s] beforeExecute() success",
                    engineExtension.getClass().getName());
        }
    }

    /**
     * Method to call engine extension
     *
     * @param engineExtensionContext engineExtensionContext
     * @throws Throwable Throwable
     */
    public void afterExecute(EngineExtensionContext engineExtensionContext) throws Throwable {
        ArgumentSupport.notNull(engineExtensionContext, "engineExtensionContext is null");

        for (EngineExtension engineExtension : getEngineExtensionsReverse()) {
            LOGGER.trace(
                    "engine extension [%s] beforeDestroy()", engineExtension.getClass().getName());

            engineExtension.afterExecute(engineExtensionContext);
        }
    }

    /**
     * Method to get a COPY of the List of EngineExtensions
     *
     * @return a COPY of the List of EngineExtensions
     */
    private List<EngineExtension> getEngineExtensions() {
        try {
            getReadWriteLock().readLock().lock();
            return new ArrayList<>(engineExtensions);
        } finally {
            getReadWriteLock().readLock().unlock();
        }
    }

    /**
     * Method to get a COPY of the List of EngineExtensions in reverse
     *
     * @return a COPY of the List of EngineExtensions in reverse
     */
    private List<EngineExtension> getEngineExtensionsReverse() {
        try {
            getReadWriteLock().readLock().lock();
            List<EngineExtension> list = new ArrayList<>(engineExtensions);
            Collections.reverse(list);
            return list;
        } finally {
            getReadWriteLock().readLock().unlock();
        }
    }

    /**
     * Method to get the ReadWriteLock
     *
     * @return the ReadWriteLock
     */
    private ReadWriteLock getReadWriteLock() {
        return readWriteLock;
    }

    /** Method to load test engine extensions */
    private void loadEngineExtensions() {
        try {
            getReadWriteLock().writeLock().lock();

            if (!initialized) {
                LOGGER.trace("loading engine extensions");

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
                OrderSupport.orderClasses(internalEngineExtensionsClasses);
                OrderSupport.orderClasses(externalEngineExtensionsClasses);

                // Combine both internal and external engine extensions
                List<Class<?>> engineExtensionClasses =
                        new ArrayList<>(internalEngineExtensionsClasses);

                engineExtensionClasses.addAll(externalEngineExtensionsClasses);

                LOGGER.trace("engine extension count [%d]", engineExtensions.size());

                for (Class<?> engineExtensionClass : engineExtensionClasses) {
                    try {
                        LOGGER.trace(
                                "loading engine extension [%s]", engineExtensionClass.getName());

                        Object object = ObjectSupport.createObject(engineExtensionClass);
                        engineExtensions.add((EngineExtension) object);

                        LOGGER.trace(
                                "engine extension [%s] loaded", engineExtensionClass.getName());
                    } catch (EngineException e) {
                        throw e;
                    } catch (Throwable t) {
                        throw new EngineException(t);
                    }
                }

                initialized = true;
            }
        } finally {
            getReadWriteLock().writeLock().unlock();
        }
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
        DEFAULT_ENGINE_CONTEXT
                .getConfiguration()
                .getOptional(Constants.ENGINE_EXTENSIONS_INCLUDE_REGEX)
                .ifPresent(
                        regex -> {
                            LOGGER.trace(
                                    "%s [%s]", Constants.ENGINE_EXTENSIONS_INCLUDE_REGEX, regex);

                            Pattern pattern = Pattern.compile(regex);
                            Matcher matcher = pattern.matcher("");

                            Iterator<Class<?>> iterator = classes.iterator();
                            while (iterator.hasNext()) {
                                Class<?> clazz = iterator.next();
                                matcher.reset(clazz.getName());
                                if (!matcher.find()) {
                                    LOGGER.trace("removing engine extension [%s]", clazz.getName());
                                    iterator.remove();
                                }
                            }
                        });

        DEFAULT_ENGINE_CONTEXT
                .getConfiguration()
                .getOptional(Constants.ENGINE_EXTENSIONS_EXCLUDE_REGEX)
                .ifPresent(
                        regex -> {
                            LOGGER.trace(
                                    "%s [%s]", Constants.ENGINE_EXTENSIONS_EXCLUDE_REGEX, regex);

                            Pattern pattern = Pattern.compile(regex);
                            Matcher matcher = pattern.matcher("");

                            Iterator<Class<?>> iterator = classes.iterator();
                            while (iterator.hasNext()) {
                                Class<?> clazz = iterator.next();
                                matcher.reset(clazz.getName());
                                if (matcher.find()) {
                                    LOGGER.trace("removing engine extension [%s]", clazz.getName());

                                    iterator.remove();
                                }
                            }
                        });
    }

    /** Class to hold the singleton instance */
    private static class SingletonHolder {

        /** The singleton instance */
        private static final EngineExtensionRegistry SINGLETON = new EngineExtensionRegistry();
    }
}
