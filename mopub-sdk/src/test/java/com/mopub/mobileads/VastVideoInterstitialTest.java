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
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.mopub.mobileads.test.support.SdkTestRunner;
import com.mopub.mobileads.test.support.TestHttpResponseWithHeaders;
import com.mopub.mobileads.test.support.TestVastManagerFactory;
import com.mopub.mobileads.test.support.TestVastVideoDownloadTaskFactory;
import com.mopub.mobileads.util.vast.VastManager;
import org.junit.After;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.shadows.ShadowLocalBroadcastManager;

import java.io.*;
import java.util.*;

import static com.mopub.mobileads.AdFetcher.HTML_RESPONSE_BODY_KEY;
import static com.mopub.mobileads.BaseInterstitialActivity.ACTION_INTERSTITIAL_DISMISS;
import static com.mopub.mobileads.BaseInterstitialActivity.ACTION_INTERSTITIAL_SHOW;
import static com.mopub.mobileads.CustomEventInterstitial.CustomEventInterstitialListener;
import static com.mopub.mobileads.MoPubErrorCode.NETWORK_INVALID_STATE;
import static com.mopub.mobileads.MoPubErrorCode.VIDEO_DOWNLOAD_ERROR;
import static com.mopub.mobileads.VastVideoView.VIDEO_CLICK_THROUGH_TRACKERS;
import static com.mopub.mobileads.VastVideoView.VIDEO_CLICK_THROUGH_URL;
import static com.mopub.mobileads.VastVideoView.VIDEO_COMPLETE_TRACKERS;
import static com.mopub.mobileads.VastVideoView.VIDEO_FIRST_QUARTER_TRACKERS;
import static com.mopub.mobileads.VastVideoView.VIDEO_IMPRESSION_TRACKERS;
import static com.mopub.mobileads.VastVideoView.VIDEO_MID_POINT_TRACKERS;
import static com.mopub.mobileads.VastVideoView.VIDEO_START_TRACKERS;
import static com.mopub.mobileads.VastVideoView.VIDEO_THIRD_QUARTER_TRACKERS;
import static com.mopub.mobileads.util.vast.VastManager.VastManagerListener;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.stub;
import static org.mockito.Mockito.verify;

@RunWith(SdkTestRunner.class)
public class VastVideoInterstitialTest extends ResponseBodyInterstitialTest {
    private Context context;
    private CustomEventInterstitialListener customEventInterstitialListener;
    private Map<String, Object> localExtras;
    private Map<String, String> serverExtras;
    private TestHttpResponseWithHeaders response;
    private String expectedResponse;
    private VastManager vastManager;
    private String videoUrl;
    private VastVideoDownloadTask vastVideoDownloadTask;

    @Before
    public void setUp() throws Exception {
        subject = new VastVideoInterstitial();

        vastVideoDownloadTask = TestVastVideoDownloadTaskFactory.getSingletonMock();
        vastManager = TestVastManagerFactory.getSingletonMock();
        expectedResponse = "<VAST>hello</VAST>";
        videoUrl = "http://www.video.com";

        context = new Activity();
        customEventInterstitialListener = mock(CustomEventInterstitialListener.class);
        localExtras = new HashMap<String, Object>();
        serverExtras = new HashMap<String, String>();
        serverExtras.put(AdFetcher.HTML_RESPONSE_BODY_KEY, Uri.encode(expectedResponse));

        response = new TestHttpResponseWithHeaders(200, expectedResponse);
    }

    @After
    public void tearDown() throws Exception {
        reset(vastVideoDownloadTask);
    }

    @Test
    public void preRenderHtml_whenCreatingVideoCache_butItHasInitializationErrors_shouldSignalOnInterstitialFailedOnError() throws Exception {
        // context is null when loadInterstitial is not called, which causes DiskLruCache to not be created

        subject.preRenderHtml(customEventInterstitialListener);

        verify(customEventInterstitialListener).onInterstitialFailed(eq(MoPubErrorCode.VIDEO_CACHE_ERROR));
        verify(vastManager, never()).processVast(anyString(), any(VastManagerListener.class));
    }

    @Test
    public void loadInterstitial_shouldParseHtmlResponseBodyServerExtra() throws Exception {
        subject.loadInterstitial(context, customEventInterstitialListener, localExtras, serverExtras);

        assertThat(((VastVideoInterstitial) subject).getVastResponse()).isEqualTo(expectedResponse);
    }

    @Test
    public void loadInterstitial_shouldInitializeVideoCache() throws Exception {
        Robolectric.addPendingHttpResponse(response);

        subject.loadInterstitial(context, customEventInterstitialListener, localExtras, serverExtras);

        DiskLruCache videoCache = ((VastVideoInterstitial) subject).getVideoCache();
        assertThat(videoCache).isNotNull();
        assertThat(videoCache.getCacheDirectory().getName()).isEqualTo("mopub_vast_video_cache");
        assertThat(videoCache.maxSize()).isEqualTo(100 * 1000 * 1000);
    }

    @Test
    public void loadInterstitial_shouldCreateVastManagerAndProcessVast() throws Exception {
        subject.loadInterstitial(context, customEventInterstitialListener, localExtras, serverExtras);

        verify(vastManager).processVast(eq(expectedResponse), eq((VastVideoInterstitial) subject));
    }

    @Test
    public void loadInterstitial_whenServerExtrasDoesNotContainResponse_shouldSignalOnInterstitialFailed() throws Exception {
        serverExtras.remove(HTML_RESPONSE_BODY_KEY);

        subject.loadInterstitial(context, customEventInterstitialListener, localExtras, serverExtras);

        verify(customEventInterstitialListener).onInterstitialFailed(NETWORK_INVALID_STATE);
        verify(vastManager, never()).processVast(anyString(), any(VastManagerListener.class));
    }

    @Test
    public void loadInterstitial_shouldConnectListenerToBroadcastReceiver() throws Exception {
        subject.loadInterstitial(context, customEventInterstitialListener, localExtras, serverExtras);

        Intent intent;
        intent = new Intent(ACTION_INTERSTITIAL_SHOW);
        ShadowLocalBroadcastManager.getInstance(context).sendBroadcast(intent);

        verify(customEventInterstitialListener).onInterstitialShown();

        intent = new Intent(ACTION_INTERSTITIAL_DISMISS);
        ShadowLocalBroadcastManager.getInstance(context).sendBroadcast(intent);

        verify(customEventInterstitialListener).onInterstitialDismissed();
    }

    @Test
    public void showInterstitial_shouldStartVideoPlayerActivityWithAllValidTrackers() throws Exception {
        stub(vastManager.getMediaFileUrl()).toReturn(videoUrl);

        stub(vastManager.getVideoStartTrackers()).toReturn(wrapInList("start"));
        stub(vastManager.getVideoFirstQuartileTrackers()).toReturn(wrapInList("first"));
        stub(vastManager.getVideoMidpointTrackers()).toReturn(wrapInList("mid"));
        stub(vastManager.getVideoThirdQuartileTrackers()).toReturn(wrapInList("third"));
        stub(vastManager.getVideoCompleteTrackers()).toReturn(wrapInList("complete"));
        stub(vastManager.getImpressionTrackers()).toReturn(wrapInList("imp"));
        stub(vastManager.getClickThroughUrl()).toReturn("clickThrough");
        stub(vastManager.getClickTrackers()).toReturn(wrapInList("click"));

        subject.loadInterstitial(context, customEventInterstitialListener, localExtras, serverExtras);
        ((VastVideoInterstitial) subject).onComplete(vastManager);
        ((VastVideoInterstitial) subject).onDownloadSuccess();

        subject.showInterstitial();

        Intent nextActivity = Robolectric.getShadowApplication().getNextStartedActivity();
        assertThat(nextActivity.getComponent().getClassName()).isEqualTo("com.mopub.mobileads.MraidVideoPlayerActivity");
        assertThat(nextActivity.getFlags() & Intent.FLAG_ACTIVITY_NEW_TASK).isNotEqualTo(0);

        assertThat(nextActivity.getStringArrayListExtra(VIDEO_START_TRACKERS).get(0)).isEqualTo("start");
        assertThat(nextActivity.getStringArrayListExtra(VIDEO_FIRST_QUARTER_TRACKERS).get(0)).isEqualTo("first");
        assertThat(nextActivity.getStringArrayListExtra(VIDEO_MID_POINT_TRACKERS).get(0)).isEqualTo("mid");
        assertThat(nextActivity.getStringArrayListExtra(VIDEO_THIRD_QUARTER_TRACKERS).get(0)).isEqualTo("third");
        assertThat(nextActivity.getStringArrayListExtra(VIDEO_COMPLETE_TRACKERS).get(0)).isEqualTo("complete");
        assertThat(nextActivity.getStringArrayListExtra(VIDEO_IMPRESSION_TRACKERS).get(0)).isEqualTo("imp");
        assertThat(nextActivity.getStringExtra(VIDEO_CLICK_THROUGH_URL)).isEqualTo("clickThrough");
        assertThat(nextActivity.getStringArrayListExtra(VIDEO_CLICK_THROUGH_TRACKERS).get(0)).isEqualTo("click");
    }

    @Test
    public void onInvalidate_shouldCancelVastManager() throws Exception {
        subject.loadInterstitial(context, customEventInterstitialListener, localExtras, serverExtras);
        subject.onInvalidate();

        verify(vastManager).cancel();
    }

    @Test
    public void onInvalidate_whenVastManagerIsNull_shouldNotBlowUp() throws Exception {
        subject.loadInterstitial(context, customEventInterstitialListener, localExtras, serverExtras);

        ((VastVideoInterstitial) subject).setVastManager(null);

        subject.onInvalidate();

        // pass
    }

    @Test
    public void onInvalidate_shouldDisconnectListenerToBroadcastReceiver() throws Exception {
        subject.loadInterstitial(context, customEventInterstitialListener, localExtras, serverExtras);
        subject.onInvalidate();

        Intent intent;
        intent = new Intent(ACTION_INTERSTITIAL_SHOW);
        ShadowLocalBroadcastManager.getInstance(context).sendBroadcast(intent);

        verify(customEventInterstitialListener, never()).onInterstitialShown();

        intent = new Intent(ACTION_INTERSTITIAL_DISMISS);
        ShadowLocalBroadcastManager.getInstance(context).sendBroadcast(intent);

        verify(customEventInterstitialListener, never()).onInterstitialDismissed();
    }

    @Ignore("pending")
    @Test
    public void onComplete_whenVideoCacheHit_shouldCallOnDownloadSuccess() throws Exception {
        subject.loadInterstitial(context, customEventInterstitialListener, localExtras, serverExtras);

        stub(vastManager.getMediaFileUrl()).toReturn(videoUrl);
        DiskLruCache videoCache = ((VastVideoInterstitial) subject).getVideoCache();
        videoCache.putStream(videoUrl, new ByteArrayInputStream("some data".getBytes()));

        ((VastVideoInterstitial) subject).onComplete(vastManager);

        verify(customEventInterstitialListener).onInterstitialLoaded();
        verify(vastVideoDownloadTask, never()).execute((String[])anyVararg());
    }

    @Ignore("pending")
    @Test
    public void onComplete_whenVideoCacheMiss_shouldStartVastVideoDownloadTask() throws Exception {
        subject.loadInterstitial(context, customEventInterstitialListener, localExtras, serverExtras);

        stub(vastManager.getMediaFileUrl()).toReturn(videoUrl);
        DiskLruCache videoCache = ((VastVideoInterstitial) subject).getVideoCache();
        videoCache.putStream("another_video_not_in_cache", new ByteArrayInputStream("some data".getBytes()));

        ((VastVideoInterstitial) subject).onComplete(vastManager);

        verify(vastVideoDownloadTask).execute(eq(videoUrl));
        verify(customEventInterstitialListener, never()).onInterstitialLoaded();
    }

    @Test
    public void onDownloadSuccess_shouldSignalOnInterstitialLoaded() throws Exception {
        subject.loadInterstitial(context, customEventInterstitialListener, localExtras, serverExtras);
        ((VastVideoInterstitial) subject).onDownloadSuccess();

        verify(customEventInterstitialListener).onInterstitialLoaded();
    }

    @Test
    public void onDownloadFailed_shouldSignalOnInterstitialFailed() throws Exception {
        subject.loadInterstitial(context, customEventInterstitialListener, localExtras, serverExtras);
        ((VastVideoInterstitial) subject).onDownloadFailed();

        verify(customEventInterstitialListener).onInterstitialFailed(eq(VIDEO_DOWNLOAD_ERROR));
    }

    private <T> List<T> wrapInList(T object) {
        List<T> result = new ArrayList<T>();
        result.add(object);
        return result;
    }
}
