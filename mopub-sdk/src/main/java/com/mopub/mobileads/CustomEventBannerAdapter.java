package com.mopub.mobileads;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import com.mopub.mobileads.CustomEventBanner.CustomEventBannerListener;
import com.mopub.mobileads.factories.CustomEventBannerFactory;

import java.util.HashMap;
import java.util.Map;

import static com.mopub.mobileads.MoPubErrorCode.ADAPTER_NOT_FOUND;
import static com.mopub.mobileads.MoPubErrorCode.NETWORK_TIMEOUT;
import static com.mopub.mobileads.MoPubErrorCode.UNSPECIFIED;

class CustomEventBannerAdapter extends BaseAdapter implements CustomEventBannerListener {
    private Context mContext;
    private CustomEventBanner mCustomEventBanner;
    private Map<String, Object> mLocalExtras;
    private Map<String, String> mServerExtras;
    private final Handler mHandler;
    private final Runnable mTimeout;

    CustomEventBannerAdapter() {
        mLocalExtras = new HashMap<String, Object>();
        mServerExtras = new HashMap<String, String>();
        mHandler = new Handler();

        mTimeout = new Runnable() {
            @Override
            public void run() {
                Log.d("MoPub", "Third-party network timed out.");
                onBannerFailed(NETWORK_TIMEOUT);
                invalidate();
            }
        };
    }

    @Override
    void init(MoPubView moPubView, String className) {
        init(moPubView, className, null);
    }
    
    void init(MoPubView moPubView, String className, String jsonParams) {
        super.init(moPubView, jsonParams);
        
        mContext = moPubView.getContext();
        
        Log.d("MoPub", "Attempting to invoke custom event: " + className);
        
        try {
            mCustomEventBanner = CustomEventBannerFactory.create(className);
        } catch (Exception exception) {
            Log.d("MoPub", "Couldn't locate or instantiate custom event: " + className + ".");
            mMoPubView.loadFailUrl(ADAPTER_NOT_FOUND);
            return;
        }
        
        // Attempt to load the JSON extras into mServerExtras.
        try {
            mServerExtras = Utils.jsonStringToMap(jsonParams);
        } catch (Exception exception) {
            Log.d("MoPub", "Failed to create Map from JSON: " + jsonParams + exception.toString());
        }
        
        mLocalExtras = mMoPubView.getLocalExtras();
        if (mMoPubView.getLocation() != null) mLocalExtras.put("location", mMoPubView.getLocation());
    }
    
    @Override
    void loadAd() {
        if (isInvalidated() || mCustomEventBanner == null) return;

        mHandler.postDelayed(mTimeout, TIMEOUT_DELAY);
        mCustomEventBanner.loadBanner(mContext, this, mLocalExtras, mServerExtras);
    }

    @Override
    void invalidate() {
        if (mCustomEventBanner != null) mCustomEventBanner.onInvalidate();
        mContext = null;
        mCustomEventBanner = null;
        mLocalExtras = null;
        mServerExtras = null;
        super.invalidate();
    }

    private void cancelTimeout() {
        mHandler.removeCallbacks(mTimeout);
    }

    /*
     * CustomEventBanner.Listener implementation
     */
    @Override
    public void onBannerLoaded(View bannerView) {
        if (isInvalidated()) return;
        
        if (mMoPubView != null) {
            cancelTimeout();
            mMoPubView.nativeAdLoaded();
            mMoPubView.setAdContentView(bannerView);
            mMoPubView.trackNativeImpression();
        }
    }

    @Override
    public void onBannerFailed(MoPubErrorCode errorCode) {
        if (isInvalidated()) return;
        
        if (mMoPubView != null) {
            if (errorCode == null) {
                errorCode = UNSPECIFIED;
            }
            cancelTimeout();
            mMoPubView.loadFailUrl(errorCode);
        }
    }

    @Override
    public void onBannerClicked() {
        if (isInvalidated()) return;
        
        if (mMoPubView != null) mMoPubView.registerClick();
    }
    
    @Override
    public void onLeaveApplication() {
        onBannerClicked();
    }
}
