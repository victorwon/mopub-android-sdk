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
import android.content.Intent;
import android.net.Uri;
import com.mopub.mobileads.test.support.SdkTestRunner;
import com.mopub.mobileads.test.support.TestHtmlInterstitialWebViewFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.shadows.ShadowLocalBroadcastManager;

import java.util.*;

import static com.mopub.mobileads.AdFetcher.CLICKTHROUGH_URL_KEY;
import static com.mopub.mobileads.AdFetcher.HTML_RESPONSE_BODY_KEY;
import static com.mopub.mobileads.AdFetcher.REDIRECT_URL_KEY;
import static com.mopub.mobileads.AdFetcher.SCROLLABLE_KEY;
import static com.mopub.mobileads.BaseInterstitialActivity.ACTION_INTERSTITIAL_DISMISS;
import static com.mopub.mobileads.BaseInterstitialActivity.ACTION_INTERSTITIAL_SHOW;
import static com.mopub.mobileads.CustomEventInterstitial.CustomEventInterstitialListener;
import static com.mopub.mobileads.MoPubErrorCode.NETWORK_INVALID_STATE;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@RunWith(SdkTestRunner.class)
public class HtmlInterstitialTest extends ResponseBodyInterstitialTest {
    private CustomEventInterstitialListener customEventInterstitialListener;
    private Activity context;
    private Map<String,Object> localExtras;
    private Map<String,String> serverExtras;
    private HtmlInterstitialWebView htmlInterstitialWebView;
    private String expectedResponse;

    @Before
    public void setUp() throws Exception {
        subject = new HtmlInterstitial();

        expectedResponse = "this is the response";
        htmlInterstitialWebView = TestHtmlInterstitialWebViewFactory.getSingletonMock();
        context = new Activity();
        customEventInterstitialListener = mock(CustomEventInterstitialListener.class);
        localExtras = new HashMap<String, Object>();
        serverExtras = new HashMap<String, String>();
        serverExtras.put(HTML_RESPONSE_BODY_KEY, Uri.encode(expectedResponse));
    }

    @Test
    public void loadInterstitial_shouldNotifyCustomEventInterstitialListenerOnLoaded() throws Exception {
        subject.loadInterstitial(context, customEventInterstitialListener, localExtras, serverExtras);

//        verify(customEventInterstitialListener).onInterstitialLoaded();
    }

    @Test
    public void loadInterstitial_whenNoHtmlResponsePassedIn_shouldCallLoadFailUrl() throws Exception {
        serverExtras.remove(HTML_RESPONSE_BODY_KEY);
        subject.loadInterstitial(context, customEventInterstitialListener, localExtras, serverExtras);

        assertThat(TestHtmlInterstitialWebViewFactory.getLatestListener()).isNull();
        assertThat(TestHtmlInterstitialWebViewFactory.getLatestIsScrollable()).isFalse();
        assertThat(TestHtmlInterstitialWebViewFactory.getLatestRedirectUrl()).isNull();
        assertThat(TestHtmlInterstitialWebViewFactory.getLatestClickthroughUrl()).isNull();
        verify(customEventInterstitialListener).onInterstitialFailed(NETWORK_INVALID_STATE);
        verify(htmlInterstitialWebView, never()).loadHtmlResponse(anyString());
    }


    @Test
    public void showInterstitial_withMinimumExtras_shouldStartMoPubActivityWithDefaults() throws Exception {

        subject.loadInterstitial(context, customEventInterstitialListener, localExtras, serverExtras);
        subject.showInterstitial();

        Intent nextStartedActivity = Robolectric.getShadowApplication().getNextStartedActivity();
        assertThat(nextStartedActivity.getStringExtra(HTML_RESPONSE_BODY_KEY)).isEqualTo(expectedResponse);
        assertThat(nextStartedActivity.getBooleanExtra(SCROLLABLE_KEY, false)).isFalse();
        assertThat(nextStartedActivity.getStringExtra(REDIRECT_URL_KEY)).isNull();
        assertThat(nextStartedActivity.getStringExtra(CLICKTHROUGH_URL_KEY)).isNull();
        assertThat(nextStartedActivity.getFlags() & Intent.FLAG_ACTIVITY_NEW_TASK).isNotEqualTo(0);
        assertThat(nextStartedActivity.getComponent().getClassName()).isEqualTo("com.mopub.mobileads.MoPubActivity");
    }

    @Test
    public void showInterstitial_shouldStartMoPubActivityWithAllExtras() throws Exception {
        serverExtras.put(SCROLLABLE_KEY, "true");
        serverExtras.put(REDIRECT_URL_KEY, "redirectUrl");
        serverExtras.put(CLICKTHROUGH_URL_KEY, "clickthroughUrl");

        subject.loadInterstitial(context, customEventInterstitialListener, localExtras, serverExtras);
        subject.showInterstitial();

        Intent nextStartedActivity = Robolectric.getShadowApplication().getNextStartedActivity();
        assertThat(nextStartedActivity.getStringExtra(HTML_RESPONSE_BODY_KEY)).isEqualTo(expectedResponse);
        assertThat(nextStartedActivity.getBooleanExtra(SCROLLABLE_KEY, false)).isTrue();
        assertThat(nextStartedActivity.getStringExtra(REDIRECT_URL_KEY)).isEqualTo("redirectUrl");
        assertThat(nextStartedActivity.getStringExtra(CLICKTHROUGH_URL_KEY)).isEqualTo("clickthroughUrl");
        assertThat(nextStartedActivity.getFlags() & Intent.FLAG_ACTIVITY_NEW_TASK).isNotEqualTo(0);
        assertThat(nextStartedActivity.getComponent().getClassName()).isEqualTo("com.mopub.mobileads.MoPubActivity");
    }

    @Test
    public void loadInterstitial_shouldConnectListenerToBroadcastReceiver() throws Exception {
        subject.loadInterstitial(context, customEventInterstitialListener, localExtras, serverExtras);

        Intent intent;
        intent = new Intent(ACTION_INTERSTITIAL_SHOW);
        ShadowLocalBroadcastManager.getInstance(context).sendBroadcast(intent);

        verify(customEventInterstitialListener).onInterstitialShown();

        intent = new Intent(ACTION_INTERSTITIAL_DISMISS);
        ShadowLocalBroadcastManager.getInstance(context).sendBroadcast(intent);

        verify(customEventInterstitialListener).onInterstitialDismissed();
    }

    @Test
    public void onInvalidate_shouldDisconnectListenerToBroadcastReceiver() throws Exception {
        subject.loadInterstitial(context, customEventInterstitialListener, localExtras, serverExtras);
        subject.onInvalidate();

        Intent intent;
        intent = new Intent(ACTION_INTERSTITIAL_SHOW);
        ShadowLocalBroadcastManager.getInstance(context).sendBroadcast(intent);

        verify(customEventInterstitialListener, never()).onInterstitialShown();

        intent = new Intent(ACTION_INTERSTITIAL_DISMISS);
        ShadowLocalBroadcastManager.getInstance(context).sendBroadcast(intent);

        verify(customEventInterstitialListener, never()).onInterstitialDismissed();
    }
}
