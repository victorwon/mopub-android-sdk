package com.mopub.simpleadsdemo;

import java.util.Map;

import android.content.Context;
import android.util.Log;

import com.greystripe.sdk.GSAd;
import com.greystripe.sdk.GSAdErrorCode;
import com.greystripe.sdk.GSAdListener;
import com.greystripe.sdk.GSFullscreenAd;
import com.mopub.mobileads.CustomEventInterstitial;
import com.mopub.mobileads.MoPubErrorCode;

/*
 * Tested with Greystripe SDK 2.3.0.
 */
class GreystripeInterstitial extends CustomEventInterstitial implements GSAdListener {
    private CustomEventInterstitialListener mInterstitialListener;
    private GSFullscreenAd mGreystripeAd;
    
    /*
     * Abstract methods from CustomEventInterstitial
     */
    @Override
    protected void loadInterstitial(Context context, CustomEventInterstitialListener interstitialListener,
                                    Map<String, Object> localExtras, Map<String, String> serverExtras) {
        mInterstitialListener = interstitialListener;

        /*
         * You may also pass this String down in the serverExtras Map by specifying Custom Event Data
         * in MoPub's web interface.
         */
        String greystripeAppId = "YOUR_GREYSTRIPE_APP_ID";
        
        mGreystripeAd = new GSFullscreenAd(context, greystripeAppId);
        mGreystripeAd.addListener(this);
        
        mGreystripeAd.fetch();
    }

    @Override
    protected void showInterstitial() {
        if (!mGreystripeAd.isAdReady()) {
            mInterstitialListener.onInterstitialFailed(MoPubErrorCode.NETWORK_INVALID_STATE);
            return;
        }
        
        Log.d("MoPub", "Showing Greystripe interstitial ad.");
        mGreystripeAd.display();
        mInterstitialListener.onInterstitialShown();
    }
    
    @Override
    protected void onInvalidate() {
        mGreystripeAd.removeListener(this);
    }

    /*
     * GSAdListener implementation
     */
    @Override
    public void onAdClickthrough(GSAd greystripeAd) {
        Log.d("MoPub", "Greystripe interstitial ad clicked.");
        mInterstitialListener.onInterstitialClicked();

        /*
         * XXX: When a Greystripe interstitial is dismissed as a result of a user click, the
         * onAdDismissal callback does not get fired. This call ensures that the custom event
         * listener is informed of all dismissals.
         */
        mInterstitialListener.onInterstitialDismissed();
    }

    @Override
    public void onAdDismissal(GSAd greystripeAd) {
        Log.d("MoPub", "Greystripe interstitial ad dismissed.");
        mInterstitialListener.onInterstitialDismissed();
    }

    @Override
    public void onFailedToFetchAd(GSAd greystripeAd, GSAdErrorCode errorCode) {
        Log.d("MoPub", "Greystripe interstitial ad failed to load.");
        mInterstitialListener.onInterstitialFailed(MoPubErrorCode.NETWORK_NO_FILL);
    }

    @Override
    public void onFetchedAd(GSAd greystripeAd) {
        if (mGreystripeAd != null && mGreystripeAd.isAdReady()) {
            Log.d("MoPub", "Greysripe interstitial ad loaded successfully.");
            mInterstitialListener.onInterstitialLoaded();
        } else {
            mInterstitialListener.onInterstitialFailed(MoPubErrorCode.NETWORK_INVALID_STATE);
        }
    }

    @Override
    public void onAdCollapse(GSAd greystripeAd) {
    }

    @Override
    public void onAdExpansion(GSAd greystripeAd) {
    }
}
