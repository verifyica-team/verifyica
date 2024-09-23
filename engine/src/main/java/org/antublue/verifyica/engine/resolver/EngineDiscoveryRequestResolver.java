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

package org.antublue.verifyica.engine.resolver;

import static java.lang.String.format;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Stream;
import org.antublue.verifyica.api.Argument;
import org.antublue.verifyica.api.Verifyica;
import org.antublue.verifyica.api.interceptor.ClassInterceptor;
import org.antublue.verifyica.api.interceptor.engine.ClassDefinition;
import org.antublue.verifyica.api.interceptor.engine.EngineInterceptorContext;
import org.antublue.verifyica.api.interceptor.engine.MethodDefinition;
import org.antublue.verifyica.engine.common.Precondition;
import org.antublue.verifyica.engine.common.Stopwatch;
import org.antublue.verifyica.engine.descriptor.ArgumentTestDescriptor;
import org.antublue.verifyica.engine.descriptor.ClassTestDescriptor;
import org.antublue.verifyica.engine.descriptor.TestMethodTestDescriptor;
import org.antublue.verifyica.engine.exception.EngineException;
import org.antublue.verifyica.engine.exception.TestClassDefinitionException;
import org.antublue.verifyica.engine.interceptor.ClassInterceptorManager;
import org.antublue.verifyica.engine.interceptor.EngineInterceptorManager;
import org.antublue.verifyica.engine.invocation.InvocationContext;
import org.antublue.verifyica.engine.logger.Logger;
import org.antublue.verifyica.engine.logger.LoggerFactory;
import org.antublue.verifyica.engine.support.ClassSupport;
import org.antublue.verifyica.engine.support.DisplayNameSupport;
import org.antublue.verifyica.engine.support.HierarchyTraversalMode;
import org.antublue.verifyica.engine.support.OrderSupport;
import org.junit.platform.engine.DiscoverySelector;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.discovery.ClassSelector;
import org.junit.platform.engine.discovery.ClasspathResourceSelector;
import org.junit.platform.engine.discovery.ClasspathRootSelector;
import org.junit.platform.engine.discovery.DirectorySelector;
import org.junit.platform.engine.discovery.FileSelector;
import org.junit.platform.engine.discovery.IterationSelector;
import org.junit.platform.engine.discovery.MethodSelector;
import org.junit.platform.engine.discovery.ModuleSelector;
import org.junit.platform.engine.discovery.PackageSelector;
import org.junit.platform.engine.discovery.UniqueIdSelector;
import org.junit.platform.engine.discovery.UriSelector;
import org.junit.platform.engine.support.descriptor.EngineDescriptor;

/** Class to implement EngineDiscoveryRequestResolver */
public class EngineDiscoveryRequestResolver {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(EngineDiscoveryRequestResolver.class);

    private static final List<Class<? extends DiscoverySelector>> DISCOVERY_SELECTORS_CLASSES;

    private static final Comparator<Object> CLASS_COMPARATOR =
            Comparator.comparing(clazz -> OrderSupport.getOrder((Class<?>) clazz))
                    .thenComparing(clazz -> DisplayNameSupport.getDisplayName((Class<?>) clazz));

    private final EngineInterceptorManager engineInterceptorManager;
    private final ClassInterceptorManager classInterceptorManager;
    private final EngineInterceptorContext engineInterceptorContext;

    static {
        DISCOVERY_SELECTORS_CLASSES = new ArrayList<>();
        DISCOVERY_SELECTORS_CLASSES.add(FileSelector.class);
        DISCOVERY_SELECTORS_CLASSES.add(DirectorySelector.class);
        DISCOVERY_SELECTORS_CLASSES.add(IterationSelector.class);
        DISCOVERY_SELECTORS_CLASSES.add(ClasspathResourceSelector.class);
        DISCOVERY_SELECTORS_CLASSES.add(ModuleSelector.class);
        DISCOVERY_SELECTORS_CLASSES.add(UriSelector.class);
        DISCOVERY_SELECTORS_CLASSES.add(ClasspathRootSelector.class);
        DISCOVERY_SELECTORS_CLASSES.add(PackageSelector.class);
        DISCOVERY_SELECTORS_CLASSES.add(ClassSelector.class);
        DISCOVERY_SELECTORS_CLASSES.add(MethodSelector.class);
        DISCOVERY_SELECTORS_CLASSES.add(UniqueIdSelector.class);
    }

    /**
     * Constructor
     *
     * @param invocationContext invocationContext
     */
    public EngineDiscoveryRequestResolver(InvocationContext invocationContext) {
        Precondition.notNull(invocationContext, "invocationContext is null");

        this.engineInterceptorManager = invocationContext.get(EngineInterceptorManager.class);
        this.classInterceptorManager = invocationContext.get(ClassInterceptorManager.class);
        this.engineInterceptorContext = invocationContext.get(EngineInterceptorContext.class);
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

        Stopwatch stopWatch = new Stopwatch();

        Map<Class<?>, List<Method>> testClassMethodMap = new TreeMap<>(CLASS_COMPARATOR);
        Map<Class<?>, List<Argument<?>>> testClassArgumentMap = new TreeMap<>(CLASS_COMPARATOR);
        Map<Class<?>, Set<Integer>> testClassArgumentIndexMap = new TreeMap<>(CLASS_COMPARATOR);

        try {
            List<DiscoverySelector> discoverySelectors = new ArrayList<>();

            if (LOGGER.isTraceEnabled()) {
                for (Class<? extends DiscoverySelector> discoverySelectorClass :
                        DISCOVERY_SELECTORS_CLASSES) {
                    discoverySelectors.addAll(
                            engineDiscoveryRequest.getSelectorsByType(discoverySelectorClass));
                }

                discoverySelectors.forEach(
                        discoverySelector ->
                                LOGGER.trace(
                                        "discoverySelector [%s]",
                                        discoverySelector.toIdentifier().isPresent()
                                                ? discoverySelector.toIdentifier().get()
                                                : "null"));
            }

            new ClasspathRootSelectorResolver().resolve(engineDiscoveryRequest, testClassMethodMap);
            new PackageSelectorResolver().resolve(engineDiscoveryRequest, testClassMethodMap);
            new ClassSelectorResolver().resolve(engineDiscoveryRequest, testClassMethodMap);
            new MethodSelectorResolver().resolve(engineDiscoveryRequest, testClassMethodMap);
            new UniqueIdSelectorResolver()
                    .resolve(engineDiscoveryRequest, testClassMethodMap, testClassArgumentIndexMap);

            resolveTestArguments(
                    testClassMethodMap, testClassArgumentMap, testClassArgumentIndexMap);

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

                                String testClassDisplayName =
                                        DisplayNameSupport.getDisplayName(testClass);

                                List<MethodDefinition> testMethodDefinitions = new ArrayList<>();

                                testMethods.forEach(
                                        method -> {
                                            String methodDisplayName =
                                                    DisplayNameSupport.getDisplayName(method);
                                            testMethodDefinitions.add(
                                                    new ConcreteMethodDefinition(
                                                            method, methodDisplayName));
                                        });

                                classDefinitions.add(
                                        new ConcreteClassDefinition(
                                                testClass,
                                                testClassDisplayName,
                                                testMethodDefinitions,
                                                testArguments,
                                                testArgumentParallelism));
                            });

            pruneClassDefinitions(classDefinitions);
            onTestDiscovery(classDefinitions);
            pruneClassDefinitions(classDefinitions);
            loadClassInterceptors(classDefinitions);
            buildEngineDescriptor(classDefinitions, engineDescriptor);
            onInitialize(classDefinitions);
        } catch (EngineException e) {
            throw e;
        } catch (Throwable t) {
            throw new EngineException(t);
        } finally {
            stopWatch.stop();
            LOGGER.trace(
                    "resolveSelectors() elapsedTime [%d] ms", stopWatch.elapsedTime().toMillis());
        }
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
            Map<Class<?>, List<Argument<?>>> testClassArgumentMap,
            Map<Class<?>, Set<Integer>> argumentIndexMap)
            throws Throwable {
        LOGGER.trace("resolveTestArguments()");

        Stopwatch stopWatch = new Stopwatch();

        for (Class<?> testClass : testClassMethodMap.keySet()) {
            List<Argument<?>> testArguments = getTestArguments(testClass);
            Set<Integer> testArgumentIndices = argumentIndexMap.get(testClass);
            if (testArgumentIndices != null) {
                List<Argument<?>> specificTestArguments = new ArrayList<>();
                for (int i = 0; i < testArguments.size(); i++) {
                    if (testArgumentIndices.contains(i)) {
                        specificTestArguments.add(testArguments.get(i));
                    }
                }
                testClassArgumentMap.put(testClass, specificTestArguments);
            } else {
                testClassArgumentMap.put(testClass, testArguments);
            }
        }

        LOGGER.trace(
                "resolveTestArguments() elapsedTime [%d] ms", stopWatch.elapsedTime().toMillis());
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

        Stopwatch stopWatch = new Stopwatch();

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

        LOGGER.trace("getTestArguments() elapsedTime [%d] ms", stopWatch.elapsedTime().toMillis());

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
                ClassSupport.findMethods(
                        testClass,
                        ResolverPredicates.ARGUMENT_SUPPLIER_METHOD,
                        HierarchyTraversalMode.BOTTOM_UP);

        validateSingleMethodPerClass(Verifyica.ArgumentSupplier.class, methods);

        return methods.get(0);
    }

    /**
     * Method to invoke engine interceptors
     *
     * @param classDefinitions classDefinitions
     * @throws Throwable Throwable
     */
    private void onTestDiscovery(List<ClassDefinition> classDefinitions) throws Throwable {
        LOGGER.trace("onTestDiscovery()");

        if (!classDefinitions.isEmpty()) {
            engineInterceptorManager.onTestDiscovery(engineInterceptorContext, classDefinitions);
        }
    }

    private void onInitialize(List<ClassDefinition> classDefinitions) throws Throwable {
        LOGGER.trace("onInitialize()");

        if (!classDefinitions.isEmpty()) {
            engineInterceptorManager.onInitialize(engineInterceptorContext);
        }
    }

    /**
     * Method to prune ClassDefinitions for test classes without arguments or test methods
     *
     * @param classDefinitions classDefinitions
     */
    private static void pruneClassDefinitions(List<ClassDefinition> classDefinitions) {
        LOGGER.trace("pruneClassDefinitions()");

        classDefinitions.removeIf(
                classDefinition ->
                        classDefinition.getArguments().isEmpty()
                                || classDefinition.getTestMethodDefinitions().isEmpty());
    }

    /**
     * Method to load test class ClassInterceptors
     *
     * @param classDefinitions classDefinitions
     * @throws Throwable Throwable
     */
    private void loadClassInterceptors(List<ClassDefinition> classDefinitions) throws Throwable {
        LOGGER.trace("loadClassInterceptors()");

        for (ClassDefinition classDefinition : classDefinitions) {
            Class<?> testClass = classDefinition.getTestClass();

            List<Method> classInterceptorSupplierMethods =
                    ClassSupport.findMethods(
                            testClass,
                            ResolverPredicates.CLASS_INTERCEPTOR_SUPPLIER,
                            HierarchyTraversalMode.BOTTOM_UP);

            validateSingleMethodPerClass(
                    Verifyica.ClassInterceptorSupplier.class, classInterceptorSupplierMethods);

            if (!classInterceptorSupplierMethods.isEmpty()) {
                Method classInterceptorSupplierMethod = classInterceptorSupplierMethods.get(0);
                Object object = classInterceptorSupplierMethod.invoke(null);

                if (object == null) {
                    throw new TestClassDefinitionException(
                            format(
                                    "Null Object supplied by test class"
                                            + " [%s] @Verifyica.ClassInterceptorSupplier"
                                            + " method",
                                    testClass.getName()));
                } else if (object instanceof ClassInterceptor) {
                    classInterceptorManager.register(testClass, (ClassInterceptor) object);
                } else if (object.getClass().isArray()) {
                    Object[] objects = (Object[]) object;
                    if (objects.length > 0) {
                        int index = 0;
                        for (Object o : objects) {
                            if (o instanceof ClassInterceptor) {
                                classInterceptorManager.register(testClass, (ClassInterceptor) o);
                            } else {
                                throw new TestClassDefinitionException(
                                        format(
                                                "Invalid type [%s] supplied by test class [%s]"
                                                    + " @Verifyica.ClassInterceptorSupplier method"
                                                    + " at index [%d]",
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
                            classInterceptorManager.register(testClass, (ClassInterceptor) o);
                        } else {
                            throw new TestClassDefinitionException(
                                    format(
                                            "Invalid type [%s] supplied by test class"
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
     * @param classDefinitions classDefinitions
     * @param engineDescriptor engineDescriptor
     */
    private static void buildEngineDescriptor(
            List<ClassDefinition> classDefinitions, EngineDescriptor engineDescriptor) {
        LOGGER.trace("buildEngineDescriptor()");

        Stopwatch stopWatch = new Stopwatch();

        for (ClassDefinition classDefinition : classDefinitions) {
            Class<?> testClass = classDefinition.getTestClass();

            UniqueId classTestDescriptorUniqueId =
                    engineDescriptor.getUniqueId().append("class", testClass.getName());

            List<Method> prepareMethods =
                    ClassSupport.findMethods(
                            testClass,
                            ResolverPredicates.PREPARE_METHOD,
                            HierarchyTraversalMode.TOP_DOWN);

            validateSingleMethodPerClass(Verifyica.Prepare.class, prepareMethods);

            List<Method> concludeMethods =
                    ClassSupport.findMethods(
                            testClass,
                            ResolverPredicates.CONCLUDE_METHOD,
                            HierarchyTraversalMode.BOTTOM_UP);

            validateSingleMethodPerClass(Verifyica.Conclude.class, concludeMethods);

            ClassTestDescriptor classTestDescriptor =
                    new ClassTestDescriptor(
                            classTestDescriptorUniqueId,
                            classDefinition.getDisplayName(),
                            testClass,
                            classDefinition.getArgumentParallelism(),
                            prepareMethods,
                            concludeMethods);

            engineDescriptor.addChild(classTestDescriptor);

            int testArgumentIndex = 0;
            for (Argument<?> testArgument : classDefinition.getArguments()) {
                UniqueId argumentTestDescriptorUniqueId =
                        classTestDescriptorUniqueId.append(
                                "argument", String.valueOf(testArgumentIndex));

                List<Method> beforeAllMethods =
                        ClassSupport.findMethods(
                                testClass,
                                ResolverPredicates.BEFORE_ALL_METHOD,
                                HierarchyTraversalMode.TOP_DOWN);

                validateSingleMethodPerClass(Verifyica.BeforeAll.class, beforeAllMethods);

                List<Method> afterAllMethods =
                        ClassSupport.findMethods(
                                testClass,
                                ResolverPredicates.AFTER_ALL_METHOD,
                                HierarchyTraversalMode.BOTTOM_UP);

                validateSingleMethodPerClass(Verifyica.AfterAll.class, afterAllMethods);

                ArgumentTestDescriptor argumentTestDescriptor =
                        new ArgumentTestDescriptor(
                                argumentTestDescriptorUniqueId,
                                testArgument.getName(),
                                testClass,
                                testArgumentIndex,
                                testArgument,
                                beforeAllMethods,
                                afterAllMethods);

                classTestDescriptor.addChild(argumentTestDescriptor);

                for (MethodDefinition testMethodDefinition :
                        classDefinition.getTestMethodDefinitions()) {
                    UniqueId testMethodDescriptorUniqueId =
                            argumentTestDescriptorUniqueId.append(
                                    "method", testMethodDefinition.getMethod().getName());

                    List<Method> beforeEachMethods =
                            ClassSupport.findMethods(
                                    testClass,
                                    ResolverPredicates.BEFORE_EACH_METHOD,
                                    HierarchyTraversalMode.TOP_DOWN);

                    validateSingleMethodPerClass(Verifyica.BeforeEach.class, beforeEachMethods);

                    List<Method> afterEachMethods =
                            ClassSupport.findMethods(
                                    testClass,
                                    ResolverPredicates.AFTER_EACH_METHOD,
                                    HierarchyTraversalMode.BOTTOM_UP);

                    validateSingleMethodPerClass(Verifyica.AfterEach.class, beforeEachMethods);

                    TestMethodTestDescriptor testMethodTestDescriptor =
                            new TestMethodTestDescriptor(
                                    testMethodDescriptorUniqueId,
                                    testMethodDefinition.getDisplayName(),
                                    beforeEachMethods,
                                    testMethodDefinition.getMethod(),
                                    afterEachMethods);

                    argumentTestDescriptor.addChild(testMethodTestDescriptor);
                }

                testArgumentIndex++;
            }
        }

        LOGGER.trace(
                "buildEngineDescriptor() elapsedTime [%d] ms", stopWatch.elapsedTime().toMillis());
    }

    /**
     * Method to validate only a single method per declared class is annotation with the given
     * annotation
     *
     * @param annotationClass annotationClass
     * @param methods methods
     */
    private static void validateSingleMethodPerClass(
            Class<?> annotationClass, List<Method> methods) {
        Precondition.notNull(annotationClass, "annotationClass is null");

        if (methods != null) {
            Set<Class<?>> classes = new HashSet<>();

            methods.forEach(
                    method -> {
                        if (classes.contains(method.getDeclaringClass())) {
                            String annotationDisplayName =
                                    "@Verifyica." + annotationClass.getSimpleName();
                            throw new TestClassDefinitionException(
                                    format(
                                            "Test class [%s] contains more than one method"
                                                    + " annotated with [%s]",
                                            method.getDeclaringClass().getName(),
                                            annotationDisplayName));
                        }

                        classes.add(method.getDeclaringClass());
                    });
        }
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
