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
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.webkit.WebView;
import android.widget.FrameLayout;
import com.mopub.mobileads.MoPubView.LocationAwareness;
import com.mopub.mobileads.factories.AdFetcherFactory;
import com.mopub.mobileads.factories.HtmlBannerWebViewFactory;
import com.mopub.mobileads.factories.HtmlInterstitialWebViewFactory;
import com.mopub.mobileads.factories.HttpClientFactory;
import com.mopub.mobileads.util.Dips;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import static android.Manifest.permission.ACCESS_NETWORK_STATE;
import static com.mopub.mobileads.AdFetcher.AD_TIMEOUT_HEADER;
import static com.mopub.mobileads.AdFetcher.CLICKTHROUGH_URL_HEADER;
import static com.mopub.mobileads.AdFetcher.REDIRECT_URL_HEADER;
import static com.mopub.mobileads.util.HttpResponses.extractHeader;
import static com.mopub.mobileads.util.HttpResponses.extractIntHeader;
import static com.mopub.mobileads.util.HttpResponses.extractIntegerHeader;

public class AdViewController {
    static final int MINIMUM_REFRESH_TIME_MILLISECONDS = 10000;
    static final int DEFAULT_REFRESH_TIME_MILLISECONDS = 60000;
    private static final FrameLayout.LayoutParams WRAP_AND_CENTER_LAYOUT_PARAMS =
            new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    Gravity.CENTER);
    private static WeakHashMap<View,Boolean> sViewShouldHonorServerDimensions = new WeakHashMap<View, Boolean>();;

    private final Context mContext;
    private MoPubView mMoPubView;
    private Map<String, Object> mLocalExtras;
    private final AdUrlGenerator mUrlGenerator;
    private boolean mAutorefreshEnabled;
    private final String mUserAgent;
    private AdFetcher mAdFetcher;

    private final Runnable mRefreshRunnable;
    private String mRedirectUrl;
    private String mClickthroughUrl;
    private String mImpressionUrl;
    private int mWidth;
    private int mHeight;
    private Integer mAdTimeoutDelay;
    private int mRefreshTimeMilliseconds = DEFAULT_REFRESH_TIME_MILLISECONDS;

    private String mAdUnitId;
    private String mKeywords;
    private boolean mFacebookSupportEnabled = true;
    private Location mLocation;
    private boolean mTesting;
    private String mResponseString;
    private boolean mIsDestroyed;
    private Handler mHandler;

    private boolean mIsLoading;
    private String mFailUrl;
    private String mUrl;

    protected static void setShouldHonorServerDimensions(View view) {
        sViewShouldHonorServerDimensions.put(view, true);
    }

    private static boolean getShouldHonorServerDimensions(View view) {
        return sViewShouldHonorServerDimensions.get(view) != null;
    }

    public AdViewController(Context context, MoPubView view) {
        mContext = context;
        mMoPubView = view;

        mLocalExtras = new HashMap<String, Object>();
        mUrlGenerator = new AdUrlGenerator(context);

        mAutorefreshEnabled = true;
        mRefreshRunnable = new Runnable() {
            public void run() {
                loadAd();
            }
        };

        /* Store user agent string at beginning to prevent NPE during background
         * thread operations.
         */
        mUserAgent = new WebView(context).getSettings().getUserAgentString();
        mAdFetcher = AdFetcherFactory.create(this, mUserAgent);

        HtmlBannerWebViewFactory.initialize(context);
        HtmlInterstitialWebViewFactory.initialize(context);

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
        loadNonJavascript(adUrl);
    }

    void loadNonJavascript(String url) {
        if (url == null) return;

        Log.d("MoPub", "Loading url: " + url);
        if (mIsLoading) {
            Log.i("MoPub", "Already loading an ad for " + mAdUnitId + ", wait to finish.");
            return;
        }

        mUrl = url;
        mFailUrl = null;
        mIsLoading = true;

        fetchAd(mUrl);
    }

    public void reload() {
        Log.d("MoPub", "Reload ad: " + mUrl);
        loadNonJavascript(mUrl);
    }

    void loadFailUrl(MoPubErrorCode errorCode) {
        mIsLoading = false;

        Log.v("MoPub", "MoPubErrorCode: " + (errorCode == null ? "" : errorCode.toString()));

        if (mFailUrl != null) {
            Log.d("MoPub", "Loading failover url: " + mFailUrl);
            loadNonJavascript(mFailUrl);
        } else {
            // No other URLs to try, so signal a failure.
            adDidFail(MoPubErrorCode.NO_FILL);
        }
    }

    void setFailUrl(String failUrl) {
        this.mFailUrl = failUrl;
    }

    void setNotLoading() {
        this.mIsLoading = false;
    }

    public String getKeywords() {
        return mKeywords;
    }

    public void setKeywords(String keywords) {
        mKeywords = keywords;
    }

    public boolean isFacebookSupported() {
        return mFacebookSupportEnabled;
    }

    public void setFacebookSupported(boolean enabled) {
        mFacebookSupportEnabled = enabled;
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

        // WebView subclasses are not garbage-collected in a timely fashion on Froyo and below,
        // thanks to some persistent references in WebViewCore. We manually release some resources
        // to compensate for this "leak".

        mAdFetcher.cleanup();
        mAdFetcher = null;

        HtmlBannerWebViewFactory.cleanup();
        HtmlInterstitialWebViewFactory.cleanup();

        mLocalExtras = null;

        mResponseString = null;

        mMoPubView = null;

        // Flag as destroyed. LoadUrlTask checks this before proceeding in its onPostExecute().
        mIsDestroyed = true;
    }

    void configureUsingHttpResponse(final HttpResponse response) {
        // Print the ad network type to the console.
        String networkType = extractHeader(response, "X-Networktype");
        if (networkType != null) Log.i("MoPub", "Fetching ad network type: " + networkType);

        // Set the redirect URL prefix: navigating to any matching URLs will send us to the browser.
        mRedirectUrl = extractHeader(response, REDIRECT_URL_HEADER);
        // Set the URL that is prepended to links for click-tracking purposes.
        mClickthroughUrl = extractHeader(response, CLICKTHROUGH_URL_HEADER);
        // Set the fall-back URL to be used if the current request fails.
        setFailUrl(extractHeader(response, "X-Failurl"));
        // Set the URL to be used for impression tracking.
        mImpressionUrl = extractHeader(response, "X-Imptracker");
        // Set the width and height.
        mWidth = extractIntHeader(response, "X-Width", 0);
        mHeight = extractIntHeader(response, "X-Height", 0);
        // Set the allowable amount of time an ad has before it automatically fails.
        mAdTimeoutDelay = extractIntegerHeader(response, AD_TIMEOUT_HEADER);

        // Set the auto-refresh time. A timer will be scheduled upon ad success or failure.
        if (!response.containsHeader("X-Refreshtime")) {
            mRefreshTimeMilliseconds = 0;
        } else {
            mRefreshTimeMilliseconds = extractIntHeader(response, "X-Refreshtime", 0) * 1000;
            mRefreshTimeMilliseconds = Math.max(
                    mRefreshTimeMilliseconds,
                    MINIMUM_REFRESH_TIME_MILLISECONDS);
        }
    }

    Integer getAdTimeoutDelay() {
        return mAdTimeoutDelay;
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
                    Log.d("MoPub", "Tracking click for: " + mClickthroughUrl);
                    HttpGet httpget = new HttpGet(mClickthroughUrl);
                    httpget.addHeader("User-Agent", mUserAgent);
                    httpClient.execute(httpget);
                } catch (Exception e) {
                    Log.d("MoPub", "Click tracking failed: " + mClickthroughUrl, e);
                } finally {
                    httpClient.getConnectionManager().shutdown();
                }
            }
        }).start();
    }

    void fetchAd(String mUrl) {
        if (mAdFetcher != null) {
            mAdFetcher.fetchAdForUrl(mUrl);
        }
    }

    void forceRefresh() {
        setNotLoading();
        loadAd();
    }

    String generateAdUrl() {
        return mUrlGenerator
                .withAdUnitId(mAdUnitId)
                .withKeywords(mKeywords)
                .withFacebookSupported(mFacebookSupportEnabled)
                .withLocation(mLocation)
                .generateUrlString(getServerHostname());
    }

    void adDidFail(MoPubErrorCode errorCode) {
        Log.i("MoPub", "Ad failed to load.");
        setNotLoading();
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

    void setAdContentView(final View view) {
        // XXX: This method is called from the WebViewClient's callbacks, which has caused an error on a small portion of devices
        // We suspect that the code below may somehow be running on the wrong UI Thread in the rare case.
        // see: http://stackoverflow.com/questions/10426120/android-got-calledfromwrongthreadexception-in-onpostexecute-how-could-it-be
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                MoPubView moPubView = getMoPubView();
                if(moPubView == null) {
                    return;
                }
                moPubView.removeAllViews();
                moPubView.addView(view, getAdLayoutParams(view));
            }
        });
    }

    private FrameLayout.LayoutParams getAdLayoutParams(View view) {
        if (getShouldHonorServerDimensions(view) && mWidth > 0 && mHeight > 0) {
            int scaledWidth = Dips.asIntPixels(mWidth, mContext);
            int scaledHeight = Dips.asIntPixels(mHeight, mContext);

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
        Location result;

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

    HtmlBannerWebView getAdWebView() {
        return null;
    }

    @Deprecated
    public void customEventDidLoadAd() {
        setNotLoading();
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
