package com.mopub.mobileads;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import com.mopub.mobileads.CustomEventInterstitial.CustomEventInterstitialListener;
import com.mopub.mobileads.factories.CustomEventInterstitialFactory;

import java.util.HashMap;
import java.util.Map;

import static com.mopub.mobileads.MoPubErrorCode.ADAPTER_NOT_FOUND;
import static com.mopub.mobileads.MoPubErrorCode.NETWORK_TIMEOUT;
import static com.mopub.mobileads.MoPubErrorCode.UNSPECIFIED;

class CustomEventInterstitialAdapter extends BaseInterstitialAdapter implements CustomEventInterstitialListener {
    private CustomEventInterstitial mCustomEventInterstitial;
    private Context mContext;
    private Map<String, Object> mLocalExtras;
    private Map<String, String> mServerExtras;
    private final Handler mHandler;
    private final Runnable mTimeout;

    CustomEventInterstitialAdapter() {
        mHandler = new Handler();
        mServerExtras = new HashMap<String, String>();
        mLocalExtras = new HashMap<String, Object>();

        mTimeout = new Runnable() {
            @Override
            public void run() {
                Log.d("MoPub", "Third-party network timed out.");
                onInterstitialFailed(NETWORK_TIMEOUT);
                invalidate();
            }
        };
    }

    @Override
    void init(MoPubInterstitial moPubInterstitial, String className) {
        init(moPubInterstitial, className, null);
    }
    
    void init(MoPubInterstitial moPubInterstitial, String className, String jsonParams) {
        super.init(moPubInterstitial, jsonParams);
        
        mContext = moPubInterstitial.getActivity();
        
        Log.d("MoPub", "Attempting to invoke custom event: " + className);
        
        try {
            mCustomEventInterstitial = CustomEventInterstitialFactory.create(className);
        } catch (Exception exception) {
            Log.d("MoPub", "Couldn't locate or instantiate custom event: " + className + ".");
            if (mAdapterListener != null) mAdapterListener.onNativeInterstitialFailed(this, ADAPTER_NOT_FOUND);
        }
        
        // Attempt to load the JSON extras into mServerExtras.
        try {
            mServerExtras = Utils.jsonStringToMap(jsonParams);
        } catch (Exception exception) {
            Log.d("MoPub", "Failed to create Map from JSON: " + jsonParams);
        }
        
        mLocalExtras = mInterstitial.getLocalExtras();
        if (mInterstitial.getLocation() != null) mLocalExtras.put("location", mInterstitial.getLocation());
    }
    
    @Override
    void loadInterstitial() {
        if (isInvalidated() || mCustomEventInterstitial == null) return;

        mHandler.postDelayed(mTimeout, TIMEOUT_DELAY);
        mCustomEventInterstitial.loadInterstitial(mContext, this, mLocalExtras, mServerExtras);
    }
    
    @Override
    void showInterstitial() {
        if (isInvalidated() || mCustomEventInterstitial == null) return;
        
        mCustomEventInterstitial.showInterstitial();
    }

    @Override
    void invalidate() {
        if (mCustomEventInterstitial != null) mCustomEventInterstitial.onInvalidate();
        mCustomEventInterstitial = null;
        mContext = null;
        mServerExtras = null;
        mLocalExtras = null;
        super.invalidate();
    }

    private void cancelTimeout() {
        mHandler.removeCallbacks(mTimeout);
    }

    /*
     * CustomEventInterstitial.Listener implementation
     */
    @Override
    public void onInterstitialLoaded() {
        if (isInvalidated()) return;
        
        if (mAdapterListener != null) {
            cancelTimeout();
            mAdapterListener.onNativeInterstitialLoaded(this);
        }
    }

    @Override
    public void onInterstitialFailed(MoPubErrorCode errorCode) {
        if (isInvalidated()) return;
        
        if (mAdapterListener != null) {
            if (errorCode == null) {
                errorCode = UNSPECIFIED;
            }
            cancelTimeout();
            mAdapterListener.onNativeInterstitialFailed(this, errorCode);
        }
    }
    
    @Override
    public void onInterstitialShown() {
        if (isInvalidated()) return;
        
        if (mAdapterListener != null) mAdapterListener.onNativeInterstitialShown(this);
    }

    @Override
    public void onInterstitialClicked() {
        if (isInvalidated()) return;
        
        if (mAdapterListener != null) mAdapterListener.onNativeInterstitialClicked(this);
    }

    @Override
    public void onLeaveApplication() {
        onInterstitialClicked();
    }

    @Override
    public void onInterstitialDismissed() {
        if (isInvalidated()) return;
        
        if (mAdapterListener != null) mAdapterListener.onNativeInterstitialDismissed(this);
    }
}
