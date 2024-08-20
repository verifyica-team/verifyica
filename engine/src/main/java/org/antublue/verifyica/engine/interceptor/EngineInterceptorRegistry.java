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

package org.antublue.verifyica.engine.interceptor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.antublue.verifyica.api.interceptor.engine.ClassDefinition;
import org.antublue.verifyica.api.interceptor.engine.EngineInterceptor;
import org.antublue.verifyica.api.interceptor.engine.EngineInterceptorContext;
import org.antublue.verifyica.engine.configuration.Constants;
import org.antublue.verifyica.engine.configuration.DefaultConfiguration;
import org.antublue.verifyica.engine.discovery.Predicates;
import org.antublue.verifyica.engine.exception.EngineException;
import org.antublue.verifyica.engine.interceptor.internal.engine.filter.EngineFiltersInterceptor;
import org.antublue.verifyica.engine.logger.Logger;
import org.antublue.verifyica.engine.logger.LoggerFactory;
import org.antublue.verifyica.engine.support.ArgumentSupport;
import org.antublue.verifyica.engine.support.ClassPathSupport;
import org.antublue.verifyica.engine.support.ObjectSupport;
import org.antublue.verifyica.engine.support.OrderSupport;

/** Class to implement EngineInterceptorRegistry */
@SuppressWarnings("PMD.EmptyCatchBlock")
public class EngineInterceptorRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(EngineInterceptorRegistry.class);

    private final ReentrantReadWriteLock readWriteLock;
    private final List<EngineInterceptor> engineInterceptors;
    private boolean initialized;

    /** Constructor */
    private EngineInterceptorRegistry() {
        readWriteLock = new ReentrantReadWriteLock(true);
        engineInterceptors = new ArrayList<>();

        engineInterceptors.add(new EngineFiltersInterceptor());

        loadEngineInterceptors();
    }

    /**
     * Method to register an engine interceptor
     *
     * @param engineInterceptor engineInterceptor
     * @return this EngineInterceptorRegistry
     */
    public EngineInterceptorRegistry register(EngineInterceptor engineInterceptor) {
        ArgumentSupport.notNull(engineInterceptor, "engineInterceptor is null");

        try {
            getReadWriteLock().writeLock().lock();
            engineInterceptors.add(engineInterceptor);
        } finally {
            getReadWriteLock().writeLock().unlock();
        }

        return this;
    }

    /**
     * Method to unregister an engine interceptor
     *
     * @param engineInterceptor engineInterceptor
     * @return this EngineInterceptorRegistry
     */
    public EngineInterceptorRegistry remove(EngineInterceptor engineInterceptor) {
        ArgumentSupport.notNull(engineInterceptor, "testClass is null");

        try {
            getReadWriteLock().writeLock().lock();
            engineInterceptors.remove(engineInterceptor);
        } finally {
            getReadWriteLock().writeLock().unlock();
        }

        return this;
    }

    /**
     * Method to get the number of engine interceptors
     *
     * @return the number of class interceptors
     */
    public int size() {
        try {
            getReadWriteLock().readLock().lock();
            return engineInterceptors.size();
        } finally {
            getReadWriteLock().readLock().unlock();
        }
    }

    /**
     * Method to call engine interceptors
     *
     * @param engineInterceptorContext engineInterceptorContext
     * @throws Throwable Throwable
     */
    public void onInitialize(EngineInterceptorContext engineInterceptorContext) throws Throwable {
        ArgumentSupport.notNull(engineInterceptorContext, "engineInterceptorContext is null");

        for (EngineInterceptor engineInterceptor : getEngineInterceptors()) {
            LOGGER.trace(
                    "engine interceptor [%s] onInitialize()",
                    engineInterceptor.getClass().getName());

            engineInterceptor.onInitialize(engineInterceptorContext);

            LOGGER.trace(
                    "engine interceptor [%s] onInitialize() success",
                    engineInterceptor.getClass().getName());
        }
    }

    /**
     * Method to call engine interceptors
     *
     * @param engineInterceptorContext engineInterceptorContext
     * @param classDefinitions classDefinitions
     * @throws Throwable Throwable
     */
    public void onTestDiscovery(
            EngineInterceptorContext engineInterceptorContext,
            List<ClassDefinition> classDefinitions)
            throws Throwable {
        ArgumentSupport.notNull(engineInterceptorContext, "engineInterceptorContext is null");
        ArgumentSupport.notNull(classDefinitions, "classDefinitions is null");

        for (EngineInterceptor engineInterceptor : getEngineInterceptors()) {
            LOGGER.trace(
                    "engine interceptor [%s] onTestDiscovery()",
                    engineInterceptor.getClass().getName());

            engineInterceptor.onTestDiscovery(engineInterceptorContext, classDefinitions);

            LOGGER.trace(
                    "engine interceptor [%s] onTestDiscovery()" + " success",
                    engineInterceptor.getClass().getName());
        }
    }

    /**
     * Method to call engine interceptors
     *
     * @param engineInterceptorContext engineInterceptorContext
     * @param classDefinition classDefinition
     * @throws Throwable Throwable
     */
    public void onTestDiscovery(
            EngineInterceptorContext engineInterceptorContext, ClassDefinition classDefinition)
            throws Throwable {
        ArgumentSupport.notNull(engineInterceptorContext, "engineInterceptorContext is null");
        ArgumentSupport.notNull(classDefinition, "classDefinition is null");

        for (EngineInterceptor engineInterceptor : getEngineInterceptors()) {
            LOGGER.trace(
                    "engine interceptor [%s] onTestDiscovery() classDefinition testClass [%s]",
                    engineInterceptor.getClass().getName(),
                    classDefinition.getTestClass().getName());

            engineInterceptor.onTestDiscovery(engineInterceptorContext, classDefinition);

            LOGGER.trace(
                    "engine interceptor [%s] onTestDiscovery() classDefinition testClass [%s]"
                            + " success",
                    engineInterceptor.getClass().getName(),
                    classDefinition.getTestClass().getName());
        }
    }

    /**
     * Method to call engine interceptors
     *
     * @param engineInterceptorContext engineInterceptorContext
     * @throws Throwable Throwable
     */
    public void beforeExecute(EngineInterceptorContext engineInterceptorContext) throws Throwable {
        ArgumentSupport.notNull(engineInterceptorContext, "engineInterceptorContext is null");

        for (EngineInterceptor engineInterceptor : getEngineInterceptors()) {
            LOGGER.trace(
                    "engine interceptor [%s] beforeExecute()",
                    engineInterceptor.getClass().getName());

            engineInterceptor.preExecute(engineInterceptorContext);

            LOGGER.trace(
                    "engine interceptor [%s] beforeExecute() success",
                    engineInterceptor.getClass().getName());
        }
    }

    /**
     * Method to call engine interceptors
     *
     * @param engineInterceptorContext engineInterceptorContext
     * @throws Throwable Throwable
     */
    public void afterExecute(EngineInterceptorContext engineInterceptorContext) throws Throwable {
        ArgumentSupport.notNull(engineInterceptorContext, "engineInterceptorContext is null");

        for (EngineInterceptor engineInterceptor : getEngineInterceptorsReverse()) {
            LOGGER.trace(
                    "engine interceptor [%s] beforeDestroy()",
                    engineInterceptor.getClass().getName());

            engineInterceptor.postExecute(engineInterceptorContext);
        }
    }

    /**
     * Method to get a COPY of the List of EngineInterceptors
     *
     * @return a COPY of the List of EngineInterceptors
     */
    private List<EngineInterceptor> getEngineInterceptors() {
        try {
            getReadWriteLock().readLock().lock();
            return new ArrayList<>(engineInterceptors);
        } finally {
            getReadWriteLock().readLock().unlock();
        }
    }

    /**
     * Method to get a COPY of the List of EngineInterceptors in reverse
     *
     * @return a COPY of the List of EngineInterceptors in reverse
     */
    private List<EngineInterceptor> getEngineInterceptorsReverse() {
        try {
            getReadWriteLock().readLock().lock();
            List<EngineInterceptor> list = new ArrayList<>(engineInterceptors);
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

    /** Method to load test engine interceptors */
    private void loadEngineInterceptors() {
        try {
            getReadWriteLock().writeLock().lock();

            if (!initialized) {
                LOGGER.trace("loading engine interceptors");

                List<Class<?>> autowiredEngineInterceptors =
                        new ArrayList<>(
                                ClassPathSupport.findAllClasses(
                                        Predicates.AUTOWIRED_ENGINE_INTERCEPTOR_CLASS));

                filter(autowiredEngineInterceptors);

                OrderSupport.orderClasses(autowiredEngineInterceptors);

                LOGGER.trace(
                        "autowired engine interceptor count [%d]",
                        autowiredEngineInterceptors.size());

                for (Class<?> engineInterceptorClass : autowiredEngineInterceptors) {
                    try {
                        LOGGER.trace(
                                "loading autowired engine interceptor [%s]",
                                engineInterceptorClass.getName());

                        Object object = ObjectSupport.createObject(engineInterceptorClass);
                        engineInterceptors.add((EngineInterceptor) object);

                        LOGGER.trace(
                                "autowired engine interceptor [%s] loaded",
                                engineInterceptorClass.getName());
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
    public static EngineInterceptorRegistry getInstance() {
        return SingletonHolder.SINGLETON;
    }

    /**
     * Method to filter engine interceptors
     *
     * @param classes classes
     */
    private static void filter(List<Class<?>> classes) {
        Set<Class<?>> filteredClasses = new LinkedHashSet<>(classes);

        DefaultConfiguration.getInstance()
                .getOptional(Constants.ENGINE_AUTOWIRED_ENGINE_INTERCEPTORS_EXCLUDE_REGEX)
                .ifPresent(
                        regex -> {
                            LOGGER.trace(
                                    "%s [%s]",
                                    Constants.ENGINE_AUTOWIRED_ENGINE_INTERCEPTORS_EXCLUDE_REGEX,
                                    regex);

                            Pattern pattern = Pattern.compile(regex);
                            Matcher matcher = pattern.matcher("");

                            Iterator<Class<?>> iterator = filteredClasses.iterator();
                            while (iterator.hasNext()) {
                                Class<?> clazz = iterator.next();
                                matcher.reset(clazz.getName());
                                if (matcher.find()) {
                                    LOGGER.trace(
                                            "removing engine interceptor [%s]", clazz.getName());

                                    iterator.remove();
                                }
                            }
                        });

        DefaultConfiguration.getInstance()
                .getOptional(Constants.ENGINE_AUTOWIRED_ENGINE_INTERCEPTORS_INCLUDE_REGEX)
                .ifPresent(
                        regex -> {
                            LOGGER.trace(
                                    "%s [%s]",
                                    Constants.ENGINE_AUTOWIRED_ENGINE_INTERCEPTORS_INCLUDE_REGEX,
                                    regex);

                            Pattern pattern = Pattern.compile(regex);
                            Matcher matcher = pattern.matcher("");

                            Iterator<Class<?>> iterator = classes.iterator();
                            while (iterator.hasNext()) {
                                Class<?> clazz = iterator.next();
                                matcher.reset(clazz.getName());
                                if (matcher.find()) {
                                    LOGGER.trace("adding engine interceptor [%s]", clazz.getName());
                                    filteredClasses.add(clazz);
                                }
                            }
                        });

        classes.clear();
        classes.addAll(filteredClasses);
    }

    /** Class to hold the singleton instance */
    private static class SingletonHolder {

        /** The singleton instance */
        private static final EngineInterceptorRegistry SINGLETON = new EngineInterceptorRegistry();
    }
}
