package com.mopub.simpleadsdemo;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import com.inmobi.androidsdk.IMAdListener;
import com.inmobi.androidsdk.IMAdRequest;
import com.inmobi.androidsdk.IMAdRequest.ErrorCode;
import com.inmobi.androidsdk.IMAdView;
import com.mopub.mobileads.CustomEventBanner;
import com.mopub.mobileads.MoPubErrorCode;

import java.util.HashMap;
import java.util.Map;

/*
 * Tested with InMobi SDK 3.7.0.
 */
class InMobiBanner extends CustomEventBanner implements IMAdListener {
    private CustomEventBannerListener mBannerListener;
    private IMAdView mInMobiBanner;

    /*
     * Abstract methods from CustomEventBanner
     */
    @Override
    protected void loadBanner(Context context, CustomEventBannerListener bannerListener,
            Map<String, Object> localExtras, Map<String, String> serverExtras) {
        mBannerListener = bannerListener;
        
        Activity activity = null;
        if (context instanceof Activity) {
            activity = (Activity) context;
        } else {
            // You may also pass in an Activity Context in the localExtras map and retrieve it here.
        }
        
        if (activity == null) {
            mBannerListener.onBannerFailed(MoPubErrorCode.ADAPTER_CONFIGURATION_ERROR);
            return;
        }
        
        /*
         * You may also pass this String down in the serverExtras Map by specifying Custom Event Data
         * in MoPub's web interface.
         */
        String inMobiAppId = "YOUR_INMOBI_APP_ID";
        mInMobiBanner = new IMAdView(activity, IMAdView.INMOBI_AD_UNIT_320X50, inMobiAppId);
        
        mInMobiBanner.setIMAdListener(this);

        IMAdRequest imAdRequest = new IMAdRequest();
        Map<String, String> requestParameters = new HashMap<String, String>();
        requestParameters.put("tp", "c_mopub");
        imAdRequest.setRequestParams(requestParameters);

        mInMobiBanner.loadNewAd(imAdRequest);
    }

    @Override
    protected void onInvalidate() {
        mInMobiBanner.setIMAdListener(null);
    }

    /*
     * IMAdListener implementation
     */
    @Override
    public void onAdRequestCompleted(IMAdView adView) {
        if (mInMobiBanner != null) {
            Log.d("MoPub", "InMobi banner ad loaded successfully. Showing ad...");
            mBannerListener.onBannerLoaded(mInMobiBanner);
        } else {
            mBannerListener.onBannerFailed(MoPubErrorCode.NETWORK_INVALID_STATE);
        }
    }

    @Override
    public void onAdRequestFailed(IMAdView adView, ErrorCode errorCode) {
        Log.d("MoPub", "InMobi banner ad failed to load.");
        mBannerListener.onBannerFailed(MoPubErrorCode.NETWORK_NO_FILL);
    }

    @Override
    public void onDismissAdScreen(IMAdView adView) {
        Log.d("MoPub", "InMobi banner ad modal dismissed.");
    }

    @Override
    public void onLeaveApplication(IMAdView adView) {
        /*
         * Because InMobi does not have an onClick equivalent, we use onLeaveApplication
         * as a click notification.
         */
        Log.d("MoPub", "InMobi banner ad leaving application.");
        mBannerListener.onBannerClicked();
    }

    @Override
    public void onShowAdScreen(IMAdView adView) {
        Log.d("MoPub", "InMobi banner ad modal shown.");
    }
}
