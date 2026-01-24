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

package org.verifyica.examples.testcontainers.nginx;

import static java.util.Optional.ofNullable;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.testcontainers.containers.Network;
import org.verifyica.api.ArgumentContext;
import org.verifyica.api.Trap;
import org.verifyica.api.Verifyica;
import org.verifyica.examples.support.Logger;

public class NginxTest2 {

    private static final Logger LOGGER = Logger.createLogger(NginxTest2.class);

    private final String NETWORK = "network";

    @Verifyica.ArgumentSupplier(parallelism = Integer.MAX_VALUE)
    public static Stream<NginxTestEnvironment> arguments() throws IOException {
        return NginxTestEnvironment.createTestEnvironments();
    }

    @Verifyica.BeforeAll
    public void initializeTestEnvironment(ArgumentContext argumentContext) {
        LOGGER.info(
                "[%s] initialize test environment ...",
                argumentContext.getTestArgument().getName());

        Network network = Network.newNetwork();
        network.getId();

        argumentContext.getMap().put(NETWORK, network);
        argumentContext.getTestArgument().getPayload(NginxTestEnvironment.class).initialize(network);

        assertThat(argumentContext
                        .getTestArgument()
                        .getPayload(NginxTestEnvironment.class)
                        .isRunning())
                .isTrue();
    }

    @Verifyica.Test
    @Verifyica.Order(1)
    public void testGet(ArgumentContext argumentContext) throws Throwable {
        LOGGER.info(
                "[%s] testing testGet() ...", argumentContext.getTestArgument().getName());

        int port = argumentContext
                .getTestArgument()
                .getPayload(NginxTestEnvironment.class)
                .getNginxContainer()
                .getMappedPort(80);

        String content = doGet("http://localhost:" + port);

        assertThat(content).contains("Welcome to nginx!");
    }

    @Verifyica.AfterAll
    public void destroyTestEnvironment(ArgumentContext argumentContext) throws Throwable {
        LOGGER.info(
                "[%s] destroy test environment ...",
                argumentContext.getTestArgument().getName());

        List<Trap> traps = new ArrayList<>();

        traps.add(new Trap(() -> argumentContext
                .getTestArgument()
                .getPayload(NginxTestEnvironment.class)
                .destroy()));
        traps.add(new Trap(() -> ofNullable(argumentContext.getMap().getAs(NETWORK, Network.class))
                .ifPresent(Network::close)));
        traps.add(new Trap(() -> argumentContext.getMap().clear()));

        Trap.assertEmpty(traps);
    }

    private static String doGet(String url) throws Throwable {
        StringBuilder result = new StringBuilder();
        URLConnection connection = URI.create(url).toURL().openConnection();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
        }

        return result.toString();
    }
}
