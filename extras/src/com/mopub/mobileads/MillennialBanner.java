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

import android.content.Context;
import android.location.Location;
import android.util.Log;
import com.millennialmedia.android.*;

import java.util.Map;

/**
 * Compatible with version 5.0.0 of the Millennial Media SDK.
 */

class MillennialBanner extends CustomEventBanner {
    private MMAdView mMillennialAdView;
    private CustomEventBannerListener bannerListener;
    private String apid;
    private int adWidth;
    private int adHeight;
    public static final String APID_KEY = "adUnitID";
    public static final String AD_WIDTH_KEY = "adWidth";
    public static final String AD_HEIGHT_KEY = "adHeight";

    @Override
    protected void loadBanner(Context context, CustomEventBannerListener customEventBannerListener,
                              Map<String, Object> localExtras, Map<String, String> serverExtras) {
        bannerListener = customEventBannerListener;

        if (extrasAreValid(serverExtras)) {
            apid = serverExtras.get(APID_KEY);
            adWidth = Integer.parseInt(serverExtras.get(AD_WIDTH_KEY));
            adHeight = Integer.parseInt(serverExtras.get(AD_HEIGHT_KEY));
        } else {
            bannerListener.onBannerFailed(MoPubErrorCode.ADAPTER_CONFIGURATION_ERROR);
            return;
        }

        MMSDK.initialize(context);
        MMSDK.setBroadcastEvents(true);

        mMillennialAdView = new MMAdView(context);
        mMillennialAdView.setApid(apid);
        mMillennialAdView.setWidth(adWidth);
        mMillennialAdView.setHeight(adHeight);

        Location location = (Location) localExtras.get("location");
        if (location != null) MMRequest.setUserLocation(location);

        mMillennialAdView.setMMRequest(new MMRequest());
        mMillennialAdView.setId(MMSDK.getDefaultAdId());
        mMillennialAdView.getAd(new MillennialRequestListener());
    }

    private boolean extrasAreValid(Map<String, String> serverExtras) {
        try {
            Integer.parseInt(serverExtras.get(AD_WIDTH_KEY));
            Integer.parseInt(serverExtras.get(AD_HEIGHT_KEY));
        } catch (NumberFormatException e) {
            return false;
        }

        return serverExtras.containsKey(APID_KEY);
    }

    @Override
    protected void onInvalidate() {
        mMillennialAdView.setListener(null);
    }

    class MillennialRequestListener implements RequestListener {
        @Override
        public void MMAdOverlayLaunched(MMAd mmAd) {
            Log.d("MoPub", "Millennial banner ad clicked.");
            bannerListener.onBannerClicked();
        }

        @Override
        public void MMAdRequestIsCaching(MMAd mmAd) {
        }

        @Override
        public void requestCompleted(MMAd mmAd) {
            Log.d("MoPub", "Millennial banner ad loaded successfully. Showing ad...");
            bannerListener.onBannerLoaded(mMillennialAdView);
        }

        @Override
        public void requestFailed(MMAd mmAd, MMException e) {
            Log.d("MoPub", "Millennial banner ad failed to load.");
            bannerListener.onBannerFailed(MoPubErrorCode.NETWORK_NO_FILL);
        }
    }
}
