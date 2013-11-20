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

import android.R;
import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.VideoView;
import com.mopub.mobileads.test.support.SdkTestRunner;
import com.mopub.mobileads.test.support.TestMraidViewFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.Robolectric;
import org.robolectric.shadows.ShadowLog;
import org.robolectric.shadows.ShadowVideoView;

import java.util.*;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.reset;
import static org.robolectric.Robolectric.buildActivity;
import static org.robolectric.Robolectric.shadowOf;

@RunWith(SdkTestRunner.class)
public class MraidVideoPlayerActivityTest {

    private MraidVideoPlayerActivity subject;
    private MraidView mraidView;

    public static void assertVideoPlayerActivityStarted(String expectedURI) {
        Intent intent = Robolectric.getShadowApplication().getNextStartedActivity();
        assertThat(intent.getComponent().getClassName()).isEqualTo("com.mopub.mobileads.MraidVideoPlayerActivity");
        assertThat(intent.getStringExtra(MraidVideoPlayerActivity.MRAID_VIDEO_URL)).isEqualTo(expectedURI);
        assertThat(intent.getFlags() & Intent.FLAG_ACTIVITY_NEW_TASK).isNotEqualTo(0);
    }

    @Before
    public void setup() {
        subject = buildActivity(MraidVideoPlayerActivity.class)
                .withIntent(MraidVideoPlayerActivity.createIntent(new Activity(), "http://video"))
                .create()
                .get();
        mraidView = TestMraidViewFactory.getSingletonMock();
        reset(mraidView);
    }

    @Test
    public void start_shouldStartVideoPlayer() throws Exception {
        MraidVideoPlayerActivity.start(new Activity(), mraidView, "http://video");

        assertVideoPlayerActivityStarted("http://video");
    }

    @Test
    public void onCreate_shouldSetupVideoView() throws Exception {
        VideoView videoView = findVideoView();
        ShadowVideoView shadowVideoView = shadowOf(videoView);
        assertThat(videoView).isNotNull();
        assertThat(shadowVideoView.getVideoPath()).isEqualTo("http://video");
        assertThat(shadowVideoView.getCurrentVideoState()).isEqualTo(ShadowVideoView.START);
    }

    @Test
    public void onCreate_shouldCenterVideoView() throws Exception {
        VideoView videoView = findVideoView();

        RelativeLayout.LayoutParams videoLayout = (RelativeLayout.LayoutParams) videoView.getLayoutParams();
        assertThat(videoLayout.getRules()[RelativeLayout.CENTER_IN_PARENT]).isNotEqualTo(0);
    }

    @Test
    public void whenVideoFinishes_shouldFinish() throws Exception {
        VideoView videoView = findVideoView();
        ShadowVideoView shadowVideoView = shadowOf(videoView);

        shadowVideoView.getOnCompletionListener().onCompletion(null);

        assertThat(shadowOf(subject).isFinishing()).isTrue();
    }

    @Test
    public void shouldSetOnErrorListener() throws Exception {
        VideoView videoView = findVideoView();
        ShadowVideoView shadowVideoView = shadowOf(videoView);

        assertThat(shadowVideoView.getOnErrorListener()).isNotNull();
        assertThat(shadowVideoView.getOnErrorListener()).isInstanceOf(MediaPlayer.OnErrorListener.class);
    }

    @Test
    public void whenOnErrorListenerIsCalled_shouldLogOneErrorMessage() throws Exception {
        VideoView videoView = findVideoView();
        ShadowVideoView shadowVideoView = shadowOf(videoView);

        shadowVideoView.getOnErrorListener().onError(null, MediaPlayer.MEDIA_ERROR_UNKNOWN, MediaPlayer.MEDIA_ERROR_SERVER_DIED);
        assertThat(isOneErrorLogged()).isTrue();
    }

    @Test
    public void whenOnErrorListenerIsCalled_shouldFireErrorEvent() throws Exception {
        MraidVideoPlayerActivity.start(new Activity(), mraidView, "http://video");
        VideoView videoView = findVideoView();
        ShadowVideoView shadowVideoView = shadowOf(videoView);

        reset(mraidView);
        shadowVideoView.getOnErrorListener().onError(null, MediaPlayer.MEDIA_ERROR_UNKNOWN, MediaPlayer.MEDIA_ERROR_SERVER_DIED);
        Mockito.verify(mraidView).fireErrorEvent(eq(MraidCommandRegistry.MRAID_JAVASCRIPT_COMMAND_PLAY_VIDEO), any(String.class));
    }

    private boolean isOneErrorLogged() {
        List<ShadowLog.LogItem> logs = ShadowLog.getLogsForTag("VideoPlayerActivity");
        if(logs == null || logs.size() < 1){
            return false;
        }
        return logs.get(0).msg.startsWith("Error:");
    }

    private VideoView findVideoView() {
        ViewGroup parentView = (ViewGroup) subject.findViewById(R.id.content);
        return findVideoView(parentView);
    }

    // @phil, why did you write this? It looks like way more than we need.
    // phil: it came to me in a vision. Besides now we can mess with the guts of this thing and still always be able to get the VideoView
    private VideoView findVideoView(ViewGroup parentView) {
        for (int index = 0; index < parentView.getChildCount(); index++) {
            View childView = parentView.getChildAt(index);
            if (childView instanceof VideoView) {
                return (VideoView) childView;
            }
            if (childView instanceof ViewGroup) {
                return findVideoView((ViewGroup) childView);
            }
        }

        return null;
    }
}
