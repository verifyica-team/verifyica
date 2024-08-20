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

package org.antublue.verifyica.engine.discovery;

import static java.lang.String.format;
import static org.junit.platform.engine.Filter.composeFilters;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.antublue.verifyica.api.Argument;
import org.antublue.verifyica.api.Verifyica;
import org.antublue.verifyica.api.interceptor.ClassInterceptor;
import org.antublue.verifyica.api.interceptor.engine.ClassDefinition;
import org.antublue.verifyica.engine.common.StopWatch;
import org.antublue.verifyica.engine.context.DefaultEngineContext;
import org.antublue.verifyica.engine.context.DefaultEngineInterceptorContext;
import org.antublue.verifyica.engine.descriptor.ArgumentTestDescriptor;
import org.antublue.verifyica.engine.descriptor.ClassTestDescriptor;
import org.antublue.verifyica.engine.descriptor.TestMethodTestDescriptor;
import org.antublue.verifyica.engine.exception.EngineException;
import org.antublue.verifyica.engine.exception.TestClassDefinitionException;
import org.antublue.verifyica.engine.exception.UncheckedClassNotFoundException;
import org.antublue.verifyica.engine.interceptor.ClassInterceptorRegistry;
import org.antublue.verifyica.engine.interceptor.EngineInterceptorRegistry;
import org.antublue.verifyica.engine.logger.Logger;
import org.antublue.verifyica.engine.logger.LoggerFactory;
import org.antublue.verifyica.engine.support.ClassPathSupport;
import org.antublue.verifyica.engine.support.DisplayNameSupport;
import org.antublue.verifyica.engine.support.HierarchyTraversalMode;
import org.antublue.verifyica.engine.support.MethodSupport;
import org.antublue.verifyica.engine.support.OrderSupport;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.discovery.ClassNameFilter;
import org.junit.platform.engine.discovery.ClassSelector;
import org.junit.platform.engine.discovery.ClasspathRootSelector;
import org.junit.platform.engine.discovery.MethodSelector;
import org.junit.platform.engine.discovery.PackageNameFilter;
import org.junit.platform.engine.discovery.PackageSelector;
import org.junit.platform.engine.discovery.UniqueIdSelector;
import org.junit.platform.engine.support.descriptor.EngineDescriptor;

/** Class to implement EngineDiscoveryRequestResolver */
@SuppressWarnings("unchecked")
public class EngineDiscoveryRequestResolver {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(EngineDiscoveryRequestResolver.class);

    private static Comparator<Object> getClassComparator() {
        return Comparator.comparing(clazz -> OrderSupport.getOrder((Class<?>) clazz))
                .thenComparing(clazz -> DisplayNameSupport.getDisplayName((Class<?>) clazz));
    }

    /** Constructor */
    public EngineDiscoveryRequestResolver() {
        // INTENTIONALLY BLANK
    }

    /**
     * Method to resolve the engine discovery request, building an engine descriptor
     *
     * @param engineDiscoveryRequest engineDiscoveryRequest
     * @param engineDescriptor engineDescriptor
     */
    public void resolveSelectors(
            EngineDiscoveryRequest engineDiscoveryRequest, EngineDescriptor engineDescriptor) {
        LOGGER.trace("resolveSelectors()");

        StopWatch stopWatch = new StopWatch();

        Map<Class<?>, List<Method>> testClassMethodMap = new TreeMap<>(getClassComparator());
        Map<Class<?>, List<Argument<?>>> testClassArgumentMap = new TreeMap<>(getClassComparator());
        Map<Class<?>, Set<Integer>> testClassArgumentIndexMap = new TreeMap<>(getClassComparator());

        try {
            resolveClasspathRootSelectors(engineDiscoveryRequest, testClassMethodMap);
            resolvePackageSelectors(engineDiscoveryRequest, testClassMethodMap);
            resolveClassSelectors(engineDiscoveryRequest, testClassMethodMap);
            resolveMethodSelectors(engineDiscoveryRequest, testClassMethodMap);
            resolveUniqueIdSelectors(
                    engineDiscoveryRequest, testClassMethodMap, testClassArgumentIndexMap);

            resolveTestArguments(testClassMethodMap, testClassArgumentMap);

            List<ClassDefinition> classDefinitions = new ArrayList<>();

            testClassMethodMap
                    .keySet()
                    .forEach(
                            testClass -> {
                                List<Argument<?>> testArguments =
                                        testClassArgumentMap.get(testClass);

                                List<Method> testMethods = testClassMethodMap.get(testClass);

                                int testArgumentParallelism = getTestArgumentParallelism(testClass);

                                OrderSupport.orderMethods(testMethods);

                                classDefinitions.add(
                                        new DefaultClassDefinition(
                                                testClass,
                                                testMethods,
                                                testArguments,
                                                testArgumentParallelism));
                            });

            afterTestDiscovery(classDefinitions);
            prune(classDefinitions);
            loadClassInterceptors(classDefinitions);
            buildEngineDescriptor(engineDescriptor, classDefinitions);
        } catch (EngineException e) {
            throw e;
        } catch (Throwable t) {
            throw new EngineException(t);
        } finally {
            stopWatch.stop();
            LOGGER.trace("resolveSelectors() [%d] ms", stopWatch.elapsedTime().toMillis());
        }
    }

    /**
     * Method to resolve ClassPathSelectors
     *
     * @param engineDiscoveryRequest engineDiscoveryRequest
     * @param classMethodMap classMethodMap
     */
    private static void resolveClasspathRootSelectors(
            EngineDiscoveryRequest engineDiscoveryRequest,
            Map<Class<?>, List<Method>> classMethodMap) {
        LOGGER.trace("resolveClasspathRootSelectors()");

        StopWatch stopWatch = new StopWatch();

        engineDiscoveryRequest
                .getSelectorsByType(ClasspathRootSelector.class)
                .forEach(
                        classpathRootSelector -> {
                            List<Class<?>> testClasses =
                                    ClassPathSupport.findAllClasses(
                                            classpathRootSelector.getClasspathRoot(),
                                            Predicates.TEST_CLASS);

                            List<? extends ClassNameFilter> classNameFilters =
                                    engineDiscoveryRequest.getFiltersByType(ClassNameFilter.class);

                            Predicate<String> classNamePredicate =
                                    composeFilters(classNameFilters).toPredicate();

                            List<? extends PackageNameFilter> packageNameFilters =
                                    engineDiscoveryRequest.getFiltersByType(
                                            PackageNameFilter.class);

                            Predicate<String> packageNamePredicate =
                                    composeFilters(packageNameFilters).toPredicate();

                            testClasses.forEach(
                                    testClass -> {
                                        if (classNamePredicate.test(testClass.getName())
                                                && packageNamePredicate.test(
                                                        testClass.getPackage().getName())) {
                                            classMethodMap
                                                    .computeIfAbsent(
                                                            testClass, method -> new ArrayList<>())
                                                    .addAll(
                                                            MethodSupport.findMethods(
                                                                    testClass,
                                                                    Predicates.TEST_METHOD,
                                                                    HierarchyTraversalMode
                                                                            .BOTTOM_UP));
                                        }
                                    });
                        });

        LOGGER.trace("resolveClasspathRootSelectors() [%d] ms", stopWatch.elapsedTime().toMillis());
    }

    /**
     * Method to resolve PackageSelectors
     *
     * @param engineDiscoveryRequest engineDiscoveryRequest
     * @param classMethodMap classMethodMap
     */
    private static void resolvePackageSelectors(
            EngineDiscoveryRequest engineDiscoveryRequest,
            Map<Class<?>, List<Method>> classMethodMap) {
        LOGGER.trace("resolvePackageSelectors()");

        StopWatch stopWatch = new StopWatch();

        engineDiscoveryRequest
                .getSelectorsByType(PackageSelector.class)
                .forEach(
                        packageSelector -> {
                            String packageName = packageSelector.getPackageName();

                            LOGGER.trace("packageName [%s]", packageName);

                            List<Class<?>> testClasses =
                                    ClassPathSupport.findAllClasses(
                                            packageName, Predicates.TEST_CLASS);

                            testClasses.forEach(
                                    testClass ->
                                            classMethodMap
                                                    .computeIfAbsent(
                                                            testClass, method -> new ArrayList<>())
                                                    .addAll(
                                                            MethodSupport.findMethods(
                                                                    testClass,
                                                                    Predicates.TEST_METHOD,
                                                                    HierarchyTraversalMode
                                                                            .BOTTOM_UP)));
                        });

        LOGGER.trace("resolvePackageSelectors() [%d] ms", stopWatch.elapsedTime().toMillis());
    }

    /**
     * Method to resolve ClassSelectors
     *
     * @param engineDiscoveryRequest engineDiscoveryRequest
     * @param classMethodMap classMethodMap
     */
    private static void resolveClassSelectors(
            EngineDiscoveryRequest engineDiscoveryRequest,
            Map<Class<?>, List<Method>> classMethodMap) {
        LOGGER.trace("resolveClassSelectors()");

        StopWatch stopWatch = new StopWatch();

        engineDiscoveryRequest
                .getSelectorsByType(ClassSelector.class)
                .forEach(
                        classSelector -> {
                            Class<?> testClass = classSelector.getJavaClass();

                            if (Predicates.TEST_CLASS.test(testClass)) {
                                classMethodMap
                                        .computeIfAbsent(testClass, method -> new ArrayList())
                                        .addAll(
                                                MethodSupport.findMethods(
                                                        testClass,
                                                        Predicates.TEST_METHOD,
                                                        HierarchyTraversalMode.BOTTOM_UP));
                            }
                        });

        LOGGER.trace("resolveClassSelectors() [%d] ms", stopWatch.elapsedTime().toMillis());
    }

    /**
     * Method to resolve MethodSelectors
     *
     * @param engineDiscoveryRequest engineDiscoveryRequest
     * @param classMethodMap classMethodMap
     */
    private static void resolveMethodSelectors(
            EngineDiscoveryRequest engineDiscoveryRequest,
            Map<Class<?>, List<Method>> classMethodMap) {
        LOGGER.trace("resolveMethodSelectors()");

        StopWatch stopWatch = new StopWatch();

        engineDiscoveryRequest
                .getSelectorsByType(MethodSelector.class)
                .forEach(
                        methodSelector -> {
                            Class<?> testClass = methodSelector.getJavaClass();
                            Method testMethod = methodSelector.getJavaMethod();

                            if (Predicates.TEST_CLASS.test(testClass)
                                    && Predicates.TEST_METHOD.test(testMethod)) {
                                classMethodMap
                                        .computeIfAbsent(testClass, method -> new ArrayList())
                                        .add(testMethod);
                            }
                        });

        LOGGER.trace("resolveMethodSelectors() [%d] ms", stopWatch.elapsedTime().toMillis());
    }

    /**
     * Method to resolve UniqueIdSelectors
     *
     * @param engineDiscoveryRequest engineDiscoveryRequest
     * @param classMethodMap classMethodMap
     * @param argumentIndexMap argumentIndexMap
     */
    private static void resolveUniqueIdSelectors(
            EngineDiscoveryRequest engineDiscoveryRequest,
            Map<Class<?>, List<Method>> classMethodMap,
            Map<Class<?>, Set<Integer>> argumentIndexMap) {
        LOGGER.trace("resolveUniqueIdSelectors()");

        StopWatch stopWatch = new StopWatch();

        engineDiscoveryRequest
                .getSelectorsByType(UniqueIdSelector.class)
                .forEach(
                        uniqueIdSelector -> {
                            UniqueId uniqueId = uniqueIdSelector.getUniqueId();
                            List<UniqueId.Segment> segments = uniqueId.getSegments();

                            LOGGER.trace("uniqueId [%s]", uniqueId);

                            // Specific argument selected
                            if (segments.size() == 3) {
                                UniqueId.Segment classSegment = segments.get(1);
                                UniqueId.Segment argumentSegment = segments.get(2);

                                Class<?> testClass = null;

                                try {
                                    testClass =
                                            Thread.currentThread()
                                                    .getContextClassLoader()
                                                    .loadClass(classSegment.getValue());
                                } catch (ClassNotFoundException e) {
                                    UncheckedClassNotFoundException.throwUnchecked(e);
                                }

                                classMethodMap
                                        .computeIfAbsent(testClass, method -> new ArrayList<>())
                                        .addAll(
                                                MethodSupport.findMethods(
                                                        testClass,
                                                        Predicates.TEST_METHOD,
                                                        HierarchyTraversalMode.BOTTOM_UP));

                                argumentIndexMap
                                        .computeIfAbsent(testClass, clazz -> new LinkedHashSet<>())
                                        .add(Integer.parseInt(argumentSegment.getValue()));
                            } else {
                                segments.forEach(
                                        segment -> {
                                            String segmentType = segment.getType();

                                            if (segmentType.equals(
                                                    ClassTestDescriptor.class.getName())) {
                                                String javaClassName = segment.getValue();

                                                Class<?> testClass = null;

                                                try {
                                                    testClass =
                                                            Thread.currentThread()
                                                                    .getContextClassLoader()
                                                                    .loadClass(javaClassName);
                                                } catch (ClassNotFoundException e) {
                                                    UncheckedClassNotFoundException.throwUnchecked(
                                                            e);
                                                }

                                                classMethodMap
                                                        .computeIfAbsent(
                                                                testClass,
                                                                method -> new ArrayList<>())
                                                        .addAll(
                                                                MethodSupport.findMethods(
                                                                        testClass,
                                                                        Predicates.TEST_METHOD,
                                                                        HierarchyTraversalMode
                                                                                .BOTTOM_UP));
                                            }
                                        });
                            }
                        });

        LOGGER.trace("resolveUniqueIdSelectors() [%d] ms", stopWatch.elapsedTime().toMillis());
    }

    /**
     * Method to resolve test class test arguments
     *
     * @param testClassMethodMap testClassMethodMap
     * @param testClassArgumentMap testClassArgumentMap
     * @throws Throwable Throwable
     */
    private static void resolveTestArguments(
            Map<Class<?>, List<Method>> testClassMethodMap,
            Map<Class<?>, List<Argument<?>>> testClassArgumentMap)
            throws Throwable {
        LOGGER.trace("resolveTestArguments()");

        StopWatch stopWatch = new StopWatch();

        for (Class<?> testClass : testClassMethodMap.keySet()) {
            List<Argument<?>> testArguments = getTestArguments(testClass);
            testClassArgumentMap.put(testClass, testArguments);
        }

        LOGGER.trace("resolveTestArguments() [%d] ms", stopWatch.elapsedTime().toMillis());
    }

    /**
     * Method to get test class test arguments
     *
     * @param testClass testClass
     * @return a List of arguments
     * @throws Throwable Throwable
     */
    private static List<Argument<?>> getTestArguments(Class<?> testClass) throws Throwable {
        LOGGER.trace("getTestArguments() testClass [%s]", testClass.getName());

        StopWatch stopWatch = new StopWatch();

        List<Argument<?>> testArguments = new ArrayList<>();

        Object object = getArgumentSupplierMethod(testClass).invoke(null, (Object[]) null);
        if (object == null) {
            return testArguments;
        } else if (object.getClass().isArray()) {
            Object[] objects = (Object[]) object;
            if (objects.length > 0) {
                int index = 0;
                for (Object o : objects) {
                    if (o instanceof Argument<?>) {
                        testArguments.add((Argument<?>) o);
                    } else {
                        testArguments.add(Argument.of("argument[" + index + "]", o));
                    }
                    index++;
                }
            } else {
                return testArguments;
            }
        } else if (object instanceof Argument<?>) {
            testArguments.add((Argument<?>) object);
            return testArguments;
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

            long index = 0;
            while (iterator.hasNext()) {
                Object o = iterator.next();
                if (o instanceof Argument<?>) {
                    testArguments.add((Argument<?>) o);
                } else {
                    testArguments.add(Argument.of("argument[" + index + "]", o));
                }
                index++;
            }
        } else {
            testArguments.add(Argument.of("argument[0]", object));
        }

        LOGGER.trace("getTestArguments() [%d] ms", stopWatch.elapsedTime().toMillis());

        return testArguments;
    }

    /**
     * Method to get a class argument supplier method
     *
     * @param testClass testClass
     * @return the argument supplier method
     */
    private static Method getArgumentSupplierMethod(Class<?> testClass) {
        LOGGER.trace("getArgumentSupplierMethod() testClass [%s]", testClass.getName());

        List<Method> methods =
                MethodSupport.findMethods(
                        testClass,
                        Predicates.ARGUMENT_SUPPLIER_METHOD,
                        HierarchyTraversalMode.BOTTOM_UP);

        if (methods.size() > 1) {
            // Defensive code to get the first argument supplier method
            final Class<?> declaredClass = methods.get(0).getDeclaringClass();
            methods.removeIf(m -> !m.getDeclaringClass().equals(declaredClass));
            OrderSupport.orderMethods(methods);
        }

        return methods.get(0);
    }

    /**
     * Method to invoke engine interceptors
     *
     * @param classDefinitions classDefinitions
     * @throws Throwable Throwable
     */
    private static void afterTestDiscovery(List<ClassDefinition> classDefinitions)
            throws Throwable {
        LOGGER.trace("afterTestDiscovery()");

        DefaultEngineInterceptorContext defaultEngineInterceptorContext =
                new DefaultEngineInterceptorContext(DefaultEngineContext.getInstance());

        EngineInterceptorRegistry.getInstance()
                .onTestDiscovery(defaultEngineInterceptorContext, classDefinitions);

        for (ClassDefinition classDefinition : classDefinitions) {
            EngineInterceptorRegistry.getInstance()
                    .onTestDiscovery(defaultEngineInterceptorContext, classDefinition);
        }
    }

    /**
     * Method to prune test classes without arguments or test methods
     *
     * @param classDefinitions classDefinitions
     */
    private static void prune(List<ClassDefinition> classDefinitions) {
        LOGGER.trace("prune()");

        classDefinitions.removeIf(
                classDefinition ->
                        classDefinition.getTestArguments().isEmpty()
                                || classDefinition.getTestMethods().isEmpty());
    }

    /**
     * Method to load test class ClassInterceptors
     *
     * @param classDefinitions classDefinitions
     * @throws Throwable Throwable
     */
    private static void loadClassInterceptors(List<ClassDefinition> classDefinitions)
            throws Throwable {
        LOGGER.trace("loadClassInterceptors()");

        for (ClassDefinition classDefinition : classDefinitions) {
            Class<?> testClass = classDefinition.getTestClass();

            List<Method> classInterceptorSupplierMethods =
                    MethodSupport.findMethods(
                            testClass,
                            Predicates.CLASS_INTERCEPTOR_SUPPLIER,
                            HierarchyTraversalMode.BOTTOM_UP);

            for (Method classInterceptorMethod : classInterceptorSupplierMethods) {
                Object object = classInterceptorMethod.invoke(null);

                if (object instanceof ClassInterceptor) {
                    ClassInterceptorRegistry.getInstance()
                            .register(testClass, (ClassInterceptor) object);
                } else if (object.getClass().isArray()) {
                    Object[] objects = (Object[]) object;
                    if (objects.length > 0) {
                        int index = 0;
                        for (Object o : objects) {
                            if (o instanceof ClassInterceptor) {
                                ClassInterceptorRegistry.getInstance()
                                        .register(testClass, (ClassInterceptor) o);
                            } else {
                                throw new TestClassDefinitionException(
                                        format(
                                                "Invalid argument type [%s] supplied by test class"
                                                    + " [%s] @Verifyica.ClassInterceptorSupplier"
                                                    + " method at index [%d]",
                                                o.getClass().getName(),
                                                testClass.getName(),
                                                index));
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
                            ClassInterceptorRegistry.getInstance()
                                    .register(testClass, (ClassInterceptor) o);
                        } else {
                            throw new TestClassDefinitionException(
                                    format(
                                            "Invalid argument type [%s] supplied by test class"
                                                    + " [%s] @Verifyica.ClassInterceptorSupplier"
                                                    + " method",
                                            o.getClass().getName(), testClass.getName()));
                        }
                    }
                }
            }
        }
    }

    /**
     * Method to build the EngineDescriptor
     *
     * @param engineDescriptor engineDescriptor
     * @param classDefinitions classDefinitions
     */
    private static void buildEngineDescriptor(
            EngineDescriptor engineDescriptor, List<ClassDefinition> classDefinitions) {
        LOGGER.trace("buildEngineDescriptor()");

        StopWatch stopWatch = new StopWatch();

        for (ClassDefinition classDefinition : classDefinitions) {
            Class<?> testClass = classDefinition.getTestClass();

            UniqueId classTestDescriptorUniqueId =
                    engineDescriptor.getUniqueId().append("class", testClass.getName());

            ClassTestDescriptor classTestDescriptor =
                    new ClassTestDescriptor(
                            classTestDescriptorUniqueId,
                            DisplayNameSupport.getDisplayName(testClass),
                            classDefinition.getTestArgumentParallelism(),
                            testClass,
                            MethodSupport.findMethods(
                                    testClass,
                                    Predicates.PREPARE_METHOD,
                                    HierarchyTraversalMode.TOP_DOWN),
                            MethodSupport.findMethods(
                                    testClass,
                                    Predicates.CONCLUDE_METHOD,
                                    HierarchyTraversalMode.BOTTOM_UP));

            engineDescriptor.addChild(classTestDescriptor);

            int testArgumentIndex = 0;
            for (Argument<?> testArgument : classDefinition.getTestArguments()) {
                UniqueId argumentTestDescriptorUniqueId =
                        classTestDescriptorUniqueId.append(
                                "argument", String.valueOf(testArgumentIndex));

                ArgumentTestDescriptor argumentTestDescriptor =
                        new ArgumentTestDescriptor(
                                argumentTestDescriptorUniqueId,
                                testArgument.getName(),
                                testClass,
                                testArgumentIndex,
                                testArgument,
                                MethodSupport.findMethods(
                                        testClass,
                                        Predicates.BEFORE_ALL_METHOD,
                                        HierarchyTraversalMode.TOP_DOWN),
                                MethodSupport.findMethods(
                                        testClass,
                                        Predicates.AFTER_ALL_METHOD,
                                        HierarchyTraversalMode.BOTTOM_UP));

                classTestDescriptor.addChild(argumentTestDescriptor);

                for (Method testMethod : classDefinition.getTestMethods()) {
                    UniqueId testMethodDescriptorUniqueId =
                            argumentTestDescriptorUniqueId.append("test", testMethod.getName());

                    TestMethodTestDescriptor testMethodTestDescriptor =
                            new TestMethodTestDescriptor(
                                    testMethodDescriptorUniqueId,
                                    DisplayNameSupport.getDisplayName(testMethod),
                                    MethodSupport.findMethods(
                                            testClass,
                                            Predicates.BEFORE_EACH_METHOD,
                                            HierarchyTraversalMode.TOP_DOWN),
                                    testMethod,
                                    MethodSupport.findMethods(
                                            testClass,
                                            Predicates.AFTER_EACH_METHOD,
                                            HierarchyTraversalMode.BOTTOM_UP));

                    argumentTestDescriptor.addChild(testMethodTestDescriptor);
                }

                testArgumentIndex++;
            }
        }

        LOGGER.trace("buildEngineDescriptor() [%d] ms", stopWatch.elapsedTime().toMillis());
    }

    /**
     * Method to get test class parallelism
     *
     * @param testClass testClass
     * @return test class parallelism
     */
    private static int getTestArgumentParallelism(Class<?> testClass) {
        LOGGER.trace("getTestArgumentParallelism() testClass [%s]", testClass.getName());

        Method argumentSupplierMethod = getArgumentSupplierMethod(testClass);

        Verifyica.ArgumentSupplier annotation =
                argumentSupplierMethod.getAnnotation(Verifyica.ArgumentSupplier.class);

        int parallelism = Math.max(annotation.parallelism(), 1);

        LOGGER.trace("testClass [%s] parallelism [%d]", testClass.getName(), parallelism);

        return parallelism;
    }
}
