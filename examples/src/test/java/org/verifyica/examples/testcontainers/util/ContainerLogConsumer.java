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

package org.verifyica.examples.testcontainers.util;

import java.util.function.Consumer;
import org.testcontainers.containers.output.OutputFrame;

public class ContainerLogConsumer implements Consumer<OutputFrame> {

    private final String name;

    public ContainerLogConsumer(String name) {
        this.name = name;
    }

    @Override
    public void accept(OutputFrame outputFrame) {
        String message = outputFrame.getUtf8String();
        if (message != null) {
            message = message.trim();

            if (!message.isEmpty()) {
                System.out.print("[" + name + "] ");
                System.out.print(outputFrame.getUtf8String());
            }
        }
    }
}
