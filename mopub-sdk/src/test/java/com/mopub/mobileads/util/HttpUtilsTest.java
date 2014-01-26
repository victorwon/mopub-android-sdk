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
import org.apache.http.HttpResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;

import java.io.*;
import java.util.*;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.fail;

@RunWith(SdkTestRunner.class)
public class HttpUtilsTest {

    TestHttpResponseWithHeaders response;

    @Before
    public void setup() {
        response = new TestHttpResponseWithHeaders(200, "woot");
    }

    @Test
    public void ping_shouldSendNetworkRequestAndGetResponse() throws Exception {
        Robolectric.addPendingHttpResponse(response);

        HttpResponse expectedResponse = HttpUtils.ping("http://myurl.tv");

        Scanner scanner = new Scanner(new InputStreamReader(expectedResponse.getEntity().getContent()));
        StringBuilder content = new StringBuilder();
        while (scanner.hasNext()) {
            content.append(scanner.next());
        }

        assertThat(content.toString()).isEqualTo("woot");
    }

    @Test
    public void ping_withNullUrl_shouldThrowException() throws Exception {
        try {
            HttpUtils.ping(null);
            fail("logic error - should have thrown IllegalArgumentException");
        } catch (Exception exception) {
            assertThat(exception).isInstanceOf(IllegalArgumentException.class);
        }
    }
}
