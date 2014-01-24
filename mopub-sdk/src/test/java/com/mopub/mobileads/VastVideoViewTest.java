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
import android.media.MediaPlayer;
import android.view.View;
import com.mopub.mobileads.test.support.GestureUtils;
import com.mopub.mobileads.test.support.SdkTestRunner;
import com.mopub.mobileads.test.support.TestHttpResponseWithHeaders;
import com.mopub.mobileads.test.support.ThreadUtils;
import org.apache.http.HttpRequest;
import org.apache.http.client.methods.HttpGet;
import org.apache.maven.artifact.ant.shaded.ReflectionUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.shadows.ShadowVideoView;

import java.util.*;

import static com.mopub.mobileads.BaseVideoView.BaseVideoViewListener;
import static com.mopub.mobileads.MraidVideoPlayerActivity.VIDEO_URL;
import static com.mopub.mobileads.VastVideoView.VIDEO_CLICK_THROUGH_TRACKERS;
import static com.mopub.mobileads.VastVideoView.VIDEO_CLICK_THROUGH_URL;
import static com.mopub.mobileads.VastVideoView.VIDEO_COMPLETE_TRACKERS;
import static com.mopub.mobileads.VastVideoView.VIDEO_FIRST_QUARTER_TRACKERS;
import static com.mopub.mobileads.VastVideoView.VIDEO_IMPRESSION_TRACKERS;
import static com.mopub.mobileads.VastVideoView.VIDEO_MID_POINT_TRACKERS;
import static com.mopub.mobileads.VastVideoView.VIDEO_START_TRACKERS;
import static com.mopub.mobileads.VastVideoView.VIDEO_THIRD_QUARTER_TRACKERS;
import static com.mopub.mobileads.test.support.ThreadUtils.NETWORK_DELAY;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Fail.fail;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.stub;
import static org.mockito.Mockito.verify;
import static org.robolectric.Robolectric.shadowOf;

@RunWith(SdkTestRunner.class)
public class VastVideoViewTest {
    private VastVideoView subject;
    private Context context;
    private BaseVideoViewListener baseVideoViewListener;
    private MediaPlayer.OnCompletionListener defaultOnCompletionListener;
    private MediaPlayer.OnErrorListener defaultOnErrorListener;
    private ShadowVideoView shadowSubject;
    private View.OnTouchListener defaultOnTouchListener;
    private MediaPlayer.OnPreparedListener defaultOnPreparedListener;
    private MediaPlayer mediaPlayer;

    @Before
    public void setUp() throws Exception {
        mediaPlayer = mock(MediaPlayer.class);
        context = new Activity();
        baseVideoViewListener = mock(BaseVideoViewListener.class);

        // we want to pause all threads in this test, so that we can properly test the network calls
        Robolectric.getUiThreadScheduler().pause();
        Robolectric.getBackgroundScheduler().pause();
    }

    @After
    public void tearDown() throws Exception {
        Robolectric.getUiThreadScheduler().reset();
        Robolectric.getBackgroundScheduler().reset();
        Robolectric.clearPendingHttpResponses();
    }

    @Test
    public void constructor_shouldSetListenersAndVideoPath() throws Exception {
        initializeSubject(createIntentForVastVideo(), null);

        assertThat(defaultOnCompletionListener).isNotNull();
        assertThat(defaultOnErrorListener).isNotNull();
        assertThat(defaultOnTouchListener).isNotNull();
        assertThat(defaultOnPreparedListener).isNotNull();

        assertThat(shadowSubject.getVideoPath()).isEqualTo("http://video");
        assertThat(subject.hasFocus()).isTrue();
    }

    @Test
    public void constructor_shouldNotChangeShowCloseButtonDelay() throws Exception {
        initializeSubject(createIntentForVastVideo(), null);

        assertThat(subject.getShowCloseButtonDelay()).isEqualTo(VastVideoView.DEFAULT_VIDEO_DURATION_FOR_CLOSE_BUTTON);
    }

    @Test
    public void constructor_shouldPingStartAndImpressionTrackers() throws Exception {
        Robolectric.addPendingHttpResponse(new TestHttpResponseWithHeaders(200, "start"));
        Robolectric.addPendingHttpResponse(new TestHttpResponseWithHeaders(200, "imp"));

        Intent intent = createIntentForVastVideo();
        addExtrasToStub(intent, VIDEO_START_TRACKERS, new ArrayList<String>(Arrays.asList("http://start")));
        addExtrasToStub(intent, VIDEO_IMPRESSION_TRACKERS, new ArrayList<String>(Arrays.asList("http://impressions")));
        initializeSubject(intent, null);

        Robolectric.getBackgroundScheduler().unPause();
        ThreadUtils.pause(NETWORK_DELAY);

        assertNetworkCallsMade("http://start", "http://impressions");
    }

    @Test
    public void constructor_shouldBeginVideoProgressChecker() throws Exception {
        initializeSubject(createIntentForVastVideo(), null);

        assertThat(Robolectric.getUiThreadScheduler().enqueuedTaskCount()).isEqualTo(1);
    }

    @Test
    public void onPrepared_whenDurationIsLessThanMaxVideoDurationForCloseButton_shouldSetShowCloseButtonDelayToDuration() throws Exception {
        initializeSubject(createIntentForVastVideo(), null);

        // by default, duration is -1 since the video hasn't started playing.
        int duration = subject.getDuration();
        assertThat(duration).isLessThan(VastVideoView.MAX_VIDEO_DURATION_FOR_CLOSE_BUTTON);

        defaultOnPreparedListener.onPrepared(null);

        assertThat(subject.getShowCloseButtonDelay()).isEqualTo(duration);
    }

    @Test
    public void onPrepared_whenDurationIsGreaterThanMaxVideoDurationForCloseButton_shouldNotSetShowCloseButtonDelay() throws Exception {
        stub(mediaPlayer.getDuration()).toReturn(16001);
        initializeSubject(createIntentForVastVideo(), null);
        setMediaPlayer(mediaPlayer);

        defaultOnPreparedListener.onPrepared(null);

        assertThat(subject.getShowCloseButtonDelay()).isEqualTo(5000);
    }

    @Test
    public void onTouch_withTouchUp_whenVideoLessThan16Seconds_andClickBeforeEnd_shouldDoNothing() throws Exception {
        Intent intent = createIntentForVastVideo();
        addExtrasToStub(intent, VIDEO_CLICK_THROUGH_URL, "http://clickThroughUrl");

        stub(mediaPlayer.getDuration()).toReturn(15999);
        stub(mediaPlayer.getCurrentPosition()).toReturn(15998);

        initializeSubject(intent, null);
        setMediaPlayer(mediaPlayer);
        defaultOnPreparedListener.onPrepared(mediaPlayer);

        Robolectric.getUiThreadScheduler().unPause();

        defaultOnTouchListener.onTouch(null, GestureUtils.createActionUp(0, 0));

        Intent nextStartedActivity = Robolectric.getShadowApplication().getNextStartedActivity();
        assertThat(nextStartedActivity).isNull();
    }

    @Test
    public void onTouch_withTouchUp_whenVideoLessThan16Seconds_andClickAfterEnd_shouldStartMraidBrowser() throws Exception {
        Intent intent = createIntentForVastVideo();
        addExtrasToStub(intent, VIDEO_CLICK_THROUGH_URL, "http://clickThroughUrl");

        stub(mediaPlayer.getDuration()).toReturn(15999);
        stub(mediaPlayer.getCurrentPosition()).toReturn(16001);

        initializeSubject(intent, null);
        setMediaPlayer(mediaPlayer);
        defaultOnPreparedListener.onPrepared(mediaPlayer);

        Robolectric.getUiThreadScheduler().unPause();

        defaultOnTouchListener.onTouch(null, GestureUtils.createActionUp(0, 0));

        Intent nextStartedActivity = Robolectric.getShadowApplication().getNextStartedActivity();
        assertThat(nextStartedActivity).isNotNull();
        assertThat(nextStartedActivity.getStringExtra(MraidBrowser.URL_EXTRA)).isEqualTo("http://clickThroughUrl");
        assertThat(nextStartedActivity.getComponent().getClassName()).isEqualTo("com.mopub.mobileads.MraidBrowser");
    }

    @Test
    public void onTouch_withTouchUp_whenVideoLongerThan16Seconds_andClickBefore5Seconds_shouldDoNothing() throws Exception {
        Intent intent = createIntentForVastVideo();
        addExtrasToStub(intent, VIDEO_CLICK_THROUGH_URL, "http://clickThroughUrl");

        stub(mediaPlayer.getDuration()).toReturn(100000);
        stub(mediaPlayer.getCurrentPosition()).toReturn(4999);

        initializeSubject(intent, null);
        setMediaPlayer(mediaPlayer);
        defaultOnPreparedListener.onPrepared(mediaPlayer);

        Robolectric.getUiThreadScheduler().unPause();

        defaultOnTouchListener.onTouch(null, GestureUtils.createActionUp(0, 0));

        Intent nextStartedActivity = Robolectric.getShadowApplication().getNextStartedActivity();
        assertThat(nextStartedActivity).isNull();
    }

    @Test
    public void onTouch_withTouchUp_whenVideoLongerThan16Seconds_andClickAfter5Seconds_shouldStartMraidBrowser() throws Exception {
        Intent intent = createIntentForVastVideo();
        addExtrasToStub(intent, VIDEO_CLICK_THROUGH_URL, "http://clickThroughUrl");

        stub(mediaPlayer.getDuration()).toReturn(100000);
        stub(mediaPlayer.getCurrentPosition()).toReturn(5001);

        initializeSubject(intent, null);
        setMediaPlayer(mediaPlayer);
        defaultOnPreparedListener.onPrepared(mediaPlayer);

        Robolectric.getUiThreadScheduler().unPause();

        defaultOnTouchListener.onTouch(null, GestureUtils.createActionUp(0, 0));

        Intent nextStartedActivity = Robolectric.getShadowApplication().getNextStartedActivity();
        assertThat(nextStartedActivity).isNotNull();
        assertThat(nextStartedActivity.getStringExtra(MraidBrowser.URL_EXTRA)).isEqualTo("http://clickThroughUrl");
        assertThat(nextStartedActivity.getComponent().getClassName()).isEqualTo("com.mopub.mobileads.MraidBrowser");
    }

    @Test
    public void onTouch_whenCloseButtonVisible_shouldPingClickThroughTrackers() throws Exception {
        Robolectric.addPendingHttpResponse(new TestHttpResponseWithHeaders(200, "body"));

        Intent intent = createIntentForVastVideo();
        addExtrasToStub(intent, VIDEO_CLICK_THROUGH_TRACKERS, new ArrayList<String>(Arrays.asList("http://clickThroughTrackers")));
        initializeSubject(intent, null);

        subject.setCloseButtonVisible(true);

        defaultOnTouchListener.onTouch(null, GestureUtils.createActionUp(0, 0));
        ThreadUtils.pause(NETWORK_DELAY);

        assertNetworkCallsMade("http://clickThroughTrackers");
    }

    @Test
    public void onTouch_whenCloseButtonNotVisible_shouldNotPingClickThroughTrackers() throws Exception {
        Robolectric.addPendingHttpResponse(new TestHttpResponseWithHeaders(200, "body"));

        Intent intent = createIntentForVastVideo();
        addExtrasToStub(intent, VIDEO_CLICK_THROUGH_TRACKERS, new ArrayList<String>(Arrays.asList("http://clickThroughTrackers")));
        initializeSubject(intent, null);

        subject.setCloseButtonVisible(false);

        defaultOnTouchListener.onTouch(null, GestureUtils.createActionUp(0, 0));
        ThreadUtils.pause(NETWORK_DELAY);

        assertThat(Robolectric.getNextSentHttpRequest()).isNull();
    }

    @Test
    public void onTouch_withNullBaseVideoViewListener_andActionTouchUp_shouldReturnTrueAndNotBlowUp() throws Exception {
        initializeSubject(createIntentForVastVideo(), null);

        boolean result = defaultOnTouchListener.onTouch(null, GestureUtils.createActionUp(0, 0));

        // pass

        assertThat(result).isTrue();
    }

    @Test
    public void onTouch_withActionTouchDown_shouldConsumeMotionEvent() throws Exception {
        initializeSubject(createIntentForVastVideo(), null);

        boolean result = defaultOnTouchListener.onTouch(null, GestureUtils.createActionDown(0, 0));

        assertThat(result).isTrue();
    }

    @Test
    public void onCompletion_shouldFireVideoCompleted() throws Exception {
        initializeSubject(createIntentForVastVideo(), baseVideoViewListener);

        defaultOnCompletionListener.onCompletion(null);

        verify(baseVideoViewListener).videoCompleted(eq(false));
    }

    @Test
    public void onCompletion_withNullBaseVideoViewListener_shouldNotBlowUp() throws Exception {
        initializeSubject(createIntentForVastVideo(), null);

        defaultOnCompletionListener.onCompletion(null);

        // pass
    }

    @Test
    public void onCompletion_shouldPingCompletionTrackers() throws Exception {
        Robolectric.addPendingHttpResponse(new TestHttpResponseWithHeaders(200, "body"));

        Intent intent = createIntentForVastVideo();
        addExtrasToStub(intent, VIDEO_COMPLETE_TRACKERS, new ArrayList<String>(Arrays.asList("http://completeTrackers")));
        initializeSubject(intent, null);

        defaultOnCompletionListener.onCompletion(null);
        ThreadUtils.pause(NETWORK_DELAY);

        assertNetworkCallsMade("http://completeTrackers");
    }

    @Test
    public void onCompletion_shouldPreventOnResumeFromStartingVideo() throws Exception {
        initializeSubject(createIntentForVastVideo(), null);

        defaultOnCompletionListener.onCompletion(null);

        subject.onResume();

        assertThat(shadowSubject.isPlaying()).isFalse();
    }

    @Test
    public void onCompletion_shouldStopProgressChecker() throws Exception {
        initializeSubject(createIntentForVastVideo(), baseVideoViewListener);
        assertThat(Robolectric.getUiThreadScheduler().enqueuedTaskCount()).isEqualTo(1);

        defaultOnCompletionListener.onCompletion(null);

        assertThat(Robolectric.getUiThreadScheduler().enqueuedTaskCount()).isEqualTo(0);
    }

    @Test
    public void onError_shouldFireVideoErrorAndReturnFalse() throws Exception {
        initializeSubject(createIntentForVastVideo(), baseVideoViewListener);

        boolean result = defaultOnErrorListener.onError(null, 0, 0);

        assertThat(result).isFalse();
        verify(baseVideoViewListener).videoError(eq(false));
    }

    @Test
    public void onError_withNullBaseVideoViewListener_shouldNotBlowUp () throws Exception {
        initializeSubject(createIntentForVastVideo(), baseVideoViewListener);

        boolean result = defaultOnErrorListener.onError(null, 0, 0);

        assertThat(result).isFalse();
    }

    @Test
    public void onError_shouldStopProgressChecker() throws Exception {
        initializeSubject(createIntentForVastVideo(), baseVideoViewListener);
        assertThat(Robolectric.getUiThreadScheduler().enqueuedTaskCount()).isEqualTo(1);

        defaultOnErrorListener.onError(null, 0, 0);

        assertThat(Robolectric.getUiThreadScheduler().enqueuedTaskCount()).isEqualTo(0);
    }

    @Test
    public void videoProgressCheckerRunnableRun_shouldFireOffAllProgressTrackers() throws Exception {
        Robolectric.addPendingHttpResponse(new TestHttpResponseWithHeaders(200, "first"));
        Robolectric.addPendingHttpResponse(new TestHttpResponseWithHeaders(200, "second"));
        Robolectric.addPendingHttpResponse(new TestHttpResponseWithHeaders(200, "third"));
        stub(mediaPlayer.getDuration()).toReturn(9001);
        stub(mediaPlayer.getCurrentPosition()).toReturn(9002);
        Intent intent = createIntentForVastVideo();
        addExtrasToStub(intent, VIDEO_FIRST_QUARTER_TRACKERS, new ArrayList<String>(Arrays.asList("http://first")));
        addExtrasToStub(intent, VIDEO_MID_POINT_TRACKERS, new ArrayList<String>(Arrays.asList("http://second")));
        addExtrasToStub(intent, VIDEO_THIRD_QUARTER_TRACKERS, new ArrayList<String>(Arrays.asList("http://third")));
        initializeSubject(intent, null);
        setMediaPlayer(mediaPlayer);

        // this runs the videoProgressChecker
        runRunnableWithNetworkCall();

        assertNetworkCallsMade("http://first", "http://second", "http://third");
    }

    @Test
    public void videoProgressCheckerRunnableRun_whenDurationIsInvalid_shouldNotMakeAnyNetworkCalls() throws Exception {
        Robolectric.addPendingHttpResponse(new TestHttpResponseWithHeaders(200, "error"));
        initializeSubject(createIntentForVastVideo(), null);
        assertThat(Robolectric.getUiThreadScheduler().enqueuedTaskCount()).isEqualTo(1);

        Robolectric.getUiThreadScheduler().runOneTask();
        // make sure the repeated task hasn't run yet
        assertThat(Robolectric.getUiThreadScheduler().enqueuedTaskCount()).isEqualTo(1);
        Robolectric.getBackgroundScheduler().unPause();
        ThreadUtils.pause(NETWORK_DELAY);

        assertThat(Robolectric.getNextSentHttpRequest()).isNull();
    }

    @Test
    public void videoProgressCheckerRunnableRun_whenProgressIsPastFirstQuartile_shouldOnlyPingFirstQuartileTrackersOnce() throws Exception {
        Robolectric.addPendingHttpResponse(new TestHttpResponseWithHeaders(200, "first"));
        stub(mediaPlayer.getDuration()).toReturn(100);
        stub(mediaPlayer.getCurrentPosition()).toReturn(26);
        Intent intent = createIntentForVastVideo();
        addExtrasToStub(intent, VIDEO_FIRST_QUARTER_TRACKERS, new ArrayList<String>(Arrays.asList("http://first")));
        initializeSubject(intent, null);
        setMediaPlayer(mediaPlayer);

        runRunnableWithNetworkCall();

        assertNetworkCallsMade("http://first");

        // run checker another time
        Robolectric.addPendingHttpResponse(new TestHttpResponseWithHeaders(200, "first2"));
        assertThat(Robolectric.getUiThreadScheduler().enqueuedTaskCount()).isEqualTo(1);

        runRunnableWithNetworkCall();

        assertThat(Robolectric.getNextSentHttpRequest()).isNull();
    }

    @Test
    public void videoProgressCheckerRunnableRun_whenProgressIsPastMidQuartile_shouldPingFirstQuartileTrackers_andMidQuartileTrackersBothOnlyOnce() throws Exception {
        Robolectric.addPendingHttpResponse(new TestHttpResponseWithHeaders(200, "first"));
        Robolectric.addPendingHttpResponse(new TestHttpResponseWithHeaders(200, "mid"));
        stub(mediaPlayer.getDuration()).toReturn(100);
        stub(mediaPlayer.getCurrentPosition()).toReturn(51);
        Intent intent = createIntentForVastVideo();
        addExtrasToStub(intent, VIDEO_FIRST_QUARTER_TRACKERS, new ArrayList<String>(Arrays.asList("http://first")));
        addExtrasToStub(intent, VIDEO_MID_POINT_TRACKERS, new ArrayList<String>(Arrays.asList("http://mid")));
        initializeSubject(intent, null);
        setMediaPlayer(mediaPlayer);

        runRunnableWithNetworkCall();

        assertNetworkCallsMade("http://first", "http://mid");

        // run checker again
        Robolectric.addPendingHttpResponse(new TestHttpResponseWithHeaders(200, "first2"));
        Robolectric.addPendingHttpResponse(new TestHttpResponseWithHeaders(200, "mid2"));
        assertThat(Robolectric.getUiThreadScheduler().enqueuedTaskCount()).isEqualTo(1);

        runRunnableWithNetworkCall();

        assertThat(Robolectric.getNextSentHttpRequest()).isNull();
    }

    @Test
    public void videoProgressCheckerRunnableRun_whenProgressIsPastThirdQuartile_shouldPingFirstQuartileTrackers_andMidQuartileTrackers_andThirdQuartileTrackersAllOnlyOnce() throws Exception {
        Robolectric.addPendingHttpResponse(new TestHttpResponseWithHeaders(200, "first"));
        Robolectric.addPendingHttpResponse(new TestHttpResponseWithHeaders(200, "mid"));
        Robolectric.addPendingHttpResponse(new TestHttpResponseWithHeaders(200, "third"));
        stub(mediaPlayer.getDuration()).toReturn(100);
        stub(mediaPlayer.getCurrentPosition()).toReturn(76);
        Intent intent = createIntentForVastVideo();
        addExtrasToStub(intent, VIDEO_FIRST_QUARTER_TRACKERS, new ArrayList<String>(Arrays.asList("http://first")));
        addExtrasToStub(intent, VIDEO_MID_POINT_TRACKERS, new ArrayList<String>(Arrays.asList("http://mid")));
        addExtrasToStub(intent, VIDEO_THIRD_QUARTER_TRACKERS, new ArrayList<String>(Arrays.asList("http://third")));
        initializeSubject(intent, null);
        setMediaPlayer(mediaPlayer);

        runRunnableWithNetworkCall();

        assertNetworkCallsMade("http://first", "http://mid", "http://third");

        // run checker again
        Robolectric.addPendingHttpResponse(new TestHttpResponseWithHeaders(200, "first2"));
        Robolectric.addPendingHttpResponse(new TestHttpResponseWithHeaders(200, "mid2"));
        Robolectric.addPendingHttpResponse(new TestHttpResponseWithHeaders(200, "third2"));
        assertThat(Robolectric.getUiThreadScheduler().enqueuedTaskCount()).isEqualTo(1);

        runRunnableWithNetworkCall();

        assertThat(Robolectric.getNextSentHttpRequest()).isNull();
    }

    @Test
    public void videoProgressCheckerRunnableRun_asVideoPlays_shouldPingAllThreeTrackersIndividuallyOnce() throws Exception {
        Robolectric.addPendingHttpResponse(new TestHttpResponseWithHeaders(200, "first"));
        Robolectric.addPendingHttpResponse(new TestHttpResponseWithHeaders(200, "mid"));
        Robolectric.addPendingHttpResponse(new TestHttpResponseWithHeaders(200, "third"));
        Robolectric.addPendingHttpResponse(new TestHttpResponseWithHeaders(200, "error"));
        stub(mediaPlayer.getDuration()).toReturn(100);
        Intent intent = createIntentForVastVideo();
        addExtrasToStub(intent, VIDEO_FIRST_QUARTER_TRACKERS, new ArrayList<String>(Arrays.asList("http://first")));
        addExtrasToStub(intent, VIDEO_MID_POINT_TRACKERS, new ArrayList<String>(Arrays.asList("http://mid")));
        addExtrasToStub(intent, VIDEO_THIRD_QUARTER_TRACKERS, new ArrayList<String>(Arrays.asList("http://third")));
        initializeSubject(intent, null);
        setMediaPlayer(mediaPlayer);

        // before any trackers are fired
        fastForwardMediaPlayerAndAssertRequestMade(1, null);

        fastForwardMediaPlayerAndAssertRequestMade(24, null);

        // after it hits first tracker
        fastForwardMediaPlayerAndAssertRequestMade(26, "http://first");

        // before mid quartile is hit
        fastForwardMediaPlayerAndAssertRequestMade(49, null);

        // after it hits mid trackers
        fastForwardMediaPlayerAndAssertRequestMade(51, "http://mid");

        // before third quartile is hit
        fastForwardMediaPlayerAndAssertRequestMade(74, null);

        // after third quartile is hit
        fastForwardMediaPlayerAndAssertRequestMade(76, "http://third");

        // way after third quartile is hit
        fastForwardMediaPlayerAndAssertRequestMade(99, null);
    }

    @Test
    public void videoProgressCheckerRunnableRun_whenCurrentPositionIsGreaterThanShowCloseButtonDelay_shouldShowCloseButton() throws Exception {
        stub(mediaPlayer.getDuration()).toReturn(5002);
        stub(mediaPlayer.getCurrentPosition()).toReturn(5001);

        initializeSubject(createIntentForVastVideo(), baseVideoViewListener);
        setMediaPlayer(mediaPlayer);

        Robolectric.getUiThreadScheduler().unPause();

        verify(baseVideoViewListener).showCloseButton();
    }

    @Test
    public void videoProgressCheckerRunnableRun_whenCurrentPositionIsGreaterThanShowCloseButtonDelay_andBaseVideoViewListenerIsNull_shouldNotBlowUp() throws Exception {
        stub(mediaPlayer.getDuration()).toReturn(5002);
        stub(mediaPlayer.getCurrentPosition()).toReturn(5001);

        initializeSubject(createIntentForVastVideo(), null);
        setMediaPlayer(mediaPlayer);

        Robolectric.getUiThreadScheduler().unPause();

        // pass
    }

    @Test
    public void videoProgressCheckerRunnableRun_whenVideoProgressShouldNotBeChecked_shouldNotPostAnotherRunnable() throws Exception {
        initializeSubject(createIntentForVastVideo(), null);
        Robolectric.getUiThreadScheduler().unPause();

        assertThat(Robolectric.getUiThreadScheduler().enqueuedTaskCount()).isEqualTo(1);
        subject.setIsVideoProgressShouldBeChecked(false);

        Robolectric.getUiThreadScheduler().runOneTask();
        assertThat(Robolectric.getUiThreadScheduler().enqueuedTaskCount()).isEqualTo(0);
    }

    @Test
    public void onPause_shouldStopProgressChecker() throws Exception {
        initializeSubject(createIntentForVastVideo(), null);
        assertThat(Robolectric.getUiThreadScheduler().enqueuedTaskCount()).isEqualTo(1);

        subject.onPause();

        assertThat(Robolectric.getUiThreadScheduler().enqueuedTaskCount()).isEqualTo(0);
    }

    @Test
    public void onResume_shouldResumeVideoProgressChecker() throws Exception {
        initializeSubject(createIntentForVastVideo(), null);
        assertThat(Robolectric.getUiThreadScheduler().enqueuedTaskCount()).isEqualTo(1);

        subject.onPause();
        assertThat(Robolectric.getUiThreadScheduler().enqueuedTaskCount()).isEqualTo(0);

        subject.onResume();
        assertThat(Robolectric.getUiThreadScheduler().enqueuedTaskCount()).isEqualTo(1);
    }

    @Test
    public void onResume_shouldSetVideoViewStateToStarted() throws Exception {
        initializeSubject(createIntentForVastVideo(), null);

        subject.onResume();

        assertThat(shadowSubject.getCurrentVideoState()).isEqualTo(ShadowVideoView.START);
        assertThat(shadowSubject.getPrevVideoState()).isNotEqualTo(ShadowVideoView.START);
    }

    private void initializeSubject(final Intent intent, final BaseVideoViewListener baseVideoViewListener) {
        subject = new VastVideoView(context, intent, baseVideoViewListener);

        shadowSubject = shadowOf(subject);
        defaultOnCompletionListener = shadowSubject.getOnCompletionListener();
        defaultOnErrorListener = shadowSubject.getOnErrorListener();
        defaultOnTouchListener = shadowSubject.getOnTouchListener();
        defaultOnPreparedListener = shadowSubject.getOnPreparedListener();
    }

    private Intent createIntentForVastVideo() {
        Intent result = mock(Intent.class);
        stub(result.getStringExtra(VIDEO_URL)).toReturn("http://video");
        return result;
    }

    private Intent addExtrasToStub(Intent intent, String key, String value) {
        stub(intent.getStringExtra(eq(key))).toReturn(value);
        return intent;
    }

    private Intent addExtrasToStub(Intent intent, String key, ArrayList<String> value) {
        stub(intent.getStringArrayListExtra(eq(key))).toReturn(value);
        return intent;
    }

    private void setMediaPlayer(MediaPlayer mockMediaPlayer) throws IllegalAccessException {
        // tricks the media player to think it's playing currently
        ReflectionUtils.setVariableValueInObject(subject, "mMediaPlayer", mockMediaPlayer);
        int state = (Integer) ReflectionUtils.getValueIncludingSuperclasses("STATE_PLAYING", subject);
        ReflectionUtils.setVariableValueInObject(subject, "mCurrentState", state);
    }

    private void runRunnableWithNetworkCall() {
        Robolectric.getUiThreadScheduler().runOneTask();
        Robolectric.getBackgroundScheduler().unPause();
        ThreadUtils.pause(NETWORK_DELAY);
        Robolectric.getBackgroundScheduler().pause();
    }

    // note: this is needed because network calls don't always return in order.
    private void assertNetworkCallsMade(String... urls) {
        if (urls == null || urls.length == 0) {
            fail("your test shouldn't call this method with nothing in it");
        }

        List<String> expectedUris = Arrays.asList(urls);
        List<String> actualUris = new ArrayList<String>();

        HttpRequest httpRequest;
        while ((httpRequest = Robolectric.getNextSentHttpRequest()) != null) {
            assertThat(httpRequest).isInstanceOf(HttpGet.class);
            actualUris.add(((HttpGet) httpRequest).getURI().toString());
        }

        assertThat(expectedUris.size()).isEqualTo(actualUris.size());

        Collections.sort(expectedUris);
        Collections.sort(actualUris);

        assertThat(expectedUris).isEqualTo(actualUris);
    }

    private void fastForwardMediaPlayerAndAssertRequestMade(int time, String... uri) throws Exception {
        stub(mediaPlayer.getCurrentPosition()).toReturn(time);
        runRunnableWithNetworkCall();

        if (uri == null) {
            assertThat(Robolectric.getNextSentHttpRequest()).isNull();
        } else {
            assertNetworkCallsMade(uri);
        }
    }
}
