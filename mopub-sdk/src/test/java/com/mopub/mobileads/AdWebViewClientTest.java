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
public class AdWebViewClientTest {

    private AdWebViewClient subject;
    private WebView webView;
    private AdViewController adViewController;
    private AdWebView adWebView;

    @Before
    public void setUp() throws Exception {
        adViewController = mock(AdViewController.class);
        adWebView = mock(AdWebView.class);
        stub(adWebView.getContext()).toReturn(new Activity());
        subject = new AdWebViewClient(adViewController, adWebView);
    }

    @Test
    public void shouldOverrideUrlLoading_withMoPubFinishLoad_shouldCallAdDidLoad() throws Exception {
        boolean didOverrideUrl = subject.shouldOverrideUrlLoading(adWebView, "mopub://finishLoad");

        assertThat(didOverrideUrl).isTrue();
        verify(adViewController).adDidLoad();
    }

    @Test
    public void shouldOverrideUrlLoading_withMoPubClose_shouldCallAdDidClose() throws Exception {
        boolean didOverrideUrl = subject.shouldOverrideUrlLoading(adWebView, "mopub://close");

        assertThat(didOverrideUrl).isTrue();
        verify(adViewController).adDidClose();
    }

    @Test
    public void shouldOverrideUrlLoading_withMoPubFailLoad_shouldCallLoadFailUrl() throws Exception {
        boolean didOverrideUrl = subject.shouldOverrideUrlLoading(adWebView, "mopub://failLoad");

        assertThat(didOverrideUrl).isTrue();
        verify(adWebView).loadFailUrl(UNSPECIFIED);
    }

    @Test
    public void shouldOverrideUrlLoading_withMoPubCustom_shouldStartCustomIntent() throws Exception {
        boolean didOverrideUrl = subject.shouldOverrideUrlLoading(adWebView, "mopub://custom?fnc=myFnc&data=myData");

        assertThat(didOverrideUrl).isTrue();
        verify(adViewController).registerClick();
        Intent startedActivity = Robolectric.getShadowApplication().getNextStartedActivity();
        assertThat(startedActivity).isNotNull();
        assertThat(startedActivity.getAction()).isEqualTo("myFnc");
        assertThat(startedActivity.getFlags() & Intent.FLAG_ACTIVITY_NEW_TASK).isNotEqualTo(0);
        assertThat(startedActivity.getStringExtra(AdWebView.EXTRA_AD_CLICK_DATA)).isEqualTo("myData");
    }

    @Test
    public void shouldOverrideUrlLoading_withMoPubCustomAndNullData_shouldStartCustomIntent() throws Exception {
        boolean didOverrideUrl = subject.shouldOverrideUrlLoading(adWebView, "mopub://custom?fnc=myFnc");

        assertThat(didOverrideUrl).isTrue();
        verify(adViewController).registerClick();
        Intent startedActivity = Robolectric.getShadowApplication().getNextStartedActivity();
        assertThat(startedActivity).isNotNull();
        assertThat(startedActivity.getAction()).isEqualTo("myFnc");
        assertThat(startedActivity.getFlags() & Intent.FLAG_ACTIVITY_NEW_TASK).isNotEqualTo(0);
        assertThat(startedActivity.getStringExtra(AdWebView.EXTRA_AD_CLICK_DATA)).isNull();
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
    public void shouldOverrideUrlLoading_withValidMarketIntent_shouldOpenBrowser() throws Exception {
        MoPubView moPubView = mock(MoPubView.class);
        stub(adViewController.getMoPubView()).toReturn(moPubView);
        String validMarketUrl = "market://somethingValid";
        Robolectric.packageManager.addResolveInfoForIntent(new Intent(Intent.ACTION_VIEW, Uri.parse(validMarketUrl)), new ResolveInfo());
        boolean didOverrideUrl = subject.shouldOverrideUrlLoading(adWebView, validMarketUrl);

        assertThat(didOverrideUrl).isTrue();
        verify(moPubView).adClicked();

        Intent startedActivity = assertActivityStarted();
        assertThat(startedActivity.getComponent().getClassName()).isEqualTo("com.mopub.mobileads.MraidBrowser");
        assertThat(startedActivity.getStringExtra(URL_EXTRA)).isEqualTo(validMarketUrl);
        assertThat(startedActivity.getData()).isNull();
    }

    @Test
    public void shouldOverrideUrlLoading_withUnhandleableMarketIntent_shouldNotOpenBrowser() throws Exception {
        MoPubView moPubView = mock(MoPubView.class);
        stub(adViewController.getMoPubView()).toReturn(moPubView);
        String validMarketUrl = "market://somethingValid";
        boolean didOverrideUrl = subject.shouldOverrideUrlLoading(adWebView, validMarketUrl);

        assertThat(didOverrideUrl).isTrue();
        verify(moPubView, never()).adClicked();

        Intent startedActivity = Robolectric.getShadowApplication().getNextStartedActivity();
        assertThat(startedActivity).isNull();
    }

    @Test
    public void shouldOverrideUrlLoading_withHttpUrl_shouldOpenBrowser() throws Exception {
        MoPubView moPubView = mock(MoPubView.class);
        stub(adViewController.getMoPubView()).toReturn(moPubView);
        String validUrl = "http://www.mopub.com";
        boolean didOverrideUrl = subject.shouldOverrideUrlLoading(adWebView, validUrl);

        assertThat(didOverrideUrl).isTrue();
        verify(moPubView).adClicked();

        Intent startedActivity = assertActivityStarted();
        assertThat(startedActivity.getComponent().getClassName()).isEqualTo("com.mopub.mobileads.MraidBrowser");
        assertThat(startedActivity.getStringExtra(URL_EXTRA)).isEqualTo(validUrl);
        assertThat(startedActivity.getData()).isNull();
    }

    @Test
    public void shouldOverrideUrlLoading_withClickTrackingRedirect_shouldChangeUrl() throws Exception {
        String validUrl = "http://www.mopub.com";
        stub(adViewController.getMoPubView()).toReturn(mock(MoPubView.class));
        stub(adViewController.getClickthroughUrl()).toReturn("http://redirecturl.com");
        subject.shouldOverrideUrlLoading(adWebView, validUrl);

        Intent startedActivity = assertActivityStarted();
        assertThat(startedActivity.getStringExtra(URL_EXTRA)).isEqualTo("http://redirecturl.com&r=http%3A%2F%2Fwww.mopub.com");
    }

    @Test
    public void shouldOverrideUrlLoading_withEmptyUrl_shouldLoadAboutBlank() throws Exception {
        stub(adViewController.getMoPubView()).toReturn(mock(MoPubView.class));
        subject.shouldOverrideUrlLoading(adWebView, "");

        Intent startedActivity = assertActivityStarted();
        assertThat(startedActivity.getComponent().getClassName()).isEqualTo("com.mopub.mobileads.MraidBrowser");
        assertThat(startedActivity.getStringExtra(URL_EXTRA)).isEqualTo("about:blank");
        assertThat(startedActivity.getData()).isNull();
    }

    private void assertPhoneUrlStartedCorrectIntent(String url) {
        boolean didOverrideUrl = subject.shouldOverrideUrlLoading(adWebView, url);

        assertThat(didOverrideUrl).isTrue();

        Intent startedActivity = assertActivityStarted();
        assertThat(startedActivity.getAction()).isEqualTo(Intent.ACTION_VIEW);
        assertThat(startedActivity.getData().toString()).isEqualTo(url);

        verify(adViewController).registerClick();
        reset(adViewController);
    }

    private Intent assertActivityStarted() {
        Intent startedActivity = Robolectric.getShadowApplication().getNextStartedActivity();
        assertThat(startedActivity).isNotNull();
        assertThat(startedActivity.getFlags() & Intent.FLAG_ACTIVITY_NEW_TASK).isNotEqualTo(0);
        return startedActivity;
    }
}
