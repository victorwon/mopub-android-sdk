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
import com.mopub.mobileads.MoPubView.BannerAdListener;
import com.mopub.mobileads.MoPubView.LocationAwareness;
import com.mopub.mobileads.factories.CustomEventInterstitialAdapterFactory;

import java.util.Map;

import static com.mopub.mobileads.AdFetcher.CUSTOM_EVENT_DATA_HEADER;
import static com.mopub.mobileads.AdFetcher.CUSTOM_EVENT_NAME_HEADER;
import static com.mopub.mobileads.BaseActivity.SOURCE_KEY;
import static com.mopub.mobileads.MoPubActivity.AD_UNIT_ID_KEY;
import static com.mopub.mobileads.MoPubActivity.CLICKTHROUGH_URL_KEY;
import static com.mopub.mobileads.MoPubActivity.KEYWORDS_KEY;

public class MoPubInterstitial implements CustomEventInterstitialAdapter.CustomEventInterstitialAdapterListener {

    private enum InterstitialState {
        HTML_AD_READY,
        CUSTOM_EVENT_AD_READY,
        NOT_READY;

        boolean isReady() {
            return this != InterstitialState.NOT_READY;
        }
    }

    private MoPubInterstitialView mInterstitialView;
    private CustomEventInterstitialAdapter mCustomEventInterstitialAdapter;
    private InterstitialAdListener mInterstitialAdListener;
    private Activity mActivity;
    private String mAdUnitId;
    private InterstitialState mCurrentInterstitialState;
    private BaseActivityBroadcastReceiver mBaseActivityBroadcastReceiver;
    private boolean mIsDestroyed;

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

        mInterstitialView.setBannerAdListener(new MoPubInterstitialBannerListener());
        
        mCurrentInterstitialState = InterstitialState.NOT_READY;

        mBaseActivityBroadcastReceiver = new MoPubInterstitialBroadcastReceiver();
        
        // This IntentFilter contains HTML interstitial show and dismiss actions.
        LocalBroadcastManager.getInstance(mActivity).registerReceiver(mBaseActivityBroadcastReceiver,
                BaseActivity.HTML_INTERSTITIAL_INTENT_FILTER);
    }

    public void load() {
        resetCurrentInterstitial();
        mInterstitialView.loadAd();
    }
    
    public void forceRefresh() {
        resetCurrentInterstitial();
        mInterstitialView.forceRefresh();
    }
    
    private void resetCurrentInterstitial() {
        mCurrentInterstitialState = InterstitialState.NOT_READY;
        
        if (mCustomEventInterstitialAdapter != null) {
            mCustomEventInterstitialAdapter.invalidate();
            mCustomEventInterstitialAdapter = null;
        }
        
        mIsDestroyed = false;
    }
    
    public boolean isReady() {
        return mCurrentInterstitialState.isReady();
    }

    boolean isDestroyed() {
        return mIsDestroyed;
    }
    
    public boolean show() {
        switch (mCurrentInterstitialState) {
            case HTML_AD_READY:
                showHtmlInterstitial();
                return true;
            case CUSTOM_EVENT_AD_READY:
                showCustomEventInterstitial();
                return true;
        }
        return false;
    }
    
    private void showHtmlInterstitial() {
        String responseString = mInterstitialView.getResponseString();
        Intent i = new Intent(mActivity, MoPubActivity.class);
        i.putExtra(AD_UNIT_ID_KEY, mAdUnitId);
        i.putExtra(KEYWORDS_KEY, mInterstitialView.getKeywords());
        i.putExtra(SOURCE_KEY, responseString);
        i.putExtra(CLICKTHROUGH_URL_KEY, mInterstitialView.getClickthroughUrl());
        mActivity.startActivity(i);
    }
    
    private void showCustomEventInterstitial() {
        if (mCustomEventInterstitialAdapter != null) mCustomEventInterstitialAdapter.showInterstitial();
    }
    
    ////////////////////////////////////////////////////////////////////////////////////////////////
    
    public void setKeywords(String keywords) {
        mInterstitialView.setKeywords(keywords);
    }

    public String getKeywords() {
        return mInterstitialView.getKeywords();
    }
    
    public Activity getActivity() {
    	return mActivity;
    }
    
    public Location getLocation() {
        return mInterstitialView.getLocation();
    }
    
    public void destroy() {
        mIsDestroyed = true;

        if (mCustomEventInterstitialAdapter != null) {
            mCustomEventInterstitialAdapter.invalidate();
            mCustomEventInterstitialAdapter = null;
        }
        
        LocalBroadcastManager.getInstance(mActivity).unregisterReceiver(mBaseActivityBroadcastReceiver);
        
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
    
    /*
     * Implements CustomEventInterstitialAdapter.CustomEventInterstitialListener
     */

    @Override
    public void onCustomEventInterstitialLoaded() {
        if (mIsDestroyed) return;

        mCurrentInterstitialState = InterstitialState.CUSTOM_EVENT_AD_READY;
        mInterstitialView.trackImpression();

        if (mInterstitialAdListener != null) {
            mInterstitialAdListener.onInterstitialLoaded(MoPubInterstitial.this);
        } else if (mListener != null) {
            mListener.OnInterstitialLoaded();
        }
    }

    @Override
    public void onCustomEventInterstitialFailed(MoPubErrorCode errorCode) {
        if (isDestroyed()) return;

        mCurrentInterstitialState = InterstitialState.NOT_READY;
        mInterstitialView.loadFailUrl(errorCode);
    }

    @Override
    public void onCustomEventInterstitialShown() {
        if (isDestroyed()) return;

        if (mInterstitialAdListener != null) {
            mInterstitialAdListener.onInterstitialShown(MoPubInterstitial.this);
        }
    }

    @Override
    public void onCustomEventInterstitialClicked() {
        if (isDestroyed()) return;

        mInterstitialView.registerClick();
    }

    @Override
    public void onCustomEventInterstitialDismissed() {
        if (isDestroyed()) return;

        mCurrentInterstitialState = InterstitialState.NOT_READY;

        if (mInterstitialAdListener != null) {
            mInterstitialAdListener.onInterstitialDismissed(MoPubInterstitial.this);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    
    public class MoPubInterstitialView extends MoPubView {
        
        public MoPubInterstitialView(Context context) {
            super(context);
            setAutorefreshEnabled(false);
        }

        @Override
        protected void loadCustomEvent(Map<String, String> paramsMap) {
            if (mCustomEventInterstitialAdapter != null) mCustomEventInterstitialAdapter.invalidate();

            Log.i("MoPub", "Loading custom event interstitial adapter.");

            mCustomEventInterstitialAdapter = CustomEventInterstitialAdapterFactory.create(
                    MoPubInterstitial.this,
                    paramsMap.get(CUSTOM_EVENT_NAME_HEADER),
                    paramsMap.get(CUSTOM_EVENT_DATA_HEADER));
            mCustomEventInterstitialAdapter.setAdapterListener(MoPubInterstitial.this);
            mCustomEventInterstitialAdapter.loadInterstitial();
        }
        
        protected void trackImpression() {
            Log.d("MoPub", "Tracking impression for interstitial.");
            if (mAdViewController != null) mAdViewController.trackImpression();
        }
    }

    class MoPubInterstitialBannerListener implements BannerAdListener {
        @Override
        public void onBannerLoaded(MoPubView ignored) {
            mCurrentInterstitialState = InterstitialState.HTML_AD_READY;
            if (mCustomEventInterstitialAdapter != null) {
                mCustomEventInterstitialAdapter.invalidate();
                mCustomEventInterstitialAdapter = null;
            }

            if (mInterstitialAdListener != null) {
                mInterstitialAdListener.onInterstitialLoaded(MoPubInterstitial.this);
            } else if (mListener != null) {
                mListener.OnInterstitialLoaded();
            }
        }

        @Override
        public void onBannerFailed(MoPubView ignored, MoPubErrorCode errorCode) {
            mCurrentInterstitialState = InterstitialState.NOT_READY;
            if (mInterstitialAdListener != null) {
                mInterstitialAdListener.onInterstitialFailed(MoPubInterstitial.this, errorCode);
            } else if (mListener != null) {
                mListener.OnInterstitialFailed();
            }
        }

        @Override public void onBannerClicked(MoPubView banner) {}
        @Override public void onBannerExpanded(MoPubView banner) {}
        @Override public void onBannerCollapsed(MoPubView banner) {}
    }

    class MoPubInterstitialBroadcastReceiver extends BaseActivityBroadcastReceiver {
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
    }

    @Deprecated // for testing
    void setInterstitialView(MoPubInterstitialView interstitialView) {
        mInterstitialView = interstitialView;
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
