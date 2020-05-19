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

import com.amplifyframework.logging.AndroidLoggingPlugin;
import com.amplifyframework.logging.LogLevel;
import com.amplifyframework.logging.Logger;
import com.amplifyframework.testutils.LogOutputStream;
import com.amplifyframework.testutils.Resources;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowLog;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Collections;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.logging.HttpLoggingInterceptor;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

import static org.junit.Assert.assertEquals;

/**
 * Tests the {@link OkHttpLoggingInterceptor}.
 */
@RunWith(RobolectricTestRunner.class)
public final class OkHttpLoggingInterceptorTest {
    private MockWebServer mockWebServer;
    private OkHttpClient okHttpClient;
    private LogOutputStream capturedLogLines;

    /**
     * Configures an {@link OkHttpClient} instance that will communicate with an {@link MockWebServer}.
     * When it does, a {@link OkHttpLoggingInterceptor} will be invoked, which will log the contents
     * of the request.
     * @throws IOException On failure to start the {@link MockWebServer}
     */
    @Before
    public void setup() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start(8080);
        this.okHttpClient = new OkHttpClient.Builder().build();
        this.capturedLogLines = new LogOutputStream();
        ShadowLog.stream = new PrintStream(capturedLogLines);
    }

    /**
     * Tear down the fake WebServer.
     * @throws IOException On failure to do so.
     */
    @After
    public void cleanup() throws IOException {
        mockWebServer.shutdown();
    }

    /**
     * In ordinary operation, request/response is not logged.
     * @throws IOException On failure to execute OkHttp call
     * @throws JSONException On failure to arrange JSON request
     */
    @Test
    public void nothingLoggedAtDefaultLogLevel() throws IOException, JSONException {
        AndroidLoggingPlugin plugin = new AndroidLoggingPlugin(/* default */);
        Logger logger = plugin.forNamespace("amplify:aws-api");
        HttpLoggingInterceptor interceptor = OkHttpLoggingInterceptor.fromLogger(logger);

        String requestJson = new JSONObject().put("request", "value").toString();
        String responseJson = new JSONObject().put("response", "value").toString();
        mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody(responseJson));

        okHttpClient.newBuilder()
            .addInterceptor(interceptor)
            .build()
            .newCall(new Request.Builder()
                .url(mockWebServer.url("/"))
                .post(RequestBody.create(requestJson, MediaType.parse("application/json")))
                .build())
            .execute();

        assertEquals(
            Collections.emptyList(),
            capturedLogLines.getLines()
        );
    }

    /**
     * The {@link OkHttpLoggingInterceptor} receives logs.
     * @throws IOException From OkHttpClient request
     * @throws JSONException On failure to arrange request body JSON via org.json.JSON* fam
     */
    @Test
    public void interceptorLogsRequestsAndResponses() throws IOException, JSONException {
        // Arrange: some request and response data
        String responseJson = new JSONObject()
            .put("animals", new JSONArray()
                .put("Goat")
                .put("Dat Donka-Donkey")
                .put("Ze-Bro")
            ).toString();
        mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody(responseJson));

        String requestJson = new JSONObject()
            .put("animals", new JSONArray().put("Goat"))
            .toString();
        Request request = new Request.Builder()
            .url(mockWebServer.url("/animals"))
            .post(RequestBody.create(requestJson, MediaType.parse("application/json" /* Like, DUH 🙄 */)))
            .build();

        // Set log level above DEBUG.
        AndroidLoggingPlugin plugin = new AndroidLoggingPlugin(LogLevel.VERBOSE);
        Logger logger = plugin.forNamespace("amplify:aws-api");
        HttpLoggingInterceptor interceptor = OkHttpLoggingInterceptor.fromLogger(logger);

        // Act: make a request using OkHttp.
        okHttpClient.newBuilder()
            .addInterceptor(interceptor)
            .build()
            .newCall(request)
            .execute();

        assertEqualsIgnoringResponseTimes(
            Resources.readLines("okhttp-logging-output.txt"),
            capturedLogLines.getLines()
        );
    }

    // Validate that all expected lines are the same as the captured lines.
    // Except, ignore strings like "(105ms)", which will vary from test to test.
    private void assertEqualsIgnoringResponseTimes(List<String> expectedLines, List<String> capturedLines) {
        final String timeRegex = "\\([0-9]{1,3}ms\\)";
        final String replacement = "(xxx ms)";
        assertEquals(expectedLines.size(), capturedLines.size());
        for (int index = 0; index < expectedLines.size(); index++) {
            assertEquals(
                expectedLines.get(0).replaceAll(timeRegex, replacement),
                capturedLines.get(0).replaceAll(timeRegex, replacement)
            );
        }
    }
}
