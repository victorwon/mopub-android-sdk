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
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.RelativeLayout;
import android.widget.VideoView;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static com.mopub.mobileads.MraidCommandRegistry.MRAID_JAVASCRIPT_COMMAND_PLAY_VIDEO;

public class MraidVideoPlayerActivity extends Activity {
    protected static final String MRAID_VIDEO_URL = "mraid_video_url";
    private static final String TAG = "VideoPlayerActivity";
    private static MraidView mraidView;

    static void start(Context context, MraidView view, String videoUrl) {
        MraidVideoPlayerActivity.mraidView = view;
        Intent intentVideoPlayerActivity = createIntent(context, videoUrl);
        try {
            context.startActivity(intentVideoPlayerActivity);
        } catch (ActivityNotFoundException anfe) {
            Log.d("MraidVideoPlayerActivity", "Activity MraidVideoPlayerActivity not found. Did you declare it in your AndroidManifest.xml?");
        }
    }

    static Intent createIntent(Context context, String videoUrl) {
        Intent intentVideoPlayerActivity = new Intent(context, MraidVideoPlayerActivity.class);
        intentVideoPlayerActivity.setFlags(FLAG_ACTIVITY_NEW_TASK);
        intentVideoPlayerActivity.putExtra(MRAID_VIDEO_URL, videoUrl);
        return intentVideoPlayerActivity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        RelativeLayout contentView = new RelativeLayout(this);
        VideoView videoView = new VideoView(this);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.FILL_PARENT,
                RelativeLayout.LayoutParams.FILL_PARENT);
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        contentView.addView(videoView, layoutParams);

        setContentView(contentView);

        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                finish();
            }
        });
        videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mediaPlayer, int what, int extra) {
                Log.d(TAG, "Error: video can not be played.");
                mraidView.fireErrorEvent(MRAID_JAVASCRIPT_COMMAND_PLAY_VIDEO, "Video could not be played");
                return false;
            }
        });

        videoView.setVideoPath(getIntent().getStringExtra(MRAID_VIDEO_URL));

        videoView.start();
    }
}
