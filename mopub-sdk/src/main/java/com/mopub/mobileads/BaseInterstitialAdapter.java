package com.mopub.mobileads;

import android.util.Log;

import java.lang.reflect.Constructor;
import java.util.HashMap;

abstract class BaseInterstitialAdapter {
    public static final int TIMEOUT_DELAY = 30000;
    protected boolean mInvalidated;
    protected MoPubInterstitial mInterstitial;
    protected String mJsonParams;
    protected BaseInterstitialAdapterListener mAdapterListener;
    
    public interface BaseInterstitialAdapterListener {
        public void onNativeInterstitialLoaded(BaseInterstitialAdapter adapter);
        public void onNativeInterstitialFailed(BaseInterstitialAdapter adapter, MoPubErrorCode errorCode);
        public void onNativeInterstitialShown(BaseInterstitialAdapter adapter);
        public void onNativeInterstitialClicked(BaseInterstitialAdapter adapter);
        public void onNativeInterstitialDismissed(BaseInterstitialAdapter adapter);
        public void onNativeInterstitialExpired(BaseInterstitialAdapter adapter);
    }
    
    private static final HashMap<String, String> sInterstitialAdapterMap;
    static {
        sInterstitialAdapterMap = new HashMap<String, String>();
        sInterstitialAdapterMap.put("mraid", "com.mopub.mobileads.MraidInterstitialAdapter");
        sInterstitialAdapterMap.put("custom_event", "com.mopub.mobileads.CustomEventInterstitialAdapter");
    }
    
    abstract void loadInterstitial();
    abstract void showInterstitial();
    
    void init(MoPubInterstitial interstitial, String jsonParams) {
        mInterstitial = interstitial;
        mJsonParams = jsonParams;
        mInvalidated = false;
    }
    
    void invalidate() {
        mInterstitial = null;
        mAdapterListener = null;
        mInvalidated = true;
    }
    
    boolean isInvalidated() {
        return mInvalidated;
    }
    
    void setAdapterListener(BaseInterstitialAdapterListener listener) {
        mAdapterListener = listener;
    }
    
    static BaseInterstitialAdapter getAdapterForType(String type) {
        if (type == null) return null;
        
        Class<?> adapterClass = classForAdapterType(type);
        if (adapterClass == null) return null;
        
        try {
            Constructor<?> constructor = adapterClass.getDeclaredConstructor();
            constructor.setAccessible(true);
            BaseInterstitialAdapter nativeAdapter = 
                    (BaseInterstitialAdapter) constructor.newInstance();
            return nativeAdapter;
        } catch (Exception e) {
            Log.d("MoPub", "Couldn't create native interstitial adapter for type: " + type);
            return null;
        }
    }
        
    private static String classStringForAdapterType(String type) {
        return sInterstitialAdapterMap.get(type);
    }
        
    private static Class<?> classForAdapterType(String type) {
        String className = classStringForAdapterType(type);
        if (className == null) {
            Log.d("MoPub", "Couldn't find a handler for this ad type: " + type + "."
            + " MoPub for Android does not support it at this time.");
            return null;
        }
        
        try {
            return (Class<?>) Class.forName(className);
        } catch (ClassNotFoundException e) {
            Log.d("MoPub", "Couldn't find " + className + "class."
            + " Make sure the project includes the adapter library for " + className
            + " from the extras folder");
            return null;
        }
    }
}
