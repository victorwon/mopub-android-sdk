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
import android.widget.EditText;

import com.mopub.mobileads.MoPubErrorCode;
import com.mopub.mobileads.MoPubInterstitial;
import com.mopub.mobileads.MoPubInterstitial.InterstitialAdListener;

import static com.mopub.simpleadsdemo.Utils.LOGTAG;
import static com.mopub.simpleadsdemo.Utils.hideSoftKeyboard;
import static com.mopub.simpleadsdemo.Utils.logToast;
import static com.mopub.simpleadsdemo.Utils.validateAdUnitId;

public class InterstitialsTab extends Activity implements InterstitialAdListener {
    private MoPubInterstitial mMoPubInterstitial;
    private EditText mInterstitialAdUnitField;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.interstitials);

        mInterstitialAdUnitField = (EditText) findViewById(R.id.interstitials_edit_text_interstitial);
        hideSoftKeyboard(mInterstitialAdUnitField);

        Button interstitialLoadButton = (Button) findViewById(R.id.interstitials_load_interstitial);
        interstitialLoadButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                String adUnitId = mInterstitialAdUnitField.getText().toString();

                try {
                    validateAdUnitId(adUnitId);

                    mMoPubInterstitial = new MoPubInterstitial(InterstitialsTab.this, adUnitId);
                    mMoPubInterstitial.setInterstitialAdListener(InterstitialsTab.this);
                    mMoPubInterstitial.load();
                } catch (IllegalArgumentException exception) {
                    String message = exception.getMessage();

                    if (message != null) {
                        logToast(InterstitialsTab.this, message);
                    }
                }
            }
        });
        
        Button interstitialShowButton = (Button) findViewById(R.id.interstitials_show_interstitial);
        interstitialShowButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (mMoPubInterstitial != null && mMoPubInterstitial.isReady()) {
                    mMoPubInterstitial.show();
                } else {
                    logToast(InterstitialsTab.this, "Interstitial was not ready. Try reloading.");
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        if (mMoPubInterstitial != null) {
            mMoPubInterstitial.destroy();
        }
        super.onDestroy();
    }

    /*
     * MoPubInterstitial.InterstitialAdListener implementation
     */
    @Override
    public void onInterstitialLoaded(MoPubInterstitial interstitial) {
        logToast(this, "Interstitial loaded successfully.");
    }

    @Override
    public void onInterstitialFailed(MoPubInterstitial interstitial, MoPubErrorCode errorCode) {
        logToast(this, "Interstitial failed to load with error: " + errorCode.toString());
    }

    @Override
    public void onInterstitialShown(MoPubInterstitial interstitial) {
        logToast(this, "Interstitial shown.");
    }

    @Override
    public void onInterstitialClicked(MoPubInterstitial interstitial) {
        logToast(this, "Interstitial clicked.");
    }

    @Override
    public void onInterstitialDismissed(MoPubInterstitial interstitial) {
        logToast(this, "Interstitial dismissed.");
    }
}
