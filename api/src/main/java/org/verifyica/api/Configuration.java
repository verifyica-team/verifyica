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

package org.verifyica.api;

import java.nio.file.Path;
import java.util.Optional;
import java.util.Properties;

/** Interface to implement Configuration */
public interface Configuration {

    /**
     * Get the Path to the Properties configuration file
     *
     * @return an Optional containing the Path to the Properties configuration file, or Optional.empty()
     *     if no properties configuration file was found
     */
    default Optional<Path> propertiesPath() {
        return getPropertiesPath();
    }

    /**
     * Get the Configuration Properties
     *
     * @return Configuration Properties
     */
    default Properties properties() {
        return getProperties();
    }

    /**
     * Get the Path to the Properties configuration file
     *
     * @return an Optional containing the Path to the Properties configuration file, or Optional.empty()
     *     if no properties configuration file was found
     */
    default Optional<Path> getPropertiesFilename() {
        return getPropertiesPath();
    }

    /**
     * Get the Path to the Properties configuration file
     *
     * @return an Optional containing the Path to the Properties configuration file, or Optional.empty()
     *     if no properties configuration file was found
     */
    Optional<Path> getPropertiesPath();

    /**
     * Get the Configuration Properties
     *
     * @return Configuration Properties
     */
    Properties getProperties();
}
