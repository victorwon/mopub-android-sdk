package com.mopub.mobileads;

import android.app.Activity;
import com.mopub.mobileads.test.support.TestAdViewControllerFactory;
import com.mopub.mobileads.test.support.TestCustomEventInterstitialAdapterFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static com.mopub.mobileads.AdFetcher.CUSTOM_EVENT_DATA_HEADER;
import static com.mopub.mobileads.AdFetcher.CUSTOM_EVENT_NAME_HEADER;
import static com.mopub.mobileads.MoPubErrorCode.ADAPTER_NOT_FOUND;
import static com.mopub.mobileads.MoPubErrorCode.CANCELLED;
import static com.mopub.mobileads.MoPubErrorCode.INTERNAL_ERROR;
import static com.mopub.mobileads.MoPubErrorCode.UNSPECIFIED;
import static com.mopub.mobileads.MoPubView.LocationAwareness.LOCATION_AWARENESS_NORMAL;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;

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
    private AdViewController adViewController;

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
        adViewController = TestAdViewControllerFactory.getSingletonMock();
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
    public void onCustomEventInterstitialLoaded_shouldNotifyListener() throws Exception {
        subject.setInterstitialView(interstitialView);

        subject.onCustomEventInterstitialLoaded();
        verify(interstitialAdListener).onInterstitialLoaded(eq(subject));

        verify(interstitialView, never()).trackImpression();
    }

    @Test
    public void onCustomEventInterstitialLoaded_whenInterstitialAdListenerIsNull_shouldNotNotifyListenerOrTrackImpression() throws Exception {
        subject.setInterstitialView(interstitialView);
        subject.setInterstitialAdListener(null);

        subject.onCustomEventInterstitialLoaded();

        verify(interstitialView, never()).trackImpression();
        verify(interstitialAdListener, never()).onInterstitialLoaded(eq(subject));
    }

    @Test
    public void onCustomEventInterstitialFailed_shouldLoadFailUrl() throws Exception {
        subject.setInterstitialView(interstitialView);

        subject.onCustomEventInterstitialFailed(INTERNAL_ERROR);

        verify(interstitialView).loadFailUrl(INTERNAL_ERROR);
    }

    @Test
    public void onCustomEventInterstitialShown_shouldTrackImpressionAndNotifyListener() throws Exception {
        subject.setInterstitialView(interstitialView);
        subject.onCustomEventInterstitialShown(true);

        verify(interstitialView).trackImpression();
        verify(interstitialAdListener).onInterstitialShown(eq(subject));
    }

    @Test
    public void onCustomEventInterstitialShown_whenShouldntTrackImpression_shouldNotTrackImpressionButStillNotifyListener() throws Exception {
        loadCustomEvent();

        subject.setInterstitialView(interstitialView);
        subject.onCustomEventInterstitialShown(false);

        verify(interstitialView, never()).trackImpression();
        verify(interstitialAdListener).onInterstitialShown(eq(subject));
    }

    @Test
    public void onCustomEventInterstitialShown_whenInterstitialAdListenerIsNull_shouldNotNotifyListener() throws Exception {
        subject.setInterstitialAdListener(null);
        subject.onCustomEventInterstitialShown(true);
        verify(interstitialAdListener, never()).onInterstitialShown(eq(subject));
    }

    @Test
    public void onCustomEventInterstitialClicked_shouldRegisterClickAndNotifyListener() throws Exception {
        subject.setInterstitialView(interstitialView);

        subject.onCustomEventInterstitialClicked();

        verify(interstitialView).registerClick();
        verify(interstitialAdListener).onInterstitialClicked(eq(subject));
    }

    @Test
    public void onCustomEventInterstitialClicked_whenInterstitialAdListenerIsNull_shouldNotNotifyListener() throws Exception {
        subject.setInterstitialAdListener(null);

        subject.onCustomEventInterstitialClicked();

        verify(interstitialAdListener, never()).onInterstitialClicked(eq(subject));
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

        subject.onCustomEventInterstitialShown(true);

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
        assertShowsCustomEventInterstitial(false);
    }

    @Test
    public void loadingCustomEventInterstitial_shouldBecomeReadyToShowCustomEventAd() throws Exception {
        subject.onCustomEventInterstitialLoaded();

        assertShowsCustomEventInterstitial(true);
    }

    @Test
    public void dismissingHtmlInterstitial_shouldNotBecomeReadyToShowHtmlAd() throws Exception {
//        EventForwardingBroadcastReceiver broadcastReceiver = new EventForwardingBroadcastReceiver(subject.mInterstitialAdListener);
//
//        subject.onCustomEventInterstitialLoaded();
//        broadcastReceiver.onHtmlInterstitialDismissed();
//
//        assertShowsCustomEventInterstitial(false);
    }

    @Test
    public void failingCustomEventInterstitial_shouldNotBecomeReadyToShowCustomEventAd() throws Exception {
        subject.onCustomEventInterstitialLoaded();
        subject.onCustomEventInterstitialFailed(CANCELLED);

        assertShowsCustomEventInterstitial(false);
    }

    @Test
    public void dismissingCustomEventInterstitial_shouldNotBecomeReadyToShowCustomEventAd() throws Exception {
        subject.onCustomEventInterstitialLoaded();
        subject.onCustomEventInterstitialDismissed();

        assertShowsCustomEventInterstitial(false);
    }

    @Test
    public void loadCustomEvent_shouldInitializeCustomEventBannerAdapter() throws Exception {
        MoPubInterstitial.MoPubInterstitialView moPubInterstitialView = subject.new MoPubInterstitialView(activity);

        paramsMap.put(AdFetcher.CUSTOM_EVENT_NAME_HEADER, "name");
        paramsMap.put(AdFetcher.CUSTOM_EVENT_DATA_HEADER, "data");
        paramsMap.put(AdFetcher.CUSTOM_EVENT_HTML_DATA, "html");
        moPubInterstitialView.loadCustomEvent(paramsMap);

        assertThat(TestCustomEventInterstitialAdapterFactory.getLatestMoPubInterstitial()).isEqualTo(subject);
        assertThat(TestCustomEventInterstitialAdapterFactory.getLatestClassName()).isEqualTo("name");
        assertThat(TestCustomEventInterstitialAdapterFactory.getLatestClassData()).isEqualTo("data");

        verify(customEventInterstitialAdapter).setAdapterListener(eq(subject));
        verify(customEventInterstitialAdapter).loadInterstitial();
    }

    @Test
    public void loadCustomEvent_whenParamsMapIsNull_shouldCallLoadFailUrl() throws Exception {
        MoPubInterstitial.MoPubInterstitialView moPubInterstitialView = subject.new MoPubInterstitialView(activity);

        moPubInterstitialView.loadCustomEvent(null);

        verify(adViewController).loadFailUrl(eq(ADAPTER_NOT_FOUND));
        verify(customEventInterstitialAdapter, never()).invalidate();
        verify(customEventInterstitialAdapter, never()).loadInterstitial();
    }

    @Test
    public void adFailed_shouldNotifyInterstitialAdListener() throws Exception {
        MoPubInterstitial.MoPubInterstitialView moPubInterstitialView = subject.new MoPubInterstitialView(activity);
        moPubInterstitialView.adFailed(CANCELLED);

        verify(interstitialAdListener).onInterstitialFailed(eq(subject), eq(CANCELLED));
    }

    private void loadCustomEvent() {
        MoPubInterstitial.MoPubInterstitialView moPubInterstitialView = subject.new MoPubInterstitialView(activity);

        paramsMap.put(AdFetcher.CUSTOM_EVENT_NAME_HEADER, "name");
        paramsMap.put(AdFetcher.CUSTOM_EVENT_DATA_HEADER, "data");
        paramsMap.put(AdFetcher.CUSTOM_EVENT_HTML_DATA, "html");
        moPubInterstitialView.loadCustomEvent(paramsMap);
    }

    private void assertShowsCustomEventInterstitial(boolean shouldBeReady) {
        MoPubInterstitial.MoPubInterstitialView moPubInterstitialView = subject.new MoPubInterstitialView(activity);
        moPubInterstitialView.loadCustomEvent(paramsMap);

        assertThat(subject.isReady()).isEqualTo(shouldBeReady);
        assertThat(subject.show()).isEqualTo(shouldBeReady);

        if (shouldBeReady) {
            verify(customEventInterstitialAdapter).showInterstitial();
        } else {
            verify(customEventInterstitialAdapter, never()).showInterstitial();
        }
    }
}
