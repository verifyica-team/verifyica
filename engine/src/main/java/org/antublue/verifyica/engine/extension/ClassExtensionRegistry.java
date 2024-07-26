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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.antublue.verifyica.api.ArgumentContext;
import org.antublue.verifyica.api.ClassContext;
import org.antublue.verifyica.api.EngineContext;
import org.antublue.verifyica.api.extension.ArgumentExtensionContext;
import org.antublue.verifyica.api.extension.ClassExtension;
import org.antublue.verifyica.engine.context.DefaultArgumentContext;
import org.antublue.verifyica.engine.context.DefaultArgumentExtensionContext;
import org.antublue.verifyica.engine.context.DefaultClassExtensionContext;
import org.antublue.verifyica.engine.context.DefaultEngineExtensionContext;
import org.antublue.verifyica.engine.discovery.Predicates;
import org.antublue.verifyica.engine.exception.EngineException;
import org.antublue.verifyica.engine.logger.Logger;
import org.antublue.verifyica.engine.logger.LoggerFactory;
import org.antublue.verifyica.engine.support.ClassPathSupport;
import org.antublue.verifyica.engine.support.ObjectSupport;
import org.antublue.verifyica.engine.support.OrderSupport;

/** Class to implement ClassExtensionRegistry */
@SuppressWarnings("PMD.EmptyCatchBlock")
public class ClassExtensionRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClassExtensionRegistry.class);

    private final ReadWriteLock readWriteLock;
    private final List<ClassExtension> internalClassExtensions;
    private final Map<Class<?>, List<ClassExtension>> mappedClassExtensions;
    private boolean initialized;

    /** Constructor */
    private ClassExtensionRegistry() {
        readWriteLock = new ReentrantReadWriteLock(true);
        internalClassExtensions = new ArrayList<>();
        mappedClassExtensions = new LinkedHashMap<>();

        load();
    }

    /**
     * Method to register a class extension
     *
     * @param testClass testClass
     * @param classExtension classExtensions
     * @return this ClassExtensionRegistry
     */
    public ClassExtensionRegistry register(Class<?> testClass, ClassExtension classExtension) {
        notNull(testClass, "testClass is null");
        notNull(classExtension, "classExtension is null");

        try {
            getReadWriteLock().writeLock().lock();
            mappedClassExtensions
                    .computeIfAbsent(testClass, c -> new ArrayList<>())
                    .add(classExtension);
        } finally {
            getReadWriteLock().writeLock().unlock();
        }

        return this;
    }

    /**
     * Method to unregister a class extension
     *
     * @param testClass testClass
     * @param classExtension classExtension
     * @return this ClassExtensionRegistry
     */
    public ClassExtensionRegistry unregister(Class<?> testClass, ClassExtension classExtension) {
        notNull(testClass, "testClass is null");
        notNull(classExtension, "classExtension is null");

        try {
            getReadWriteLock().writeLock().lock();
            mappedClassExtensions.get(testClass).remove(classExtension);
        } finally {
            getReadWriteLock().writeLock().unlock();
        }

        return this;
    }

    /**
     * Method to get the number of class extensions
     *
     * @param testClass testClass
     * @return the number of class extensions
     */
    public int size(Class<?> testClass) {
        notNull(testClass, "testClass is null");

        try {
            getReadWriteLock().readLock().lock();
            List<ClassExtension> classExtensions = mappedClassExtensions.get(testClass);
            return classExtensions != null ? classExtensions.size() : 0;
        } finally {
            getReadWriteLock().readLock().unlock();
        }
    }

    /**
     * Method to remove all class extensions
     *
     * @param testClass testClass
     * @return this ClassExtensionRegistry
     */
    public ClassExtensionRegistry clear(Class<?> testClass) {
        notNull(testClass, "testClass is null");

        try {
            getReadWriteLock().writeLock().lock();
            mappedClassExtensions.remove(testClass);
        } finally {
            getReadWriteLock().writeLock().unlock();
        }

        return this;
    }

    /**
     * Method to execute class extensions
     *
     * @param engineContext engineContext
     * @param testClass testClass
     * @throws Throwable Throwable
     */
    public void beforeInstantiate(EngineContext engineContext, Class<?> testClass)
            throws Throwable {
        DefaultEngineExtensionContext defaultEngineExtensionContext =
                new DefaultEngineExtensionContext(engineContext);

        try {
            for (ClassExtension classExtension : getClassExtensions(testClass)) {
                classExtension.beforeInstantiate(defaultEngineExtensionContext, testClass);
            }
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }

    /**
     * Method to execute class extensions
     *
     * @param classContext classContext
     * @param prepareMethods prepareMethods
     * @throws Throwable Throwable
     */
    public void prepare(ClassContext classContext, List<Method> prepareMethods) throws Throwable {
        Class<?> testClass = classContext.getTestClass();
        DefaultClassExtensionContext defaultClassExtensionContext =
                new DefaultClassExtensionContext(classContext);

        try {
            for (ClassExtension classExtension : getClassExtensions(testClass)) {
                classExtension.beforePrepare(defaultClassExtensionContext);
            }
            for (Method prepareMethod : prepareMethods) {
                prepareMethod.invoke(null, classContext);
            }
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }

    /**
     * Method to execute class extensions
     *
     * @param argumentContext argumentContext
     * @param beforeAllMethods beforeAllMethods
     * @throws Throwable Throwable
     */
    public void beforeAll(ArgumentContext argumentContext, List<Method> beforeAllMethods)
            throws Throwable {
        ClassContext classContext = argumentContext.getClassContext();
        Class<?> testClass = classContext.getTestClass();
        Object testInstance = classContext.getTestInstance();
        DefaultArgumentExtensionContext defaultArgumentExtensionContext =
                new DefaultArgumentExtensionContext(argumentContext);
        try {
            for (ClassExtension classExtension : getClassExtensions(testClass)) {
                classExtension.beforeBeforeAll(defaultArgumentExtensionContext);
            }
            for (Method beforeAllMethod : beforeAllMethods) {
                beforeAllMethod.invoke(testInstance, argumentContext);
            }
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }

    /**
     * Method to execute class extensions
     *
     * @param argumentContext argumentContext
     * @param beforeEachMethods beforeEachMethods
     * @throws Throwable Throwable
     */
    public void beforeEach(ArgumentContext argumentContext, List<Method> beforeEachMethods)
            throws Throwable {
        ClassContext classContext = argumentContext.getClassContext();
        Class<?> testClass = classContext.getTestClass();
        Object testInstance = classContext.getTestInstance();
        DefaultArgumentExtensionContext defaultArgumentExtensionContext =
                new DefaultArgumentExtensionContext(argumentContext);

        try {
            for (ClassExtension classExtension : getClassExtensions(testClass)) {
                classExtension.beforeBeforeEach(defaultArgumentExtensionContext);
            }
            for (Method beforeEachMethod : beforeEachMethods) {
                beforeEachMethod.invoke(testInstance, argumentContext);
            }
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }

    /**
     * Method to execute class extensions
     *
     * @param argumentContext argumentContext
     * @param testMethod testMethod
     * @throws Throwable Throwable
     */
    public void test(ArgumentContext argumentContext, Method testMethod) throws Throwable {
        ClassContext classContext = argumentContext.getClassContext();
        Class<?> testClass = classContext.getTestClass();
        Object testInstance = classContext.getTestInstance();
        ArgumentExtensionContext argumentExtensionContext =
                new DefaultArgumentExtensionContext(argumentContext);
        Throwable throwable = null;

        try {
            for (ClassExtension classExtension : getClassExtensions(testClass)) {
                classExtension.beforeTest(argumentExtensionContext, testMethod);
            }
            testMethod.invoke(
                    testInstance, ((DefaultArgumentContext) argumentContext).asImmutable());
        } catch (InvocationTargetException e) {
            throwable = e.getCause();
        } catch (Throwable t) {
            throwable = t;
        }

        try {
            for (ClassExtension classExtension : getClassExtensionsReversed(testClass)) {
                classExtension.afterTest(argumentExtensionContext, testMethod, throwable);
            }
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }

    /**
     * Method to execute class extensions
     *
     * @param argumentContext argumentContext
     * @param afterEachMethods afterEachMethods
     * @throws Throwable Throwable
     */
    public void afterEach(ArgumentContext argumentContext, List<Method> afterEachMethods)
            throws Throwable {
        ClassContext classContext = argumentContext.getClassContext();
        Class<?> testClass = classContext.getTestClass();
        Object testInstance = classContext.getTestInstance();
        DefaultArgumentExtensionContext defaultArgumentExtensionContext =
                new DefaultArgumentExtensionContext(argumentContext);

        try {
            for (ClassExtension classExtension : getClassExtensionsReversed(testClass)) {
                classExtension.beforeAfterEach(defaultArgumentExtensionContext);
            }
            for (Method afterEachMethod : afterEachMethods) {
                afterEachMethod.invoke(testInstance, argumentContext);
            }
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }

    /**
     * Method to execute class extensions
     *
     * @param argumentContext argumentContext
     * @param afterAllMethods afterAllMethods
     * @throws Throwable Throwable
     */
    public void afterAll(ArgumentContext argumentContext, List<Method> afterAllMethods)
            throws Throwable {
        ClassContext classContext = argumentContext.getClassContext();
        Class<?> testClass = classContext.getTestClass();
        Object testInstance = classContext.getTestInstance();
        DefaultArgumentExtensionContext defaultArgumentExtensionContext =
                new DefaultArgumentExtensionContext(argumentContext);

        try {
            for (ClassExtension classExtension : getClassExtensionsReversed(testClass)) {
                classExtension.beforeAfterAll(defaultArgumentExtensionContext);
            }
            for (Method afterAllMethod : afterAllMethods) {
                afterAllMethod.invoke(testInstance, argumentContext);
            }
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }

    /**
     * Method to execute class extensions
     *
     * @param classContext classContext
     * @param concludeMethods concludeMethods
     * @throws Throwable Throwable
     */
    public void conclude(ClassContext classContext, List<Method> concludeMethods) throws Throwable {
        Class<?> testClass = classContext.getTestClass();
        Object testInstance = classContext.getTestInstance();
        DefaultClassExtensionContext defaultClassExtensionContext =
                new DefaultClassExtensionContext(classContext);

        try {
            for (ClassExtension classExtension : getClassExtensionsReversed(testClass)) {
                classExtension.beforeConclude(defaultClassExtensionContext);
            }
            for (Method concludeMethod : concludeMethods) {
                concludeMethod.invoke(testInstance, classContext);
            }
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }

    /**
     * Method to execute class extensions
     *
     * @param classContext classContext
     * @return a List of Throwables
     */
    public List<Throwable> beforeDestroy(ClassContext classContext) {
        Class<?> testClass = classContext.getTestClass();
        DefaultClassExtensionContext defaultClassExtensionContext =
                new DefaultClassExtensionContext(classContext);
        List<Throwable> throwables = new ArrayList<>();

        for (ClassExtension classExtension : getClassExtensions(testClass)) {
            try {
                classExtension.onDestroy(defaultClassExtensionContext);
            } catch (InvocationTargetException e) {
                throwables.add(e.getCause());
            } catch (Throwable t) {
                throwables.add(t);
            }
        }

        return throwables;
    }

    /**
     * Method to get a COPY of the List of ClassExtensions (internal + class specific)
     *
     * @param testClass testClass
     * @return a COPY of the List of ClassExtensions (internal + class specific)
     */
    private List<ClassExtension> getClassExtensions(Class<?> testClass) {
        try {
            getReadWriteLock().writeLock().lock();
            List<ClassExtension> classExtensions = new ArrayList<>(internalClassExtensions);
            classExtensions.addAll(
                    mappedClassExtensions.computeIfAbsent(testClass, o -> new ArrayList<>()));
            return classExtensions;
        } finally {
            getReadWriteLock().writeLock().unlock();
        }
    }

    /**
     * Method to get a COPY of List of ClassExtensions in reverse (internal + class specific)
     *
     * @param testClass testClass
     * @return a COPY of the List of ClassExtensions in reverse (internal + class specific)
     */
    private List<ClassExtension> getClassExtensionsReversed(Class<?> testClass) {
        List<ClassExtension> classExtensions = getClassExtensions(testClass);
        Collections.reverse(classExtensions);
        return classExtensions;
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
    private void load() {
        try {
            getReadWriteLock().writeLock().lock();

            if (!initialized) {
                LOGGER.trace("load()");

                // Load class extensions
                List<Class<?>> internalClassExtensionClasses =
                        new ArrayList<>(
                                ClassPathSupport.findClasses(
                                        Predicates.CLASS_INTERNAL_EXTENSION_CLASS));

                // Order extensions
                OrderSupport.order(internalClassExtensionClasses);

                LOGGER.trace(
                        "internal class extension count [%d]",
                        internalClassExtensionClasses.size());

                for (Class<?> classExtensionClass : internalClassExtensionClasses) {
                    try {
                        LOGGER.trace(
                                "loading global class extension [%s]",
                                classExtensionClass.getName());
                        Object object = ObjectSupport.createObject(classExtensionClass);
                        internalClassExtensions.add((ClassExtension) object);
                        LOGGER.trace(
                                "global class extension [%s] loaded",
                                classExtensionClass.getName());
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
    public static ClassExtensionRegistry getInstance() {
        return SingletonHolder.SINGLETON;
    }

    /**
     * Check if a Object is not null
     *
     * @param object object
     * @param message message
     */
    private static void notNull(Object object, String message) {
        if (object == null) {
            throw new IllegalArgumentException(message);
        }
    }

    /** Class to hold the singleton instance */
    private static class SingletonHolder {

        /** The singleton instance */
        private static final ClassExtensionRegistry SINGLETON = new ClassExtensionRegistry();
    }
}
