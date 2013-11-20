/*
 * Copyright (c) 2010-2013, MoPub Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *  Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 *  Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 *  Neither the name of 'MoPub Inc.' nor the names of its contributors
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

package com.mopub.mobileads.robotium;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import com.mopub.mobileads.MoPubInterstitial;
import com.mopub.mobileads.MoPubView;

import static com.mopub.mobileads.MoPubInterstitial.InterstitialAdListener;

public class RobotiumTestSupportActivity extends Activity {

    private MoPubView moPubView;
    private String bannerAdUnitId;
    private EditText bannerEditText;
    private InterstitialAdListener interstitialListener;
    private EditText interstitialEditText;
    private MoPubInterstitial moPubInterstitial;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        moPubView = (MoPubView) findViewById(R.id.mopubview);
        bannerEditText = (EditText) findViewById(R.id.banner_adunit_id_field);
        interstitialEditText = (EditText) findViewById(R.id.interstitial_adunit_id_field);

        // Banners
        Button loadBannerButton = (Button) findViewById(R.id.banner_load_button);
        loadBannerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bannerAdUnitId = bannerEditText.getText().toString();
                moPubView.setAdUnitId(bannerAdUnitId);
                moPubView.loadAd();
            }
        });

        // Interstitials
        Button loadInterstitialButton = (Button) findViewById(R.id.interstitial_load_button);
        loadInterstitialButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String interstitialAdUnitId = interstitialEditText.getText().toString();
                moPubInterstitial = new MoPubInterstitial(RobotiumTestSupportActivity.this, interstitialAdUnitId);
                moPubInterstitial.setInterstitialAdListener(interstitialListener);
                moPubInterstitial.load();
            }
        });

        Button showInterstitialButton = (Button) findViewById(R.id.interstitial_show_button);
        showInterstitialButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (moPubInterstitial != null && moPubInterstitial.isReady()) {
                    moPubInterstitial.show();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        if (moPubView != null) moPubView.destroy();
        if (moPubInterstitial != null) moPubInterstitial.destroy();
        super.onDestroy();
    }

    public void setInterstitialListener(InterstitialAdListener interstitialListener) {
        this.interstitialListener = interstitialListener;
    }
}
