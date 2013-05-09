package com.mopub.mobileads;

import android.content.Context;
import android.location.Location;
import com.mopub.mobileads.factories.CustomEventInterstitialFactory;
import com.mopub.mobileads.test.support.SdkTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;

import java.util.HashMap;
import java.util.Map;

import static com.mopub.mobileads.BaseInterstitialAdapter.BaseInterstitialAdapterListener;
import static com.mopub.mobileads.CustomEventInterstitial.CustomEventInterstitialListener;
import static com.mopub.mobileads.MoPubErrorCode.ADAPTER_CONFIGURATION_ERROR;
import static com.mopub.mobileads.MoPubErrorCode.NETWORK_TIMEOUT;
import static com.mopub.mobileads.MoPubErrorCode.UNSPECIFIED;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.stub;
import static org.mockito.Mockito.verify;

// todo how do we test class_name given that we are using TestCustomEventInterstitialFactory

@RunWith(SdkTestRunner.class)
public class CustomEventInterstitialAdapterTest {
    private CustomEventInterstitialAdapter subject;
    private MoPubInterstitial moPubInterstitial;
    private CustomEventInterstitial interstitial;
    private Map<String,Object> expectedLocalExtras;
    private HashMap<String,String> expectedServerExtras;
    private static final String CLASS_NAME = "arbitrary_interstitial_adapter_class_name";
    private static final String JSON_PARAMS = "{\"key\":\"value\",\"a different key\":\"a different value\"}";
    private BaseInterstitialAdapterListener interstitialAdapterListener;

    @Before
    public void setUp() throws Exception {
        subject = new CustomEventInterstitialAdapter();
        moPubInterstitial = mock(MoPubInterstitial.class);

        expectedLocalExtras = new HashMap<String, Object>();
        expectedServerExtras = new HashMap<String, String>();

        interstitial = CustomEventInterstitialFactory.create(CLASS_NAME);

        interstitialAdapterListener = mock(BaseInterstitialAdapterListener.class);
        subject.setAdapterListener(interstitialAdapterListener);
    }

    @Test
    public void timeout_shouldSignalFailureAndInvalidate() throws Exception {
        subject.init(moPubInterstitial, CLASS_NAME, JSON_PARAMS);
        subject.loadInterstitial();

        Robolectric.idleMainLooper(BaseInterstitialAdapter.TIMEOUT_DELAY - 1);
        verify(interstitialAdapterListener, never()).onNativeInterstitialFailed(eq(subject), eq(NETWORK_TIMEOUT));
        assertThat(subject.isInvalidated()).isFalse();

        Robolectric.idleMainLooper(1);
        verify(interstitialAdapterListener).onNativeInterstitialFailed(eq(subject), eq(NETWORK_TIMEOUT));
        assertThat(subject.isInvalidated()).isTrue();
    }

    @Test
    public void loadInterstitial_shouldHaveEmptyServerExtrasOnInvalidJsonParams() throws Exception {
        subject.init(moPubInterstitial, CLASS_NAME, "{this is terrible JSON");
        subject.loadInterstitial();

        verify(interstitial).loadInterstitial(
                any(Context.class),
                eq(subject),
                eq(expectedLocalExtras),
                eq(expectedServerExtras)
        );
    }

    @Test
    public void loadInterstitial_shouldPropagateLocationInLocalExtras() throws Exception {
        Location expectedLocation = new Location("");
        expectedLocation.setLongitude(10.0);
        expectedLocation.setLongitude(20.1);
        stub(moPubInterstitial.getLocation()).toReturn(expectedLocation);
        subject.init(moPubInterstitial, CLASS_NAME, null);
        subject.loadInterstitial();

        expectedLocalExtras.put("location", moPubInterstitial.getLocation());

        verify(interstitial).loadInterstitial(
                any(Context.class),
                eq(subject),
                eq(expectedLocalExtras),
                eq(expectedServerExtras)
        );
    }

    @Test
    public void loadInterstitial_shouldPropagateJsonParamsInServerExtras() throws Exception {
        subject.init(moPubInterstitial, CLASS_NAME, JSON_PARAMS);
        subject.loadInterstitial();

        expectedServerExtras.put("key", "value");
        expectedServerExtras.put("a different key", "a different value");

        verify(interstitial).loadInterstitial(
                any(Context.class),
                eq(subject),
                eq(expectedLocalExtras),
                eq(expectedServerExtras)
        );
    }

    @Test
    public void loadInterstitial_shouldScheduleTimeout_interstitialLoadedAndFailed_shouldCancelTimeout() throws Exception {
        Robolectric.pauseMainLooper();

        subject.init(moPubInterstitial, CLASS_NAME, JSON_PARAMS);
        assertThat(Robolectric.getUiThreadScheduler().enqueuedTaskCount()).isEqualTo(0);

        subject.loadInterstitial();
        assertThat(Robolectric.getUiThreadScheduler().enqueuedTaskCount()).isEqualTo(1);

        subject.onInterstitialLoaded();
        assertThat(Robolectric.getUiThreadScheduler().enqueuedTaskCount()).isEqualTo(0);

        subject.loadInterstitial();
        assertThat(Robolectric.getUiThreadScheduler().enqueuedTaskCount()).isEqualTo(1);

        subject.onInterstitialFailed(null);
        assertThat(Robolectric.getUiThreadScheduler().enqueuedTaskCount()).isEqualTo(0);
    }

    @Test
    public void showInterstitial_shouldCallCustomEventInterstitialShowInterstitial() throws Exception {
        subject.init(moPubInterstitial, CLASS_NAME, JSON_PARAMS);
        subject.showInterstitial();

        verify(interstitial).showInterstitial();
    }

    @Test
    public void onInterstitialLoaded_shouldSignalAdapterListener() throws Exception {
        subject.init(moPubInterstitial, CLASS_NAME, JSON_PARAMS);
        subject.onInterstitialLoaded();

        verify(interstitialAdapterListener).onNativeInterstitialLoaded(eq(subject));
    }

    @Test
    public void onInterstitialFailed_shouldLoadFailUrl() throws Exception {
        subject.init(moPubInterstitial, CLASS_NAME, JSON_PARAMS);
        subject.onInterstitialFailed(ADAPTER_CONFIGURATION_ERROR);

        verify(interstitialAdapterListener).onNativeInterstitialFailed(eq(subject), eq(ADAPTER_CONFIGURATION_ERROR));
    }

    @Test
    public void onInterstitialFailed_whenErrorCodeIsNull_shouldPassUnspecifiedError() throws Exception {
        subject.init(moPubInterstitial, CLASS_NAME, JSON_PARAMS);
        subject.onInterstitialFailed(null);

        verify(interstitialAdapterListener).onNativeInterstitialFailed(eq(subject), eq(UNSPECIFIED));
    }

    @Test
    public void onInterstitialShown_shouldSignalAdapterListener() throws Exception {
        subject.init(moPubInterstitial, CLASS_NAME, JSON_PARAMS);
        subject.onInterstitialShown();

        verify(interstitialAdapterListener).onNativeInterstitialShown(eq(subject));
    }

    @Test
    public void onInterstitialClicked_shouldSignalAdapterListener() throws Exception {
        subject.init(moPubInterstitial, CLASS_NAME, JSON_PARAMS);
        subject.onInterstitialClicked();

        verify(interstitialAdapterListener).onNativeInterstitialClicked(eq(subject));
    }

    @Test
    public void onLeaveApplication_shouldSignalAdapterListener() throws Exception {
        subject.init(moPubInterstitial, CLASS_NAME, JSON_PARAMS);
        subject.onLeaveApplication();

        verify(interstitialAdapterListener).onNativeInterstitialClicked(eq(subject));
    }

    @Test
    public void onInterstitialDismissed_shouldSignalAdapterListener() throws Exception {
        subject.init(moPubInterstitial, CLASS_NAME, JSON_PARAMS);
        subject.onInterstitialDismissed();

        verify(interstitialAdapterListener).onNativeInterstitialDismissed(eq(subject));
    }

    @Test
    public void invalidate_shouldCauseLoadInterstitialToDoNothing() throws Exception {
        subject.init(moPubInterstitial, CLASS_NAME, JSON_PARAMS);
        subject.invalidate();

        subject.loadInterstitial();

        verify(interstitial, never()).loadInterstitial(
                any(Context.class),
                any(CustomEventInterstitialListener.class),
                any(Map.class),
                any(Map.class)
        );
    }

    @Test
    public void invalidate_shouldCauseShowInterstitialToDoNothing() throws Exception {
        subject.init(moPubInterstitial, CLASS_NAME, JSON_PARAMS);
        subject.invalidate();

        subject.showInterstitial();

        verify(interstitial, never()).showInterstitial();
    }

    @Test
    public void invalidate_shouldCauseInterstitialListenerMethodsToDoNothing() throws Exception {
        subject.init(moPubInterstitial, CLASS_NAME, JSON_PARAMS);
        subject.invalidate();

        subject.onInterstitialLoaded();
        subject.onInterstitialFailed(null);
        subject.onInterstitialShown();
        subject.onInterstitialClicked();
        subject.onLeaveApplication();
        subject.onInterstitialDismissed();

        verify(interstitialAdapterListener, never()).onNativeInterstitialLoaded(any(CustomEventInterstitialAdapter.class));
        verify(interstitialAdapterListener, never()).onNativeInterstitialFailed(any(CustomEventInterstitialAdapter.class), any(MoPubErrorCode.class));
        verify(interstitialAdapterListener, never()).onNativeInterstitialShown(any(CustomEventInterstitialAdapter.class));
        verify(interstitialAdapterListener, never()).onNativeInterstitialClicked(any(CustomEventInterstitialAdapter.class));
        verify(interstitialAdapterListener, never()).onNativeInterstitialDismissed(any(CustomEventInterstitialAdapter.class));
    }
}
