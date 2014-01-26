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

import android.content.ComponentName;
import android.content.Intent;
import android.os.Build;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import com.mopub.mobileads.test.support.SdkTestRunner;
import com.mopub.mobileads.test.support.TestMraidViewFactory;
import org.fest.assertions.api.ANDROID;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.robolectric.Robolectric;
import org.robolectric.shadows.ShadowLocalBroadcastManager;

import static com.mopub.mobileads.AdFetcher.HTML_RESPONSE_BODY_KEY;
import static com.mopub.mobileads.BaseInterstitialActivity.ACTION_INTERSTITIAL_DISMISS;
import static com.mopub.mobileads.BaseInterstitialActivity.HTML_INTERSTITIAL_INTENT_FILTER;
import static com.mopub.mobileads.MraidView.MraidListener;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.robolectric.Robolectric.shadowOf;

@RunWith(SdkTestRunner.class)
public class MraidActivityTest extends BaseInterstitialActivityTest {

    private MraidView mraidView;
    private CustomEventInterstitial.CustomEventInterstitialListener customEventInterstitialListener;

    @Before
    public void setUp() throws Exception {
        super.setup();
        Intent mraidActivityIntent = createMraidActivityIntent(EXPECTED_SOURCE);
        mraidView = TestMraidViewFactory.getSingletonMock();
        resetMockedView(mraidView);
        subject = Robolectric.buildActivity(MraidActivity.class).withIntent(mraidActivityIntent).create().get();
        resetMockedView(mraidView);
        customEventInterstitialListener = mock(CustomEventInterstitial.CustomEventInterstitialListener.class);
    }

    @Test
    public void preRenderHtml_shouldDisablePluginsSetListenersAndLoadHtml() throws Exception {
        MraidActivity.preRenderHtml(null, customEventInterstitialListener, "3:27");

        verify(mraidView).enablePlugins(eq(false));
        verify(mraidView).setMraidListener(any(MraidListener.class));
        verify(mraidView).setWebViewClient(any(WebViewClient.class));
        verify(mraidView).loadHtmlData(eq("3:27"));
    }

    @Test
    public void preRenderHtml_shouldCallCustomEventInterstitialOnInterstitialLoaded_whenMraidListenerOnReady() throws Exception {
        MraidActivity.preRenderHtml(null, customEventInterstitialListener, "");

        ArgumentCaptor<MraidListener> mraidListenerArgumentCaptorr = ArgumentCaptor.forClass(MraidListener.class);
        verify(mraidView).setMraidListener(mraidListenerArgumentCaptorr.capture());
        MraidListener mraidListener = mraidListenerArgumentCaptorr.getValue();

        mraidListener.onReady(null);

        verify(customEventInterstitialListener).onInterstitialLoaded();
    }

    @Test
    public void preRenderHtml_shouldCallCustomEventInterstitialOnInterstitialFailed_whenMraidListenerOnFailure() throws Exception {
        MraidActivity.preRenderHtml(null, customEventInterstitialListener, "");

        ArgumentCaptor<MraidListener> mraidListenerArgumentCaptorr = ArgumentCaptor.forClass(MraidListener.class);
        verify(mraidView).setMraidListener(mraidListenerArgumentCaptorr.capture());
        MraidListener mraidListener = mraidListenerArgumentCaptorr.getValue();

        mraidListener.onFailure(null);

        verify(customEventInterstitialListener).onInterstitialFailed(null);
    }

    @Test
    public void preRenderHtml_whenWebViewClientShouldOverrideUrlLoading_shouldReturnTrue() throws Exception {
        MraidActivity.preRenderHtml(null, customEventInterstitialListener, "");

        ArgumentCaptor<WebViewClient> webViewClientArgumentCaptor = ArgumentCaptor.forClass(WebViewClient.class);
        verify(mraidView).setWebViewClient(webViewClientArgumentCaptor.capture());
        WebViewClient webViewClient = webViewClientArgumentCaptor.getValue();

        boolean consumeUrlLoading = webViewClient.shouldOverrideUrlLoading(null, null);

        assertThat(consumeUrlLoading).isTrue();
        verify(customEventInterstitialListener, never()).onInterstitialLoaded();
        verify(customEventInterstitialListener, never()).onInterstitialFailed(any(MoPubErrorCode.class));
    }

    @Test
    public void preRenderHtml_shouldCallCustomEventInterstitialOnInterstitialLoaded_whenWebViewClientOnPageFinished() throws Exception {
        MraidActivity.preRenderHtml(null, customEventInterstitialListener, "");

        ArgumentCaptor<WebViewClient> webViewClientArgumentCaptor = ArgumentCaptor.forClass(WebViewClient.class);
        verify(mraidView).setWebViewClient(webViewClientArgumentCaptor.capture());
        WebViewClient webViewClient = webViewClientArgumentCaptor.getValue();

        webViewClient.onPageFinished(null, null);

        verify(customEventInterstitialListener).onInterstitialLoaded();
    }

    @Test
    public void onCreate_shouldSetupAnMraidView() throws Exception {
        subject.onCreate(null);

        assertThat(getContentView(subject).getChildAt(0)).isSameAs(mraidView);
        verify(mraidView).setMraidListener(any(MraidListener.class));
        verify(mraidView).setOnCloseButtonStateChange(any(MraidView.OnCloseButtonStateChangeListener.class));

        verify(mraidView).loadHtmlData(EXPECTED_SOURCE);
    }

    @Test
    public void onCreate_whenICS_shouldSetHardwareAcceleratedFlag() throws Exception {
        Robolectric.Reflection.setFinalStaticField(Build.VERSION.class, "SDK_INT", 14);

        subject.onCreate(null);

        boolean hardwareAccelerated = shadowOf(subject.getWindow()).getFlag(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
        assertThat(hardwareAccelerated).isTrue();
    }

    @Test
    public void onCreate_whenPreICS_shouldNotSetHardwareAcceleratedFlag() throws Exception {
        Robolectric.Reflection.setFinalStaticField(Build.VERSION.class, "SDK_INT", 13);

        subject.onCreate(null);

        boolean hardwareAccelerated = shadowOf(subject.getWindow()).getFlag(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
        assertThat(hardwareAccelerated).isFalse();
    }

    @Test
    public void onDestroy_DestroyMraidView() throws Exception {
        Intent expectedIntent = new Intent(ACTION_INTERSTITIAL_DISMISS);
        ShadowLocalBroadcastManager.getInstance(subject).registerReceiver(broadcastReceiver, HTML_INTERSTITIAL_INTENT_FILTER);

        subject.onCreate(null);
        subject.onDestroy();

        verify(broadcastReceiver).onReceive(eq(subject), eq(expectedIntent));
        verify(mraidView).destroy();
        assertThat(getContentView(subject).getChildCount()).isEqualTo(0);
    }

    @Test
    public void getAdView_shouldSetupOnReadyListener() throws Exception {
        subject.onCreate(null);
        resetMockedView(mraidView);
        ArgumentCaptor<MraidListener> captor = ArgumentCaptor.forClass(MraidListener.class);
        View actualAdView = subject.getAdView();

        assertThat(actualAdView).isSameAs(mraidView);
        verify(mraidView).setMraidListener(captor.capture());

        subject.hideInterstitialCloseButton();
        captor.getValue().onReady(null);
        ImageButton closeButton = (ImageButton) getContentView(subject).getChildAt(1);
        assertThat(closeButton).isNotNull();
    }

    @Test
    public void baseMraidListenerOnReady_shouldFireJavascriptWebViewDidAppear() throws Exception {
        subject.onCreate(null);
        resetMockedView(mraidView);
        ArgumentCaptor<MraidListener> captor = ArgumentCaptor.forClass(MraidListener.class);
        View actualAdView = subject.getAdView();

        assertThat(actualAdView).isSameAs(mraidView);
        verify(mraidView).setMraidListener(captor.capture());

        MraidListener baseMraidListener = captor.getValue();
        baseMraidListener.onReady(null);

        verify(mraidView).loadUrl(eq("javascript:webviewDidAppear();"));
    }

    @Test
    public void baseMraidListenerOnClose_shouldFireJavascriptWebViewDidClose() throws Exception {
        subject.onCreate(null);
        resetMockedView(mraidView);
        ArgumentCaptor<MraidListener> captor = ArgumentCaptor.forClass(MraidListener.class);
        View actualAdView = subject.getAdView();

        assertThat(actualAdView).isSameAs(mraidView);
        verify(mraidView).setMraidListener(captor.capture());

        MraidListener baseMraidListener = captor.getValue();
        baseMraidListener.onClose(null, null);

        verify(mraidView).loadUrl(eq("javascript:webviewDidClose();"));
    }

    @Test
    public void getAdView_shouldSetupOnCloseButtonStateChangeListener() throws Exception {
        subject.onCreate(null);
        resetMockedView(mraidView);
        ArgumentCaptor<MraidView.OnCloseButtonStateChangeListener> captor = ArgumentCaptor.forClass(MraidView.OnCloseButtonStateChangeListener.class);
        View actualAdView = subject.getAdView();

        assertThat(actualAdView).isSameAs(mraidView);
        verify(mraidView).setOnCloseButtonStateChange(captor.capture());
        MraidView.OnCloseButtonStateChangeListener listener = captor.getValue();

        ANDROID.assertThat(getCloseButton()).isVisible();

        listener.onCloseButtonStateChange(null, false);
        ANDROID.assertThat(getCloseButton()).isNotVisible();

        listener.onCloseButtonStateChange(null, true);
        ANDROID.assertThat(getCloseButton()).isVisible();
    }

    @Test
    public void getAdView_shouldSetupOnCloseListener() throws Exception {
        subject.onCreate(null);
        resetMockedView(mraidView);
        ArgumentCaptor<MraidListener> captor = ArgumentCaptor.forClass(MraidListener.class);
        View actualAdView = subject.getAdView();

        assertThat(actualAdView).isSameAs(mraidView);
        verify(mraidView).setMraidListener(captor.capture());

        captor.getValue().onClose(null, null);

        ANDROID.assertThat(subject).isFinishing();
    }

    @Test
    public void onPause_shouldOnPauseMraidView() throws Exception {
        subject.onCreate(null);
        ((MraidActivity)subject).onPause();

        verify(mraidView).onPause();
    }

    @Test
    public void onResume_shouldResumeMraidView() throws Exception {
        subject.onCreate(null);
        ((MraidActivity)subject).onPause();
        ((MraidActivity)subject).onResume();

        verify(mraidView).onResume();
    }

    private Intent createMraidActivityIntent(String expectedSource) {
        Intent mraidActivityIntent = new Intent();
        mraidActivityIntent.setComponent(new ComponentName("", ""));
        mraidActivityIntent.putExtra(HTML_RESPONSE_BODY_KEY, expectedSource);
        return mraidActivityIntent;
    }
}
