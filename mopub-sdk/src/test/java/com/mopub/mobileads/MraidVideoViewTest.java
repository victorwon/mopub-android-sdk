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
import com.mopub.mobileads.test.support.SdkTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.shadows.ShadowVideoView;

import static com.mopub.mobileads.MraidVideoPlayerActivity.VIDEO_URL;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.stub;
import static org.mockito.Mockito.verify;
import static org.robolectric.Robolectric.shadowOf;

@RunWith(SdkTestRunner.class)
public class MraidVideoViewTest {
    private MraidVideoView subject;
    private Context context;
    private Intent intent;
    private BaseVideoView.BaseVideoViewListener baseVideoViewListener;

    @Before
    public void setUp() throws Exception {
        context = new Activity();
        intent = mock(Intent.class);
        stub(intent.getStringExtra(VIDEO_URL)).toReturn("videoUrl");
        baseVideoViewListener = mock(BaseVideoView.BaseVideoViewListener.class);

        subject = new MraidVideoView(context, intent, baseVideoViewListener);
    }

    @Test
    public void constructor_shouldSetListenersAndVideoPath() throws Exception {
        ShadowVideoView shadowSubject = shadowOf(subject);

        assertThat(shadowSubject.getOnCompletionListener()).isNotNull();
        assertThat(shadowSubject.getOnErrorListener()).isNotNull();
        assertThat(shadowSubject.getVideoPath()).isEqualTo("videoUrl");
    }

    @Test
    public void onCompletionListener_shouldCallBaseVideoViewVideoCompleted() throws Exception {
        ShadowVideoView shadowSubject = shadowOf(subject);

        shadowSubject.getOnCompletionListener().onCompletion(null);

        verify(baseVideoViewListener).videoCompleted(eq(true));
    }

    @Test
    public void onCompletion_withNullBaseVideoViewListener_shouldNotBlowUp() throws Exception {
        subject = new MraidVideoView(context, intent, null);
        ShadowVideoView shadowSubject = shadowOf(subject);

        shadowSubject.getOnCompletionListener().onCompletion(null);

        // pass
    }

    @Test
    public void onErrorListener_shouldCallBaseVideoViewVideoErrorAndReturnFalse() throws Exception {
        ShadowVideoView shadowSubject = shadowOf(subject);

        boolean result = shadowSubject.getOnErrorListener().onError(null, 0, 0);

        verify(baseVideoViewListener).videoError(false);
        assertThat(result).isFalse();
    }

    @Test
    public void onError_withNullBaseVideoViewListener_shouldNotBlowUp() throws Exception {
        subject = new MraidVideoView(context, intent, null);
        ShadowVideoView shadowSubject = shadowOf(subject);

        shadowSubject.getOnErrorListener().onError(null, 0, 0);

        // pass
    }
}
