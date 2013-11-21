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
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.view.View;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;
import com.mopub.mobileads.test.support.SdkTestRunner;
import com.mopub.mobileads.test.support.TestHtmlInterstitialWebViewFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.robolectric.Robolectric;
import org.robolectric.shadows.ShadowLocalBroadcastManager;

import static android.widget.RelativeLayout.LayoutParams;
import static com.mopub.mobileads.AdFetcher.CLICKTHROUGH_URL_KEY;
import static com.mopub.mobileads.AdFetcher.HTML_RESPONSE_BODY_KEY;
import static com.mopub.mobileads.AdFetcher.REDIRECT_URL_KEY;
import static com.mopub.mobileads.AdFetcher.SCROLLABLE_KEY;
import static com.mopub.mobileads.BaseInterstitialActivity.ACTION_INTERSTITIAL_CLICK;
import static com.mopub.mobileads.BaseInterstitialActivity.ACTION_INTERSTITIAL_FAIL;
import static com.mopub.mobileads.BaseInterstitialActivity.HTML_INTERSTITIAL_INTENT_FILTER;
import static com.mopub.mobileads.CustomEventInterstitial.CustomEventInterstitialListener;
import static com.mopub.mobileads.HtmlInterstitialWebView.MoPubUriJavascriptFireFinishLoadListener;
import static com.mopub.mobileads.MoPubErrorCode.UNSPECIFIED;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.robolectric.Robolectric.shadowOf;

@RunWith(SdkTestRunner.class)
public class MoPubActivityTest extends BaseInterstitialActivityTest {
    public static final String EXPECTED_HTML_DATA = "htmlData";
    public static final boolean EXPECTED_IS_SCROLLABLE = true;
    public static final String EXPECTED_REDIRECT_URL = "redirectUrl";
    public static final String EXPECTED_CLICKTHROUGH_URL = "http://expected_url";

    private HtmlInterstitialWebView htmlInterstitialWebView;
    private Activity context;
    private CustomEventInterstitialListener customEventInterstitialListener;

    @Before
    public void setUp() throws Exception {
        super.setup();
        Intent moPubActivityIntent = createMoPubActivityIntent(EXPECTED_HTML_DATA, EXPECTED_IS_SCROLLABLE, EXPECTED_REDIRECT_URL, EXPECTED_CLICKTHROUGH_URL, adConfiguration);
        htmlInterstitialWebView = TestHtmlInterstitialWebViewFactory.getSingletonMock();
        resetMockedView(htmlInterstitialWebView);
        subject = Robolectric.buildActivity(MoPubActivity.class).withIntent(moPubActivityIntent).create().get();

        context = new Activity();
        customEventInterstitialListener = mock(CustomEventInterstitialListener.class);

        reset(htmlInterstitialWebView);
        resetMockedView(htmlInterstitialWebView);
    }

    @Test
    public void preRenderHtml_shouldPreloadTheHtml() throws Exception {
        String htmlData = "this is nonsense";
        MoPubActivity.preRenderHtml(context, customEventInterstitialListener, htmlData);

        verify(htmlInterstitialWebView).enablePlugins(eq(false));
        verify(htmlInterstitialWebView).addMoPubUriJavascriptInterface(any(MoPubUriJavascriptFireFinishLoadListener.class));
        verify(htmlInterstitialWebView).loadHtmlResponse(htmlData);
    }

    @Test
    public void preRenderHtml_shouldHaveAWebViewClientThatForwardsFinishLoad() throws Exception {
        MoPubActivity.preRenderHtml(context, customEventInterstitialListener, null);

        ArgumentCaptor<WebViewClient> webViewClientCaptor = ArgumentCaptor.forClass(WebViewClient.class);
        verify(htmlInterstitialWebView).setWebViewClient(webViewClientCaptor.capture());
        WebViewClient webViewClient = webViewClientCaptor.getValue();

        webViewClient.shouldOverrideUrlLoading(null, "mopub://finishLoad");

        verify(customEventInterstitialListener).onInterstitialLoaded();
        verify(customEventInterstitialListener, never()).onInterstitialFailed(any(MoPubErrorCode.class));
    }

    @Test
    public void preRenderHtml_shouldHaveAWebViewClientThatForwardsFailLoad() throws Exception {
        MoPubActivity.preRenderHtml(context, customEventInterstitialListener, null);

        ArgumentCaptor<WebViewClient> webViewClientCaptor = ArgumentCaptor.forClass(WebViewClient.class);
        verify(htmlInterstitialWebView).setWebViewClient(webViewClientCaptor.capture());
        WebViewClient webViewClient = webViewClientCaptor.getValue();

        webViewClient.shouldOverrideUrlLoading(null, "mopub://failLoad");

        verify(customEventInterstitialListener, never()).onInterstitialLoaded();
        verify(customEventInterstitialListener).onInterstitialFailed(any(MoPubErrorCode.class));
    }

    @Test
    public void preRenderHtml_shouldHaveAMoPubUriInterfaceThatForwardsOnInterstitialLoaded() throws Exception {
        MoPubActivity.preRenderHtml(context, customEventInterstitialListener, null);

        ArgumentCaptor<MoPubUriJavascriptFireFinishLoadListener> moPubUriJavascriptFireFinishLoadListenerCaptor = ArgumentCaptor.forClass(MoPubUriJavascriptFireFinishLoadListener.class);
        verify(htmlInterstitialWebView).addMoPubUriJavascriptInterface(moPubUriJavascriptFireFinishLoadListenerCaptor.capture());
        MoPubUriJavascriptFireFinishLoadListener moPubUriJavascriptFireFinishLoadListener = moPubUriJavascriptFireFinishLoadListenerCaptor.getValue();

        moPubUriJavascriptFireFinishLoadListener.onInterstitialLoaded();

        verify(customEventInterstitialListener).onInterstitialLoaded();
    }

    @Test
    public void onCreate_shouldLayoutWebView() throws Exception {
        subject.onCreate(null);

        ArgumentCaptor<RelativeLayout.LayoutParams> captor = ArgumentCaptor.forClass(RelativeLayout.LayoutParams.class);
        verify(htmlInterstitialWebView).setLayoutParams(captor.capture());
        RelativeLayout.LayoutParams actualLayoutParams = captor.getValue();

        assertThat(actualLayoutParams.width).isEqualTo(RelativeLayout.LayoutParams.FILL_PARENT);
        assertThat(actualLayoutParams.height).isEqualTo(RelativeLayout.LayoutParams.WRAP_CONTENT);
        assertOnlyOneRuleSet(actualLayoutParams, RelativeLayout.CENTER_IN_PARENT);
    }

    @Test
    public void getAdView_shouldReturnPopulatedHtmlWebView() throws Exception {
        View adView = subject.getAdView();

        assertThat(adView).isSameAs(htmlInterstitialWebView);
        assertThat(TestHtmlInterstitialWebViewFactory.getLatestListener()).isNotNull();
        assertThat(TestHtmlInterstitialWebViewFactory.getLatestIsScrollable()).isEqualTo(EXPECTED_IS_SCROLLABLE);
        assertThat(TestHtmlInterstitialWebViewFactory.getLatestClickthroughUrl()).isEqualTo(EXPECTED_CLICKTHROUGH_URL);
        assertThat(TestHtmlInterstitialWebViewFactory.getLatestRedirectUrl()).isEqualTo(EXPECTED_REDIRECT_URL);
        verify(htmlInterstitialWebView).loadHtmlResponse(EXPECTED_HTML_DATA);
    }

    @Test
    public void onDestroy_shouldDestroyMoPubView() throws Exception {
        subject.onCreate(null);
        subject.onDestroy();

        verify(htmlInterstitialWebView).destroy();
        assertThat(getContentView(subject).getChildCount()).isEqualTo(0);
    }

    @Test
    public void onDestroy_shouldFireJavascriptWebviewDidClose() throws Exception {
        subject.onCreate(null);
        subject.onDestroy();

        verify(htmlInterstitialWebView).loadUrl(eq("javascript:webviewDidClose();"));
    }

    @Test
    public void start_shouldStartMoPubActivityWithCorrectParameters() throws Exception {
        MoPubActivity.start(subject, "expectedResponse", true, "redirectUrl", "clickthroughUrl", adConfiguration);

        Intent nextStartedActivity = Robolectric.getShadowApplication().getNextStartedActivity();
        assertThat(nextStartedActivity.getStringExtra(HTML_RESPONSE_BODY_KEY)).isEqualTo("expectedResponse");
        assertThat(nextStartedActivity.getBooleanExtra(SCROLLABLE_KEY, false)).isTrue();
        assertThat(nextStartedActivity.getStringExtra(REDIRECT_URL_KEY)).isEqualTo("redirectUrl");
        assertThat(nextStartedActivity.getStringExtra(CLICKTHROUGH_URL_KEY)).isEqualTo("clickthroughUrl");
        assertThat(nextStartedActivity.getFlags() & Intent.FLAG_ACTIVITY_NEW_TASK).isNotEqualTo(0);
        assertThat(nextStartedActivity.getComponent().getClassName()).isEqualTo("com.mopub.mobileads.MoPubActivity");
    }

    @Test
    public void getAdView_shouldCreateHtmlInterstitialWebViewAndLoadResponse() throws Exception {
        subject.getAdView();

        assertThat(TestHtmlInterstitialWebViewFactory.getLatestListener()).isNotNull();
        assertThat(TestHtmlInterstitialWebViewFactory.getLatestIsScrollable()).isEqualTo(EXPECTED_IS_SCROLLABLE);
        assertThat(TestHtmlInterstitialWebViewFactory.getLatestRedirectUrl()).isEqualTo(EXPECTED_REDIRECT_URL);
        assertThat(TestHtmlInterstitialWebViewFactory.getLatestClickthroughUrl()).isEqualTo(EXPECTED_CLICKTHROUGH_URL);
        verify(htmlInterstitialWebView).loadHtmlResponse(EXPECTED_HTML_DATA);
    }

    @Test
    public void getAdView_shouldSetUpForBroadcastingClicks() throws Exception {
        subject.getAdView();
        BroadcastReceiver broadcastReceiver = mock(BroadcastReceiver.class);
        ShadowLocalBroadcastManager.getInstance(subject).registerReceiver(broadcastReceiver, HTML_INTERSTITIAL_INTENT_FILTER);

        TestHtmlInterstitialWebViewFactory.getLatestListener().onInterstitialClicked();

        ArgumentCaptor<Intent> intentCaptor = ArgumentCaptor.forClass(Intent.class);
        verify(broadcastReceiver).onReceive(eq(subject), intentCaptor.capture());
        Intent intent = intentCaptor.getValue();
        assertThat(intent.getAction()).isEqualTo(BaseInterstitialActivity.ACTION_INTERSTITIAL_CLICK);
    }

    @Test
    public void getAdView_shouldSetUpForBroadcastingFail() throws Exception {
        subject.getAdView();
        BroadcastReceiver broadcastReceiver = mock(BroadcastReceiver.class);
        ShadowLocalBroadcastManager.getInstance(subject).registerReceiver(broadcastReceiver, HTML_INTERSTITIAL_INTENT_FILTER);

        TestHtmlInterstitialWebViewFactory.getLatestListener().onInterstitialFailed(UNSPECIFIED);

        ArgumentCaptor<Intent> intentCaptor = ArgumentCaptor.forClass(Intent.class);
        verify(broadcastReceiver).onReceive(eq(subject), intentCaptor.capture());
        Intent intent = intentCaptor.getValue();
        assertThat(intent.getAction()).isEqualTo(ACTION_INTERSTITIAL_FAIL);

        assertThat(shadowOf(subject).isFinishing()).isTrue();
    }

    @Test
    public void broadcastingInterstitialListener_onInterstitialLoaded_shouldCallJavascriptWebViewDidAppear() throws Exception {
        MoPubActivity.BroadcastingInterstitialListener broadcastingInterstitialListener = ((MoPubActivity) subject).new BroadcastingInterstitialListener();

        broadcastingInterstitialListener.onInterstitialLoaded();

        verify(htmlInterstitialWebView).loadUrl(eq("javascript:webviewDidAppear();"));
    }

    @Test
    public void broadcastingInterstitialListener_onInterstitialFailed_shouldBroadcastFailAndFinish() throws Exception {
        Intent expectedIntent = new Intent(ACTION_INTERSTITIAL_FAIL);
        ShadowLocalBroadcastManager.getInstance(subject).registerReceiver(broadcastReceiver, HTML_INTERSTITIAL_INTENT_FILTER);

        MoPubActivity.BroadcastingInterstitialListener broadcastingInterstitialListener = ((MoPubActivity) subject).new BroadcastingInterstitialListener();
        broadcastingInterstitialListener.onInterstitialFailed(null);

        verify(broadcastReceiver).onReceive(eq(subject), eq(expectedIntent));
        assertThat(shadowOf(subject).isFinishing()).isTrue();
    }

    @Test
    public void broadcastingInterstitialListener_onInterstitialClicked_shouldBroadcastClick() throws Exception {
        Intent expectedIntent = new Intent(ACTION_INTERSTITIAL_CLICK);
        ShadowLocalBroadcastManager.getInstance(subject).registerReceiver(broadcastReceiver, HTML_INTERSTITIAL_INTENT_FILTER);

        MoPubActivity.BroadcastingInterstitialListener broadcastingInterstitialListener = ((MoPubActivity) subject).new BroadcastingInterstitialListener();
        broadcastingInterstitialListener.onInterstitialClicked();

        verify(broadcastReceiver).onReceive(eq(subject), eq(expectedIntent));
    }

    private Intent createMoPubActivityIntent(String htmlData, boolean isScrollable, String redirectUrl, String clickthroughUrl, AdConfiguration adConfiguration) {
        return MoPubActivity.createIntent(new Activity(), htmlData, isScrollable, redirectUrl, clickthroughUrl, adConfiguration);
    }

    private void assertOnlyOneRuleSet(LayoutParams layoutParams, int desiredRule) {
        int[] rules = layoutParams.getRules();
        for (int ruleIndex = 0; ruleIndex < rules.length; ruleIndex++) {
            int currentRule = rules[ruleIndex];
            if (ruleIndex == desiredRule) {
                assertThat(currentRule).isNotEqualTo(0);
            } else {
                assertThat(currentRule).isEqualTo(0);
            }
        }
    }
}

