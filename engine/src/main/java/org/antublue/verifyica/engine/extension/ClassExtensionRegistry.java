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

/** Class to implement ClassExtensionRegistry */
public class ClassExtensionRegistry {

    private static final List<ClassExtension> EMPTY_CLASS_EXTENSIONS = new ArrayList<>();

    private final ReadWriteLock readWriteLock;
    private final Map<Class<?>, List<ClassExtension>> map;

    /** Constructor */
    private ClassExtensionRegistry() {
        readWriteLock = new ReentrantReadWriteLock(true);
        map = new LinkedHashMap<>();
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
            getLock().writeLock().lock();
            map.computeIfAbsent(testClass, c -> new ArrayList<>()).add(classExtension);
        } finally {
            getLock().writeLock().unlock();
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
            getLock().writeLock().lock();
            map.get(testClass).remove(classExtension);
        } finally {
            getLock().writeLock().unlock();
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
            getLock().readLock().lock();
            return map.getOrDefault(testClass, EMPTY_CLASS_EXTENSIONS).size();
        } finally {
            getLock().readLock().unlock();
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
            getLock().writeLock().lock();
            map.remove(testClass);
        } finally {
            getLock().writeLock().unlock();
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
        List<ClassExtension> classExtensions = getClassExtensions(testClass);

        for (ClassExtension classExtension : classExtensions) {
            classExtension.beforeInstantiate(defaultEngineExtensionContext, testClass);
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
        List<ClassExtension> classExtensions = getClassExtensions(testClass);
        DefaultClassExtensionContext defaultClassExtensionContext =
                new DefaultClassExtensionContext(classContext);

        for (ClassExtension classExtension : classExtensions) {
            classExtension.beforePrepare(defaultClassExtensionContext);
        }

        for (Method prepareMethod : prepareMethods) {
            prepareMethod.invoke(null, classContext);
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
        List<ClassExtension> classExtensions = getClassExtensions(testClass);
        DefaultArgumentExtensionContext defaultArgumentExtensionContext =
                new DefaultArgumentExtensionContext(argumentContext);

        for (ClassExtension classExtension : classExtensions) {
            classExtension.beforeBeforeAll(defaultArgumentExtensionContext);
        }

        for (Method beforeAllMethod : beforeAllMethods) {
            beforeAllMethod.invoke(testInstance, argumentContext);
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
        List<ClassExtension> classExtensions = getClassExtensions(testClass);
        DefaultArgumentExtensionContext defaultArgumentExtensionContext =
                new DefaultArgumentExtensionContext(argumentContext);

        for (ClassExtension classExtension : classExtensions) {
            classExtension.beforeBeforeEach(defaultArgumentExtensionContext);
        }

        for (Method beforeEachMethod : beforeEachMethods) {
            beforeEachMethod.invoke(testInstance, argumentContext);
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
        List<ClassExtension> classExtensions = getClassExtensions(testClass);
        ArgumentExtensionContext argumentExtensionContext =
                new DefaultArgumentExtensionContext(argumentContext);
        Throwable throwable = null;

        try {
            for (ClassExtension classExtension : classExtensions) {
                classExtension.beforeTest(argumentExtensionContext, testMethod);
            }

            testMethod.invoke(
                    testInstance, ((DefaultArgumentContext) argumentContext).asImmutable());
        } catch (Throwable t) {
            throwable = t;
        }

        for (ClassExtension classExtension : getClassExtensionsReversed(testClass)) {
            classExtension.afterTest(argumentExtensionContext, testMethod, throwable);
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
        List<ClassExtension> classExtensions = getClassExtensions(testClass);
        DefaultArgumentExtensionContext defaultArgumentExtensionContext =
                new DefaultArgumentExtensionContext(argumentContext);

        for (ClassExtension classExtension : classExtensions) {
            classExtension.beforeAfterEach(defaultArgumentExtensionContext);
        }

        for (Method afterEachMethod : afterEachMethods) {
            afterEachMethod.invoke(testInstance, argumentContext);
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
        List<ClassExtension> classExtensions = getClassExtensions(testClass);
        DefaultArgumentExtensionContext defaultArgumentExtensionContext =
                new DefaultArgumentExtensionContext(argumentContext);

        for (ClassExtension classExtension : classExtensions) {
            classExtension.beforeAfterAll(defaultArgumentExtensionContext);
        }

        for (Method afterAllMethod : afterAllMethods) {
            afterAllMethod.invoke(testInstance, argumentContext);
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
        List<ClassExtension> classExtensions = getClassExtensions(testClass);
        DefaultClassExtensionContext defaultClassExtensionContext =
                new DefaultClassExtensionContext(classContext);

        for (ClassExtension classExtension : classExtensions) {
            classExtension.beforeConclude(defaultClassExtensionContext);
        }

        for (Method concludeMethod : concludeMethods) {
            concludeMethod.invoke(testInstance, classContext);
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
        List<ClassExtension> classExtensions = getClassExtensions(testClass);
        DefaultClassExtensionContext defaultClassExtensionContext =
                new DefaultClassExtensionContext(classContext);
        List<Throwable> throwables = new ArrayList<>();

        for (ClassExtension classExtension : classExtensions) {
            try {
                classExtension.beforeDestroy(defaultClassExtensionContext);
            } catch (Throwable t) {
                throwables.add(t);
            }
        }

        return throwables;
    }

    /**
     * Method to get a COPY of the List of ClassExtensions
     *
     * @param testClass testClass
     * @return a COPY of the List of ClassExtensions
     */
    private List<ClassExtension> getClassExtensions(Class<?> testClass) {
        try {
            getLock().readLock().lock();
            List<ClassExtension> classExtensions = map.get(testClass);
            if (classExtensions != null) {
                return new ArrayList<>(classExtensions);
            } else {
                return EMPTY_CLASS_EXTENSIONS;
            }
        } finally {
            getLock().readLock().unlock();
        }
    }

    /**
     * Method to get a COPY of List of ClassExtensions in reverse
     *
     * @param testClass testClass
     * @return a COPY of the List of ClassExtensions in reverse
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
    private ReadWriteLock getLock() {
        return readWriteLock;
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
