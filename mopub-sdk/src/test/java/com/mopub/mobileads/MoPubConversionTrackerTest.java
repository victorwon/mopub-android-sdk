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

package com.mopub.mobileads;

import android.app.Activity;
import com.mopub.mobileads.test.support.SdkTestRunner;
import org.apache.http.HttpRequest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.tester.org.apache.http.FakeHttpLayer;
import org.robolectric.tester.org.apache.http.HttpRequestInfo;

import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(SdkTestRunner.class)
public class MoPubConversionTrackerTest {
    private MoPubConversionTracker subject;
    private Activity context;
    private FakeHttpLayer fakeHttpLayer;

    @Before
    public void setUp() throws Exception {
        subject = new MoPubConversionTracker();
        context = new Activity();
        fakeHttpLayer = Robolectric.getFakeHttpLayer();
    }

    @Test
    public void reportAppOpen_onValidHttpResponse_isIdempotent() throws Exception {
        fakeHttpLayer.addPendingHttpResponse(200, "doesn't matter what this is as long as it's not nothing");
        subject.reportAppOpen(context);
        assertThat(requestWasMade()).isTrue();

        fakeHttpLayer.addPendingHttpResponse(200, "doesn't matter what this is as long as it's not nothing");
        subject.reportAppOpen(context);
        assertThat(requestWasMade()).isFalse();
    }

    @Test
    public void reportAppOpen_onInvalidStatusCode_shouldMakeSecondRequest() throws Exception {
        fakeHttpLayer.addPendingHttpResponse(404, "doesn't matter what this is as long as it's not nothing");
        subject.reportAppOpen(context);
        assertThat(requestWasMade()).isTrue();

        fakeHttpLayer.addPendingHttpResponse(404, "doesn't matter what this is as long as it's not nothing");
        subject.reportAppOpen(context);
        assertThat(requestWasMade()).isTrue();
    }

    @Test
    public void reportAppOpen_onEmptyResponse_shouldMakeSecondRequest() throws Exception {
        fakeHttpLayer.addPendingHttpResponse(200, "");
        subject.reportAppOpen(context);
        assertThat(requestWasMade()).isTrue();

        fakeHttpLayer.addPendingHttpResponse(200, "");
        subject.reportAppOpen(context);
        assertThat(requestWasMade()).isTrue();
    }

    private boolean requestWasMade() throws Exception {
        String expectedUrl = new StringBuilder("http://ads.mopub.com/m/open")
                .append("?v=6")
                .append("&id=").append("com.mopub.mobileads")
                .append("&udid=sha%3A").append("")
                .append("&av=").append("1.0")
                .toString();

        Thread.sleep(500);
        HttpRequestInfo lastSentHttpRequestInfo = fakeHttpLayer.getLastSentHttpRequestInfo();
        if (lastSentHttpRequestInfo == null) {
            return false;
        }
        HttpRequest request = lastSentHttpRequestInfo.getHttpRequest();
        fakeHttpLayer.clearRequestInfos();
        return request.getRequestLine().getUri().equals(expectedUrl);
    }
}
