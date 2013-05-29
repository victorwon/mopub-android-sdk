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

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import com.mopub.mobileads.MoPubView.LocationAwareness;
import com.mopub.mobileads.factories.AdFetcherFactory;
import com.mopub.mobileads.factories.HttpClientFactory;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static android.Manifest.permission.ACCESS_NETWORK_STATE;

public class AdViewController {
    private static final int MINIMUM_REFRESH_TIME_MILLISECONDS = 10000;
    private static final FrameLayout.LayoutParams WRAP_AND_CENTER_LAYOUT_PARAMS =
            new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    Gravity.CENTER);

    private final Context mContext;
    private MoPubView mMoPubView;
    private Map<String, Object> mLocalExtras;
    private final AdUrlGenerator mUrlGenerator;
    private final AdWebView mAdWebView;
    private boolean mAutorefreshEnabled;
    private final String mUserAgent;
    private AdFetcher mAdFetcher;

    private final Runnable mRefreshRunnable;
    private String mRedirectUrl;
    private String mClickthroughUrl;
    private String mImpressionUrl;
    private int mWidth;
    private int mHeight;
    private int mRefreshTimeMilliseconds;

    private String mAdUnitId;
    private String mKeywords;
    private Location mLocation;
    private boolean mTesting;
    private String mResponseString;
    private boolean mIsDestroyed;
    private Handler mHandler;

    public AdViewController(Context context, MoPubView view) {
        mContext = context;
        mMoPubView = view;

        mLocalExtras = new HashMap<String, Object>();
        mUrlGenerator = new AdUrlGenerator(context);
        mAdWebView = new AdWebView(this, context);

        mAutorefreshEnabled = true;
        mRefreshRunnable = new Runnable() {
            public void run() {
                loadAd();
            }
        };

        /* Store user agent string at beginning to prevent NPE during background
         * thread operations.
         */
        mUserAgent = mAdWebView.getSettings().getUserAgentString();
        mAdFetcher = AdFetcherFactory.create(this, mUserAgent);

        mHandler = new Handler();
    }

    public MoPubView getMoPubView() {
        return mMoPubView;
    }

    public void loadAd() {
        if (mAdUnitId == null) {
            Log.d("MoPub", "Can't load an ad in this ad view because the ad unit ID is null. " +
                    "Did you forget to call setAdUnitId()?");
            return;
        }

        if (!isNetworkAvailable()) {
            Log.d("MoPub", "Can't load an ad because there is no network connectivity.");
            scheduleRefreshTimerIfEnabled();
            return;
        }

        if (mLocation == null) mLocation = getLastKnownLocation();

        // tested (remove me when the rest of this is tested)
        String adUrl = generateAdUrl();
        mAdWebView.loadUrl(adUrl);
    }

    public String getKeywords() {
        return mKeywords;
    }

    public void setKeywords(String keywords) {
        mKeywords = keywords;
    }

    public Location getLocation() {
        return mLocation;
    }

    public void setLocation(Location location) {
        mLocation = location;
    }

    public String getAdUnitId() {
        return mAdUnitId;
    }

    public void setAdUnitId(String adUnitId) {
        mAdUnitId = adUnitId;
    }

    public void setTimeout(int milliseconds) {
        if (mAdFetcher != null) {
            mAdFetcher.setTimeout(milliseconds);
        }
    }

    public int getAdWidth() {
        return mWidth;
    }

    public int getAdHeight() {
        return mHeight;
    }

    public String getClickthroughUrl() {
        return mClickthroughUrl;
    }

    public void setClickthroughUrl(String url) {
        mClickthroughUrl = url;
    }

    public String getRedirectUrl() {
        return mRedirectUrl;
    }

    public String getResponseString() {
        return mResponseString;
    }

    public boolean getAutorefreshEnabled() {
        return mAutorefreshEnabled;
    }

    public void setAutorefreshEnabled(boolean enabled) {
        mAutorefreshEnabled = enabled;

        Log.d("MoPub", "Automatic refresh for " + mAdUnitId + " set to: " + enabled + ".");

        if (!mAutorefreshEnabled) cancelRefreshTimer();
        else scheduleRefreshTimerIfEnabled();
    }

    public boolean getTesting() {
        return mTesting;
    }

    public void setTesting(boolean testing) {
        mTesting = testing;
    }

    boolean isDestroyed() {
        return mIsDestroyed;
    }

    /*
     * Clean up the internal state of the AdViewController.
     */
    void cleanup() {
        if (mIsDestroyed) {
            return;
        }

        setAutorefreshEnabled(false);
        cancelRefreshTimer();
        mAdWebView.destroy();

        // WebView subclasses are not garbage-collected in a timely fashion on Froyo and below,
        // thanks to some persistent references in WebViewCore. We manually release some resources
        // to compensate for this "leak".

        mAdFetcher.cleanup();
        mAdFetcher = null;

        mLocalExtras = null;

        mResponseString = null;

        getMoPubView().removeView(mAdWebView);
        mMoPubView = null;

        // Flag as destroyed. LoadUrlTask checks this before proceeding in its onPostExecute().
        mIsDestroyed = true;
    }

    void loadResponseString(String responseString) {
        mAdWebView.loadDataWithBaseURL("http://" + getServerHostname() + "/", responseString, "text/html",
                "utf-8", null);
    }

    void configureUsingHttpResponse(final HttpResponse response) {
        // Print the ad network type to the console.
        String networkType = extractHeader(response, "X-Networktype");
        if (networkType != null) Log.i("MoPub", "Fetching ad network type: " + networkType);

        // Set the redirect URL prefix: navigating to any matching URLs will send us to the browser.
        mRedirectUrl = extractHeader(response, "X-Launchpage");
        // Set the URL that is prepended to links for click-tracking purposes.
        mClickthroughUrl = extractHeader(response, "X-Clickthrough");
        // Set the fall-back URL to be used if the current request fails.
        mAdWebView.setFailUrl(extractHeader(response, "X-Failurl"));
        // Set the URL to be used for impression tracking.
        mImpressionUrl = extractHeader(response, "X-Imptracker");
        // Set the webview's scrollability.
        boolean enabled = extractBooleanHeader(response, "X-Scrollable");
        setWebViewScrollingEnabled(enabled);
        // Set the width and height.
        mWidth = extractIntHeader(response, "X-Width");
        mHeight = extractIntHeader(response, "X-Height");

        // Set the auto-refresh time. A timer will be scheduled upon ad success or failure.
        if (!response.containsHeader("X-Refreshtime")) {
            mRefreshTimeMilliseconds = 0;
        } else {
            mRefreshTimeMilliseconds = extractIntHeader(response, "X-Refreshtime") * 1000;
            mRefreshTimeMilliseconds = Math.max(
                    mRefreshTimeMilliseconds,
                    MINIMUM_REFRESH_TIME_MILLISECONDS);
        }
    }

    int getRefreshTimeMilliseconds() {
        return mRefreshTimeMilliseconds;
    }

    void setRefreshTimeMilliseconds(int refreshTimeMilliseconds) {
        mRefreshTimeMilliseconds = refreshTimeMilliseconds;
    }

    void trackImpression() {
        new Thread(new Runnable() {
            public void run () {
                if (mImpressionUrl == null) return;

                DefaultHttpClient httpClient = HttpClientFactory.create();
                try {
                    HttpGet httpget = new HttpGet(mImpressionUrl);
                    httpget.addHeader("User-Agent", mUserAgent);
                    httpClient.execute(httpget);
                } catch (Exception e) {
                    Log.d("MoPub", "Impression tracking failed : " + mImpressionUrl, e);
                } finally {
                    httpClient.getConnectionManager().shutdown();
                }
            }
        }).start();
    }

    void registerClick() {
        new Thread(new Runnable() {
            public void run () {
                if (mClickthroughUrl == null) return;

                DefaultHttpClient httpClient = HttpClientFactory.create();
                try {
                    HttpGet httpget = new HttpGet(mClickthroughUrl);
                    httpget.addHeader("User-Agent", mUserAgent);
                    httpClient.execute(httpget);
                } catch (Exception e) {
                    Log.i("MoPub", "Click tracking failed: " + mImpressionUrl, e);
                } finally {
                    httpClient.getConnectionManager().shutdown();
                }
            }
        }).start();
    }

    void adAppeared() {
        mAdWebView.loadUrl("javascript:webviewDidAppear();");
    }

    void setResponseString(String responseString) {
        mResponseString = responseString;
    }

    void setNotLoading() {
        mAdWebView.setNotLoading();
    }

    void fetchAd(String mUrl) {
        if (mAdFetcher != null) {
            mAdFetcher.fetchAdForUrl(mUrl);
        }
    }

    void forceRefresh() {
        mAdWebView.setNotLoading();
        loadAd();
    }

    void loadFailUrl(MoPubErrorCode errorCode) {
        mAdWebView.loadFailUrl(errorCode);
    }

    String generateAdUrl() {
        return mUrlGenerator
                .withAdUnitId(mAdUnitId)
                .withKeywords(mKeywords)
                .withLocation(mLocation)
                .generateUrlString(getServerHostname());
    }

    void adDidFail(MoPubErrorCode errorCode) {
        Log.i("MoPub", "Ad failed to load.");
        mAdWebView.setNotLoading();
        scheduleRefreshTimerIfEnabled();
        getMoPubView().adFailed(errorCode);
    }

    void scheduleRefreshTimerIfEnabled() {
        cancelRefreshTimer();
        if (mAutorefreshEnabled && mRefreshTimeMilliseconds > 0) {
            mHandler.postDelayed(mRefreshRunnable, mRefreshTimeMilliseconds);
        }

    }

    void setLocalExtras(Map<String, Object> localExtras) {
        mLocalExtras = (localExtras != null)
                ? new HashMap<String,Object>(localExtras)
                : new HashMap<String,Object>();
    }

    Map<String, Object> getLocalExtras() {
        return (mLocalExtras != null)
                ? new HashMap<String,Object>(mLocalExtras)
                : new HashMap<String,Object>();
    }

    void adDidLoad() {
        Log.i("MoPub", "Ad successfully loaded.");
        mAdWebView.setNotLoading();
        scheduleRefreshTimerIfEnabled();
        setAdContentView(mAdWebView, getHtmlAdLayoutParams());
        getMoPubView().adLoaded();
    }

    void setAdContentView(View view) {
        setAdContentView(view, WRAP_AND_CENTER_LAYOUT_PARAMS);
    }

    void adDidClose() {
        getMoPubView().adClosed();
    }

    private void cancelRefreshTimer() {
        mHandler.removeCallbacks(mRefreshRunnable);
    }

    private String getServerHostname() {
        return mTesting ? MoPubView.HOST_FOR_TESTING : MoPubView.HOST;
    }

    private boolean isNetworkAvailable() {
        // If we don't have network state access, just assume the network is up.
        int result = mContext.checkCallingPermission(ACCESS_NETWORK_STATE);
        if (result == PackageManager.PERMISSION_DENIED) return true;

        // Otherwise, perform the connectivity check.
        ConnectivityManager cm
                = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    private int extractIntHeader(HttpResponse response, String headerName) {
        String headerValue = extractHeader(response, headerName);
        return (headerValue != null) ? Integer.parseInt(headerValue.trim()) : 0;
    }

    private String extractHeader(HttpResponse response, String headerName) {
        Header header = response.getFirstHeader(headerName);
        return header != null ? header.getValue() : null;
    }

    private boolean extractBooleanHeader(HttpResponse response, String headerName) {
        return !"0".equals(extractHeader(response, headerName));
    }

    private void setWebViewScrollingEnabled(boolean enabled) {
        mAdWebView.setWebViewScrollingEnabled(enabled);
    }

    private void setAdContentView(View view, FrameLayout.LayoutParams layoutParams) {
        getMoPubView().removeAllViews();
        getMoPubView().addView(view, layoutParams);
    }

    private FrameLayout.LayoutParams getHtmlAdLayoutParams() {
        if (mWidth > 0 && mHeight > 0) {
            DisplayMetrics displayMetrics = mContext.getResources().getDisplayMetrics();

            int scaledWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, mWidth,
                    displayMetrics);
            int scaledHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, mHeight,
                    displayMetrics);

            return new FrameLayout.LayoutParams(scaledWidth, scaledHeight, Gravity.CENTER);
        } else {
            return WRAP_AND_CENTER_LAYOUT_PARAMS;
        }
    }

    /*
     * Returns the last known location of the device using its GPS and network location providers.
     * May be null if:
     * - Location permissions are not requested in the Android manifest file
     * - The location providers don't exist
     * - Location awareness is disabled in the parent MoPubView
     */
    private Location getLastKnownLocation() {
        LocationAwareness locationAwareness = getMoPubView().getLocationAwareness();
        int locationPrecision = getMoPubView().getLocationPrecision();
        Location result = null;

        if (locationAwareness == LocationAwareness.LOCATION_AWARENESS_DISABLED) {
            return null;
        }

        LocationManager lm = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        Location gpsLocation = null;
        try {
            gpsLocation = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        } catch (SecurityException e) {
            Log.d("MoPub", "Failed to retrieve GPS location: access appears to be disabled.");
        } catch (IllegalArgumentException e) {
            Log.d("MoPub", "Failed to retrieve GPS location: device has no GPS provider.");
        }

        Location networkLocation = null;
        try {
            networkLocation = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        } catch (SecurityException e) {
            Log.d("MoPub", "Failed to retrieve network location: access appears to be disabled.");
        } catch (IllegalArgumentException e) {
            Log.d("MoPub", "Failed to retrieve network location: device has no network provider.");
        }

        if (gpsLocation == null && networkLocation == null) {
            return null;
        }
        else if (gpsLocation != null && networkLocation != null) {
            if (gpsLocation.getTime() > networkLocation.getTime()) result = gpsLocation;
            else result = networkLocation;
        }
        else if (gpsLocation != null) result = gpsLocation;
        else result = networkLocation;

        // Truncate latitude/longitude to the number of digits specified by locationPrecision.
        if (locationAwareness == LocationAwareness.LOCATION_AWARENESS_TRUNCATED) {
            double lat = result.getLatitude();
            double truncatedLat = BigDecimal.valueOf(lat)
                    .setScale(locationPrecision, BigDecimal.ROUND_HALF_DOWN)
                    .doubleValue();
            result.setLatitude(truncatedLat);

            double lon = result.getLongitude();
            double truncatedLon = BigDecimal.valueOf(lon)
                    .setScale(locationPrecision, BigDecimal.ROUND_HALF_DOWN)
                    .doubleValue();
            result.setLongitude(truncatedLon);
        }

        return result;
    }

    Context getContext() {
        return mContext;
    }

    AdWebView getAdWebView() {
        return mAdWebView;
    }

    @Deprecated
    public void customEventDidLoadAd() {
        mAdWebView.setNotLoading();
        trackImpression();
        scheduleRefreshTimerIfEnabled();
    }

    @Deprecated
    public void customEventDidFailToLoadAd() {
        loadFailUrl(MoPubErrorCode.UNSPECIFIED);
    }

    @Deprecated
    public void customEventActionWillBegin() {
        registerClick();
    }
}
