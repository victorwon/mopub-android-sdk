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

package com.mopub.simpleadsdemo;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;
import com.mopub.mobileads.MoPubErrorCode;
import com.mopub.mobileads.MoPubInterstitial;
import com.mopub.mobileads.MoPubInterstitial.InterstitialAdListener;

public class InterstitialsTab extends Activity implements InterstitialAdListener {

    private MoPubInterstitial mMoPubInterstitial;
    
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.interstitials);

        mMoPubInterstitial = new MoPubInterstitial(this, SimpleAdsDemoConstants.PUB_ID_INTERSTITIAL);
        mMoPubInterstitial.setInterstitialAdListener(this);
        
        Button loadInterstitialButton = (Button) findViewById(R.id.load_interstitial);
        loadInterstitialButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // Load interstitial.
                mMoPubInterstitial.load();
            }
        });
        
        Button showInterstitialButton = (Button) findViewById(R.id.show_interstitial);
        showInterstitialButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // Show interstitial.
                if (mMoPubInterstitial.isReady()) {
                    mMoPubInterstitial.show();
                } else {
                    logToast("Interstitial was not ready. Try reloading.");
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        mMoPubInterstitial.destroy();
        super.onDestroy();
    }

    /*
     * MoPubInterstitial.MoPubInterstitialListener implementation
     */
    @Override
    public void onInterstitialLoaded(MoPubInterstitial interstitial) {
        logToast("Interstitial loaded successfully.");
    }

    @Override
    public void onInterstitialFailed(MoPubInterstitial interstitial, MoPubErrorCode errorCode) {
        logToast("Interstitial failed to load with error: " + errorCode.toString());
    }

    @Override
    public void onInterstitialShown(MoPubInterstitial interstitial) {
        logToast("Interstitial shown.");
    }

    @Override
    public void onInterstitialClicked(MoPubInterstitial interstitial) {
        logToast("Interstitial clicked.");
    }

    @Override
    public void onInterstitialDismissed(MoPubInterstitial interstitial) {
        logToast("Interstitial dismissed.");
    }

    private void logToast(String message) {
        Log.d("MoPub Demo", message);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
