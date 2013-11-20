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
import android.webkit.WebViewClient;
import com.mopub.mobileads.test.support.SdkTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.mopub.mobileads.CustomEventBanner.CustomEventBannerListener;
import static com.mopub.mobileads.MoPubErrorCode.NETWORK_INVALID_STATE;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.robolectric.Robolectric.shadowOf;

@RunWith(SdkTestRunner.class)
public class HtmlBannerWebViewTest {

    private AdConfiguration adConfiguration;
    private HtmlBannerWebView subject;
    private CustomEventBannerListener customEventBannerListener;
    private String clickthroughUrl;
    private String redirectUrl;

    @Before
    public void setup() throws Exception {
        adConfiguration = mock(AdConfiguration.class);
        subject = new HtmlBannerWebView(new Activity(), adConfiguration);
        customEventBannerListener = mock(CustomEventBannerListener.class);
        clickthroughUrl = "clickthroughUrl";
        redirectUrl = "redirectUrl";
    }

    @Test
    public void init_shouldSetupWebViewClient() throws Exception {
        subject.init(customEventBannerListener, false, clickthroughUrl, redirectUrl);
        WebViewClient webViewClient = shadowOf(subject).getWebViewClient();
        assertThat(webViewClient).isNotNull();
        assertThat(webViewClient).isInstanceOf(HtmlWebViewClient.class);
    }

    @Test
    public void htmlBannerWebViewListener_shouldForwardCalls() throws Exception {
        HtmlBannerWebView.HtmlBannerWebViewListener listenerSubject = new HtmlBannerWebView.HtmlBannerWebViewListener(customEventBannerListener);

        listenerSubject.onClicked();
        verify(customEventBannerListener).onBannerClicked();

        listenerSubject.onLoaded(subject);
        verify(customEventBannerListener).onBannerLoaded(eq(subject));

        listenerSubject.onCollapsed();
        verify(customEventBannerListener).onBannerCollapsed();

        listenerSubject.onFailed(NETWORK_INVALID_STATE);
        verify(customEventBannerListener).onBannerFailed(eq(NETWORK_INVALID_STATE));
    }
}
