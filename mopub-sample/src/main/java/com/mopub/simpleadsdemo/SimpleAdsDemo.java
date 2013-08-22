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

package com.mopub.simpleadsdemo;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;
import android.widget.Toast;
import com.mopub.mobileads.MoPubConversionTracker;

public class SimpleAdsDemo extends TabActivity {
    private final int INTERSTITIAL_AD_REQUEST = 0;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        TabHost tabHost = getTabHost();
        TabHost.TabSpec spec;

        spec = tabHost.newTabSpec("banners")
                .setIndicator("Banners")
                .setContent(new Intent().setClass(this, BannersTab.class));
        tabHost.addTab(spec);

        spec = tabHost.newTabSpec("interstitials")
                .setIndicator("Full")
                .setContent(new Intent().setClass(this, InterstitialsTab.class));
        tabHost.addTab(spec);

        spec = tabHost.newTabSpec("console")
                .setIndicator("Console")
                .setContent(new Intent().setClass(this, ConsoleTab.class));
        tabHost.addTab(spec);

        spec = tabHost.newTabSpec("about")
                .setIndicator("About")
                .setContent(new Intent().setClass(this, AboutTab.class));
        tabHost.addTab(spec);

        // Conversion tracking
        new MoPubConversionTracker().reportAppOpen(this);
    }

    // Listen for results from the interstitial ad
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
        case INTERSTITIAL_AD_REQUEST:
            // Handle interstitial closed result here if needed.
            // This is called immediately before onResume()
            if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "No ad available", Toast.LENGTH_SHORT).show();
            }
        }
    }
}