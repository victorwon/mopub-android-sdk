/*
 * Copyright (c) 2010-2013, MoPub Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *  Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 *  Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 *  Neither the name of 'MoPub Inc.' nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.mopub.mobileads.util;

import com.mopub.mobileads.test.support.SdkTestRunner;
import com.mopub.mobileads.test.support.TestHttpResponseWithHeaders;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.mopub.mobileads.util.ResponseHeader.AD_TIMEOUT;
import static com.mopub.mobileads.util.ResponseHeader.SCROLLABLE;
import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(SdkTestRunner.class)
public class HttpResponsesTest {
    private TestHttpResponseWithHeaders response;

    @Before
    public void setup() {
        response = new TestHttpResponseWithHeaders(200, "all is well");
    }

    @Test
    public void extractBooleanHeader_whenValueIsZero_shouldReturnFalse() throws Exception {
        response.addHeader(SCROLLABLE.getKey(), "0");
        assertThat(HttpResponses.extractBooleanHeader(response, SCROLLABLE, false)).isFalse();

        response.addHeader(SCROLLABLE.getKey(), "0");
        assertThat(HttpResponses.extractBooleanHeader(response, SCROLLABLE, true)).isFalse();
    }

    @Test
    public void extractBooleanHeader_whenValueIsOne_shouldReturnTrue() throws Exception {
        response.addHeader(SCROLLABLE.getKey(), "1");
        assertThat(HttpResponses.extractBooleanHeader(response, SCROLLABLE, false)).isTrue();

        response.addHeader(SCROLLABLE.getKey(), "1");
        assertThat(HttpResponses.extractBooleanHeader(response, SCROLLABLE, true)).isTrue();
    }

    @Test
    public void extractBooleanHeader_shouldReturnDefaultValue() throws Exception {
        // no header added to response

        assertThat(HttpResponses.extractBooleanHeader(response, SCROLLABLE, false)).isFalse();
        assertThat(HttpResponses.extractBooleanHeader(response, SCROLLABLE, true)).isTrue();
    }

    @Test
    public void extractIntegerHeader_shouldReturnIntegerValue() throws Exception {
        response.addHeader(AD_TIMEOUT.getKey(), "10");
        assertThat(HttpResponses.extractIntegerHeader(response, AD_TIMEOUT)).isEqualTo(10);

        response.addHeader(AD_TIMEOUT.getKey(), "0");
        assertThat(HttpResponses.extractIntegerHeader(response, AD_TIMEOUT)).isEqualTo(0);

        response.addHeader(AD_TIMEOUT.getKey(), "-2");
        assertThat(HttpResponses.extractIntegerHeader(response, AD_TIMEOUT)).isEqualTo(-2);
    }

    @Test
    public void extractIntegerHeader_withDoubleValue_shouldTruncateValue() throws Exception {
        response.addHeader(AD_TIMEOUT.getKey(), "3.14");
        assertThat(HttpResponses.extractIntegerHeader(response, AD_TIMEOUT)).isEqualTo(3);

        response.addHeader(AD_TIMEOUT.getKey(), "-3.14");
        assertThat(HttpResponses.extractIntegerHeader(response, AD_TIMEOUT)).isEqualTo(-3);
    }

    @Test
    public void extractIntegerHeader_whenNoHeaderPresent_shouldReturnNull() throws Exception {
        // no header added to response
        assertThat(HttpResponses.extractIntegerHeader(response, AD_TIMEOUT)).isNull();

        response.addHeader(AD_TIMEOUT.getKey(), null);
        assertThat(HttpResponses.extractIntegerHeader(response, AD_TIMEOUT)).isNull();
    }

    @Test
    public void extractIntegerHeader_withNonsenseStringValue_shouldReturnNull() throws Exception {
        response.addHeader(AD_TIMEOUT.getKey(), "llama!!guy");
        assertThat(HttpResponses.extractIntegerHeader(response, AD_TIMEOUT)).isNull();
    }

    @Test
    public void extractIntHeader_withInvalidHeader_shouldUseDefaultValue() throws Exception {
        response.addHeader(AD_TIMEOUT.getKey(), "5");
        assertThat(HttpResponses.extractIntHeader(response, AD_TIMEOUT, 10)).isEqualTo(5);

        response.addHeader(AD_TIMEOUT.getKey(), "five!");
        assertThat(HttpResponses.extractIntHeader(response, AD_TIMEOUT, 10)).isEqualTo(10);
    }
}
