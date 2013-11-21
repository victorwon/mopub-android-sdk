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
import android.net.Uri;
import android.view.Gravity;
import android.widget.FrameLayout;
import com.mopub.mobileads.test.support.TestHtmlBannerWebViewFactory;
import com.mopub.mobileads.test.support.TestHttpResponseWithHeaders;
import com.mopub.mobileads.test.support.TestMoPubViewFactory;
import org.apache.http.HttpResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;

import java.util.*;

import static com.mopub.mobileads.AdFetcher.CLICKTHROUGH_URL_KEY;
import static com.mopub.mobileads.AdFetcher.HTML_RESPONSE_BODY_KEY;
import static com.mopub.mobileads.AdFetcher.REDIRECT_URL_KEY;
import static com.mopub.mobileads.AdFetcher.SCROLLABLE_KEY;
import static com.mopub.mobileads.MoPubErrorCode.NETWORK_INVALID_STATE;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.stub;
import static org.mockito.Mockito.verify;

@RunWith(com.mopub.mobileads.test.support.SdkTestRunner.class)
public class HtmlBannerTest {

    private HtmlBanner subject;
    private HtmlBannerWebView htmlBannerWebView;
    private CustomEventBanner.CustomEventBannerListener customEventBannerListener;
    private Map<String, Object> localExtras;
    private Map<String, String> serverExtras;
    private Activity context;
    private String responseBody;

    @Before
    public void setup() {
        subject = new HtmlBanner();
        htmlBannerWebView = TestHtmlBannerWebViewFactory.getSingletonMock();
        customEventBannerListener = mock(CustomEventBanner.CustomEventBannerListener.class);
        context = new Activity();
        localExtras = new HashMap<String, Object>();
        serverExtras = new HashMap<String, String>();
        responseBody = "expected response body";
        serverExtras.put(HTML_RESPONSE_BODY_KEY, Uri.encode(responseBody));
        serverExtras.put(SCROLLABLE_KEY, "false");
    }

    @Test
    public void loadBanner_shouldPopulateTheHtmlWebViewWithHtml() throws Exception {
        subject.loadBanner(context, customEventBannerListener, localExtras, serverExtras);

        assertThat(TestHtmlBannerWebViewFactory.getLatestListener()).isSameAs(customEventBannerListener);
        assertThat(TestHtmlBannerWebViewFactory.getLatestIsScrollable()).isFalse();
        assertThat(TestHtmlBannerWebViewFactory.getLatestRedirectUrl()).isNull();
        assertThat(TestHtmlBannerWebViewFactory.getLatestClickthroughUrl()).isNull();
        verify(htmlBannerWebView).loadHtmlResponse(responseBody);
    }

    @Test
    public void loadBanner_whenNoHtmlResponse_shouldNotifyBannerFailed() throws Exception {
        serverExtras.remove(HTML_RESPONSE_BODY_KEY);
        subject.loadBanner(context, customEventBannerListener, localExtras, serverExtras);

        verify(customEventBannerListener).onBannerFailed(eq(NETWORK_INVALID_STATE));
        assertThat(TestHtmlBannerWebViewFactory.getLatestListener()).isNull();
        assertThat(TestHtmlBannerWebViewFactory.getLatestIsScrollable()).isFalse();
        assertThat(TestHtmlBannerWebViewFactory.getLatestRedirectUrl()).isNull();
        assertThat(TestHtmlBannerWebViewFactory.getLatestClickthroughUrl()).isNull();
        verify(htmlBannerWebView, never()).loadHtmlResponse(anyString());
    }

    @Test
    public void loadBanner_shouldPassParametersThrough() throws Exception {
        serverExtras.put(SCROLLABLE_KEY, "true");
        serverExtras.put(REDIRECT_URL_KEY, "redirectUrl");
        serverExtras.put(CLICKTHROUGH_URL_KEY, "clickthroughUrl");
        subject.loadBanner(context, customEventBannerListener, localExtras, serverExtras);

        assertThat(TestHtmlBannerWebViewFactory.getLatestListener()).isSameAs(customEventBannerListener);
        assertThat(TestHtmlBannerWebViewFactory.getLatestIsScrollable()).isTrue();
        assertThat(TestHtmlBannerWebViewFactory.getLatestRedirectUrl()).isEqualTo("redirectUrl");
        assertThat(TestHtmlBannerWebViewFactory.getLatestClickthroughUrl()).isEqualTo("clickthroughUrl");
        verify(htmlBannerWebView).loadHtmlResponse(responseBody);
    }

    @Test
    public void onInvalidate_shouldDestroyTheHtmlWebView() throws Exception {
        subject.loadBanner(context, customEventBannerListener, localExtras, serverExtras);
        subject.onInvalidate();

        verify(htmlBannerWebView).destroy();
    }

    @Test
    public void loadBanner_shouldCauseServerDimensionsToBeHonoredWhenLayingOutView() throws Exception {
        subject.loadBanner(context, customEventBannerListener, localExtras, serverExtras);
        MoPubView moPubView = TestMoPubViewFactory.getSingletonMock();
        stub(moPubView.getContext()).toReturn(context);
        AdViewController adViewController = new AdViewController(context, moPubView);

        HttpResponse response = new TestHttpResponseWithHeaders(200, "I ain't got no-body");
        response.addHeader("X-Width", "320");
        response.addHeader("X-Height", "50");
        adViewController.configureUsingHttpResponse(response);

        adViewController.setAdContentView(htmlBannerWebView);
        ArgumentCaptor<FrameLayout.LayoutParams> layoutParamsCaptor = ArgumentCaptor.forClass(FrameLayout.LayoutParams.class);
        verify(moPubView).addView(eq(htmlBannerWebView), layoutParamsCaptor.capture());
        FrameLayout.LayoutParams layoutParams = layoutParamsCaptor.getValue();

        assertThat(layoutParams.width).isEqualTo(320);
        assertThat(layoutParams.height).isEqualTo(50);
        assertThat(layoutParams.gravity).isEqualTo(Gravity.CENTER);
    }
}
