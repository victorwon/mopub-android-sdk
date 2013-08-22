package com.mopub.mobileads;

import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import com.mopub.mobileads.MraidView.ExpansionStyle;
import com.mopub.mobileads.MraidView.NativeCloseButtonStyle;
import com.mopub.mobileads.MraidView.PlacementType;
import com.mopub.mobileads.MraidView.ViewState;
import com.mopub.mobileads.factories.MraidViewFactory;
import com.mopub.mobileads.util.WebViews;

import static com.mopub.mobileads.AdFetcher.HTML_RESPONSE_BODY_KEY;
import static com.mopub.mobileads.util.VersionCode.ICE_CREAM_SANDWICH;
import static com.mopub.mobileads.util.VersionCode.currentApiLevel;

public class MraidActivity extends BaseInterstitialActivity {
    private MraidView mMraidView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        broadcastInterstitialAction(ACTION_INTERSTITIAL_SHOW);

        if (currentApiLevel().isAtLeast(ICE_CREAM_SANDWICH)) {
            getWindow().setFlags(
                    WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                    WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
        }
    }

    @Override
    public View getAdView() {
        mMraidView = MraidViewFactory.create(this, ExpansionStyle.DISABLED, NativeCloseButtonStyle.AD_CONTROLLED,
                PlacementType.INTERSTITIAL);

        mMraidView.setMraidListener(new MraidView.BaseMraidListener(){
            public void onReady(MraidView view) {
                showInterstitialCloseButton();
            }
            public void onClose(MraidView view, ViewState newViewState) {
                finish();
            }
        });

        mMraidView.setOnCloseButtonStateChange(new MraidView.OnCloseButtonStateChangeListener() {
            public void onCloseButtonStateChange(MraidView view, boolean enabled) {
                if (enabled) {
                    showInterstitialCloseButton();
                } else {
                    hideInterstitialCloseButton();
                }
            }
        });

        String source = getIntent().getStringExtra(HTML_RESPONSE_BODY_KEY);
        mMraidView.loadHtmlData(source);

        return mMraidView;
    }

    @Override
    protected void onPause() {
        super.onPause();
        WebViews.onPause(mMraidView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        WebViews.onResume(mMraidView);
    }

    @Override
    protected void onDestroy() {
        mMraidView.destroy();
        super.onDestroy();
    }
}
