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

package com.mopub.mobileads.test.support;

import android.content.Context;
import android.webkit.WebSettings;
import com.mopub.mobileads.AdConfiguration;
import com.mopub.mobileads.HtmlBannerWebView;
import com.mopub.mobileads.factories.HtmlBannerWebViewFactory;

import static com.mopub.mobileads.CustomEventBanner.CustomEventBannerListener;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.stub;

public class TestHtmlBannerWebViewFactory extends HtmlBannerWebViewFactory {
    private HtmlBannerWebView mockHtmlBannerWebView = mock(HtmlBannerWebView.class);
    private CustomEventBannerListener latestListener;
    private boolean latestIsScrollable;
    private String latestRedirectUrl;
    private String latestClickthroughUrl;

    public TestHtmlBannerWebViewFactory() {
        WebSettings webSettings = mock(WebSettings.class);
        stub(mockHtmlBannerWebView.getSettings()).toReturn(webSettings);
        stub(webSettings.getUserAgentString()).toReturn("Mozilla/5.0 (Linux; U; Android 4.0.3; ko-kr; LG-L160L Build/IML74K) AppleWebkit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30");
    }

    public static HtmlBannerWebView getSingletonMock() {
        return getTestFactory().mockHtmlBannerWebView;
    }

    private static TestHtmlBannerWebViewFactory getTestFactory() {
        return (TestHtmlBannerWebViewFactory) instance;
    }

    @Override
    public HtmlBannerWebView internalCreate(
            Context context, CustomEventBannerListener
            customEventBannerListener,
            boolean isScrollable,
            String redirectUrl,
            String clickthroughUrl, AdConfiguration adConfiguration) {
        latestListener = customEventBannerListener;
        latestIsScrollable = isScrollable;
        latestRedirectUrl = redirectUrl;
        latestClickthroughUrl = clickthroughUrl;
        return mockHtmlBannerWebView;
    }

    public static CustomEventBannerListener getLatestListener() {
        return getTestFactory().latestListener;
    }

    public static boolean getLatestIsScrollable() {
        return getTestFactory().latestIsScrollable;
    }

    public static String getLatestRedirectUrl() {
        return getTestFactory().latestRedirectUrl;
    }

    public static String getLatestClickthroughUrl() {
        return getTestFactory().latestClickthroughUrl;
    }
}
