package com.mopub.mobileads;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import com.inmobi.androidsdk.IMAdInterstitial;
import com.inmobi.androidsdk.IMAdInterstitialListener;
import com.inmobi.androidsdk.IMAdRequest;
import com.inmobi.androidsdk.IMAdRequest.ErrorCode;
import com.mopub.mobileads.CustomEventInterstitial;
import com.mopub.mobileads.MoPubErrorCode;

import java.util.HashMap;
import java.util.Map;

/*
 * Tested with InMobi SDK 3.7.0.
 */
class InMobiInterstitial extends CustomEventInterstitial implements IMAdInterstitialListener {
    public static final String APP_ID_KEY = "appId";

    private CustomEventInterstitialListener mInterstitialListener;
    private IMAdInterstitial mInMobiInterstitial;

    /*
     * Abstract methods from CustomEventInterstitial
     */
    @Override
    protected void loadInterstitial(Context context, CustomEventInterstitialListener interstitialListener,
            Map<String, Object> localExtras, Map<String, String> serverExtras) {
        mInterstitialListener = interstitialListener;
        
        Activity activity = null;
        if (context instanceof Activity) {
            activity = (Activity) context;
        } else {
            // You may also pass in an Activity Context in the localExtras map and retrieve it here.
        }
        
        if (activity == null) {
            mInterstitialListener.onInterstitialFailed(MoPubErrorCode.ADAPTER_CONFIGURATION_ERROR);
            return;
        }
        
        /*
         * You may also pass this String down in the serverExtras Map by specifying Custom Event Data
         * in MoPub's web interface.
         */
        String inMobiAppId = serverExtras.get(APP_ID_KEY);
        mInMobiInterstitial = new IMAdInterstitial(activity, inMobiAppId);
        
        mInMobiInterstitial.setIMAdInterstitialListener(this);

        IMAdRequest imAdRequest = new IMAdRequest();
        Map<String, String> requestParameters = new HashMap<String, String>();
        requestParameters.put("tp", "c_mopub");
        imAdRequest.setRequestParams(requestParameters);

        mInMobiInterstitial.loadNewAd(imAdRequest);
    }
    
    @Override
    protected void showInterstitial() {
        Log.d("MoPub", "Showing InMobi interstitial ad.");
        mInMobiInterstitial.show();
    }

    @Override
    protected void onInvalidate() {
        mInMobiInterstitial.setIMAdInterstitialListener(null);
    }

    /*
     * IMAdListener implementation
     */
    @Override
    public void onAdRequestLoaded(IMAdInterstitial adInterstitial) {
        Log.d("MoPub", "InMobi interstitial ad loaded successfully.");
        mInterstitialListener.onInterstitialLoaded();
    }
    
    @Override
    public void onAdRequestFailed(IMAdInterstitial adInterstitial, ErrorCode errorCode) {
        Log.d("MoPub", "InMobi interstitial ad failed to load.");
        mInterstitialListener.onInterstitialFailed(MoPubErrorCode.NETWORK_NO_FILL);
    }

    @Override
    public void onShowAdScreen(IMAdInterstitial adInterstitial) {
        mInterstitialListener.onInterstitialShown();
    }
    
    @Override
    public void onLeaveApplication(IMAdInterstitial adInterstitial) {
        /*
         * Because InMobi does not have an onClick equivalent, we use onLeaveApplication
         * as a click notification.
         */
        Log.d("MoPub", "InMobi interstitial ad leaving application.");
        mInterstitialListener.onInterstitialClicked();
    }

    @Override
    public void onDismissAdScreen(IMAdInterstitial adInterstitial) {
        Log.d("MoPub", "InMobi interstitial ad dismissed.");
        mInterstitialListener.onInterstitialDismissed();
    }
}
