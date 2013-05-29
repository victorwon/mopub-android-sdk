package com.mopub.mobileads;

import android.app.Activity;
import android.webkit.WebViewClient;
import com.mopub.mobileads.test.support.SdkTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.shadows.ShadowWebView;

import static com.mopub.mobileads.MoPubErrorCode.INTERNAL_ERROR;
import static com.mopub.mobileads.MoPubErrorCode.NO_FILL;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.robolectric.Robolectric.shadowOf;

@RunWith(SdkTestRunner.class)
public class AdWebViewTest {

    private AdViewController adWebViewController;
    private AdWebView subject;

    @Before
    public void setup() {
        adWebViewController = mock(AdViewController.class);
        subject = new AdWebView(adWebViewController, new Activity());
    }

    @Test
    public void shouldSetupWebViewClient() throws Exception {
        WebViewClient webViewClient = shadowOf(subject).getWebViewClient();
        assertThat(webViewClient).isNotNull();
        assertThat(webViewClient).isInstanceOf(AdWebViewClient.class);
    }

    @Test
    public void loadUrl_shouldFetchAd() throws Exception {
        String url = "http://www.guy.com";
        subject.loadUrl(url);

        verify(adWebViewController).fetchAd(eq(url));
    }

    @Test
    public void loadUrl_whenUrlIsJavascript_shouldCallSuperLoadUrl() throws Exception {
        String javascriptUrl = "javascript:function() {alert(\"guy\")};";
        subject.loadUrl(javascriptUrl);

        assertThat(shadowOf(subject).getLastLoadedUrl()).isEqualTo(javascriptUrl);

        verify(adWebViewController, never()).fetchAd(anyString());
    }

    @Test
    public void loadUrl_whenAlreadyLoading_shouldNotFetchAd() throws Exception {
        String url = "http://www.guy.com";
        subject.loadUrl(url);
        reset(adWebViewController);

        subject.loadUrl(url);

        verify(adWebViewController, never()).fetchAd(anyString());
    }

    @Test
    public void loadUrl_shouldClearTheFailUrl() throws Exception {
        subject.setFailUrl("blarg:");
        subject.loadUrl("http://www.goodness.com");
        reset(adWebViewController);

        subject.loadFailUrl(null);

        verify(adWebViewController, never()).fetchAd(anyString());
        verify(adWebViewController).adDidFail(NO_FILL);
    }


    @Test
    public void loadUrl_shouldAcceptNullParameter() throws Exception {
        subject.loadUrl(null);
        // pass
    }

    @Test
    public void reload_shouldReuseOldUrl() throws Exception {
        String url = "http://www.guy.com";
        subject.loadUrl(url);
        subject.setNotLoading();
        reset(adWebViewController);

        subject.reload();

        verify(adWebViewController).fetchAd(eq(url));
    }

    @Test
    public void loadFailUrl_shouldLoadFailUrl() throws Exception {
        String failUrl = "http://www.bad.man";
        subject.setFailUrl(failUrl);
        subject.loadFailUrl(INTERNAL_ERROR);

        verify(adWebViewController).fetchAd(eq(failUrl));
        verify(adWebViewController, never()).adDidFail(any(MoPubErrorCode.class));
    }

    @Test
    public void loadFailUrl_shouldAcceptNullErrorCode() throws Exception {
        subject.loadFailUrl(null);
        // pass
    }

    @Test
    public void setWebViewScrollingEnabled_shouldSetUpTouchListener() throws Exception {
        ShadowWebView shadowSubject = shadowOf(subject);

        subject.setWebViewScrollingEnabled(false);
        assertThat(shadowSubject.getOnTouchListener()).isNotNull();

        subject.setWebViewScrollingEnabled(true);
        assertThat(shadowSubject.getOnTouchListener()).isNull();
    }
}
