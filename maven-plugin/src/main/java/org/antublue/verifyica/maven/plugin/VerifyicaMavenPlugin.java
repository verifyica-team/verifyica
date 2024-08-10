/*
 * Copyright (C) 2023 The Verifyica project authors
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

package org.antublue.verifyica.maven.plugin;

import static java.lang.String.format;
import static org.junit.platform.engine.discovery.ClassNameFilter.includeClassNamePatterns;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.StringJoiner;
import org.antublue.verifyica.api.Configuration;
import org.antublue.verifyica.engine.VerifyicaEngine;
import org.antublue.verifyica.engine.common.SynchronizedPrintStream;
import org.antublue.verifyica.engine.configuration.Constants;
import org.antublue.verifyica.engine.configuration.DefaultConfigurationParameters;
import org.antublue.verifyica.engine.context.DefaultEngineContext;
import org.antublue.verifyica.engine.descriptor.StatusEngineDescriptor;
import org.antublue.verifyica.engine.listener.ChainedEngineExecutionListener;
// import org.antublue.verifyica.engine.listener.StatusEngineExecutionListener;
import org.antublue.verifyica.engine.listener.StatusEngineExecutionListener;
import org.antublue.verifyica.engine.listener.SummaryEngineExecutionListener;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;

/** Class to implement VerifyicaMavenPlugin */
@SuppressWarnings({"unused", "deprecation"})
@org.apache.maven.plugins.annotations.Mojo(
        name = "test",
        threadSafe = true,
        requiresDependencyResolution = ResolutionScope.TEST)
public class VerifyicaMavenPlugin extends AbstractMojo {

    /** Constant */
    private static final String MAVEN_PLUGIN_PROPERTIES_RESOURCE = "/maven-plugin.properties";

    /** Constant */
    private static final String MAVEN_PLUGIN_VERSION_KEY = "version";

    /** Constant */
    private static final String UNKNOWN_VERSION = "unknown";

    /** Constant */
    private static final String INTERACTIVE = "interactive";

    /** Constant */
    private static final String BATCH = "batch";

    /** Constant */
    private static final String GROUP_ID = "org.antublue.verifyica";

    /** Constant */
    private static final String ARTIFACT_ID = "maven-plugin";

    /** Constant */
    public static final String VERSION = version();

    @Parameter(defaultValue = "${session}", required = true, readonly = true)
    private MavenSession mavenSession;

    @Parameter(property = "project", required = true, readonly = true)
    private MavenProject mavenProject;

    @Parameter(property = "properties")
    private Map<String, String> properties;

    static {
        PrintStream systemOut = System.out;
        if (!(systemOut instanceof SynchronizedPrintStream)) {
            System.setOut(new SynchronizedPrintStream(systemOut));
        }

        PrintStream systemErr = System.err;
        if (!(systemErr instanceof SynchronizedPrintStream)) {
            System.setErr(new SynchronizedPrintStream(systemErr));
        }
    }

    /** Constructor */
    public VerifyicaMavenPlugin() {
        super();
    }

    /**
     * Method to get the version
     *
     * @return the version
     */
    public Optional<String> getVersion() {
        return Optional.of(VERSION);
    }

    /**
     * Method to execute the plugin
     *
     * @throws MojoExecutionException MojoExecutionException
     */
    public void execute() throws MojoFailureException, MojoExecutionException {
        Logger logger = Logger.from(getLog());

        try {
            Configuration configuration = DefaultEngineContext.getInstance().getConfiguration();

            configuration.put(Constants.MAVEN_PLUGIN, Constants.TRUE);
            logger.debug("property [%s] = [%s]", Constants.MAVEN_PLUGIN, Constants.TRUE);

            configuration.put(Constants.MAVEN_PLUGIN_VERSION, VERSION);
            logger.debug("property [%s] = [%s]", Constants.MAVEN_PLUGIN_VERSION, VERSION);

            if (mavenSession.getRequest().isInteractiveMode()) {
                configuration.put(Constants.MAVEN_PLUGIN_MODE, INTERACTIVE);
                logger.debug("property [%s] = [%s]", Constants.MAVEN_PLUGIN_MODE, INTERACTIVE);
            } else {
                configuration.put(Constants.MAVEN_PLUGIN_MODE, BATCH);
                logger.debug("property [%s] = [%s]", Constants.MAVEN_PLUGIN_MODE, BATCH);
            }

            if (properties != null) {
                for (Map.Entry<String, String> entry : properties.entrySet()) {
                    if (entry.getKey() != null && entry.getValue() != null) {
                        System.setProperty(entry.getKey(), entry.getValue());
                        logger.debug("property [%s] = [%s]", entry.getKey(), entry.getValue());
                    }
                }
            }

            Set<Path> artifactPaths = new LinkedHashSet<>();

            List<String> classpathElements = mavenProject.getCompileClasspathElements();

            if (classpathElements != null) {
                for (String classpathElement : classpathElements) {
                    Path path = new File(classpathElement).toPath();
                    artifactPaths.add(path);
                    logger.debug("classpathElement [%s]", path);
                }
            }

            classpathElements = mavenProject.getCompileClasspathElements();
            if (classpathElements != null) {
                for (String classpathElement : classpathElements) {
                    Path path = new File(classpathElement).toPath();
                    artifactPaths.add(path);
                    logger.debug("classpathElement [%s]", path);
                }
            }

            classpathElements = mavenProject.getRuntimeClasspathElements();
            if (classpathElements != null) {
                for (String classpathElement : classpathElements) {
                    Path path = new File(classpathElement).toPath();
                    artifactPaths.add(path);
                    logger.debug("classpathElement [%s]", path);
                }
            }

            classpathElements = mavenProject.getTestClasspathElements();
            if (classpathElements != null) {
                for (String classpathElement : classpathElements) {
                    Path path = new File(classpathElement).toPath();
                    artifactPaths.add(path);
                    logger.debug("classpathElement [%s]", path);
                }
            }

            Artifact artifact = mavenProject.getArtifact();
            if (artifact != null) {
                Path path = artifact.getFile().toPath();
                artifactPaths.add(path);
                logger.debug("classpathElement [%s]", path);
            }

            Set<Artifact> artifactSet = mavenProject.getDependencyArtifacts();
            if (artifactSet != null) {
                for (Artifact a : artifactSet) {
                    Path path = a.getFile().toPath();
                    artifactPaths.add(path);
                    logger.debug("classpathElement [%s]", path);
                }
            }

            List<Artifact> artifactList = mavenProject.getAttachedArtifacts();
            if (artifactList != null) {
                for (Artifact a : artifactList) {
                    Path path = a.getFile().toPath();
                    artifactPaths.add(path);
                    logger.debug("classpathElement [%s]", path);
                }
            }

            Map<String, URL> urls = new LinkedHashMap<>();

            for (Path path : artifactPaths) {
                URL url = path.toUri().toURL();
                urls.putIfAbsent(url.getPath(), url);
            }

            System.setProperty("java.class.path", buildClasspath(urls.values()));

            ClassLoader classLoader =
                    new URLClassLoader(
                            urls.values().toArray(new URL[0]),
                            Thread.currentThread().getContextClassLoader());

            Thread.currentThread().setContextClassLoader(classLoader);

            ChainedEngineExecutionListener chainedEngineExecutionListener =
                    new ChainedEngineExecutionListener(
                            new StatusEngineExecutionListener(),
                            new SummaryEngineExecutionListener());

            LauncherDiscoveryRequest launcherDiscoveryRequest =
                    LauncherDiscoveryRequestBuilder.request()
                            .selectors(DiscoverySelectors.selectClasspathRoots(artifactPaths))
                            .filters(includeClassNamePatterns(".*"))
                            .configurationParameters(Collections.emptyMap())
                            .build();

            VerifyicaEngine engine = new VerifyicaEngine();

            TestDescriptor testDescriptor =
                    engine.discover(launcherDiscoveryRequest, UniqueId.forEngine(engine.getId()));

            ExecutionRequest executionRequest =
                    new ExecutionRequest(
                            testDescriptor,
                            chainedEngineExecutionListener,
                            new DefaultConfigurationParameters(
                                    DefaultEngineContext.getInstance().getConfiguration()));

            engine.execute(executionRequest);

            if (((StatusEngineDescriptor) testDescriptor).hasFailures()) {
                throw new MojoFailureException("");
            }
        } catch (MojoFailureException e) {
            throw e;
        } catch (Throwable t) {
            throw new MojoExecutionException(t);
        }
    }

    /**
     * Method to build a String representing the class path
     *
     * @param urls urls
     * @return a String representing the class path
     */
    private static String buildClasspath(Collection<URL> urls) {
        StringJoiner stringJoiner = new StringJoiner(File.pathSeparator);
        for (URL url : urls) {
            stringJoiner.add(url.getPath());
        }
        return stringJoiner.toString();
    }

    /**
     * Method to return the version
     *
     * @return the version
     */
    private static String version() {
        String value = UNKNOWN_VERSION;

        try (InputStream inputStream =
                VerifyicaMavenPlugin.class.getResourceAsStream(MAVEN_PLUGIN_PROPERTIES_RESOURCE)) {
            if (inputStream != null) {
                Properties properties = new Properties();
                properties.load(inputStream);
                value = properties.getProperty(MAVEN_PLUGIN_VERSION_KEY).trim();
            }
        } catch (IOException e) {
            // INTENTIONALLY BLANK
        }

        return value;
    }

    /** Class to implement Logger */
    private static class Logger {

        private final Log log;

        /**
         * Constructor
         *
         * @param log log
         */
        private Logger(Log log) {
            this.log = log;
        }

        /**
         * Method to log a DEBUG message
         *
         * @param format format
         * @param objects object
         */
        public void debug(String format, Object... objects) {
            if (log.isDebugEnabled()) {
                log.debug(format(format, objects));
            }
        }

        /**
         * Method to create a Logger from a Maven Log
         *
         * @param log log
         * @return a Logger
         */
        public static Logger from(Log log) {
            return new Logger(log);
        }
    }
}
