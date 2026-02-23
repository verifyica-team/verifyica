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
 * A utility class for printing stack traces of exceptions in a formatted manner.
 *
 * <p>This class provides functionality to print stack traces with optional ANSI color formatting.
 * It can also optionally prune stack traces by removing frames that belong to the Verifyica
 * engine itself, based on a configuration setting. This helps improve readability of stack
 * traces by focusing on the relevant application code and hiding internal engine details.
 *
 * @see Throwable
 * @see AnsiColor
 */
public class StackTracePrinter {

    private static final String ENGINE_PACKAGE = "org.verifyica.engine.";
    private static final boolean PRUNE_STACK_TRACES = initializePruneStackTraces();

    /**
     * Initializes the PRUNE_STACK_TRACES flag based on configuration.
     *
     * @return true if stack traces should be pruned, false otherwise
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
     * Private constructor to prevent instantiation since this is a utility class.
     */
    private StackTracePrinter() {
        // INTENTIONALLY EMPTY
    }

    /**
     * Prints the stack trace of the given throwable using the specified ANSI color and print stream.
     *
     * @param throwable the throwable whose stack trace is to be printed (must not be null)
     * @param ansiColor the ANSI color to use for the stack trace (must not be null)
     * @param printStream the print stream to write to (must not be null)
     * @throws IllegalArgumentException if throwable, ansiColor, or printStream is null
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
     * Builds a string representation of the stack trace, optionally pruning engine frames.
     *
     * @param throwable the throwable to build the stack trace for
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
     * Calculates the number of stack trace elements to keep by finding the first frame
     * that belongs to the Verifyica engine.
     *
     * @param stackTrace the stack trace elements to analyze
     * @return the number of elements to keep
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
