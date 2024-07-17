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

import static org.junit.platform.engine.discovery.ClassNameFilter.includeClassNamePatterns;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import org.antublue.verifyica.engine.VerifyicaTestEngine;
import org.antublue.verifyica.engine.configuration.ConcreteConfiguration;
import org.antublue.verifyica.engine.configuration.Constants;
import org.antublue.verifyica.engine.util.AnsiColor;
import org.antublue.verifyica.maven.plugin.listener.DelegatingEngineExecutionListener;
import org.antublue.verifyica.maven.plugin.listener.StatusEngineExecutionListener;
import org.antublue.verifyica.maven.plugin.listener.SummaryEngineExecutionListener;
import org.antublue.verifyica.maven.plugin.logger.Logger;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherConfig;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;

/** Class to implement VerifyicaMavenPlugin */
@SuppressWarnings({"unused", "deprecation"})
@org.apache.maven.plugins.annotations.Mojo(
        name = "test",
        threadSafe = true,
        requiresDependencyResolution = ResolutionScope.TEST)
public class VerifyicaMavenPlugin extends AbstractMojo {

    private static final String INTERACTIVE = "interactive";

    private static final String BATCH = "batch";

    private static final String GROUP_ID = "org.antublue.verifyica";

    private static final String ARTIFACT_ID = "maven-plugin";

    /** Version */
    public static final String VERSION = Version.version();

    @Parameter(defaultValue = "${session}", required = true, readonly = true)
    private MavenSession mavenSession;

    @Parameter(property = "project", required = true, readonly = true)
    private MavenProject mavenProject;

    @Parameter(property = "properties")
    private Map<String, String> properties;

    /** Constructor */
    public VerifyicaMavenPlugin() {
        super();
    }

    /**
     * Method to execute the plugin
     *
     * @throws MojoExecutionException MojoExecutionException
     */
    public void execute() throws MojoFailureException, MojoExecutionException {
        Logger logger = Logger.from(getLog());

        try {
            ConcreteConfiguration concreteConfiguration = ConcreteConfiguration.getInstance();

            concreteConfiguration.set(Constants.MAVEN_PLUGIN, Constants.TRUE);
            logger.debug("property [%s] = [%s]", Constants.MAVEN_PLUGIN, Constants.TRUE);

            concreteConfiguration.set(Constants.MAVEN_PLUGIN_VERSION, VERSION);
            logger.debug("property [%s] = [%s]", Constants.MAVEN_PLUGIN_VERSION, VERSION);

            if (mavenSession.getRequest().isInteractiveMode()) {
                concreteConfiguration.set(Constants.MAVEN_PLUGIN_MODE, INTERACTIVE);
                logger.debug("property [%s] = [%s]", Constants.MAVEN_PLUGIN_MODE, INTERACTIVE);
            } else {
                concreteConfiguration.set(Constants.MAVEN_PLUGIN_MODE, BATCH);
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

            SummaryEngineExecutionListener summaryEngineExecutionListener =
                    new SummaryEngineExecutionListener();

            String summaryMessage = null;

            DelegatingEngineExecutionListener delegatingEngineExecutionListener =
                    new DelegatingEngineExecutionListener(
                            summaryEngineExecutionListener, new StatusEngineExecutionListener());

            LauncherConfig launcherConfig = LauncherConfig.builder().build();

            LauncherDiscoveryRequest launcherDiscoveryRequest =
                    LauncherDiscoveryRequestBuilder.request()
                            .selectors(DiscoverySelectors.selectClasspathRoots(artifactPaths))
                            .filters(includeClassNamePatterns(".*"))
                            .configurationParameters(Collections.emptyMap())
                            .build();

            VerifyicaTestEngine engine = new VerifyicaTestEngine();
            TestDescriptor testDescriptor = null;

            try {
                summaryEngineExecutionListener.begin();

                testDescriptor =
                        engine.discover(
                                launcherDiscoveryRequest, UniqueId.forEngine(engine.getId()));
            } catch (Throwable t) {
                summaryMessage = AnsiColor.TEXT_RED_BOLD_BRIGHT.wrap("EXCEPTION DURING DISCOVERY");

                t.printStackTrace(System.err);
                System.err.flush();
            }

            if (testDescriptor != null) {
                try {
                    ExecutionRequest executionRequest =
                            new ExecutionRequest(
                                    testDescriptor,
                                    delegatingEngineExecutionListener,
                                    ConcreteConfiguration.getInstance());

                    engine.execute(executionRequest);

                    if (summaryEngineExecutionListener.hasTests()) {
                        if (summaryEngineExecutionListener.hasFailures()) {
                            summaryMessage = AnsiColor.TEXT_RED_BOLD.wrap("FAIL");
                        } else {
                            summaryMessage = AnsiColor.TEXT_GREEN_BOLD.wrap("PASS");
                        }
                    } else {
                        summaryMessage = AnsiColor.TEXT_RED_BOLD.wrap("FAIL / NO TESTS EXECUTED");
                    }
                } catch (Throwable t) {
                    summaryMessage =
                            AnsiColor.TEXT_RED_BOLD.wrap("FAIL / EXCEPTION DURING EXECUTION");
                    t.printStackTrace(System.err);
                    System.err.flush();
                }
            }

            summaryEngineExecutionListener.end(summaryMessage);

            if (!summaryEngineExecutionListener.hasTests()
                    || summaryEngineExecutionListener.hasFailures()) {
                throw new MojoFailureException("");
            }
        } catch (MojoFailureException e) {
            throw e;
        } catch (Throwable t) {
            throw new MojoExecutionException(t);
        }
    }

    private static String buildClasspath(Collection<URL> urls) {
        StringJoiner stringJoiner = new StringJoiner(File.pathSeparator);
        for (URL url : urls) {
            stringJoiner.add(url.getPath());
        }
        return stringJoiner.toString();
    }
}
