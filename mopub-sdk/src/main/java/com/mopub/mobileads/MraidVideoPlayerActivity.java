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
