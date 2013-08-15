package com.mopub.mobileads;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import com.mopub.mobileads.util.Dips;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static com.mopub.mobileads.resource.Drawables.INTERSTITIAL_CLOSE_BUTTON_NORMAL;
import static com.mopub.mobileads.resource.Drawables.INTERSTITIAL_CLOSE_BUTTON_PRESSED;

public abstract class BaseInterstitialActivity extends Activity {
    public static final String ACTION_INTERSTITIAL_FAIL = "com.mopub.action.interstitial.fail";
    public static final String ACTION_INTERSTITIAL_SHOW = "com.mopub.action.interstitial.show";
    public static final String ACTION_INTERSTITIAL_DISMISS = "com.mopub.action.interstitial.dismiss";
    public static final String ACTION_INTERSTITIAL_CLICK = "com.mopub.action.interstitial.click";
    public static final IntentFilter HTML_INTERSTITIAL_INTENT_FILTER = createHtmlInterstitialIntentFilter();
    private static final float CLOSE_BUTTON_SIZE = 50f;
    private static final float CLOSE_BUTTON_PADDING = 8f;

    private ImageView mCloseButton;
    private RelativeLayout mLayout;
    private int mButtonSize;
    private int mButtonPadding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        mButtonSize = Dips.asIntPixels(CLOSE_BUTTON_SIZE, this);
        mButtonPadding = Dips.asIntPixels(CLOSE_BUTTON_PADDING, this);

        mLayout = new RelativeLayout(this);
        final RelativeLayout.LayoutParams adViewLayout = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        adViewLayout.addRule(RelativeLayout.CENTER_IN_PARENT);
        mLayout.addView(getAdView(), adViewLayout);
        setContentView(mLayout);

        createInterstitialCloseButton();
    }

    @Override
    protected void onDestroy() {
        broadcastInterstitialAction(ACTION_INTERSTITIAL_DISMISS);
        mLayout.removeAllViews();
        super.onDestroy();
    }

    public abstract View getAdView();

    protected void showInterstitialCloseButton() {
        mCloseButton.setVisibility(VISIBLE);
    }

    protected void hideInterstitialCloseButton() {
        mCloseButton.setVisibility(INVISIBLE);
    }

    protected void broadcastInterstitialAction(String action) {
        Intent intent = new Intent(action);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void createInterstitialCloseButton() {
        mCloseButton = new ImageButton(this);
        StateListDrawable states = new StateListDrawable();
        states.addState(new int[] {-android.R.attr.state_pressed}, INTERSTITIAL_CLOSE_BUTTON_NORMAL.decodeImage(this));
        states.addState(new int[] {android.R.attr.state_pressed}, INTERSTITIAL_CLOSE_BUTTON_PRESSED.decodeImage(this));
        mCloseButton.setImageDrawable(states);
        mCloseButton.setBackgroundDrawable(null);
        mCloseButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });

        RelativeLayout.LayoutParams buttonLayout = new RelativeLayout.LayoutParams(mButtonSize, mButtonSize);
        buttonLayout.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        buttonLayout.setMargins(mButtonPadding, 0, mButtonPadding, 0);
        mLayout.addView(mCloseButton, buttonLayout);
    }

    private static IntentFilter createHtmlInterstitialIntentFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_INTERSTITIAL_FAIL);
        intentFilter.addAction(ACTION_INTERSTITIAL_SHOW);
        intentFilter.addAction(ACTION_INTERSTITIAL_DISMISS);
        intentFilter.addAction(ACTION_INTERSTITIAL_CLICK);
        return intentFilter;
    }
}
