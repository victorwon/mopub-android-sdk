package com.mopub.mobileads;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.view.View;
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
import static com.mopub.mobileads.BaseInterstitialActivity.ACTION_INTERSTITIAL_FAIL;
import static com.mopub.mobileads.BaseInterstitialActivity.ACTION_INTERSTITIAL_SHOW;
import static com.mopub.mobileads.BaseInterstitialActivity.HTML_INTERSTITIAL_INTENT_FILTER;
import static com.mopub.mobileads.MoPubErrorCode.UNSPECIFIED;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
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

    @Before
    public void setUp() throws Exception {
        super.setup();
        Intent moPubActivityIntent = createMoPubActivityIntent(EXPECTED_HTML_DATA, EXPECTED_IS_SCROLLABLE, EXPECTED_REDIRECT_URL, EXPECTED_CLICKTHROUGH_URL);
        htmlInterstitialWebView = TestHtmlInterstitialWebViewFactory.getSingletonMock();
        resetMockedView(htmlInterstitialWebView);
        subject = Robolectric.buildActivity(MoPubActivity.class).withIntent(moPubActivityIntent).create().get();
        reset(htmlInterstitialWebView);
        resetMockedView(htmlInterstitialWebView);
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
    public void start_shouldStartMoPubActivityWithCorrectParameters() throws Exception {
        MoPubActivity.start(subject, "expectedResponse", true, "redirectUrl", "clickthroughUrl");

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
    public void getAdView_shouldSetUpForBroadcastingShow() throws Exception {
        subject.getAdView();
        BroadcastReceiver broadcastReceiver = mock(BroadcastReceiver.class);
        ShadowLocalBroadcastManager.getInstance(subject).registerReceiver(broadcastReceiver, HTML_INTERSTITIAL_INTENT_FILTER);

        TestHtmlInterstitialWebViewFactory.getLatestListener().onInterstitialShown();

        ArgumentCaptor<Intent> intentCaptor = ArgumentCaptor.forClass(Intent.class);
        verify(broadcastReceiver).onReceive(eq(subject), intentCaptor.capture());
        Intent intent = intentCaptor.getValue();
        assertThat(intent.getAction()).isEqualTo(ACTION_INTERSTITIAL_SHOW);
    }

    private Intent createMoPubActivityIntent(String htmlData, boolean isScrollable, String redirectUrl, String clickthroughUrl) {
        return MoPubActivity.createIntent(new Activity(), htmlData, isScrollable, redirectUrl, clickthroughUrl);
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

