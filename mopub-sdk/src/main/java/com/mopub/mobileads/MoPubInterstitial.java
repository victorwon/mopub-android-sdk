/*
 * Copyright (c) 2011, MoPub Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'MoPub Inc.' nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.mopub.mobileads;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import com.mopub.mobileads.BaseInterstitialAdapter.BaseInterstitialAdapterListener;
import com.mopub.mobileads.MoPubView.BannerAdListener;
import com.mopub.mobileads.MoPubView.LocationAwareness;

import java.util.Map;

public class MoPubInterstitial {
    
    private enum InterstitialState { HTML_AD_READY, NATIVE_AD_READY, NOT_READY };
    
    private MoPubInterstitialView mInterstitialView;
    private BaseInterstitialAdapter mInterstitialAdapter;
    private InterstitialAdListener mInterstitialAdListener;
    private Activity mActivity;
    private String mAdUnitId;
    private BaseInterstitialAdapterListener mAdapterListener;
    private DefaultInterstitialAdapterListener mDefaultAdapterListener;
    private InterstitialState mCurrentInterstitialState;
    private MoPubActivityBroadcastReceiver mMoPubActivityBroadcastReceiver;
    
    public interface InterstitialAdListener {
        public void onInterstitialLoaded(MoPubInterstitial interstitial);
        public void onInterstitialFailed(MoPubInterstitial interstitial, MoPubErrorCode errorCode);
        public void onInterstitialShown(MoPubInterstitial interstitial);
        public void onInterstitialDismissed(MoPubInterstitial interstitial);
    }
    
    private MoPubInterstitialListener mListener;
    
    @Deprecated
    public interface MoPubInterstitialListener {
        public void OnInterstitialLoaded();
        public void OnInterstitialFailed();
    }
    
    public MoPubInterstitial(Activity activity, String id) {
        mActivity = activity;
        mAdUnitId = id;
        
        mInterstitialView = new MoPubInterstitialView(mActivity);
        mInterstitialView.setAdUnitId(mAdUnitId);
        
        mInterstitialView.setBannerAdListener(new BannerAdListener() {
            @Override
            public void onBannerLoaded(MoPubView banner) {
                mCurrentInterstitialState = InterstitialState.HTML_AD_READY;
                if (mInterstitialAdapter != null) {
                    mInterstitialAdapter.invalidate();
                    mInterstitialAdapter = null;
                }
                
                if (mInterstitialAdListener != null) {
                    mInterstitialAdListener.onInterstitialLoaded(MoPubInterstitial.this);
                } else if (mListener != null) {
                    mListener.OnInterstitialLoaded();
                }
            }

            @Override
            public void onBannerFailed(MoPubView banner, MoPubErrorCode errorCode) {
                mCurrentInterstitialState = InterstitialState.NOT_READY;
                if (mInterstitialAdListener != null) {
                    mInterstitialAdListener.onInterstitialFailed(MoPubInterstitial.this, errorCode);
                } else if (mListener != null) {
                    mListener.OnInterstitialFailed();
                }
            }

            public void onBannerClicked(MoPubView banner) {}
            public void onBannerExpanded(MoPubView banner) {}
            public void onBannerCollapsed(MoPubView banner) {}
        });
        
        mCurrentInterstitialState = InterstitialState.NOT_READY;
        mDefaultAdapterListener = new DefaultInterstitialAdapterListener();
        mAdapterListener = mDefaultAdapterListener;
        
        mMoPubActivityBroadcastReceiver = new MoPubActivityBroadcastReceiver() {
            @Override
            void onHtmlInterstitialShown() {
                if (mInterstitialAdListener != null) {
                    mInterstitialAdListener.onInterstitialShown(MoPubInterstitial.this);
                }
            }

            @Override
            void onHtmlInterstitialDismissed() {
                mCurrentInterstitialState = InterstitialState.NOT_READY;
                if (mInterstitialAdListener != null) {
                    mInterstitialAdListener.onInterstitialDismissed(MoPubInterstitial.this);
                }
            }
        };
        
        // This IntentFilter contains HTML interstitial show and dismiss actions.
        LocalBroadcastManager.getInstance(mActivity).registerReceiver(mMoPubActivityBroadcastReceiver,
                MoPubActivity.HTML_INTERSTITIAL_INTENT_FILTER);
    }

    public void load() {
        invalidateCurrentInterstitial();
        mInterstitialView.loadAd();
    }
    
    public void forceRefresh() {
        invalidateCurrentInterstitial();
        mInterstitialView.forceRefresh();
    }
    
    private void invalidateCurrentInterstitial() {
        mCurrentInterstitialState = InterstitialState.NOT_READY;
        
        if (mInterstitialAdapter != null) {
            mInterstitialAdapter.invalidate();
            mInterstitialAdapter = null;
        }
        
        mAdapterListener = mDefaultAdapterListener;
    }
    
    public boolean isReady() {
        return (mCurrentInterstitialState == InterstitialState.HTML_AD_READY) || 
                (mCurrentInterstitialState == InterstitialState.NATIVE_AD_READY);
    }
    
    public boolean show() {
        switch (mCurrentInterstitialState) {
            case HTML_AD_READY: showHtmlInterstitial(); return true;
            case NATIVE_AD_READY: showNativeInterstitial(); return true;
            default: return false;
        }
    }
    
    private void showHtmlInterstitial() {
        String responseString = mInterstitialView.getResponseString();
        Intent i = new Intent(mActivity, MoPubActivity.class);
        i.putExtra("com.mopub.mobileads.AdUnitId", mAdUnitId);
        i.putExtra("com.mopub.mobileads.Keywords", mInterstitialView.getKeywords());
        i.putExtra("com.mopub.mobileads.Source", responseString);
        i.putExtra("com.mopub.mobileads.ClickthroughUrl", mInterstitialView.getClickthroughUrl());
        mActivity.startActivity(i);
    }
    
    private void showNativeInterstitial() {
        if (mInterstitialAdapter != null) mInterstitialAdapter.showInterstitial();
    }
    
    ////////////////////////////////////////////////////////////////////////////////////////////////
    
    public void setKeywords(String keywords) {
        if (mInterstitialView != null) mInterstitialView.setKeywords(keywords);
    }

    public String getKeywords() {
        return (mInterstitialView != null) ? mInterstitialView.getKeywords() : null;
    }
    
    public Activity getActivity() {
    	return mActivity;
    }
    
    public Location getLocation() {
        return mInterstitialView.getLocation();
    }
    
    public void destroy() {
        mAdapterListener = null;
        
        if (mInterstitialAdapter != null) {
            mInterstitialAdapter.invalidate();
            mInterstitialAdapter = null;
        }
        
        LocalBroadcastManager.getInstance(mActivity).unregisterReceiver(mMoPubActivityBroadcastReceiver);
        
        mInterstitialView.setBannerAdListener(null);
        mInterstitialView.destroy();
    }
    
    public void setInterstitialAdListener(InterstitialAdListener listener) {
        mInterstitialAdListener = listener;
    }
    
    public InterstitialAdListener getInterstitialAdListener() {
        return mInterstitialAdListener;
    }
    
    public void setLocationAwareness(LocationAwareness awareness) {
        mInterstitialView.setLocationAwareness(awareness);
    }

    public LocationAwareness getLocationAwareness() {
        return mInterstitialView.getLocationAwareness();
    }

    public void setLocationPrecision(int precision) {
        mInterstitialView.setLocationPrecision(precision);
    }

    public int getLocationPrecision() {
        return mInterstitialView.getLocationPrecision();
    }
    
    protected BaseInterstitialAdapterListener getInterstitialAdapterListener () {
        return mAdapterListener;
    }
    
    public void setTesting(boolean testing) {
        mInterstitialView.setTesting(testing);
    }
    
    public boolean getTesting() {
        return mInterstitialView.getTesting();
    }
    
    public void setLocalExtras(Map<String, Object> extras) {
        mInterstitialView.setLocalExtras(extras);
    }
    
    public Map<String, Object> getLocalExtras() {
        return mInterstitialView.getLocalExtras();
    }
    
    ////////////////////////////////////////////////////////////////////////////////////////////////
    
    public class DefaultInterstitialAdapterListener implements BaseInterstitialAdapterListener {
        public void onNativeInterstitialLoaded(BaseInterstitialAdapter adapter) {
            mCurrentInterstitialState = InterstitialState.NATIVE_AD_READY;
            mInterstitialView.trackImpression();
            
            if (mInterstitialAdListener != null) {
                mInterstitialAdListener.onInterstitialLoaded(MoPubInterstitial.this);
            } else if (mListener != null) {
                mListener.OnInterstitialLoaded();
            }
        }

        public void onNativeInterstitialFailed(BaseInterstitialAdapter adapter, MoPubErrorCode errorCode) {
            mCurrentInterstitialState = InterstitialState.NOT_READY;
            mInterstitialView.loadFailUrl(errorCode);
        }
        
        public void onNativeInterstitialShown(BaseInterstitialAdapter adapter) {
            if (mInterstitialAdListener != null) {
                mInterstitialAdListener.onInterstitialShown(MoPubInterstitial.this);
            }
        }

        public void onNativeInterstitialClicked(BaseInterstitialAdapter adapter) {
            mInterstitialView.registerClick();
        }
        
        public void onNativeInterstitialDismissed(BaseInterstitialAdapter adapter) {
            mCurrentInterstitialState = InterstitialState.NOT_READY;
            
            if (mInterstitialAdListener != null) {
                mInterstitialAdListener.onInterstitialDismissed(MoPubInterstitial.this);
            }
        }
        
        public void onNativeInterstitialExpired(BaseInterstitialAdapter adapter) {
            mCurrentInterstitialState = InterstitialState.NOT_READY;
        }
    }
    
    ////////////////////////////////////////////////////////////////////////////////////////////////
    
    public class MoPubInterstitialView extends MoPubView {
        
        public MoPubInterstitialView(Context context) {
            super(context);
            setAutorefreshEnabled(false);
        }
        
        @Override
        protected void loadNativeSDK(Map<String, String> paramsMap) {
            if (paramsMap == null) return;
            
            MoPubInterstitial interstitial = MoPubInterstitial.this;
            BaseInterstitialAdapterListener adapterListener = 
                interstitial.getInterstitialAdapterListener();
            String type = paramsMap.get("X-Adtype");
            
            if (type != null && (type.equals("interstitial") || type.equals("mraid"))) {
                String interstitialType = type.equals("interstitial") ? 
                        paramsMap.get("X-Fulladtype") : "mraid";
                
                Log.i("MoPub", "Loading native adapter for interstitial type: " + interstitialType);
                mInterstitialAdapter =
                        BaseInterstitialAdapter.getAdapterForType(interstitialType);
                
                if (mInterstitialAdapter != null) {
                    String jsonParams = paramsMap.get("X-Nativeparams");
                    mInterstitialAdapter.init(interstitial, jsonParams);
                    mInterstitialAdapter.setAdapterListener(adapterListener);
                    mInterstitialAdapter.loadInterstitial();
                    return;
                }
            }
            
            Log.i("MoPub", "Couldn't load native adapter. Trying next ad...");
            adapterListener.onNativeInterstitialFailed(null, MoPubErrorCode.ADAPTER_NOT_FOUND);
        }
        
        @Override
        protected void loadCustomEvent(Map<String, String> paramsMap) {
            if (mInterstitialAdapter != null) mInterstitialAdapter.invalidate();
            
            BaseInterstitialAdapterListener adapterListener = getInterstitialAdapterListener();
            
            mInterstitialAdapter = BaseInterstitialAdapter.getAdapterForType("custom_event");
            
            if (mInterstitialAdapter != null) {
                Log.i("MoPub", "Loading custom event interstitial adapter.");
                
                // Get the className and classData from the passed in paramsMap.
                String className = paramsMap.get("X-Custom-Event-Class-Name");
                String classData = paramsMap.get("X-Custom-Event-Class-Data");
                
                ((CustomEventInterstitialAdapter) mInterstitialAdapter)
                        .init(MoPubInterstitial.this, className, classData);
                mInterstitialAdapter.setAdapterListener(adapterListener);
                mInterstitialAdapter.loadInterstitial();
            } else {
                Log.i("MoPub", "Couldn't load custom event interstitial adapter. Trying next ad...");
                adapterListener.onNativeInterstitialFailed(null, MoPubErrorCode.ADAPTER_NOT_FOUND);
            }
        }
        
        protected void trackImpression() {
            Log.d("MoPub", "Tracking impression for interstitial.");
            if (mAdView != null) mAdView.trackImpression();
        }
    }
    
    @Deprecated
    public void setListener(MoPubInterstitialListener listener) {
        mListener = listener;
    }
    
    @Deprecated
    public MoPubInterstitialListener getListener() {
        return mListener;
    }
    
    @Deprecated
    public void customEventDidLoadAd() {
        if (mInterstitialView != null) mInterstitialView.trackImpression();
    }

    @Deprecated
    public void customEventDidFailToLoadAd() {
        if (mInterstitialView != null) mInterstitialView.loadFailUrl(MoPubErrorCode.UNSPECIFIED);
    }

    @Deprecated
    public void customEventActionWillBegin() {
        if (mInterstitialView != null) mInterstitialView.registerClick();
    }
}
