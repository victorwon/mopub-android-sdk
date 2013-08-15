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

    @Before
    public void setUp() throws Exception {
        subject = new HtmlInterstitialWebView(new Activity());
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
        verify(customEventInterstitialListener).onInterstitialShown();

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
        verify(customEventInterstitialListener).onInterstitialShown();
    }
}
