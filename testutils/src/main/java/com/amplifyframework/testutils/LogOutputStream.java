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

package com.amplifyframework.testutils;

import android.text.TextUtils;
import androidx.annotation.NonNull;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * An output stream that can be parsed into lines by newlines.
 */
public final class LogOutputStream extends ByteArrayOutputStream {
    /**
     * Gets the lines that have been logged.
     * @return The lines which have been logged
     */
    @NonNull
    public List<String> getLines() {
        final List<String> lines = new ArrayList<>();
        for (String line : toString().split("[\\r\\n]+")) {
            if (!TextUtils.isEmpty(line)) {
                lines.add(line);
            }
        }
        return lines;
    }
}
