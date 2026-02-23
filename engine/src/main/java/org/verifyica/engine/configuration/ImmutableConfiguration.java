/*
 * Copyright (C) Verifyica project authors and contributors. All rights reserved.
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

package org.verifyica.engine.configuration;

import static java.util.Optional.ofNullable;

import java.nio.file.Path;
import java.util.Optional;
import java.util.Properties;
import org.verifyica.api.Configuration;
import org.verifyica.engine.common.ImmutableProperties;

/**
 * ImmutableConfiguration provides an immutable implementation of Configuration.
 */
public class ImmutableConfiguration implements Configuration {

    /**
     * The path to the properties file.
     */
    private final Path propertiesFilenamePath;

    /**
     * The immutable properties.
     */
    private final Properties properties;

    /**
     * Constructs a new ImmutableConfiguration from the given configuration.
     *
     * @param configuration the configuration to make immutable
     */
    public ImmutableConfiguration(Configuration configuration) {
        propertiesFilenamePath = configuration.getPropertiesPath().orElse(null);
        properties = new ImmutableProperties(configuration.getProperties());
    }

    @Override
    public Optional<Path> getPropertiesPath() {
        return ofNullable(propertiesFilenamePath);
    }

    @Override
    public Properties getProperties() {
        return properties;
    }
}
