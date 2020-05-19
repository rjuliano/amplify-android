/*
 * Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.amplifyframework.api.aws;

import androidx.annotation.NonNull;

import com.amplifyframework.core.Amplify;
import com.amplifyframework.logging.LogLevel;
import com.amplifyframework.logging.Logger;

import java.util.Arrays;
import java.util.List;

import okhttp3.logging.HttpLoggingInterceptor;

/**
 * An OkHttp interceptor which logs request and response data.
 * Logging will only take place at {@link LogLevel#VERBOSE} or {@link LogLevel#DEBUG},
 * neither of which is the default for the framework.
 */
final class OkHttpLoggingInterceptor {
    // Case-insensitive redaction of some known Auth headers. Simply don't ever them.
    private static final List<String> KNOWN_SENSITIVE_KEYS = Arrays.asList("x-api-key", "authorization");

    private OkHttpLoggingInterceptor() {}

    static HttpLoggingInterceptor create() {
        return fromLogger(Amplify.Logging.forNamespace("amplify:aws-api"));
    }

    static HttpLoggingInterceptor fromLogger(Logger logger) {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor(findLogger(logger));
        interceptor.setLevel(findLevel(logger.getThresholdLevel()));
        for (String headerKey : KNOWN_SENSITIVE_KEYS) {
            interceptor.redactHeader(headerKey);
        }
        return interceptor;
    }

    private static HttpLoggingInterceptor.Logger findLogger(Logger logger) {
        switch (logger.getThresholdLevel()) {
            case VERBOSE:
                return logger::verbose;
            case DEBUG:
                return logger::debug;
            case INFO:
            case WARN:
            case ERROR:
            case NONE:
            default:
                return new NoOpLogger();
        }
    }

    private static HttpLoggingInterceptor.Level findLevel(LogLevel threshold) {
        switch (threshold) {
            case VERBOSE:
                return HttpLoggingInterceptor.Level.BODY;
            case DEBUG:
                return HttpLoggingInterceptor.Level.BASIC;
            case INFO:
            case WARN:
            case ERROR:
            case NONE:
            default:
                return HttpLoggingInterceptor.Level.NONE;
        }
    }

    private static final class NoOpLogger implements HttpLoggingInterceptor.Logger {
        @Override
        public void log(@NonNull String string) {
        }
    }
}
