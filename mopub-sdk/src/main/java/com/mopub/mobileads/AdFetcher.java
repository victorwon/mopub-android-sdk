/*
 * AdFetcher.java
 * 
 * Copyright (c) 2012, MoPub Inc.
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
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

/*
 * AdFetcher is a delegate of an AdView that handles loading ad data over a
 * network connection. The ad is fetched in a background thread by executing
 * AdFetchTask, which is an AsyncTask subclass. This class gracefully handles
 * the changes to AsyncTask in Android 4.0.1 (we continue to run parallel to
 * the app developer's background tasks). Further, AdFetcher keeps track of
 * the last completed task to prevent out-of-order execution.
 */
public class AdFetcher {
    public static final String USER_AGENT_HEADER = "User-Agent";
    public static final String WARMUP_HEADER = "X-Warmup";
    public static final String AD_TYPE_HEADER = "X-Adtype";
    public static final String CUSTOM_EVENT_NAME_HEADER = "X-Custom-Event-Class-Name";
    public static final String CUSTOM_EVENT_DATA_HEADER = "X-Custom-Event-Class-Data";
    @Deprecated
    public static final String CUSTOM_SELECTOR_HEADER = "X-Customselector";
    public static final String NATIVE_PARAMS_HEADER = "X-Nativeparams";
    public static final String FULL_AD_TYPE_HEADER = "X-Fulladtype";
    private int mTimeoutMilliseconds = 10000;
    // This is equivalent to Build.VERSION_CODES.ICE_CREAM_SANDWICH
    private static final int VERSION_CODE_ICE_CREAM_SANDWICH = 14;

    private AdView mAdView;
    private AdFetchTask mCurrentTask;
    private String mUserAgent;
    private long mCurrentTaskId;
    private long mLastCompletedTaskId;

    private enum FetchStatus {
        NOT_SET,
        FETCH_CANCELLED,
        INVALID_SERVER_RESPONSE_BACKOFF,
        INVALID_SERVER_RESPONSE_NOBACKOFF,
        CLEAR_AD_TYPE,
        AD_WARMING_UP;
    }

    public AdFetcher(AdView adview, String userAgent) {
        mAdView = adview;
        mUserAgent = userAgent;
        mCurrentTaskId = -1;
        mLastCompletedTaskId = -1;
    }

    public void fetchAdForUrl(String url) {
        mCurrentTaskId++;
        Log.i("MoPub", "Fetching ad for task #" + mCurrentTaskId);

        if (mCurrentTask != null) {
            mCurrentTask.cancel(true);
        }

        mCurrentTask = new AdFetchTask(this);

        if (Build.VERSION.SDK_INT >= VERSION_CODE_ICE_CREAM_SANDWICH) {
            Class<?> cls = AdFetchTask.class;
            Class<?>[] parameterTypes = {Executor.class, Object[].class};

            String[] parameters = {url};

            try {
                Method method = cls.getMethod("executeOnExecutor", parameterTypes);
                Field field = cls.getField("THREAD_POOL_EXECUTOR");
                method.invoke(mCurrentTask, field.get(cls), parameters);
            } catch (NoSuchMethodException exception) {
                Log.d("MoPub", "Error executing AdFetchTask on ICS+, method not found.");
            } catch (InvocationTargetException exception) {
                Log.d("MoPub", "Error executing AdFetchTask on ICS+, thrown by executeOnExecutor.");
            } catch (Exception exception) {
                Log.d("MoPub", "Error executing AdFetchTask on ICS+: " + exception.toString());
            }
        } else {
            mCurrentTask.execute(url);
        }
    }

    public void cancelFetch() {
        if (mCurrentTask != null) {
            Log.i("MoPub", "Canceling fetch ad for task #" + mCurrentTaskId);
            mCurrentTask.cancel(true);
        }
    }

    private void markTaskCompleted(long taskId) {
        if (taskId > mLastCompletedTaskId) {
            mLastCompletedTaskId = taskId;
        }
    }

    public void cleanup() {
        cancelFetch();

        mAdView = null;
        mUserAgent = "";
    }

    protected void setTimeout(int milliseconds) {
        mTimeoutMilliseconds = milliseconds;
    }

    protected int getTimeout() {
        return mTimeoutMilliseconds;
    }

    private static String getHeaderValue(HttpResponse response, String headerName) {
        Header header = response.getFirstHeader(headerName);
        return (header == null) ? null : header.getValue();
    }

    private static class AdFetchTask extends AsyncTask<String, Void, AdFetchResult> {
        private AdFetcher mAdFetcher;
        private AdView mAdView;
        private Exception mException;
        private HttpClient mHttpClient;
        private long mTaskId;

        private FetchStatus mFetchStatus = FetchStatus.NOT_SET;

        private static final int MAXIMUM_REFRESH_TIME_MILLISECONDS = 600000;
        private static final double EXPONENTIAL_BACKOFF_FACTOR = 1.5;

        private AdFetchTask(AdFetcher adFetcher) {
            mAdFetcher = adFetcher;

            mAdView = mAdFetcher.mAdView;
            mHttpClient = getDefaultHttpClient();
            mTaskId = mAdFetcher.mCurrentTaskId;
        }

        @Override
        protected AdFetchResult doInBackground(String... urls) {
            AdFetchResult result = null;
            try {
                result = fetch(urls[0]);
            } catch (Exception exception) {
                mException = exception;
            } finally {
                shutdownHttpClient();
            }
            return result;
        }

        private AdFetchResult fetch(String url) throws Exception {
            HttpGet httpget = new HttpGet(url);
            httpget.addHeader(USER_AGENT_HEADER, mAdFetcher.mUserAgent);

            // We check to see if this AsyncTask was cancelled, as per
            // http://developer.android.com/reference/android/os/AsyncTask.html
            if (isCancelled()) {
                mFetchStatus = FetchStatus.FETCH_CANCELLED;
                return null;
            }

            if (mAdView == null || mAdView.isDestroyed()) {
                Log.d("MoPub", "Error loading ad: AdView has already been GCed or destroyed.");
                return null;
            }

            HttpResponse response = mHttpClient.execute(httpget);
            HttpEntity entity = response.getEntity();

            if (response == null || entity == null) {
                Log.d("MoPub", "MoPub server returned null response.");
                mFetchStatus = FetchStatus.INVALID_SERVER_RESPONSE_NOBACKOFF;
                return null;
            }

            final int statusCode = response.getStatusLine().getStatusCode();

            // Client and Server HTTP errors should result in an exponential backoff
            if (statusCode >= 400) {
                Log.d("MoPub", "Server error: returned HTTP status code " + Integer.toString(statusCode) +
                        ". Please try again.");
                mFetchStatus = FetchStatus.INVALID_SERVER_RESPONSE_BACKOFF;
                return null;
            }
            // Other non-200 HTTP status codes should still fail
            else if (statusCode != HttpStatus.SC_OK) {
                Log.d("MoPub", "MoPub server returned invalid response: HTTP status code " +
                        Integer.toString(statusCode) + ".");
                mFetchStatus = FetchStatus.INVALID_SERVER_RESPONSE_NOBACKOFF;
                return null;
            }

            mAdView.configureUsingHttpResponse(response);

            // Ensure that the ad is not warming up.
            if ("1".equals(getHeaderValue(response, WARMUP_HEADER))) {
                Log.d("MoPub", "Ad Unit (" + mAdView.getAdUnitId() + ") is still warming up. " +
                        "Please try again in a few minutes.");
                mFetchStatus = FetchStatus.AD_WARMING_UP;
                return null;
            }

            // Ensure that the ad type header is valid and not "clear".
            String adType = getHeaderValue(response, AD_TYPE_HEADER);
            String fullAdType = getHeaderValue(response, FULL_AD_TYPE_HEADER);
            String adTypeCustomEventName = AdTypeTranslator.getCustomEventNameForAdType(adType, fullAdType);
            if ("clear".equals(adType)) {
                Log.d("MoPub", "No inventory found for adunit (" + mAdView.getAdUnitId() + ").");
                mFetchStatus = FetchStatus.CLEAR_AD_TYPE;
                return null;
            }

            // Handle custom native ad type.
            else if ("custom".equals(adType)) {
                Log.i("MoPub", "Performing custom event.");

                // If applicable, try to invoke the new custom event system (which uses custom classes)
                String customEventName = getHeaderValue(response, CUSTOM_EVENT_NAME_HEADER);
                if (customEventName != null) {
                    String customEventData = getHeaderValue(response, CUSTOM_EVENT_DATA_HEADER);
                    return createCustomEventTask(customEventName, customEventData);
                }

                // Otherwise, use the (deprecated) legacy custom event system for older clients
                Header oldCustomEventHeader = response.getFirstHeader(CUSTOM_SELECTOR_HEADER);
                return new PerformLegacyCustomEventTaskResult(mAdView, oldCustomEventHeader);

            }

            // Handle mraid ad type.
            else if ("mraid".equals(adType)) {
                Log.i("MoPub", "Loading mraid ad");
                Map<String, String> paramsMap = new HashMap<String, String>();
                paramsMap.put(AD_TYPE_HEADER, adType);

                String data = httpEntityToString(entity);
                paramsMap.put(NATIVE_PARAMS_HEADER, data);
                return new LoadNativeAdTaskResult(mAdView, paramsMap);
            }

            else if (adTypeCustomEventName != null) {
                String eventData = getHeaderValue(response, NATIVE_PARAMS_HEADER);

                return createCustomEventTask(adTypeCustomEventName, eventData);
            }

            // Handle HTML ad.
            String data = httpEntityToString(entity);
            return new LoadHtmlAdTaskResult(mAdView, data);
        }

        private AdFetchResult createCustomEventTask(String customEventName, String customEventData) {
            Map<String, String> paramsMap = new HashMap<String, String>();
            paramsMap.put(CUSTOM_EVENT_NAME_HEADER, customEventName);

            if (customEventData != null) {
                paramsMap.put(CUSTOM_EVENT_DATA_HEADER, customEventData);
            }

            return new PerformCustomEventTaskResult(mAdView, paramsMap);
        }

        @Override
        protected void onPostExecute(AdFetchResult result) {
            if (!isMostCurrentTask()) {
                Log.d("MoPub", "Ad response is stale.");
                releaseResources();
                return;
            }

            // If cleanup() has already been called on the AdView, don't proceed.
            if (mAdView == null || mAdView.isDestroyed()) {
                if (result != null) {
                    result.cleanup();
                }
                mAdFetcher.markTaskCompleted(mTaskId);
                releaseResources();
                return;
            }

            if (result == null) {
                if (mException != null) {
                    Log.d("MoPub", "Exception caught while loading ad: " + mException);
                }

                MoPubErrorCode errorCode;
                switch (mFetchStatus) {
                    case NOT_SET:
                        errorCode = MoPubErrorCode.UNSPECIFIED;
                        break;
                    case FETCH_CANCELLED:
                        errorCode = MoPubErrorCode.CANCELLED;
                        break;
                    case INVALID_SERVER_RESPONSE_BACKOFF:
                    case INVALID_SERVER_RESPONSE_NOBACKOFF:
                        errorCode = MoPubErrorCode.SERVER_ERROR;
                        break;
                    case CLEAR_AD_TYPE:
                    case AD_WARMING_UP:
                        errorCode = MoPubErrorCode.NO_FILL;
                        break;
                    default:
                        errorCode = MoPubErrorCode.UNSPECIFIED;
                        break;
                }

                mAdView.adDidFail(errorCode);

                /*
                 * There are numerous reasons for the ad fetch to fail, but only in the specific
                 * case of actual server failure should we exponentially back off.
                 *
                 * Note: We place the exponential backoff after AdView's adDidFail because we only
                 * want to increase refresh times after the first failure refresh timer is
                 * scheduled, and not before.
                 */
                if (mFetchStatus == FetchStatus.INVALID_SERVER_RESPONSE_BACKOFF) {
                    exponentialBackoff();
                    mFetchStatus = FetchStatus.NOT_SET;
                }
            } else {
                result.execute();
                result.cleanup();
            }

            mAdFetcher.markTaskCompleted(mTaskId);
            releaseResources();
        }

        @Override
        protected void onCancelled() {
            if (!isMostCurrentTask()) {
                Log.d("MoPub", "Ad response is stale.");
                releaseResources();
                return;
            }

            Log.d("MoPub", "Ad loading was cancelled.");
            if (mException != null) {
                Log.d("MoPub", "Exception caught while loading ad: " + mException);
            }
            mAdFetcher.markTaskCompleted(mTaskId);
            releaseResources();
        }

        private String httpEntityToString(HttpEntity entity)
                throws IOException {

            InputStream inputStream = entity.getContent();
            int numberBytesRead = 0;
            StringBuffer out = new StringBuffer();
            byte[] bytes = new byte[4096];

            while (numberBytesRead != -1) {
                out.append(new String(bytes, 0, numberBytesRead));
                numberBytesRead = inputStream.read(bytes);
            }

            inputStream.close();

            return out.toString();
        }

        /* This helper function is called when a 4XX or 5XX error is received during an ad fetch.
         * It exponentially increases the parent AdView's refreshTime up to a specified cap.
         */
        private void exponentialBackoff() {
            if (mAdView == null) {
                return;
            }

            int refreshTimeMilliseconds = mAdView.getRefreshTimeMilliseconds();

            refreshTimeMilliseconds = (int) (refreshTimeMilliseconds * EXPONENTIAL_BACKOFF_FACTOR);
            if (refreshTimeMilliseconds > MAXIMUM_REFRESH_TIME_MILLISECONDS) {
                refreshTimeMilliseconds = MAXIMUM_REFRESH_TIME_MILLISECONDS;
            }

            mAdView.setRefreshTimeMilliseconds(refreshTimeMilliseconds);
        }

        private void releaseResources() {
            mAdFetcher = null;
            mException = null;
            mFetchStatus = FetchStatus.NOT_SET;
        }

        private DefaultHttpClient getDefaultHttpClient() {
            HttpParams httpParameters = new BasicHttpParams();
            int timeoutMilliseconds = mAdFetcher.getTimeout();

            if (timeoutMilliseconds > 0) {
                // Set timeouts to wait for connection establishment / receiving data.
                HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutMilliseconds);
                HttpConnectionParams.setSoTimeout(httpParameters, timeoutMilliseconds);
            }

            // Set the buffer size to avoid OutOfMemoryError exceptions on certain HTC devices.
            // http://stackoverflow.com/questions/5358014/android-httpclient-oom-on-4g-lte-htc-thunderbolt
            HttpConnectionParams.setSocketBufferSize(httpParameters, 8192);

            return new DefaultHttpClient(httpParameters);
        }

        private void shutdownHttpClient() {
            if (mHttpClient != null) {
                ClientConnectionManager manager = mHttpClient.getConnectionManager();
                if (manager != null) {
                    manager.shutdown();
                }
                mHttpClient = null;
            }
        }

        private boolean isMostCurrentTask() {
            return mTaskId >= mAdFetcher.mLastCompletedTaskId;
        }

    }

    private static abstract class AdFetchResult {

        WeakReference<AdView> mWeakAdView;
        public AdFetchResult(AdView adView) {
            mWeakAdView = new WeakReference<AdView>(adView);
        }

        abstract void execute();

        /* The AsyncTask thread pool often appears to keep references to these
         * objects, preventing GC. This method should be used to release
         * resources to mitigate the GC issue.
         */
        abstract void cleanup();

    }
    /*
     * This is the old way of performing Custom Events, and is now deprecated. This will still be
     * invoked on old clients when X-Adtype is "custom" and the new X-Custom-Event-Class-Name header
     * is not specified (legacy custom events parse the X-Customselector header instead).
     */
    @Deprecated
    private static class PerformLegacyCustomEventTaskResult extends AdFetchResult {

        protected Header mHeader;
        public PerformLegacyCustomEventTaskResult(AdView adView, Header header) {
            super(adView);
            mHeader = header;
        }

        public void execute() {
            AdView adView = mWeakAdView.get();
            if (adView == null || adView.isDestroyed()) {
                return;
            }

            adView.setIsLoading(false);
            MoPubView mpv = adView.getMoPubView();

            if (mHeader == null) {
                Log.i("MoPub", "Couldn't call custom method because the server did not specify one.");
                mpv.loadFailUrl(MoPubErrorCode.ADAPTER_NOT_FOUND);
                return;
            }

            String methodName = mHeader.getValue();
            Log.i("MoPub", "Trying to call method named " + methodName);

            Class<? extends Activity> c;
            Method method;
            Activity userActivity = mpv.getActivity();
            try {
                c = userActivity.getClass();
                method = c.getMethod(methodName, MoPubView.class);
                method.invoke(userActivity, mpv);
            } catch (NoSuchMethodException e) {
                Log.d("MoPub", "Couldn't perform custom method named " + methodName +
                        "(MoPubView view) because your activity class has no such method");
                mpv.loadFailUrl(MoPubErrorCode.ADAPTER_NOT_FOUND);
                return;
            } catch (Exception e) {
                Log.d("MoPub", "Couldn't perform custom method named " + methodName);
                mpv.loadFailUrl(MoPubErrorCode.ADAPTER_NOT_FOUND);
                return;
            }
        }

        public void cleanup() {
            mHeader = null;
        }

    }
    /*
     * This is the new way of performing Custom Events. This will  be invoked on new clients when
     * X-Adtype is "custom" and the X-Custom-Event-Class-Name header is specified.
     */
    private static class PerformCustomEventTaskResult extends AdFetchResult {

        protected Map<String,String> mParamsMap;
        public PerformCustomEventTaskResult(AdView adView, Map<String,String> paramsMap) {
            super(adView);
            mParamsMap = paramsMap;
        }

        public void execute() {
            AdView adView = mWeakAdView.get();
            if (adView == null || adView.isDestroyed()) {
                return;
            }

            adView.setIsLoading(false);
            MoPubView moPubView = adView.getMoPubView();

            if (mParamsMap == null) {
                Log.i("MoPub", "Couldn't invoke custom event because the server did not specify one.");
                moPubView.loadFailUrl(MoPubErrorCode.ADAPTER_NOT_FOUND);
                return;
            }

            moPubView.loadCustomEvent(mParamsMap);
        }

        public void cleanup() {
            mParamsMap = null;
        }

    }
    private static class LoadNativeAdTaskResult extends AdFetchResult {

        protected Map<String, String> mParamsMap;
        private LoadNativeAdTaskResult(AdView adView, Map<String, String> paramsMap) {
            super(adView);
            mParamsMap = paramsMap;
        }

        public void execute() {
            AdView adView = mWeakAdView.get();
            if (adView == null || adView.isDestroyed()) {
                return;
            }

            adView.setIsLoading(false);
            MoPubView mpv = adView.getMoPubView();
            mpv.loadNativeSDK(mParamsMap);
        }

        public void cleanup() {
            mParamsMap = null;
        }

    }
    private static class LoadHtmlAdTaskResult extends AdFetchResult {

        protected String mData;
        private LoadHtmlAdTaskResult(AdView adView, String data) {
            super(adView);
            mData = data;
        }

        public void execute() {
            AdView adView = mWeakAdView.get();
            if (adView == null || adView.isDestroyed()) {
                return;
            }

            if (mData == null) {
                return;
            }

            adView.setResponseString(mData);
            adView.loadDataWithBaseURL("http://" + adView.getServerHostname() + "/", mData,
                    "text/html", "utf-8", null);
        }

        public void cleanup() {
            mData = null;
        }

    }
}
