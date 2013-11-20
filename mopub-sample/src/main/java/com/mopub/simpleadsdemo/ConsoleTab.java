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
import android.content.Context;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.mopub.mobileads.MoPubErrorCode;
import com.mopub.mobileads.MoPubView;
import com.mopub.mobileads.MoPubView.BannerAdListener;

public class ConsoleTab extends Activity {
    private MoPubView mBanner;
    private EditText mSearchText;
    private TextView mConsoleText;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.console);

        mBanner = (MoPubView) findViewById(R.id.bannerview);
        mBanner.setAdUnitId(SimpleAdsDemoConstants.PUB_ID_320x50);
        
        mBanner.setBannerAdListener(new BannerAdListener() {
            @Override
            public void onBannerLoaded(MoPubView banner) {
                outputLine("Ad was loaded. Success.");
                outputLine("Payload = " + banner.getResponseString());
            }

            @Override
            public void onBannerFailed(MoPubView banner, MoPubErrorCode errorCode) {
                outputLine("Ad did not load.");
                outputLine("Payload = " + banner.getResponseString());
            }

            public void onBannerClicked(MoPubView banner) {}
            public void onBannerExpanded(MoPubView banner) {}
            public void onBannerCollapsed(MoPubView banner) {}
        });
        
        mConsoleText = (TextView) findViewById(R.id.consoletext);
        mConsoleText.setMovementMethod(new ScrollingMovementMethod());
        
        clearConsole();

        mSearchText = (EditText) findViewById(R.id.searchtext);
        Button mSearchButton = (Button) findViewById(R.id.searchbutton);
        mSearchButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                InputMethodManager imm
                        = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(mSearchText.getWindowToken(), 0);
                mBanner.setKeywords(mSearchText.getText().toString());
                mBanner.loadAd();
            }
        });
    }

    private void clearConsole() {
        mConsoleText.setText("MoPub Ad Loading Console\n========================\n");
        mConsoleText.bringPointIntoView(0);
    }

    private void outputLine(String str) {
        mConsoleText.append(str+"\n");
    }
    
    @Override
    protected void onDestroy() {
        mBanner.destroy();
        super.onDestroy();
    }
}