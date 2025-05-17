/*
 * Copyright (C) Verifyica project authors and contributors
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

package org.verifyica.engine.interceptor;

import static java.lang.String.format;
import static java.util.Optional.ofNullable;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.verifyica.api.ClassInterceptor;
import org.verifyica.api.Configuration;
import org.verifyica.api.EngineContext;
import org.verifyica.api.Verifyica;
import org.verifyica.engine.common.AnsiColor;
import org.verifyica.engine.common.Precondition;
import org.verifyica.engine.common.StackTracePrinter;
import org.verifyica.engine.configuration.Constants;
import org.verifyica.engine.exception.EngineException;
import org.verifyica.engine.exception.TestClassDefinitionException;
import org.verifyica.engine.logger.Logger;
import org.verifyica.engine.logger.LoggerFactory;
import org.verifyica.engine.resolver.ResolverPredicates;
import org.verifyica.engine.support.ClassSupport;
import org.verifyica.engine.support.HierarchyTraversalMode;
import org.verifyica.engine.support.ListSupport;
import org.verifyica.engine.support.ObjectSupport;
import org.verifyica.engine.support.OrderSupport;

/** Class to implement ClassInterceptorRegistry */
public class ClassInterceptorRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClassInterceptorRegistry.class);

    private final Configuration configuration;
    private final ReadWriteLock readWriteLock;
    private final List<ClassInterceptor> classInterceptors;
    private final Map<Class<?>, List<ClassInterceptor>> mappedClassInterceptors;

    /**
     * Constructor
     *
     * @param configuration configuration
     */
    public ClassInterceptorRegistry(Configuration configuration) {
        this.configuration = configuration;
        this.readWriteLock = new ReentrantReadWriteLock(true);

        this.classInterceptors = new ArrayList<>();
        this.classInterceptors.add(new DefaultClassInterceptor());

        this.mappedClassInterceptors = new ConcurrentHashMap<>();
    }

    /**
     * Method to initialize the registry
     *
     * @param engineContext engineContext
     */
    public void initialize(EngineContext engineContext) {
        readWriteLock.writeLock().lock();
        try {
            LOGGER.trace("initialize()");
            LOGGER.trace("loading autowired class interceptors");

            List<Class<?>> autowiredClassInterceptors = new ArrayList<>(
                    ClassSupport.findAllClasses(InterceptorPredicates.AUTOWIRED_CLASS_INTERCEPTOR_CLASS));

            filter(autowiredClassInterceptors);

            OrderSupport.orderClasses(autowiredClassInterceptors);

            LOGGER.trace("autowired class interceptor count [%d]", autowiredClassInterceptors.size());

            for (Class<?> classInterceptorClass : autowiredClassInterceptors) {
                try {
                    LOGGER.trace("loading autowired class interceptor [%s]", classInterceptorClass.getName());

                    ClassInterceptor classInterceptor = ObjectSupport.createObject(classInterceptorClass);
                    classInterceptor.initialize(engineContext);

                    classInterceptors.add(classInterceptor);

                    LOGGER.trace("autowired class interceptor [%s] loaded", classInterceptorClass.getName());
                } catch (EngineException e) {
                    throw e;
                } catch (Throwable t) {
                    throw new EngineException(t);
                }
            }
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    /**
     * Method to get class interceptors for a test class
     *
     * @param engineContext engineContext
     * @param testClass testClass
     * @return a List of ClassInterceptors
     * @throws Throwable Throwable
     */
    public List<ClassInterceptor> getClassInterceptors(EngineContext engineContext, Class<?> testClass)
            throws Throwable {
        readWriteLock.readLock().lock();
        try {
            List<ClassInterceptor> classInterceptors = new ArrayList<>(this.classInterceptors);
            classInterceptors.addAll(getDeclaredClassInterceptor(engineContext, testClass));
            return classInterceptors;
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    private synchronized List<ClassInterceptor> getDeclaredClassInterceptor(
            EngineContext engineContext, Class<?> testClass) throws Throwable {
        List<ClassInterceptor> classInterceptors = mappedClassInterceptors.get(testClass);

        if (classInterceptors != null) {
            return classInterceptors;
        }

        classInterceptors = new ArrayList<>();
        mappedClassInterceptors.put(testClass, classInterceptors);

        List<Method> classInterceptorSupplierMethods = ClassSupport.findMethods(
                testClass, ResolverPredicates.CLASS_INTERCEPTOR_SUPPLIER, HierarchyTraversalMode.BOTTOM_UP);

        validateSingleMethodPerClass(Verifyica.ClassInterceptorSupplier.class, classInterceptorSupplierMethods);

        if (!classInterceptorSupplierMethods.isEmpty()) {
            Method classInterceptorSupplierMethod = classInterceptorSupplierMethods.get(0);
            Object object = classInterceptorSupplierMethod.invoke(null);

            if (object == null) {
                throw new TestClassDefinitionException(format(
                        "Null Object supplied by test class" + " [%s] @Verifyica.ClassInterceptorSupplier" + " method",
                        testClass.getName()));
            } else if (object instanceof ClassInterceptor) {
                ClassInterceptor classInterceptor = (ClassInterceptor) object;
                classInterceptor.initialize(engineContext);
                classInterceptors.add(classInterceptor);
            } else if (object.getClass().isArray()) {
                Object[] objects = (Object[]) object;
                if (objects.length > 0) {
                    int index = 0;
                    for (Object o : objects) {
                        if (o instanceof ClassInterceptor) {
                            ClassInterceptor classInterceptor = (ClassInterceptor) o;
                            classInterceptor.initialize(engineContext);
                            classInterceptors.add(classInterceptor);
                        } else {
                            throw new TestClassDefinitionException(format(
                                    "Invalid type [%s] supplied by test class [%s]"
                                            + " @Verifyica.ClassInterceptorSupplier method"
                                            + " at index [%d]",
                                    o.getClass().getName(), testClass.getName(), index));
                        }
                        index++;
                    }
                }
            } else if (object instanceof Stream
                    || object instanceof Iterable
                    || object instanceof Iterator
                    || object instanceof Enumeration) {
                Iterator<?> iterator;
                if (object instanceof Enumeration) {
                    iterator = Collections.list((Enumeration<?>) object).iterator();
                } else if (object instanceof Iterator) {
                    iterator = (Iterator<?>) object;
                } else if (object instanceof Stream) {
                    Stream<?> stream = (Stream<?>) object;
                    iterator = stream.iterator();
                } else {
                    Iterable<?> iterable = (Iterable<?>) object;
                    iterator = iterable.iterator();
                }

                while (iterator.hasNext()) {
                    Object o = iterator.next();
                    if (o instanceof ClassInterceptor) {
                        ClassInterceptor classInterceptor = (ClassInterceptor) o;
                        classInterceptor.initialize(engineContext);
                        classInterceptors.add(classInterceptor);
                    } else {
                        throw new TestClassDefinitionException(format(
                                "Invalid type [%s] supplied by test class"
                                        + " [%s] @Verifyica.ClassInterceptorSupplier"
                                        + " method",
                                o.getClass().getName(), testClass.getName()));
                    }
                }
            }
        }

        return classInterceptors;
    }

    /**
     * Method to destroy class interceptors
     *
     * @param engineContext engineContext
     */
    public void destroy(EngineContext engineContext) {
        for (Map.Entry<Class<?>, List<ClassInterceptor>> entry : mappedClassInterceptors.entrySet()) {
            List<ClassInterceptor> classInterceptorsReversed = ListSupport.copyAndReverse(entry.getValue());
            for (ClassInterceptor classInterceptor : classInterceptorsReversed) {
                try {
                    classInterceptor.destroy(engineContext);
                } catch (Throwable t) {
                    StackTracePrinter.printStackTrace(t, AnsiColor.TEXT_RED_BOLD, System.err);
                }
            }
        }

        List<ClassInterceptor> classInterceptorsReversed = ListSupport.copyAndReverse(classInterceptors);
        for (ClassInterceptor classInterceptor : classInterceptorsReversed) {
            try {
                classInterceptor.destroy(engineContext);
            } catch (Throwable t) {
                StackTracePrinter.printStackTrace(t, AnsiColor.TEXT_RED_BOLD, System.err);
            }
        }
    }

    /**
     * Method to filter ClassInterceptors
     *
     * @param classes classes
     */
    private void filter(List<Class<?>> classes) {
        Set<Class<?>> filteredClasses = new LinkedHashSet<>(classes);

        ofNullable(configuration
                        .getProperties()
                        .getProperty(Constants.ENGINE_AUTOWIRED_CLASS_INTERCEPTORS_EXCLUDE_REGEX))
                .ifPresent(regex -> {
                    LOGGER.trace("%s [%s]", Constants.ENGINE_AUTOWIRED_CLASS_INTERCEPTORS_EXCLUDE_REGEX, regex);

                    Pattern pattern = Pattern.compile(regex);
                    Matcher matcher = pattern.matcher("");

                    Iterator<Class<?>> iterator = filteredClasses.iterator();
                    while (iterator.hasNext()) {
                        Class<?> clazz = iterator.next();
                        matcher.reset(clazz.getName());
                        if (matcher.find()) {
                            LOGGER.trace("removing class interceptor [%s]", clazz.getName());
                            iterator.remove();
                        }
                    }
                });

        ofNullable(configuration
                        .getProperties()
                        .getProperty(Constants.ENGINE_AUTOWIRED_CLASS_INTERCEPTORS_INCLUDE_REGEX))
                .ifPresent(regex -> {
                    LOGGER.trace("%s [%s]", Constants.ENGINE_AUTOWIRED_CLASS_INTERCEPTORS_INCLUDE_REGEX, regex);

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

    /**
     * Method to validate only a single method per declared class is annotation with the given
     * annotation
     *
     * @param annotationClass annotationClass
     * @param methods methods
     */
    private static void validateSingleMethodPerClass(Class<?> annotationClass, List<Method> methods) {
        Precondition.notNull(annotationClass, "annotationClass is null");

        if (methods != null) {
            Set<Class<?>> classes = new HashSet<>();

            methods.forEach(method -> {
                if (classes.contains(method.getDeclaringClass())) {
                    String annotationDisplayName = "@Verifyica." + annotationClass.getSimpleName();
                    throw new TestClassDefinitionException(format(
                            "Test class [%s] contains more than one method" + " annotated with [%s]",
                            method.getDeclaringClass().getName(), annotationDisplayName));
                }

                classes.add(method.getDeclaringClass());
            });
        }
    }
}
