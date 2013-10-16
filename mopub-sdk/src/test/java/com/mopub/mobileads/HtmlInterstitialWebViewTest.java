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
import org.robolectric.Robolectric;

import java.lang.reflect.Method;

import static com.mopub.mobileads.CustomEventInterstitial.CustomEventInterstitialListener;
import static com.mopub.mobileads.HtmlInterstitialWebView.HtmlInterstitialWebViewListener;
import static com.mopub.mobileads.MoPubErrorCode.NETWORK_INVALID_STATE;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.robolectric.Robolectric.shadowOf;

@RunWith(SdkTestRunner.class)
public class HtmlInterstitialWebViewTest {

    private HtmlInterstitialWebView subject;
    private CustomEventInterstitialListener customEventInterstitialListener;
    private String clickthroughUrl;
    private boolean isScrollable;
    private String redirectUrl;
    private AdConfiguration adConfiguration;

    @Before
    public void setUp() throws Exception {
        adConfiguration = mock(AdConfiguration.class);
        subject = new HtmlInterstitialWebView(new Activity(), adConfiguration);
        customEventInterstitialListener = mock(CustomEventInterstitialListener.class);
        isScrollable = false;
        clickthroughUrl = "clickthroughUrl";
        redirectUrl = "redirectUrl";
    }

    @Test
    public void init_shouldSetupWebViewClient() throws Exception {
        subject.init(customEventInterstitialListener, false, clickthroughUrl, redirectUrl);
        WebViewClient webViewClient = shadowOf(subject).getWebViewClient();
        assertThat(webViewClient).isNotNull();
        assertThat(webViewClient).isInstanceOf(HtmlWebViewClient.class);
    }

    @Test
    public void htmlBannerWebViewListener_shouldForwardCalls() throws Exception {
        HtmlInterstitialWebViewListener listenerSubject = new HtmlInterstitialWebViewListener(customEventInterstitialListener);

        listenerSubject.onLoaded(subject);

        listenerSubject.onFailed(NETWORK_INVALID_STATE);
        verify(customEventInterstitialListener).onInterstitialFailed(eq(NETWORK_INVALID_STATE));

        listenerSubject.onClicked();
        verify(customEventInterstitialListener).onInterstitialClicked();
    }

    @Test
    public void init_shouldAddJavascriptInterface() throws Exception {
        subject.init(customEventInterstitialListener, isScrollable, clickthroughUrl, redirectUrl);

        Object javascriptInterface = shadowOf(subject).getJavascriptInterface("mopubUriInterface");
        assertThat(javascriptInterface).isNotNull();

        Method fireFinishLoad = javascriptInterface.getClass().getDeclaredMethod("fireFinishLoad");
        Robolectric.pauseMainLooper();
        boolean returnValue = (Boolean) fireFinishLoad.invoke(javascriptInterface);
        assertThat(returnValue).isTrue();
        verify(customEventInterstitialListener, never()).onInterstitialShown();

        Robolectric.unPauseMainLooper();
        verify(customEventInterstitialListener).onInterstitialLoaded();
    }
}
