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

package org.antublue.verifyica.engine.support;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Predicate;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import org.antublue.verifyica.engine.common.Precondition;
import org.antublue.verifyica.engine.exception.UncheckedURISyntaxException;
import org.junit.platform.commons.support.ReflectionSupport;

/** Class to implement ClassPathSupport */
public class ClassSupport {

    private static final ReentrantLock LOCK = new ReentrantLock(true);
    private static List<URI> URIS;

    /** Constructor */
    private ClassSupport() {
        // INTENTIONALLY BLANK
    }

    /**
     * Method to get a List of classpath URIs
     *
     * @return a List of classpath URIs
     */
    public static List<URI> getClasspathURIs() {
        LOCK.lock();
        try {
            if (URIS == null) {
                URIS = new ArrayList<>();
                Set<URI> uriSet = new LinkedHashSet<>();
                String classpath = System.getProperty("java.class.path");
                String[] paths = classpath.split(File.pathSeparator);
                for (String path : paths) {
                    uriSet.add(new File(path).toURI());
                }
                URIS.addAll(uriSet);
            }
            return URIS;
        } finally {
            LOCK.unlock();
        }
    }

    /**
     * Method to scan the Java classpath and return a list of classes matching the Predicate
     *
     * @param predicate predicate
     * @return a List of Classes
     */
    public static List<Class<?>> findAllClasses(Predicate<Class<?>> predicate) {
        Precondition.notNull(predicate, "predicate is null");

        Set<Class<?>> set = new LinkedHashSet<>();

        getClasspathURIs()
                .forEach(
                        uri ->
                                set.addAll(
                                        ReflectionSupport.findAllClassesInClasspathRoot(
                                                uri, predicate, classNameFilter -> true)));

        return new ArrayList<>(set);
    }

    /**
     * Method to scan the Java classpath URI and return a list of classes matching the Predicate
     *
     * @param uri uri
     * @param predicate predicate
     * @return a List of Classes
     */
    public static List<Class<?>> findAllClasses(URI uri, Predicate<Class<?>> predicate) {
        Precondition.notNull(uri, "uri is null");
        Precondition.notNull(predicate, "predicate is null");

        return new ArrayList<>(
                ReflectionSupport.findAllClassesInClasspathRoot(
                        uri, predicate, classNameFilter -> true));
    }

    /**
     * Method to scan the Java classpath and return a list of classes matching the package name and
     * Predicate
     *
     * @param packageName packageName
     * @param predicate predicate
     * @return a List of Classes
     */
    public static List<Class<?>> findAllClasses(String packageName, Predicate<Class<?>> predicate) {
        Precondition.notNull(packageName, "packageName is null");
        Precondition.notNull(predicate, "predicate is null");

        return findAllClasses(predicate).stream()
                .filter(clazz -> packageName.equals(clazz.getPackage().getName()))
                .collect(Collectors.toList());
    }

    /**
     * Method to find fields of a Class
     *
     * @param clazz clazz
     * @param predicate predicate
     * @param hierarchyTraversalMode hierarchyTraversalMode
     * @return a List of Fields
     */
    public static List<Field> findFields(
            Class<?> clazz,
            Predicate<Field> predicate,
            HierarchyTraversalMode hierarchyTraversalMode) {
        Precondition.notNull(clazz, "clazz is null");
        Precondition.notNull(predicate, "predicate is null");
        Precondition.notNull(hierarchyTraversalMode, "hierarchyTraversalMode is null");

        return new ArrayList<>(
                ReflectionSupport.findFields(
                        clazz, predicate, HierarchyTraversalMode.decode(hierarchyTraversalMode)));
    }

    /**
     * Method to find Methods of a Class
     *
     * @param clazz clazz
     * @param predicate predicate
     * @param hierarchyTraversalMode hierarchyTraversalMode
     * @return a List of Methods
     */
    public static List<Method> findMethods(
            Class<?> clazz,
            Predicate<Method> predicate,
            HierarchyTraversalMode hierarchyTraversalMode) {
        Precondition.notNull(clazz, "clazz is null");
        Precondition.notNull(predicate, "predicate is null");
        Precondition.notNull(hierarchyTraversalMode, "hierarchyTraversalMode is null");

        return new ArrayList<>(
                ReflectionSupport.findMethods(
                        clazz, predicate, HierarchyTraversalMode.decode(hierarchyTraversalMode)));
    }

    /**
     * Method to scan the Java classpath and return a List of URIs matching the resource name
     *
     * @param predicate predicate
     * @return a List of URIs
     * @throws IOException IOException
     */
    public static List<URI> findAllResources(Predicate<String> predicate) throws IOException {
        Precondition.notNull(predicate, "predicate is null");

        List<URI> uris = new ArrayList<>();

        try {
            for (URI uri : getClasspathURIs()) {
                Path path = Paths.get(uri);
                if (Files.isDirectory(path)) {
                    scanDirectory(path, predicate, uris);
                } else if (path.toString().toLowerCase(Locale.ENGLISH).endsWith(".jar")) {
                    scanJarFile(path, predicate, uris);
                }
            }
        } catch (URISyntaxException e) {
            UncheckedURISyntaxException.throwUnchecked(e);
        }

        return uris;
    }

    /**
     * Method to group a List of Methods by declaring Class.
     *
     * <p>Map keys are ordered by superclass to subclass
     *
     * @param methods methods
     * @return a Map
     */
    public static Map<Class<?>, List<Method>> groupMethodsByDeclaringClass(List<Method> methods) {
        return methods.stream()
                .sorted(
                        Comparator.comparingInt(
                                method -> getClassDepth(method.getDeclaringClass())))
                .collect(
                        Collectors.groupingBy(
                                Method::getDeclaringClass,
                                LinkedHashMap::new,
                                Collectors.toList()));
    }

    /**
     * Method to flatten a Map of List of Methods
     *
     * @param classMethodMap classMethodMap
     * @return a flattened List of Methods
     */
    public static List<Method> flattenMethods(Map<Class<?>, List<Method>> classMethodMap) {
        List<Method> list = new ArrayList<>();
        classMethodMap.forEach((clazz, methods) -> list.addAll(methods));
        return list;
    }

    /**
     * Method to get a Class' depth
     *
     * @param clazz clazz
     * @return the Class depth
     */
    private static int getClassDepth(Class<?> clazz) {
        int depth = 0;
        while (clazz.getSuperclass() != null) {
            clazz = clazz.getSuperclass();
            depth++;
        }
        return depth;
    }

    /**
     * Method to scan a directory
     *
     * @param directoryPath directoryPath
     * @param predicate predicate
     * @param uris uris
     * @throws IOException IOException
     */
    private static void scanDirectory(
            Path directoryPath, Predicate<String> predicate, List<URI> uris) throws IOException {
        Files.walkFileTree(directoryPath, new PathSimpleFileVisitor(predicate, uris));
    }

    /**
     * Method to scan a Jar file
     *
     * @param jarPath jarPath
     * @param predicate predicate
     * @param uris uris
     * @throws IOException IOException
     */
    private static void scanJarFile(Path jarPath, Predicate<String> predicate, List<URI> uris)
            throws IOException, URISyntaxException {
        try (JarFile jarFile = new JarFile(jarPath.toFile().getPath())) {
            Enumeration<JarEntry> jarEntryEnumeration = jarFile.entries();
            while (jarEntryEnumeration.hasMoreElements()) {
                JarEntry jarEntry = jarEntryEnumeration.nextElement();
                if (!jarEntry.isDirectory() && predicate.test(jarEntry.getName())) {
                    uris.add(new URI("jar:" + jarPath.toUri() + "!/" + jarEntry.getName()));
                }
            }
        }
    }

    /** Class to implement PathSimpleFileVisitor */
    private static class PathSimpleFileVisitor extends SimpleFileVisitor<Path> {

        private final Predicate<String> predicate;
        private final List<URI> uris;

        /**
         * Constructor
         *
         * @param uris uris
         * @param predicate predicate
         */
        public PathSimpleFileVisitor(Predicate<String> predicate, List<URI> uris) {
            this.predicate = predicate;
            this.uris = uris;
        }

        @Override
        public FileVisitResult visitFile(Path path, BasicFileAttributes basicFileAttributes) {
            if (basicFileAttributes.isRegularFile()
                    && predicate.test(path.getFileName().toString())) {
                uris.add(path.toUri());
            }

            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path path, IOException ioException) {
            return FileVisitResult.CONTINUE;
        }
    }
}
