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

package org.verifyica.engine.common;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import org.verifyica.engine.configuration.ConcreteConfiguration;
import org.verifyica.engine.configuration.Constants;

/**
 * Class to implement StackTracePrinter
 */
public class StackTracePrinter {

    private static final String ENGINE_PACKAGE = "org.verifyica.engine.";
    private static final boolean PRUNE_STACK_TRACES = initializePruneStackTraces();

    /**
     * Method to initialize the PRUNE_STACK_TRACES constant
     *
     * @return true if stack traces should be pruned
     */
    private static boolean initializePruneStackTraces() {
        try {
            String value =
                    ConcreteConfiguration.getInstance().getProperties().getProperty(Constants.ENGINE_PRUNE_STACK_TRACE);
            return !Constants.FALSE.equals(value);
        } catch (Exception e) {
            // Use default (true) if configuration fails
            return true;
        }
    }

    /**
     * Constructor
     */
    private StackTracePrinter() {
        // INTENTIONALLY EMPTY
    }

    /**
     * Method to print the stacktrace using a specific AnsiColor
     *
     * @param throwable throwable
     * @param ansiColor ansiColor
     * @param printStream printStream
     */
    public static void printStackTrace(Throwable throwable, AnsiColor ansiColor, PrintStream printStream) {
        if (throwable == null) {
            throw new IllegalArgumentException("throwable is null");
        }

        if (ansiColor == null) {
            throw new IllegalArgumentException("ansiColor is null");
        }

        if (printStream == null) {
            throw new IllegalArgumentException("printStream is null");
        }

        String stackTraceString = buildStackTraceString(throwable);

        printStream.print(ansiColor);
        printStream.print(stackTraceString);
        printStream.print(AnsiColor.NONE);
    }

    /**
     * Method to build a stack trace string, optionally pruning engine frames
     *
     * @param throwable throwable
     * @return the stack trace as a string
     */
    private static String buildStackTraceString(Throwable throwable) {
        if (throwable == null) {
            return "";
        }

        if (!PRUNE_STACK_TRACES) {
            StringWriter sw = new StringWriter(2048);
            PrintWriter pw = new PrintWriter(sw);
            throwable.printStackTrace(pw);
            pw.flush(); // keeps analyzers happy
            return sw.toString();
        }

        final String nl = System.lineSeparator();
        StringBuilder result = new StringBuilder(2048);

        java.util.IdentityHashMap<Throwable, Boolean> seen = new java.util.IdentityHashMap<>();
        Throwable current = throwable;

        while (current != null && seen.put(current, Boolean.TRUE) == null) {
            if (current != throwable) {
                result.append("Caused by: ");
            }

            result.append(current.getClass().getName());
            String msg = current.getMessage();
            if (msg != null) {
                result.append(": ").append(msg);
            }
            result.append(nl);

            StackTraceElement[] stackTrace = current.getStackTrace();
            int length = calculatePrunedLength(stackTrace);

            for (int i = 0; i < length; i++) {
                result.append("\tat ").append(stackTrace[i]).append(nl);
            }

            if (length < stackTrace.length) {
                result.append("\t... ")
                        .append(stackTrace.length - length)
                        .append(" more")
                        .append(nl);
            }

            current = current.getCause();
        }

        if (current != null) {
            result.append("[Cyclic cause chain detected]").append(nl);
        }

        return result.toString();
    }

    /**
     * Method to determine how many stack trace elements to keep
     *
     * @param stackTrace stackTrace
     * @return the number of StackTrace elements to keep
     */
    private static int calculatePrunedLength(StackTraceElement[] stackTrace) {
        for (int i = 0; i < stackTrace.length; i++) {
            String className = stackTrace[i].getClassName();
            if (className != null && className.startsWith(ENGINE_PACKAGE)) {
                return i;
            }
        }
        return stackTrace.length;
    }
}
