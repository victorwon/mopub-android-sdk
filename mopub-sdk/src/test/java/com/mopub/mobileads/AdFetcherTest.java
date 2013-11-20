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

import android.os.Build;
import com.mopub.mobileads.factories.AdFetchTaskFactory;
import com.mopub.mobileads.test.support.SdkTestRunner;
import com.mopub.mobileads.test.support.TestAdFetchTaskFactory;
import com.mopub.mobileads.test.support.TestHttpResponseWithHeaders;
import org.apache.http.HttpResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;

import java.util.*;
import java.util.concurrent.*;

import static com.mopub.mobileads.util.ResponseHeader.AD_TYPE;
import static com.mopub.mobileads.util.ResponseHeader.CUSTOM_EVENT_DATA;
import static com.mopub.mobileads.util.ResponseHeader.CUSTOM_EVENT_NAME;
import static com.mopub.mobileads.util.ResponseHeader.FULL_AD_TYPE;
import static com.mopub.mobileads.util.ResponseHeader.NATIVE_PARAMS;
import static com.mopub.mobileads.util.VersionCode.HONEYCOMB_MR2;
import static com.mopub.mobileads.util.VersionCode.ICE_CREAM_SANDWICH;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.stub;
import static org.mockito.Mockito.verify;

@RunWith(SdkTestRunner.class)
public class AdFetcherTest {
    private AdFetcher subject;
    private AdViewController adViewController;
    private MoPubView moPubView;
    private HttpResponse response;

    @Before
    public void setup() {
        adViewController = mock(AdViewController.class);
        moPubView = mock(MoPubView.class);
        stub(adViewController.getMoPubView()).toReturn(moPubView);

        subject = new AdFetcher(adViewController, "expected userAgent");
        response = new TestHttpResponseWithHeaders(200, "yahoo!!!");
    }

    @Test
    public void shouldSendResponseToAdView() {
        Robolectric.addPendingHttpResponse(response);

        subject.fetchAdForUrl("url");

        verify(adViewController).configureUsingHttpResponse(eq(response));
    }

    @Test
    public void fetchAdForUrl_shouldRouteMillennialBannerToCustomEventHandling() throws Exception {
        String json = "{\"adWidth\": 320, \"adHeight\": 50, \"adUnitID\": \"44310\"}";
        stub(adViewController.getAdConfiguration()).toReturn(mock(AdConfiguration.class));
        response.addHeader(AD_TYPE.getKey(), "millennial_native");
        response.addHeader(NATIVE_PARAMS.getKey(), json);
        Robolectric.addPendingHttpResponse(response);

        subject.fetchAdForUrl("ignored_url");

        Map<String, String> paramsMap = new HashMap<String, String>();
        paramsMap.put(CUSTOM_EVENT_NAME.getKey(), "com.mopub.mobileads.MillennialBanner");
        paramsMap.put(CUSTOM_EVENT_DATA.getKey(), json);

        verify(moPubView).loadCustomEvent(eq(paramsMap));
    }

    @Test
    public void fetchAdForUrl_shouldRouteMillennialInterstitialToCustomEventHandling() throws Exception {
        AdViewController interstitialAdViewController = mock(AdViewController.class);
        MoPubInterstitial.MoPubInterstitialView moPubInterstitialView = mock(MoPubInterstitial.MoPubInterstitialView.class);
        stub(interstitialAdViewController.getMoPubView()).toReturn(moPubInterstitialView);
        stub(interstitialAdViewController.getAdConfiguration()).toReturn(mock(AdConfiguration.class));
        subject = new AdFetcher(interstitialAdViewController, "expected userAgent");

        String json = "{\"adWidth\": 320, \"adHeight\": 480, \"adUnitID\": \"44310\"}";
        response.addHeader(AD_TYPE.getKey(), "interstitial");
        response.addHeader(FULL_AD_TYPE.getKey(), "millennial_full");
        response.addHeader(NATIVE_PARAMS.getKey(), json);
        Robolectric.addPendingHttpResponse(response);

        subject.fetchAdForUrl("ignored_url");

        Map<String, String> paramsMap = new HashMap<String, String>();
        paramsMap.put(CUSTOM_EVENT_NAME.getKey(), "com.mopub.mobileads.MillennialInterstitial");
        paramsMap.put(CUSTOM_EVENT_DATA.getKey(), json);

        verify(moPubInterstitialView).loadCustomEvent(eq(paramsMap));
    }

    @Test
    public void fetchAdForUrl_whenApiLevelIsAtLeastICS_shouldExecuteUsingAnExecutor() throws Exception {
        Robolectric.Reflection.setFinalStaticField(Build.VERSION.class, "SDK_INT", ICE_CREAM_SANDWICH.getApiLevel());
        AdFetchTaskFactory.setInstance(new TestAdFetchTaskFactory());
        AdFetchTask adFetchTask = TestAdFetchTaskFactory.getSingletonMock();

        subject.fetchAdForUrl("some url");

        verify(adFetchTask).executeOnExecutor(eq(AdFetchTask.THREAD_POOL_EXECUTOR), eq("some url"));
        verify(adFetchTask, never()).execute(anyString());
    }

    @Test
    public void fetchAdForUrl_whenApiLevelIsBelowICS_shouldExecuteWithoutAnExecutor() throws Exception {
        Robolectric.Reflection.setFinalStaticField(Build.VERSION.class, "SDK_INT", HONEYCOMB_MR2.getApiLevel());
        AdFetchTaskFactory.setInstance(new TestAdFetchTaskFactory());
        AdFetchTask adFetchTask = TestAdFetchTaskFactory.getSingletonMock();

        subject.fetchAdForUrl("some url");

        verify(adFetchTask, never()).executeOnExecutor(any(Executor.class), anyString());
        verify(adFetchTask).execute(eq("some url"));
    }
}
