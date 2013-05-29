package com.mopub.mobileads;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.os.Build;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebChromeClient.CustomViewCallback;
import android.widget.*;
import com.mopub.mobileads.util.Dips;

import static android.util.TypedValue.COMPLEX_UNIT_SP;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static com.mopub.mobileads.resource.Drawables.DEFAULT_VIDEO_POSTER;

@Deprecated
public class HTML5AdView extends AdViewController {

    private FrameLayout mCustomViewContainer;
    private View mCustomView;
    private CustomViewCallback mCustomViewCallback;
    private View mVideoProgressView;

    static final FrameLayout.LayoutParams COVER_SCREEN_GRAVITY_CENTER =
            new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.FILL_PARENT,
                    FrameLayout.LayoutParams.FILL_PARENT,
                    Gravity.CENTER);

    public HTML5AdView(Context context, MoPubView view) {
        super(context, view);

        int sdkVersion = (new Integer(Build.VERSION.SDK)).intValue();
        if (sdkVersion > 7) {
            getAdWebView().setWebChromeClient(new HTML5WebChromeClient());
        }

        mCustomViewContainer = new FrameLayout(context);
        mCustomViewContainer.setVisibility(GONE);
        mCustomViewContainer.setLayoutParams(COVER_SCREEN_GRAVITY_CENTER);
    }

    private class HTML5WebChromeClient extends WebChromeClient implements OnCompletionListener,
            OnErrorListener {

        @TargetApi(7) // equivalent to Build.VERSION_CODES.ECLAIR_MR1
        @Override
        public void onShowCustomView(View view, CustomViewCallback callback) {
            super.onShowCustomView(view, callback);

            getAdWebView().setVisibility(GONE);

            // If a custom view already exists, don't show another one.
            if (mCustomView != null) {
                callback.onCustomViewHidden();
                return;
            }

            mCustomViewContainer.addView(view, COVER_SCREEN_GRAVITY_CENTER);
            mCustomView = view;
            mCustomViewCallback = callback;

            // Display the custom view in the MoPubView's hierarchy.
            getMoPubView().addView(mCustomViewContainer);
            mCustomViewContainer.setVisibility(VISIBLE);
            mCustomViewContainer.bringToFront();
        }

        @Override
        public void onHideCustomView() {
            if (mCustomView == null) return;

            // Hide the custom view.
            mCustomView.setVisibility(GONE);

            // Remove the custom view from its container.
            mCustomViewContainer.removeView(mCustomView);
            mCustomView = null;
            mCustomViewContainer.setVisibility(GONE);
            mCustomViewCallback.onCustomViewHidden();

            // Stop displaying the custom view container and unhide the ad view.
            getMoPubView().removeView(mCustomViewContainer);
            getAdWebView().setVisibility(VISIBLE);
        }

        @Override
        public Bitmap getDefaultVideoPoster() {
            return DEFAULT_VIDEO_POSTER.decodeImage(getContext()).getBitmap();
        }

        @Override
        public View getVideoLoadingProgressView() {
            if (mVideoProgressView == null) {
                mVideoProgressView = createVideoProgressView();
            }
            return mVideoProgressView;
        }

        @Override
        public boolean onError(MediaPlayer arg0, int arg1, int arg2) {
            Log.d("MoPub", "Video errored!");
            return false;
        }

        @Override
        public void onCompletion(MediaPlayer mp) {
            mp.stop();
            mCustomViewCallback.onCustomViewHidden();
            Log.d("MoPub", "Video completed!");
        }

        private View createVideoProgressView() {
            LinearLayout mVideoProgressView = new LinearLayout(getContext());
            mVideoProgressView.setOrientation(LinearLayout.VERTICAL);

            RelativeLayout.LayoutParams videoLayoutParams = new RelativeLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
            videoLayoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
            mVideoProgressView.setLayoutParams(videoLayoutParams);

            ProgressBar progressBar = new ProgressBar(getContext(), null, android.R.attr.progressBarStyleLarge);
            LinearLayout.LayoutParams progressBarLayoutParams = new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
            progressBarLayoutParams.gravity = Gravity.CENTER;
            progressBar.setLayoutParams(progressBarLayoutParams);
            mVideoProgressView.addView(progressBar);

            TextView textView = new TextView(getContext());
            LinearLayout.LayoutParams textViewLayoutParams = new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
            textViewLayoutParams.gravity = Gravity.CENTER;

            textView.setText("Loading...");
            textView.setTextSize(COMPLEX_UNIT_SP, 14f);
            textView.setTextColor(getContext().getResources().getColor(android.R.color.white));
            textView.setPadding(0, Dips.asIntPixels(5f, getContext()), 0, 0);

            textView.setLayoutParams(textViewLayoutParams);
            mVideoProgressView.addView(textView);
            return mVideoProgressView;
        }
    }
}
