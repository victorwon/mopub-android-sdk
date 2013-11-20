package com.mopub.simpleadsdemo;

import java.util.Map;

import android.content.Context;
import android.util.Log;

import com.greystripe.sdk.GSAd;
import com.greystripe.sdk.GSAdErrorCode;
import com.greystripe.sdk.GSAdListener;
import com.greystripe.sdk.GSMobileBannerAdView;
import com.mopub.mobileads.CustomEventBanner;
import com.mopub.mobileads.MoPubErrorCode;

/*
 * Tested with Greystripe SDK 2.3.0.
 */
class GreystripeBanner extends CustomEventBanner implements GSAdListener {
    private CustomEventBannerListener mBannerListener;
    private GSMobileBannerAdView mGreystripeAd;

    /*
     * Abstract methods from CustomEventBanner
     */
    @Override
    protected void loadBanner(Context context, CustomEventBannerListener bannerListener,
                              Map<String, Object> localExtras, Map<String, String> serverExtras) {
        mBannerListener = bannerListener;

        /*
         * You may also pass this String down in the serverExtras Map by specifying Custom Event Data
         * in MoPub's web interface.
         */
        String greystripeAppId = "YOUR_GREYSTRIPE_APP_ID";
        mGreystripeAd = new GSMobileBannerAdView(context, greystripeAppId);
        mGreystripeAd.addListener(this);

        mGreystripeAd.refresh();
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
        Log.d("MoPub", "Greystripe banner ad clicked.");
        mBannerListener.onBannerClicked();
    }

    @Override
    public void onAdDismissal(GSAd greystripeAd) {
        Log.d("MoPub", "Greystripe banner ad modal dismissed.");
    }

    @Override
    public void onFailedToFetchAd(GSAd greystripeAd, GSAdErrorCode errorCode) {
        Log.d("MoPub", "Greystripe banner ad failed to load.");
        mBannerListener.onBannerFailed(MoPubErrorCode.NETWORK_NO_FILL);
    }

    @Override
    public void onFetchedAd(GSAd greystripeAd) {
        if (mGreystripeAd != null & mGreystripeAd.isAdReady()) {
            Log.d("MoPub", "Greystripe banner ad loaded successfully. Showing ad...");
            mBannerListener.onBannerLoaded(mGreystripeAd);
        } else {
            mBannerListener.onBannerFailed(MoPubErrorCode.NETWORK_INVALID_STATE);
        }
    }

    @Override
    public void onAdCollapse(GSAd greystripeAd) {
    }

    @Override
    public void onAdExpansion(GSAd greystripeAd) {
    }
}
