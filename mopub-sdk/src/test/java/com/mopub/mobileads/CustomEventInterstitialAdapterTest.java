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

import android.content.Context;
import android.location.Location;
import com.mopub.mobileads.factories.CustomEventInterstitialFactory;
import com.mopub.mobileads.test.support.SdkTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;

import java.util.*;

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

@RunWith(SdkTestRunner.class)
public class CustomEventInterstitialAdapterTest {
    private CustomEventInterstitialAdapter subject;
    private MoPubInterstitial moPubInterstitial;
    private CustomEventInterstitial interstitial;
    private Map<String, Object> expectedLocalExtras;
    private HashMap<String, String> expectedServerExtras;
    private AdViewController adViewController;
    private MoPubInterstitial.MoPubInterstitialView moPubInterstitialView;
    private static final String CLASS_NAME = "arbitrary_interstitial_adapter_class_name";
    private static final String JSON_PARAMS = "{\"key\":\"value\",\"a different key\":\"a different value\"}";
    private CustomEventInterstitialAdapter.CustomEventInterstitialAdapterListener interstitialAdapterListener;

    @Before
    public void setUp() throws Exception {
        moPubInterstitial = mock(MoPubInterstitial.class);
        stub(moPubInterstitial.getAdTimeoutDelay()).toReturn(null);
        moPubInterstitialView = mock(MoPubInterstitial.MoPubInterstitialView.class);
        adViewController = mock(AdViewController.class);
        stub(moPubInterstitialView.getAdViewController()).toReturn(adViewController);
        stub(moPubInterstitial.getMoPubInterstitialView()).toReturn(moPubInterstitialView);

        subject = new CustomEventInterstitialAdapter(moPubInterstitial, CLASS_NAME, JSON_PARAMS);

        expectedLocalExtras = new HashMap<String, Object>();
        expectedServerExtras = new HashMap<String, String>();

        interstitial = CustomEventInterstitialFactory.create(CLASS_NAME);

        interstitialAdapterListener = mock(CustomEventInterstitialAdapter.CustomEventInterstitialAdapterListener.class);
        subject.setAdapterListener(interstitialAdapterListener);
    }

    @Test
    public void timeout_shouldSignalFailureAndInvalidateWithDefaultDelay() throws Exception {
        subject.loadInterstitial();
        Robolectric.idleMainLooper(CustomEventInterstitialAdapter.DEFAULT_INTERSTITIAL_TIMEOUT_DELAY - 1);
        verify(interstitialAdapterListener, never()).onCustomEventInterstitialFailed(eq(NETWORK_TIMEOUT));
        assertThat(subject.isInvalidated()).isFalse();

        Robolectric.idleMainLooper(1);
        verify(interstitialAdapterListener).onCustomEventInterstitialFailed(eq(NETWORK_TIMEOUT));
        assertThat(subject.isInvalidated()).isTrue();
    }

    @Test
    public void timeout_withNegativeAdTimeoutDelay_shouldSignalFailureAndInvalidateWithDefaultDelay() throws Exception {
        stub(moPubInterstitial.getAdTimeoutDelay()).toReturn(-1);

        subject.loadInterstitial();
        Robolectric.idleMainLooper(CustomEventInterstitialAdapter.DEFAULT_INTERSTITIAL_TIMEOUT_DELAY - 1);
        verify(interstitialAdapterListener, never()).onCustomEventInterstitialFailed(eq(NETWORK_TIMEOUT));
        assertThat(subject.isInvalidated()).isFalse();

        Robolectric.idleMainLooper(1);
        verify(interstitialAdapterListener).onCustomEventInterstitialFailed(eq(NETWORK_TIMEOUT));
        assertThat(subject.isInvalidated()).isTrue();
    }

    @Test
    public void timeout_withNonNullAdTimeoutDelay_shouldSignalFailureAndInvalidateWithCustomDelay() throws Exception {
        stub(moPubInterstitial.getAdTimeoutDelay()).toReturn(77);

        subject.loadInterstitial();
        Robolectric.idleMainLooper(77000 - 1);
        verify(interstitialAdapterListener, never()).onCustomEventInterstitialFailed(eq(NETWORK_TIMEOUT));
        assertThat(subject.isInvalidated()).isFalse();

        Robolectric.idleMainLooper(1);
        verify(interstitialAdapterListener).onCustomEventInterstitialFailed(eq(NETWORK_TIMEOUT));
        assertThat(subject.isInvalidated()).isTrue();
    }

    @Test
    public void loadInterstitial_shouldHaveEmptyServerExtrasOnInvalidJsonParams() throws Exception {
        subject = new CustomEventInterstitialAdapter(moPubInterstitial, CLASS_NAME, "{this is terrible JSON");
        subject.loadInterstitial();
        expectedLocalExtras.put("Ad-Configuration", null);

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
        subject = new CustomEventInterstitialAdapter(moPubInterstitial, CLASS_NAME, null);
        subject.loadInterstitial();

        expectedLocalExtras.put("Ad-Configuration", null);
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
        subject.loadInterstitial();
        expectedLocalExtras.put("Ad-Configuration", null);
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
        subject.showInterstitial();

        verify(interstitial).showInterstitial();
    }

    @Test
    public void onInterstitialLoaded_shouldSignalAdapterListener() throws Exception {
        subject.onInterstitialLoaded();

        verify(interstitialAdapterListener).onCustomEventInterstitialLoaded();
    }

    @Test
    public void onInterstitialFailed_shouldLoadFailUrl() throws Exception {
        subject.onInterstitialFailed(ADAPTER_CONFIGURATION_ERROR);

        verify(interstitialAdapterListener).onCustomEventInterstitialFailed(eq(ADAPTER_CONFIGURATION_ERROR));
    }

    @Test
    public void onInterstitialFailed_whenErrorCodeIsNull_shouldPassUnspecifiedError() throws Exception {
        subject.onInterstitialFailed(null);

        verify(interstitialAdapterListener).onCustomEventInterstitialFailed(eq(UNSPECIFIED));
    }

    @Test
    public void onInterstitialShown_shouldSignalAdapterListener() throws Exception {
        subject.onInterstitialShown();

        verify(interstitialAdapterListener).onCustomEventInterstitialShown();
    }

    @Test
    public void onInterstitialClicked_shouldSignalAdapterListener() throws Exception {
        subject.onInterstitialClicked();

        verify(interstitialAdapterListener).onCustomEventInterstitialClicked();
    }

    @Test
    public void onLeaveApplication_shouldSignalAdapterListener() throws Exception {
        subject.onLeaveApplication();

        verify(interstitialAdapterListener).onCustomEventInterstitialClicked();
    }

    @Test
    public void onInterstitialDismissed_shouldSignalAdapterListener() throws Exception {
        subject.onInterstitialDismissed();

        verify(interstitialAdapterListener).onCustomEventInterstitialDismissed();
    }

    @Test
    public void invalidate_shouldCauseLoadInterstitialToDoNothing() throws Exception {
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
        subject.invalidate();

        subject.showInterstitial();

        verify(interstitial, never()).showInterstitial();
    }

    @Test
    public void invalidate_shouldCauseInterstitialListenerMethodsToDoNothing() throws Exception {
        subject.invalidate();

        subject.onInterstitialLoaded();
        subject.onInterstitialFailed(null);
        subject.onInterstitialShown();
        subject.onInterstitialClicked();
        subject.onLeaveApplication();
        subject.onInterstitialDismissed();

        verify(interstitialAdapterListener, never()).onCustomEventInterstitialLoaded();
        verify(interstitialAdapterListener, never()).onCustomEventInterstitialFailed(any(MoPubErrorCode.class));
        verify(interstitialAdapterListener, never()).onCustomEventInterstitialShown();
        verify(interstitialAdapterListener, never()).onCustomEventInterstitialClicked();
        verify(interstitialAdapterListener, never()).onCustomEventInterstitialDismissed();
    }
}
