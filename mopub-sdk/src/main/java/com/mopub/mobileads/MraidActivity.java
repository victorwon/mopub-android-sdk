package com.mopub.mobileads;

import android.view.View;
import com.mopub.mobileads.MraidView.ExpansionStyle;
import com.mopub.mobileads.MraidView.NativeCloseButtonStyle;
import com.mopub.mobileads.MraidView.PlacementType;
import com.mopub.mobileads.MraidView.ViewState;
import com.mopub.mobileads.factories.MraidViewFactory;

public class MraidActivity extends BaseActivity {    
    private MraidView mMraidView;
    
    @Override
    public View getAdView() {
        mMraidView = MraidViewFactory.create(this, ExpansionStyle.DISABLED, NativeCloseButtonStyle.AD_CONTROLLED,
                PlacementType.INTERSTITIAL);
        
        mMraidView.setOnReadyListener(new MraidView.OnReadyListener() {
            public void onReady(MraidView view) {
                showInterstitialCloseButton();
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
        
        mMraidView.setOnCloseListener(new MraidView.OnCloseListener() {
            public void onClose(MraidView view, ViewState newViewState) {
                finish();
            }
        });
        
        String source = getIntent().getStringExtra(SOURCE_KEY);
        mMraidView.loadHtmlData(source);
        
        return mMraidView;
    }
    
    @Override
    protected void onDestroy() {
        mMraidView.destroy();
        super.onDestroy();
    }
}
