package com.mopub.mobileads;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.webkit.WebView;
import com.mopub.mobileads.test.support.SdkTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;

import static com.mopub.mobileads.MoPubErrorCode.UNSPECIFIED;
import static com.mopub.mobileads.MraidBrowser.URL_EXTRA;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(SdkTestRunner.class)
public class HtmlWebViewClientTest {

    private HtmlWebViewClient subject;
    private HtmlWebViewListener htmlWebViewListener;
    private BaseHtmlWebView htmlWebView;

    @Before
    public void setUp() throws Exception {
        htmlWebViewListener = mock(HtmlWebViewListener.class);
        htmlWebView = mock(BaseHtmlWebView.class);
        stub(htmlWebView.getContext()).toReturn(new Activity());
        subject = new HtmlWebViewClient(htmlWebViewListener, htmlWebView, "clickthrough", "redirect");
    }

    @Test
    public void shouldOverrideUrlLoading_withMoPubFinishLoad_shouldCallAdDidLoad() throws Exception {
        boolean didOverrideUrl = subject.shouldOverrideUrlLoading(htmlWebView, "mopub://finishLoad");

        assertThat(didOverrideUrl).isTrue();
        verify(htmlWebViewListener).onLoaded(eq(htmlWebView));
    }

    @Test
    public void shouldOverrideUrlLoading_withMoPubClose_shouldCallAdDidClose() throws Exception {
        boolean didOverrideUrl = subject.shouldOverrideUrlLoading(htmlWebView, "mopub://close");

        assertThat(didOverrideUrl).isTrue();
        verify(htmlWebViewListener).onCollapsed();
    }

    @Test
    public void shouldOverrideUrlLoading_withMoPubFailLoad_shouldCallLoadFailUrl() throws Exception {
        boolean didOverrideUrl = subject.shouldOverrideUrlLoading(htmlWebView, "mopub://failLoad");

        assertThat(didOverrideUrl).isTrue();
        verify(htmlWebViewListener).onFailed(UNSPECIFIED);
    }

    @Test
    public void shouldOverrideUrlLoading_withMoPubCustom_shouldStartCustomIntent() throws Exception {
        boolean didOverrideUrl = subject.shouldOverrideUrlLoading(htmlWebView, "mopub://custom?fnc=myFnc&data=myData");

        assertThat(didOverrideUrl).isTrue();
        verify(htmlWebViewListener).onClicked();
        Intent startedActivity = Robolectric.getShadowApplication().getNextStartedActivity();
        assertThat(startedActivity).isNotNull();
        assertThat(startedActivity.getAction()).isEqualTo("myFnc");
        assertThat(startedActivity.getFlags() & Intent.FLAG_ACTIVITY_NEW_TASK).isNotEqualTo(0);
        assertThat(startedActivity.getStringExtra(HtmlBannerWebView.EXTRA_AD_CLICK_DATA)).isEqualTo("myData");
    }

    @Test
    public void shouldOverrideUrlLoading_withMoPubCustomAndNullData_shouldStartCustomIntent() throws Exception {
        boolean didOverrideUrl = subject.shouldOverrideUrlLoading(htmlWebView, "mopub://custom?fnc=myFnc");

        assertThat(didOverrideUrl).isTrue();
        verify(htmlWebViewListener).onClicked();
        Intent startedActivity = Robolectric.getShadowApplication().getNextStartedActivity();
        assertThat(startedActivity).isNotNull();
        assertThat(startedActivity.getAction()).isEqualTo("myFnc");
        assertThat(startedActivity.getFlags() & Intent.FLAG_ACTIVITY_NEW_TASK).isNotEqualTo(0);
        assertThat(startedActivity.getStringExtra(HtmlBannerWebView.EXTRA_AD_CLICK_DATA)).isNull();
    }

    @Test
    public void shouldOverrideUrlLoading_withPhoneIntent_shouldStartDefaultIntent() throws Exception {
        assertPhoneUrlStartedCorrectIntent("tel:");
        assertPhoneUrlStartedCorrectIntent("voicemail:");
        assertPhoneUrlStartedCorrectIntent("sms:");
        assertPhoneUrlStartedCorrectIntent("mailto:");
        assertPhoneUrlStartedCorrectIntent("geo:");
        assertPhoneUrlStartedCorrectIntent("google.streetview:");
    }

    @Test
    public void shouldOverrideUrlLoading_withValidMarketIntent_shouldOpenPlayStore() throws Exception {
        subject = new HtmlWebViewClient(htmlWebViewListener, htmlWebView, null, null);
        String validMarketUrl = "market://somethingValid";
        Robolectric.packageManager.addResolveInfoForIntent(new Intent(Intent.ACTION_VIEW, Uri.parse(validMarketUrl)), new ResolveInfo());
        boolean didOverrideUrl = subject.shouldOverrideUrlLoading(htmlWebView, validMarketUrl);

        assertThat(didOverrideUrl).isTrue();
        verify(htmlWebViewListener).onClicked();

        Intent startedActivity = assertActivityStarted();
        assertThat(startedActivity.getAction()).isEqualTo(Intent.ACTION_VIEW);
        assertThat(startedActivity.getData().toString()).isEqualTo(validMarketUrl);
    }

    @Test
    public void shouldOverrideUrlLoading_withUnhandleableMarketIntent_shouldNotOpenBrowser() throws Exception {
        String invalidMarketUrl = "market://somethingInvalid";
        boolean didOverrideUrl = subject.shouldOverrideUrlLoading(htmlWebView, invalidMarketUrl);

        assertThat(didOverrideUrl).isTrue();
        verify(htmlWebViewListener, never()).onClicked();

        Intent startedActivity = Robolectric.getShadowApplication().getNextStartedActivity();
        assertThat(startedActivity).isNull();
    }

    @Test
    public void shouldOverrideUrlLoading_withAmazonIntentAndAmazonPresent_shouldOpenAmazonMarket() throws Exception {
        subject = new HtmlWebViewClient(htmlWebViewListener, htmlWebView, null, null);
        String validAmazonUrl = "amzn://somethingValid";
        Robolectric.packageManager.addResolveInfoForIntent(new Intent(Intent.ACTION_VIEW, Uri.parse(validAmazonUrl)), new ResolveInfo());
        boolean didOverrideUrl = subject.shouldOverrideUrlLoading(htmlWebView, validAmazonUrl);

        assertThat(didOverrideUrl).isTrue();
        verify(htmlWebViewListener).onClicked();

        Intent startedActivity = assertActivityStarted();
        assertThat(startedActivity.getAction()).isEqualTo(Intent.ACTION_VIEW);
        assertThat(startedActivity.getData().toString()).isEqualTo(validAmazonUrl);
    }

    @Test
    public void shouldOverrideUrlLoading_withAmazonIntentAndNoAmazon_shouldNotTryToOpenAmazonMarket() throws Exception {
        String invalidAmazonUrl = "amzn://somethingValid";
        boolean didOverrideUrl = subject.shouldOverrideUrlLoading(htmlWebView, invalidAmazonUrl);

        assertThat(didOverrideUrl).isTrue();
        verify(htmlWebViewListener, never()).onClicked();

        Intent startedActivity = Robolectric.getShadowApplication().getNextStartedActivity();
        assertThat(startedActivity).isNull();
    }

    @Test
    public void shouldOverrideUrlLoading_withHttpUrl_shouldOpenBrowser() throws Exception {
        subject = new HtmlWebViewClient(htmlWebViewListener, htmlWebView, null, null);
        String validUrl = "http://www.mopub.com";
        boolean didOverrideUrl = subject.shouldOverrideUrlLoading(htmlWebView, validUrl);

        assertThat(didOverrideUrl).isTrue();
        verify(htmlWebViewListener).onClicked();

        Intent startedActivity = assertActivityStarted();
        assertThat(startedActivity.getComponent().getClassName()).isEqualTo("com.mopub.mobileads.MraidBrowser");
        assertThat(startedActivity.getStringExtra(URL_EXTRA)).isEqualTo(validUrl);
        assertThat(startedActivity.getData()).isNull();
    }

    @Test
    public void shouldOverrideUrlLoading_withClickTrackingRedirect_shouldChangeUrl() throws Exception {
        String validUrl = "http://www.mopub.com";
        subject.shouldOverrideUrlLoading(htmlWebView, validUrl);

        Intent startedActivity = assertActivityStarted();
        assertThat(startedActivity.getStringExtra(URL_EXTRA)).isEqualTo("clickthrough&r=http%3A%2F%2Fwww.mopub.com");
    }

    @Test
    public void shouldOverrideUrlLoading_withEmptyUrl_shouldLoadAboutBlank() throws Exception {
        subject = new HtmlWebViewClient(htmlWebViewListener, htmlWebView, null, null);
        subject.shouldOverrideUrlLoading(htmlWebView, "");

        Intent startedActivity = assertActivityStarted();
        assertThat(startedActivity.getComponent().getClassName()).isEqualTo("com.mopub.mobileads.MraidBrowser");
        assertThat(startedActivity.getStringExtra(URL_EXTRA)).isEqualTo("about:blank");
        assertThat(startedActivity.getData()).isNull();
    }

    @Test
    public void shouldOverrideUrlLoading_withNativeBrowserScheme_shouldStartIntentWithActionView() throws Exception {
        subject = new HtmlWebViewClient(htmlWebViewListener, htmlWebView, null, null);
        subject.shouldOverrideUrlLoading(htmlWebView, "mopubnativebrowser://navigate?url=http://mopub.com");

        Intent startedActivity = assertActivityStarted();
        assertThat(isWebsiteUrl(startedActivity.getData().toString()));
        assertThat(startedActivity.getAction()).isEqualTo("android.intent.action.VIEW");
    }

    @Test
    public void shouldOverrideUrlLoading_withNativeBrowserScheme_shouldCallHtmlWebViewListener() throws Exception {
        subject = new HtmlWebViewClient(htmlWebViewListener, htmlWebView, null, null);
        subject.shouldOverrideUrlLoading(htmlWebView, "mopubnativebrowser://navigate?url=http://mopub.com");

        verify(htmlWebViewListener).onClicked();
        reset(htmlWebViewListener);
    }

    @Test
    public void shouldOverrideUrlLoading_withNativeBrowserScheme_butOpaqueUri_shouldNotBeHandledByNativeBrowser() throws Exception {
        String opaqueNativeBrowserUriString = "mopubnativebrowser:navigate?url=http://mopub.com";
        subject = new HtmlWebViewClient(htmlWebViewListener, htmlWebView, null, null);
        subject.shouldOverrideUrlLoading(htmlWebView, opaqueNativeBrowserUriString);

        Intent startedActivity = assertActivityStarted();
        assertThat(startedActivity.getComponent().getClassName()).isEqualTo("com.mopub.mobileads.MraidBrowser");
        assertThat(startedActivity.getStringExtra(URL_EXTRA)).isEqualTo(opaqueNativeBrowserUriString);
        assertThat(startedActivity.getData()).isNull();
    }

    @Test
    public void shouldOverrideUrlLoading_withNativeBrowserScheme_withInvalidHostSchemeUrl_shouldNotInvokeNativeBrowser() throws Exception {
        subject = new HtmlWebViewClient(htmlWebViewListener, htmlWebView, null, null);
        subject.shouldOverrideUrlLoading(htmlWebView, "something://blah?url=invalid");

        Intent startedActivity = assertActivityStarted();
        assertThat(startedActivity.getAction()).isNotEqualTo("android.intent.action.VIEW");

        verify(htmlWebViewListener).onClicked();
        reset(htmlWebViewListener);
    }

    private boolean isWebsiteUrl(String url){
        return url.startsWith("http://") || url.startsWith("https://");
    }

    @Test
    public void onPageStarted_whenLoadedUrlStartsWithRedirect_shouldOpenInBrowser() throws Exception {
        String url = "redirectUrlToLoad";
        subject = new HtmlWebViewClient(htmlWebViewListener, htmlWebView, null, "redirect");
        WebView view = mock(WebView.class);
        subject.onPageStarted(view, url, null);

        verify(view).stopLoading();

        Intent intent = Robolectric.getShadowApplication().getNextStartedActivity();
        assertThat(intent.getStringExtra(MraidBrowser.URL_EXTRA)).isEqualTo(url);
        assertThat(intent.getFlags() & Intent.FLAG_ACTIVITY_NEW_TASK).isNotEqualTo(0);
        assertThat(intent.getComponent().getClassName()).isEqualTo("com.mopub.mobileads.MraidBrowser");
    }

    @Test
    public void onPageStarted_whenLoadedUrlStartsWithRedirectAndHasClickthrough_shouldOpenInBrowser() throws Exception {
        String url = "redirectUrlToLoad";
        String expectedTrackingUrl = "clickthrough" + "&r=" + url;
        WebView view = mock(WebView.class);
        subject.onPageStarted(view, url, null);

        verify(view).stopLoading();

        Intent intent = Robolectric.getShadowApplication().getNextStartedActivity();
        assertThat(intent.getStringExtra(MraidBrowser.URL_EXTRA)).isEqualTo(expectedTrackingUrl);
        assertThat(intent.getFlags() & Intent.FLAG_ACTIVITY_NEW_TASK).isNotEqualTo(0);
        assertThat(intent.getComponent().getClassName()).isEqualTo("com.mopub.mobileads.MraidBrowser");
    }

    @Test
    public void onPageStarted_whenLoadedUrlDoesntStartWithRedirect_shouldDoNothing() throws Exception {
        WebView view = mock(WebView.class);
        subject.onPageStarted(view, "this doesn't start with redirect", null);

        verify(view, never()).stopLoading();

        assertThat(Robolectric.getShadowApplication().getNextStartedActivity()).isNull();
    }

    private void assertPhoneUrlStartedCorrectIntent(String url) {
        boolean didOverrideUrl = subject.shouldOverrideUrlLoading(htmlWebView, url);

        assertThat(didOverrideUrl).isTrue();

        Intent startedActivity = assertActivityStarted();
        assertThat(startedActivity.getAction()).isEqualTo(Intent.ACTION_VIEW);
        assertThat(startedActivity.getData().toString()).isEqualTo(url);

        verify(htmlWebViewListener).onClicked();
        reset(htmlWebViewListener);
    }

    private Intent assertActivityStarted() {
        Intent startedActivity = Robolectric.getShadowApplication().getNextStartedActivity();
        assertThat(startedActivity).isNotNull();
        assertThat(startedActivity.getFlags() & Intent.FLAG_ACTIVITY_NEW_TASK).isNotEqualTo(0);
        return startedActivity;
    }
}
