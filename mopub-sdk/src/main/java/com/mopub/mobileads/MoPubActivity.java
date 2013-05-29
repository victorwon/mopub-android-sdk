/*
 * Copyright (c) 2010, MoPub Inc.
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

import android.view.View;
import com.mopub.mobileads.MoPubView.BannerAdListener;
import com.mopub.mobileads.factories.MoPubViewFactory;

public class MoPubActivity extends BaseActivity {
    public static final String AD_UNIT_ID_KEY = "com.mopub.mobileads.AdUnitId";
    public static final String KEYWORDS_KEY = "com.mopub.mobileads.Keywords";
    public static final String CLICKTHROUGH_URL_KEY = "com.mopub.mobileads.ClickthroughUrl";
    public static final String TIMEOUT_KEY = "com.mopub.mobileads.Timeout";

    private MoPubView mMoPubView;
    
    @Override
    public View getAdView() {
        String adUnitId = getIntent().getStringExtra(AD_UNIT_ID_KEY);
        String keywords = getIntent().getStringExtra(KEYWORDS_KEY);
        String clickthroughUrl = getIntent().getStringExtra(CLICKTHROUGH_URL_KEY);
        int timeout = getIntent().getIntExtra(TIMEOUT_KEY, 0);
        
        if (adUnitId == null) {
            throw new RuntimeException("AdUnitId isn't set in " + getClass().getCanonicalName());
        }
        
        mMoPubView = MoPubViewFactory.create(this);
        mMoPubView.setAdUnitId(adUnitId);
        mMoPubView.setKeywords(keywords);
        mMoPubView.setClickthroughUrl(clickthroughUrl);
        mMoPubView.setTimeout(timeout);
        
        mMoPubView.setBannerAdListener(new BannerAdListener() {
            @Override
            public void onBannerLoaded(MoPubView banner) {
                mMoPubView.adAppeared();
            }
            public void onBannerFailed(MoPubView banner, MoPubErrorCode errorCode) {}
            public void onBannerClicked(MoPubView banner) {}
            public void onBannerExpanded(MoPubView banner) {}
            public void onBannerCollapsed(MoPubView banner) {}
        }); 
        
        String source = getIntent().getStringExtra(SOURCE_KEY);
        if (source != null) {
            source = sourceWithImpressionTrackingDisabled(source);
            mMoPubView.loadHtmlString(source);
        }
        
        return mMoPubView;
    }
    
    @Override
    protected void onDestroy() {
        mMoPubView.destroy();
        super.onDestroy();
    }

    private String sourceWithImpressionTrackingDisabled(String source) {
        // TODO: Temporary fix. Disables impression tracking by renaming the pixel tracker's URL.
        return source.replaceAll("http://ads.mopub.com/m/imp", "mopub://null");
    }
}
