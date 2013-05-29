package com.mopub.mobileads;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import com.mopub.mobileads.test.support.TestCustomEventInterstitialAdapterFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static com.mopub.mobileads.AdFetcher.CUSTOM_EVENT_DATA_HEADER;
import static com.mopub.mobileads.AdFetcher.CUSTOM_EVENT_NAME_HEADER;
import static com.mopub.mobileads.BaseActivity.SOURCE_KEY;
import static com.mopub.mobileads.MoPubActivity.AD_UNIT_ID_KEY;
import static com.mopub.mobileads.MoPubActivity.CLICKTHROUGH_URL_KEY;
import static com.mopub.mobileads.MoPubActivity.KEYWORDS_KEY;
import static com.mopub.mobileads.MoPubErrorCode.INTERNAL_ERROR;
import static com.mopub.mobileads.MoPubErrorCode.UNSPECIFIED;
import static com.mopub.mobileads.MoPubView.LocationAwareness.LOCATION_AWARENESS_NORMAL;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.robolectric.Robolectric.shadowOf;

@RunWith(com.mopub.mobileads.test.support.SdkTestRunner.class)
public class MoPubInterstitialTest {

    private static final String KEYWORDS_VALUE = "expected_keywords";
    private static final String AD_UNIT_ID_VALUE = "expected_adunitid";
    private static final String SOURCE_VALUE = "expected_source";
    private static final String CLICKTHROUGH_URL_VALUE = "expected_clickthrough_url";
    private Activity activity;
    private MoPubInterstitial subject;
    private Map<String, String> paramsMap;
    private CustomEventInterstitialAdapter customEventInterstitialAdapter;
    private MoPubInterstitial.InterstitialAdListener interstitialAdListener;
    private MoPubInterstitial.MoPubInterstitialView interstitialView;

    @Before
    public void setUp() throws Exception {
        activity = new Activity();
        subject = new MoPubInterstitial(activity, AD_UNIT_ID_VALUE);
        interstitialAdListener = mock(MoPubInterstitial.InterstitialAdListener.class);
        subject.setInterstitialAdListener(interstitialAdListener);

        interstitialView = mock(MoPubInterstitial.MoPubInterstitialView.class);

        paramsMap = new HashMap<String, String>();
        paramsMap.put(CUSTOM_EVENT_NAME_HEADER, "class name");
        paramsMap.put(CUSTOM_EVENT_DATA_HEADER, "class data");

        customEventInterstitialAdapter = TestCustomEventInterstitialAdapterFactory.getSingletonMock();
        reset(customEventInterstitialAdapter);
    }

    @Test
    public void forceRefresh_shouldResetInterstitialViewAndMarkNotDestroyed() throws Exception {
        subject.setInterstitialView(interstitialView);
        subject.onCustomEventInterstitialLoaded();
        subject.forceRefresh();

        assertThat(subject.isReady()).isFalse();
        assertThat(subject.isDestroyed()).isFalse();
        verify(interstitialView).forceRefresh();
    }

    @Test
    public void setKeywordsTest() throws Exception {
        subject.setInterstitialView(interstitialView);
        String keywords = "these_are_keywords";

        subject.setKeywords(keywords);
        verify(interstitialView).setKeywords(eq(keywords));
    }
    @Test
    public void getKeywordsTest() throws Exception {
        subject.setInterstitialView(interstitialView);

        subject.getKeywords();
        verify(interstitialView).getKeywords();
    }

    @Test
    public void getInterstitialAdListenerTest() throws Exception {
        interstitialAdListener = mock(MoPubInterstitial.InterstitialAdListener.class);
        subject.setInterstitialAdListener(interstitialAdListener);
        assertThat(subject.getInterstitialAdListener()).isSameAs(interstitialAdListener);
    }

    @Test
    public void setLocationAwarenessTest() throws Exception {
        subject.setInterstitialView(interstitialView);
        subject.setLocationAwareness(LOCATION_AWARENESS_NORMAL);
        verify(interstitialView).setLocationAwareness(eq(LOCATION_AWARENESS_NORMAL));
    }

    @Test
    public void getLocationAwarenessTest() throws Exception {
        subject.setInterstitialView(interstitialView);
        subject.getLocationAwareness();
        verify(interstitialView).getLocationAwareness();
    }

    @Test
    public void setLocationPrecisionTest() throws Exception {
        subject.setInterstitialView(interstitialView);
        subject.setLocationPrecision(10);
        verify(interstitialView).setLocationPrecision(eq(10));
    }

    @Test
    public void getLocationPrecisionTest() throws Exception {
        subject.setInterstitialView(interstitialView);
        subject.getLocationPrecision();
        verify(interstitialView).getLocationPrecision();
    }


    @Test
    public void setTestingTest() throws Exception {
        subject.setInterstitialView(interstitialView);
        subject.setTesting(true);
        verify(interstitialView).setTesting(eq(true));
    }

    @Test
    public void getTestingTest() throws Exception {
        subject.setInterstitialView(interstitialView);
        subject.getTesting();
        verify(interstitialView).getTesting();
    }

    @Test
    public void setLocalExtrasTest() throws Exception {
        subject.setInterstitialView(interstitialView);

        Map<String,Object> localExtras = new HashMap<String, Object>();
        localExtras.put("guy", new Activity());
        localExtras.put("other guy", new BigDecimal(27f));

        subject.setLocalExtras(localExtras);
        verify(interstitialView).setLocalExtras(eq(localExtras));
    }

    @Test
    public void loadCustomEvent_shouldCreateAndLoadCustomEventInterstitialAdapter() throws Exception {
        MoPubInterstitial.MoPubInterstitialView moPubInterstitialView = subject.new MoPubInterstitialView(activity);
        moPubInterstitialView.loadCustomEvent(paramsMap);

        assertThat(TestCustomEventInterstitialAdapterFactory.getLatestMoPubInterstitial()).isSameAs(subject);
        assertThat(TestCustomEventInterstitialAdapterFactory.getLatestClassName()).isEqualTo("class name");
        assertThat(TestCustomEventInterstitialAdapterFactory.getLatestClassData()).isEqualTo("class data");
    }

    @Test
    public void onCustomEventInterstitialLoaded_shouldTrackImpressionAndNotifyListener() throws Exception {
        subject.setInterstitialView(interstitialView);

        subject.onCustomEventInterstitialLoaded();
        verify(interstitialView).trackImpression();
        verify(interstitialAdListener).onInterstitialLoaded(eq(subject));
    }

    @Test
    public void onCustomEventInterstitialLoaded_whenInterstitialAdListenerIsNull_shouldTrackImpressionAndNotNotifyListener() throws Exception {
        subject.setInterstitialView(interstitialView);
        subject.setInterstitialAdListener(null);

        subject.onCustomEventInterstitialLoaded();

        verify(interstitialView).trackImpression();
        verify(interstitialAdListener, never()).onInterstitialLoaded(eq(subject));
    }

    @Test
    public void onCustomEventInterstitialFailed_shouldLoadFailUrl() throws Exception {
        subject.setInterstitialView(interstitialView);

        subject.onCustomEventInterstitialFailed(INTERNAL_ERROR);

        verify(interstitialView).loadFailUrl(INTERNAL_ERROR);
    }

    @Test
    public void onCustomEventInterstitialShown_shouldNotifyListener() throws Exception {
        subject.onCustomEventInterstitialShown();

        verify(interstitialAdListener).onInterstitialShown(eq(subject));
    }

    @Test
    public void onCustomEventInterstitialShown_whenInterstitialAdListenerIsNull_shouldNotNotifyListener() throws Exception {
        subject.setInterstitialAdListener(null);
        subject.onCustomEventInterstitialShown();
        verify(interstitialAdListener, never()).onInterstitialShown(eq(subject));
    }

    @Test
    public void onCustomEventInterstitialClicked_shouldRegisterClick() throws Exception {
        subject.setInterstitialView(interstitialView);

        subject.onCustomEventInterstitialClicked();

        verify(interstitialView).registerClick();
    }

    @Test
    public void onCustomEventInterstitialDismissed_shouldNotifyListener() throws Exception {
        subject.onCustomEventInterstitialDismissed();

        verify(interstitialAdListener).onInterstitialDismissed(eq(subject));
    }

    @Test
    public void onCustomEventInterstitialDismissed_whenInterstitialAdListenerIsNull_shouldNotNotifyListener() throws Exception {
        subject.setInterstitialAdListener(null);
        subject.onCustomEventInterstitialDismissed();
        verify(interstitialAdListener, never()).onInterstitialDismissed(eq(subject));
    }

    @Test
    public void destroy_shouldPreventOnCustomEventInterstitialLoadedNotification() throws Exception {
        subject.destroy();

        subject.onCustomEventInterstitialLoaded();

        verify(interstitialAdListener, never()).onInterstitialLoaded(eq(subject));
    }

    @Test
    public void destroy_shouldPreventOnCustomEventInterstitialFailedNotification() throws Exception {
        subject.setInterstitialView(interstitialView);
        subject.destroy();

        subject.onCustomEventInterstitialFailed(UNSPECIFIED);

        verify(interstitialView, never()).loadFailUrl(UNSPECIFIED);
    }

    @Test
    public void destroy_shouldPreventOnCustomEventInterstitialClickedFromRegisteringClick() throws Exception {
        subject.setInterstitialView(interstitialView);
        subject.destroy();

        subject.onCustomEventInterstitialClicked();

        verify(interstitialView, never()).registerClick();
    }

    @Test
    public void destroy_shouldPreventOnCustomEventShownNotification() throws Exception {
        subject.destroy();

        subject.onCustomEventInterstitialShown();

        verify(interstitialAdListener, never()).onInterstitialShown(eq(subject));
    }

    @Test
    public void destroy_shouldPreventOnCustomEventInterstitialDismissedNotification() throws Exception {
        subject.destroy();

        subject.onCustomEventInterstitialDismissed();

        verify(interstitialAdListener, never()).onInterstitialDismissed(eq(subject));
    }

    @Test
    public void newlyCreated_shouldNotBeReadyAndNotShow() throws Exception {
        assertShowsHtmlInterstitial(false, false);
        assertShowsCustomEventInterstitial(false, false);
    }

    @Test
    public void loadingHtmlBanner_shouldBecomeReadyToShowHtmlAd() throws Exception {
        MoPubInterstitial.MoPubInterstitialBannerListener bannerListener = subject.new MoPubInterstitialBannerListener();

        bannerListener.onBannerLoaded(null);

        assertShowsHtmlInterstitial(true, true);
        assertShowsCustomEventInterstitial(true, false);
    }

    @Test
    public void loadingHtmlBannerThenFailing_shouldNotBecomeReadyToShowHtmlAd() throws Exception {
        MoPubInterstitial.MoPubInterstitialBannerListener bannerListener = subject.new MoPubInterstitialBannerListener();

        bannerListener.onBannerLoaded(null);
        bannerListener.onBannerFailed(null, null);

        assertShowsHtmlInterstitial(false, false);
        assertShowsCustomEventInterstitial(false, false);
    }

    @Test
    public void dismissingHtmlInterstitial_shouldNotBecomeReadyToShowHtmlAd() throws Exception {
        MoPubInterstitial.MoPubInterstitialBannerListener bannerListener = subject.new MoPubInterstitialBannerListener();
        BaseActivityBroadcastReceiver broadcastReceiver = subject.new MoPubInterstitialBroadcastReceiver();

        bannerListener.onBannerLoaded(null);
        broadcastReceiver.onHtmlInterstitialDismissed();

        assertShowsHtmlInterstitial(false, false);
        assertShowsCustomEventInterstitial(false, false);
    }

    @Test
    public void loadingCustomEventInterstitial_shouldBecomeReadyToShowCustomEventAd() throws Exception {
        subject.onCustomEventInterstitialLoaded();

        assertShowsHtmlInterstitial(true, false);
        assertShowsCustomEventInterstitial(true, true);
    }

    @Test
    public void failingCustomEventInterstitial_shouldNotBecomeReadyToShowCustomEventAd() throws Exception {
        subject.onCustomEventInterstitialLoaded();
        subject.onCustomEventInterstitialFailed(MoPubErrorCode.CANCELLED);

        assertShowsHtmlInterstitial(false, false);
        assertShowsCustomEventInterstitial(false, false);
    }

    @Test
    public void dismissingCustomEventInterstitial_shouldNotBecomeReadyToShowCustomEventAd() throws Exception {
        subject.onCustomEventInterstitialLoaded();
        subject.onCustomEventInterstitialDismissed();

        assertShowsHtmlInterstitial(false, false);
        assertShowsCustomEventInterstitial(false, false);
    }

    private void assertShowsHtmlInterstitial(boolean shouldBeReady, boolean shouldShowCustomEventInterstitial) {
        stub(interstitialView.getKeywords()).toReturn(KEYWORDS_VALUE);
        stub(interstitialView.getResponseString()).toReturn(SOURCE_VALUE);
        stub(interstitialView.getClickthroughUrl()).toReturn(CLICKTHROUGH_URL_VALUE);
        subject.setInterstitialView(interstitialView);

        assertThat(subject.isReady()).isEqualTo(shouldBeReady);
        assertThat(subject.show()).isEqualTo(shouldBeReady);

        Intent intent = shadowOf(activity).peekNextStartedActivity();
        if (shouldBeReady && shouldShowCustomEventInterstitial) {
            assertThat(intent.getComponent()).isEqualTo(new ComponentName(activity, MoPubActivity.class));
            assertThat(intent.getStringExtra(AD_UNIT_ID_KEY)).isEqualTo(AD_UNIT_ID_VALUE);
            assertThat(intent.getStringExtra(KEYWORDS_KEY)).isEqualTo(KEYWORDS_VALUE);
            assertThat(intent.getStringExtra(SOURCE_KEY)).isEqualTo(SOURCE_VALUE);
            assertThat(intent.getStringExtra(CLICKTHROUGH_URL_KEY)).isEqualTo(CLICKTHROUGH_URL_VALUE);
        } else {
            assertThat(intent).isNull();
        }
    }

    private void assertShowsCustomEventInterstitial(boolean shouldBeReady, boolean shouldShowHtmlInterstitial) {
        MoPubInterstitial.MoPubInterstitialView moPubInterstitialView = subject.new MoPubInterstitialView(activity);
        moPubInterstitialView.loadCustomEvent(paramsMap);

        assertThat(subject.isReady()).isEqualTo(shouldBeReady);
        assertThat(subject.show()).isEqualTo(shouldBeReady);

        if (shouldBeReady && shouldShowHtmlInterstitial) {
            verify(customEventInterstitialAdapter).showInterstitial();
        } else {
            verify(customEventInterstitialAdapter, never()).showInterstitial();
        }
    }
}
