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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.antublue.verifyica.api.ArgumentContext;
import org.antublue.verifyica.api.ClassContext;
import org.antublue.verifyica.api.interceptor.ArgumentInterceptorContext;
import org.antublue.verifyica.api.interceptor.ClassInterceptor;
import org.antublue.verifyica.api.interceptor.ClassInterceptorContext;
import org.antublue.verifyica.api.interceptor.engine.EngineInterceptorContext;
import org.antublue.verifyica.engine.common.Precondition;
import org.antublue.verifyica.engine.configuration.ConcreteConfiguration;
import org.antublue.verifyica.engine.configuration.Constants;
import org.antublue.verifyica.engine.context.ConcreteArgumentInterceptorContext;
import org.antublue.verifyica.engine.context.ConcreteClassInterceptorContext;
import org.antublue.verifyica.engine.exception.EngineException;
import org.antublue.verifyica.engine.logger.Logger;
import org.antublue.verifyica.engine.logger.LoggerFactory;
import org.antublue.verifyica.engine.support.ClassSupport;
import org.antublue.verifyica.engine.support.ObjectSupport;
import org.antublue.verifyica.engine.support.OrderSupport;

/** Class to implement ClassInterceptorManager */
@SuppressWarnings("PMD.EmptyCatchBlock")
public class ClassInterceptorManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClassInterceptorManager.class);

    private final EngineInterceptorContext engineInterceptorContext;
    private final ReadWriteLock readWriteLock;
    private final List<ClassInterceptor> classInterceptors;
    private final Map<Class<?>, List<ClassInterceptor>> mappedClassInterceptors;

    /**
     * Constructor
     *
     * @param engineInterceptorContext engineInterceptorContext
     */
    public ClassInterceptorManager(EngineInterceptorContext engineInterceptorContext) {
        Precondition.notNull(engineInterceptorContext, "engineInterceptorContext is null");

        this.engineInterceptorContext = engineInterceptorContext;
        this.readWriteLock = new ReentrantReadWriteLock(true);
        this.classInterceptors = new ArrayList<>();
        this.mappedClassInterceptors = new LinkedHashMap<>();

        initialize();
    }

    /**
     * Method to register a ClassInterceptor
     *
     * @param testClass testClass
     * @param classInterceptor classInterceptors
     * @return this
     */
    public ClassInterceptorManager register(Class<?> testClass, ClassInterceptor classInterceptor) {
        Precondition.notNull(testClass, "testClass is null");
        Precondition.notNull(classInterceptor, "classInterceptor is null");

        getReadWriteLock().writeLock().lock();
        try {
            mappedClassInterceptors
                    .computeIfAbsent(testClass, c -> new ArrayList<>())
                    .add(classInterceptor);
        } finally {
            getReadWriteLock().writeLock().unlock();
        }

        return this;
    }

    /**
     * Method to remove a ClassInterceptor
     *
     * @param testClass testClass
     * @param classInterceptor classInterceptor
     * @return this
     */
    public ClassInterceptorManager remove(Class<?> testClass, ClassInterceptor classInterceptor) {
        Precondition.notNull(testClass, "testClass is null");
        Precondition.notNull(classInterceptor, "classInterceptor is null");

        getReadWriteLock().writeLock().lock();
        try {
            mappedClassInterceptors.get(testClass).remove(classInterceptor);
        } finally {
            getReadWriteLock().writeLock().unlock();
        }

        return this;
    }

    /**
     * Method to get the number of ClassInterceptor for a Class
     *
     * @param testClass testClass
     * @return the number of class interceptors
     */
    public int size(Class<?> testClass) {
        Precondition.notNull(testClass, "testClass is null");

        getReadWriteLock().readLock().lock();
        try {
            List<ClassInterceptor> classInterceptors = mappedClassInterceptors.get(testClass);
            return classInterceptors != null ? classInterceptors.size() : 0;
        } finally {
            getReadWriteLock().readLock().unlock();
        }
    }

    /**
     * Method to remove all ClassInterceptors
     *
     * @param testClass testClass
     * @return this
     */
    public ClassInterceptorManager clear(Class<?> testClass) {
        Precondition.notNull(testClass, "testClass is null");

        getReadWriteLock().writeLock().lock();
        try {
            mappedClassInterceptors.remove(testClass);
        } finally {
            getReadWriteLock().writeLock().unlock();
        }

        return this;
    }

    /**
     * Method to execute ClassInterceptor callbacks
     *
     * @param testClass testClass
     * @param testInstanceReference testInstanceReference
     * @throws Throwable Throwable
     */
    public void instantiate(Class<?> testClass, AtomicReference<Object> testInstanceReference)
            throws Throwable {
        Object testInstance = null;
        Throwable throwable = null;

        try {
            for (ClassInterceptor classInterceptor : getClassInterceptors(testClass)) {
                classInterceptor.preInstantiate(engineInterceptorContext, testClass);
            }
        } catch (Throwable t) {
            throwable = t;
        }

        if (throwable == null) {
            try {
                testInstance =
                        testClass
                                .getDeclaredConstructor((Class<?>[]) null)
                                .newInstance((Object[]) null);
            } catch (Throwable t) {
                throwable = t.getCause();
            }
        }

        for (ClassInterceptor classInterceptor : getClassInterceptorsReversed(testClass)) {
            try {
                classInterceptor.postInstantiate(
                        engineInterceptorContext, testClass, testInstance, throwable);
            } catch (Throwable t) {
                throwable = t;
            }
        }

        if (throwable != null) {
            throw throwable;
        }

        testInstanceReference.set(testInstance);
    }

    /**
     * Method to execute ClassInterceptor callbacks
     *
     * @param classContext classContext
     * @param prepareMethods prepareMethods
     * @throws Throwable Throwable
     */
    public void prepare(ClassContext classContext, List<Method> prepareMethods) throws Throwable {
        Class<?> testClass = classContext.getTestClass();

        ClassInterceptorContext argumentInterceptorContext =
                new ConcreteClassInterceptorContext(classContext);

        Throwable throwable = null;

        try {
            for (ClassInterceptor classInterceptor : getClassInterceptors(testClass)) {
                classInterceptor.prePrepare(argumentInterceptorContext);
            }
        } catch (Throwable t) {
            throwable = t;
        }

        if (throwable == null) {
            try {
                for (Method prepareMethod : prepareMethods) {
                    prepareMethod.invoke(
                            Modifier.isStatic(prepareMethod.getModifiers())
                                    ? null
                                    : classContext.getTestInstance(),
                            classContext);
                }
            } catch (InvocationTargetException e) {
                throwable = e.getCause();
            }
        }

        try {
            for (ClassInterceptor classInterceptor : getClassInterceptorsReversed(testClass)) {
                classInterceptor.postPrepare(argumentInterceptorContext, throwable);
            }
        } catch (Throwable t) {
            throwable = t;
        }

        if (throwable != null) {
            throw throwable;
        }
    }

    /**
     * Method to execute ClassInterceptor callbacks
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

        ArgumentInterceptorContext argumentInterceptorContext =
                new ConcreteArgumentInterceptorContext(argumentContext);

        Throwable throwable = null;

        try {
            for (ClassInterceptor classInterceptor : getClassInterceptors(testClass)) {
                classInterceptor.preBeforeAll(argumentInterceptorContext);
            }
        } catch (Throwable t) {
            throwable = t;
        }

        if (throwable == null) {
            try {
                for (Method beforeAllMethod : beforeAllMethods) {
                    beforeAllMethod.invoke(testInstance, argumentContext);
                }
            } catch (InvocationTargetException e) {
                throwable = e.getCause();
            }
        }

        try {
            for (ClassInterceptor classInterceptor : getClassInterceptorsReversed(testClass)) {
                classInterceptor.postBeforeAll(argumentInterceptorContext, throwable);
            }
        } catch (Throwable t) {
            throwable = t;
        }

        if (throwable != null) {
            throw throwable;
        }
    }

    /**
     * Method to execute ClassInterceptor callbacks
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

        ArgumentInterceptorContext argumentInterceptorContext =
                new ConcreteArgumentInterceptorContext(argumentContext);

        Throwable throwable = null;

        try {
            for (ClassInterceptor classInterceptor : getClassInterceptors(testClass)) {
                classInterceptor.preBeforeEach(argumentInterceptorContext);
            }
        } catch (Throwable t) {
            throwable = t;
        }

        if (throwable == null) {
            try {
                for (Method beforEachMethod : beforeEachMethods) {
                    beforEachMethod.invoke(testInstance, argumentContext);
                }
            } catch (InvocationTargetException e) {
                throwable = e.getCause();
            }
        }

        try {
            for (ClassInterceptor classInterceptor : getClassInterceptorsReversed(testClass)) {
                classInterceptor.postBeforeEach(argumentInterceptorContext, throwable);
            }
        } catch (Throwable t) {
            throwable = t;
        }

        if (throwable != null) {
            throw throwable;
        }
    }

    /**
     * Method to execute ClassInterceptor callbacks
     *
     * @param argumentContext argumentContext
     * @param testMethod testMethod
     * @throws Throwable Throwable
     */
    public void test(ArgumentContext argumentContext, Method testMethod) throws Throwable {
        ClassContext classContext = argumentContext.getClassContext();

        Class<?> testClass = classContext.getTestClass();

        Object testInstance = classContext.getTestInstance();

        ArgumentInterceptorContext argumentInterceptorContext =
                new ConcreteArgumentInterceptorContext(argumentContext);

        Throwable throwable = null;

        try {
            for (ClassInterceptor classInterceptor : getClassInterceptors(testClass)) {
                classInterceptor.preTest(argumentInterceptorContext, testMethod);
            }
        } catch (Throwable t) {
            throwable = t;
        }

        if (throwable == null) {
            try {
                testMethod.invoke(testInstance, argumentContext);
            } catch (InvocationTargetException e) {
                throwable = e.getCause();
            }
        }

        try {
            for (ClassInterceptor classInterceptor : getClassInterceptorsReversed(testClass)) {
                classInterceptor.postTest(argumentInterceptorContext, testMethod, throwable);
            }
        } catch (Throwable t) {
            throwable = t;
        }

        if (throwable != null) {
            throw throwable;
        }
    }

    /**
     * Method to execute ClassInterceptor callbacks
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

        ArgumentInterceptorContext argumentInterceptorContext =
                new ConcreteArgumentInterceptorContext(argumentContext);

        Throwable throwable = null;

        try {
            for (ClassInterceptor classInterceptor : getClassInterceptors(testClass)) {
                classInterceptor.preAfterEach(argumentInterceptorContext);
            }
        } catch (Throwable t) {
            throwable = t;
        }

        if (throwable == null) {
            try {
                for (Method afterEachMethod : afterEachMethods) {
                    afterEachMethod.invoke(testInstance, argumentContext);
                }
            } catch (InvocationTargetException e) {
                throwable = e.getCause();
            }
        }

        try {
            for (ClassInterceptor classInterceptor : getClassInterceptorsReversed(testClass)) {
                classInterceptor.postAfterEach(argumentInterceptorContext, throwable);
            }
        } catch (Throwable t) {
            throwable = t;
        }

        if (throwable != null) {
            throw throwable;
        }
    }

    /**
     * Method to execute ClassInterceptor callbacks
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

        ArgumentInterceptorContext argumentInterceptorContext =
                new ConcreteArgumentInterceptorContext(argumentContext);

        Throwable throwable = null;

        try {
            for (ClassInterceptor classInterceptor : getClassInterceptors(testClass)) {
                classInterceptor.preAfterAll(argumentInterceptorContext);
            }
        } catch (Throwable t) {
            throwable = t;
        }

        if (throwable == null) {
            try {
                for (Method afterAllMethod : afterAllMethods) {
                    afterAllMethod.invoke(testInstance, argumentContext);
                }
            } catch (InvocationTargetException e) {
                throwable = e.getCause();
            }
        }

        try {
            for (ClassInterceptor classInterceptor : getClassInterceptorsReversed(testClass)) {
                classInterceptor.postAfterAll(argumentInterceptorContext, throwable);
            }
        } catch (Throwable t) {
            throwable = t;
        }

        if (throwable != null) {
            throw throwable;
        }
    }

    /**
     * Method to execute ClassInterceptor callbacks
     *
     * @param classContext classContext
     * @param concludeMethods concludeMethods
     * @throws Throwable Throwable
     */
    public void conclude(ClassContext classContext, List<Method> concludeMethods) throws Throwable {
        Class<?> testClass = classContext.getTestClass();

        ClassInterceptorContext defaultClassInterceptorContext =
                new ConcreteClassInterceptorContext(classContext);

        Throwable throwable = null;

        try {
            for (ClassInterceptor classInterceptor : getClassInterceptors(testClass)) {
                classInterceptor.preConclude(defaultClassInterceptorContext);
            }
        } catch (Throwable t) {
            throwable = t;
        }

        if (throwable == null) {
            try {
                for (Method concludeMethod : concludeMethods) {
                    concludeMethod.invoke(
                            Modifier.isStatic(concludeMethod.getModifiers())
                                    ? null
                                    : classContext.getTestInstance(),
                            classContext);
                }
            } catch (Throwable t) {
                throwable = t.getCause();
            }
        }

        try {
            for (ClassInterceptor classInterceptor : getClassInterceptorsReversed(testClass)) {
                classInterceptor.postConclude(defaultClassInterceptorContext, throwable);
            }
        } catch (Throwable t) {
            throwable = t;
        }

        if (throwable != null) {
            throw throwable;
        }
    }

    /**
     * Method to execute ClassInterceptor callbacks
     *
     * @param classContext classContext
     * @throws Throwable Throwable
     */
    public void onDestroy(ClassContext classContext) throws Throwable {
        Class<?> testClass = classContext.getTestClass();

        ClassInterceptorContext classInterceptorContext =
                new ConcreteClassInterceptorContext(classContext);

        for (ClassInterceptor classInterceptor : getClassInterceptorsReversed(testClass)) {
            classInterceptor.onDestroy(classInterceptorContext);
        }
    }

    /**
     * Method to get a COPY of the List of ClassInterceptors (internal + class specific)
     *
     * @param testClass testClass
     * @return a COPY of the List of ClassInterceptors (internal + class specific)
     */
    private List<ClassInterceptor> getClassInterceptors(Class<?> testClass) {
        getReadWriteLock().writeLock().lock();
        try {
            List<ClassInterceptor> classInterceptors = new ArrayList<>();

            if (this.classInterceptors != null) {
                classInterceptors.addAll(this.classInterceptors);
            }

            classInterceptors.addAll(
                    mappedClassInterceptors.computeIfAbsent(testClass, k -> new ArrayList<>()));

            return classInterceptors;
        } finally {
            getReadWriteLock().writeLock().unlock();
        }
    }

    /**
     * Method to get a COPY of the List of ClassInterceptors in reverse (internal + class specific)
     *
     * @param testClass testClass
     * @return a COPY of the List of ClassInterceptors in reverse (internal + class specific)
     */
    private List<ClassInterceptor> getClassInterceptorsReversed(Class<?> testClass) {
        List<ClassInterceptor> classInterceptors = getClassInterceptors(testClass);

        Collections.reverse(classInterceptors);

        return classInterceptors;
    }

    /**
     * Method to get the ReadWriteLock
     *
     * @return the ReadWriteLock
     */
    private ReadWriteLock getReadWriteLock() {
        return readWriteLock;
    }

    /** Method initialize by loading autowired ClassInterceptors */
    private void initialize() {
        getReadWriteLock().writeLock().lock();
        try {
            LOGGER.trace("initialize()");
            LOGGER.trace("loading autowired class interceptors");

            List<Class<?>> autowiredClassInterceptors =
                    new ArrayList<>(
                            ClassSupport.findAllClasses(
                                    InterceptorPredicates.AUTOWIRED_CLASS_INTERCEPTOR_CLASS));

            filter(autowiredClassInterceptors);

            OrderSupport.orderClasses(autowiredClassInterceptors);

            LOGGER.trace(
                    "autowired class interceptor count [%d]", autowiredClassInterceptors.size());

            for (Class<?> classInterceptorClass : autowiredClassInterceptors) {
                try {
                    LOGGER.trace(
                            "loading autowired class interceptor [%s]",
                            classInterceptorClass.getName());

                    Object object = ObjectSupport.createObject(classInterceptorClass);

                    classInterceptors.add((ClassInterceptor) object);

                    LOGGER.trace(
                            "autowired class interceptor [%s] loaded",
                            classInterceptorClass.getName());
                } catch (EngineException e) {
                    throw e;
                } catch (Throwable t) {
                    throw new EngineException(t);
                }
            }
        } finally {
            getReadWriteLock().writeLock().unlock();
        }
    }

    /**
     * Method to filter ClassInterceptors
     *
     * @param classes classes
     */
    private static void filter(List<Class<?>> classes) {
        Set<Class<?>> filteredClasses = new LinkedHashSet<>(classes);

        ConcreteConfiguration.getInstance()
                .getOptional(Constants.ENGINE_AUTOWIRED_CLASS_INTERCEPTORS_EXCLUDE_REGEX)
                .ifPresent(
                        regex -> {
                            LOGGER.trace(
                                    "%s [%s]",
                                    Constants.ENGINE_AUTOWIRED_CLASS_INTERCEPTORS_EXCLUDE_REGEX,
                                    regex);

                            Pattern pattern = Pattern.compile(regex);
                            Matcher matcher = pattern.matcher("");

                            Iterator<Class<?>> iterator = filteredClasses.iterator();
                            while (iterator.hasNext()) {
                                Class<?> clazz = iterator.next();
                                matcher.reset(clazz.getName());
                                if (matcher.find()) {
                                    LOGGER.trace(
                                            "removing class interceptor [%s]", clazz.getName());

                                    iterator.remove();
                                }
                            }
                        });

        ConcreteConfiguration.getInstance()
                .getOptional(Constants.ENGINE_AUTOWIRED_CLASS_INTERCEPTORS_INCLUDE_REGEX)
                .ifPresent(
                        regex -> {
                            LOGGER.trace(
                                    "%s [%s]",
                                    Constants.ENGINE_AUTOWIRED_CLASS_INTERCEPTORS_INCLUDE_REGEX,
                                    regex);

                            Pattern pattern = Pattern.compile(regex);
                            Matcher matcher = pattern.matcher("");

                            for (Class<?> clazz : classes) {
                                matcher.reset(clazz.getName());
                                if (matcher.find()) {
                                    LOGGER.trace("adding class interceptor [%s]", clazz.getName());
                                    filteredClasses.add(clazz);
                                }
                            }
                        });

        classes.clear();
        classes.addAll(filteredClasses);
    }
}
