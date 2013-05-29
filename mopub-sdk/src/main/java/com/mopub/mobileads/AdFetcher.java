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
import android.net.Uri;
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
 * AdFetcher is a delegate of an AdViewController that handles loading ad data over a
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
    public static final String CUSTOM_EVENT_HTML_DATA = "X-Custom-Event-Html-Data";
    @Deprecated
    public static final String CUSTOM_SELECTOR_HEADER = "X-Customselector";
    public static final String NATIVE_PARAMS_HEADER = "X-Nativeparams";
    public static final String FULL_AD_TYPE_HEADER = "X-Fulladtype";
    public static final String MRAID_HTML_DATA = "Mraid-Html-Data";
    private int mTimeoutMilliseconds = 10000;
    // This is equivalent to Build.VERSION_CODES.ICE_CREAM_SANDWICH
    private static final int VERSION_CODE_ICE_CREAM_SANDWICH = 14;

    private AdViewController mAdViewController;
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

    public AdFetcher(AdViewController adview, String userAgent) {
        mAdViewController = adview;
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

        mAdViewController = null;
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

    static class AdFetchTask extends AsyncTask<String, Void, AdLoadTask> {
        private AdFetcher mAdFetcher;
        private AdViewController mAdViewController;
        private Exception mException;
        private HttpClient mHttpClient;
        private long mTaskId;

        private FetchStatus mFetchStatus = FetchStatus.NOT_SET;

        private static final int MAXIMUM_REFRESH_TIME_MILLISECONDS = 600000;
        private static final double EXPONENTIAL_BACKOFF_FACTOR = 1.5;

        AdFetchTask(AdFetcher adFetcher) {
            mAdFetcher = adFetcher;

            mAdViewController = mAdFetcher.mAdViewController;
            mHttpClient = getDefaultHttpClient();
            mTaskId = mAdFetcher.mCurrentTaskId;
        }

        @Override
        protected AdLoadTask doInBackground(String... urls) {
            AdLoadTask result = null;
            try {
                result = fetch(urls[0]);
            } catch (Exception exception) {
                mException = exception;
            } finally {
                shutdownHttpClient();
            }
            return result;
        }

        private AdLoadTask fetch(String url) throws Exception {
            HttpGet httpget = new HttpGet(url);
            httpget.addHeader(USER_AGENT_HEADER, mAdFetcher.mUserAgent);

            if (!isStateValid()) return null;

            HttpResponse response = mHttpClient.execute(httpget);

            if (!isResponseValid(response)) return null;

            mAdViewController.configureUsingHttpResponse(response);

            if (!responseContainsContent(response)) return null;

            return extractAdLoadTaskFromResponse(response);
        }

        AdLoadTask extractAdLoadTaskFromResponse(HttpResponse response) throws IOException {
            String adType = getHeaderValue(response, AD_TYPE_HEADER);
            String adTypeCustomEventName = getAdTypeCustomEventName(response);

            if ("custom".equals(adType)) {
                return extractCustomEventAdLoadTask(response);
            } else if ("mraid".equals(adType)) {
                return extractCustomEventMraidAdLoadTask(response, adTypeCustomEventName);
            } else if (adTypeCustomEventName != null) {
                return extractCustomEventDelegateAdLoadTask(response, adTypeCustomEventName);
            } else {
                return extractHtmlAdLoadTask(response);
            }
        }

        private String getAdTypeCustomEventName(HttpResponse response) {
            String adType = getHeaderValue(response, AD_TYPE_HEADER);
            String fullAdType = getHeaderValue(response, FULL_AD_TYPE_HEADER);

            if ("mraid".equals(adType)) {
                if (mAdViewController.getMoPubView() instanceof MoPubInterstitial.MoPubInterstitialView) {
                    adType = "interstitial";
                    fullAdType = "mraid";
                }
            }

            return AdTypeTranslator.getCustomEventNameForAdType(adType, fullAdType);
        }

        private AdLoadTask extractCustomEventAdLoadTask(HttpResponse response) {
            Log.i("MoPub", "Performing custom event.");

            // If applicable, try to invoke the new custom event system (which uses custom classes)
            String customEventName = getHeaderValue(response, CUSTOM_EVENT_NAME_HEADER);
            if (customEventName != null) {
                String customEventData = getHeaderValue(response, CUSTOM_EVENT_DATA_HEADER);
                return createCustomEventAdLoadTask(customEventName, customEventData);
            }

            // Otherwise, use the (deprecated) legacy custom event system for older clients
            Header oldCustomEventHeader = response.getFirstHeader(CUSTOM_SELECTOR_HEADER);
            return new LegacyCustomEventAdLoadTask(mAdViewController, oldCustomEventHeader);
        }

        private AdLoadTask extractCustomEventDelegateAdLoadTask(HttpResponse response, String adTypeCustomEventName) throws IOException {
            String eventData = getHeaderValue(response, NATIVE_PARAMS_HEADER);

            return createCustomEventAdLoadTask(adTypeCustomEventName, eventData);
        }

        AdLoadTask extractCustomEventMraidAdLoadTask(HttpResponse response, String adTypeCustomEventName) throws IOException {
            String htmlData = httpEntityToString(response.getEntity());
            Map<String, String> eventDataMap = new HashMap<String, String>();
            eventDataMap.put(MRAID_HTML_DATA, Uri.encode(htmlData));
            String eventData = Utils.mapToJsonString(eventDataMap);

            return createCustomEventAdLoadTask(adTypeCustomEventName, eventData);
        }

        private AdLoadTask extractHtmlAdLoadTask(HttpResponse response) throws IOException {
            String data = httpEntityToString(response.getEntity());
            return new HtmlAdLoadTask(mAdViewController, data);
        }

        private boolean responseContainsContent(HttpResponse response) {
            // Ensure that the ad is not warming up.
            if ("1".equals(getHeaderValue(response, WARMUP_HEADER))) {
                Log.d("MoPub", "Ad Unit (" + mAdViewController.getAdUnitId() + ") is still warming up. " +
                        "Please try again in a few minutes.");
                mFetchStatus = FetchStatus.AD_WARMING_UP;
                return false;
            }

            // Ensure that the ad type header is valid and not "clear".
            String adType = getHeaderValue(response, AD_TYPE_HEADER);
            if ("clear".equals(adType)) {
                Log.d("MoPub", "No inventory found for adunit (" + mAdViewController.getAdUnitId() + ").");
                mFetchStatus = FetchStatus.CLEAR_AD_TYPE;
                return false;
            }

            return true;
        }

        private boolean isResponseValid(HttpResponse response) {
            if (response == null || response.getEntity() == null) {
                Log.d("MoPub", "MoPub server returned null response.");
                mFetchStatus = FetchStatus.INVALID_SERVER_RESPONSE_NOBACKOFF;
                return false;
            }

            final int statusCode = response.getStatusLine().getStatusCode();

            // Client and Server HTTP errors should result in an exponential backoff
            if (statusCode >= 400) {
                Log.d("MoPub", "Server error: returned HTTP status code " + Integer.toString(statusCode) +
                        ". Please try again.");
                mFetchStatus = FetchStatus.INVALID_SERVER_RESPONSE_BACKOFF;
                return false;
            }
            // Other non-200 HTTP status codes should still fail
            else if (statusCode != HttpStatus.SC_OK) {
                Log.d("MoPub", "MoPub server returned invalid response: HTTP status code " +
                        Integer.toString(statusCode) + ".");
                mFetchStatus = FetchStatus.INVALID_SERVER_RESPONSE_NOBACKOFF;
                return false;
            }
            return true;
        }

        private boolean isStateValid() {
            // We check to see if this AsyncTask was cancelled, as per
            // http://developer.android.com/reference/android/os/AsyncTask.html
            if (isCancelled()) {
                mFetchStatus = FetchStatus.FETCH_CANCELLED;
                return false;
            }

            if (mAdViewController == null || mAdViewController.isDestroyed()) {
                Log.d("MoPub", "Error loading ad: AdViewController has already been GCed or destroyed.");
                return false;
            }
            return true;
        }

        private AdLoadTask createCustomEventAdLoadTask(String customEventName, String customEventData) {
            Map<String, String> paramsMap = new HashMap<String, String>();
            paramsMap.put(CUSTOM_EVENT_NAME_HEADER, customEventName);

            if (customEventData != null) {
                paramsMap.put(CUSTOM_EVENT_DATA_HEADER, customEventData);
            }

            return new CustomEventAdLoadTask(mAdViewController, paramsMap);
        }

        @Override
        protected void onPostExecute(AdLoadTask adLoadTask) {
            if (!isMostCurrentTask()) {
                Log.d("MoPub", "Ad response is stale.");
                releaseResources();
                return;
            }

            // If cleanup() has already been called on the AdViewController, don't proceed.
            if (mAdViewController == null || mAdViewController.isDestroyed()) {
                if (adLoadTask != null) {
                    adLoadTask.cleanup();
                }
                mAdFetcher.markTaskCompleted(mTaskId);
                releaseResources();
                return;
            }

            if (adLoadTask == null) {
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

                mAdViewController.adDidFail(errorCode);

                /*
                 * There are numerous reasons for the ad fetch to fail, but only in the specific
                 * case of actual server failure should we exponentially back off.
                 *
                 * Note: We place the exponential backoff after AdViewController's adDidFail because we only
                 * want to increase refresh times after the first failure refresh timer is
                 * scheduled, and not before.
                 */
                if (mFetchStatus == FetchStatus.INVALID_SERVER_RESPONSE_BACKOFF) {
                    exponentialBackoff();
                    mFetchStatus = FetchStatus.NOT_SET;
                }
            } else {
                adLoadTask.execute();
                adLoadTask.cleanup();
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
         * It exponentially increases the parent AdViewController's refreshTime up to a specified cap.
         */
        private void exponentialBackoff() {
            if (mAdViewController == null) {
                return;
            }

            int refreshTimeMilliseconds = mAdViewController.getRefreshTimeMilliseconds();

            refreshTimeMilliseconds = (int) (refreshTimeMilliseconds * EXPONENTIAL_BACKOFF_FACTOR);
            if (refreshTimeMilliseconds > MAXIMUM_REFRESH_TIME_MILLISECONDS) {
                refreshTimeMilliseconds = MAXIMUM_REFRESH_TIME_MILLISECONDS;
            }

            mAdViewController.setRefreshTimeMilliseconds(refreshTimeMilliseconds);
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

    private static abstract class AdLoadTask {

        WeakReference<AdViewController> mWeakAdView;
        public AdLoadTask(AdViewController adViewController) {
            mWeakAdView = new WeakReference<AdViewController>(adViewController);
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
    static class LegacyCustomEventAdLoadTask extends AdLoadTask {

        protected Header mHeader;
        public LegacyCustomEventAdLoadTask(AdViewController adViewController, Header header) {
            super(adViewController);
            mHeader = header;
        }

        public void execute() {
            AdViewController adViewController = mWeakAdView.get();
            if (adViewController == null || adViewController.isDestroyed()) {
                return;
            }

            adViewController.setNotLoading();
            MoPubView mpv = adViewController.getMoPubView();

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
    static class CustomEventAdLoadTask extends AdLoadTask {

        protected Map<String,String> mParamsMap;
        public CustomEventAdLoadTask(AdViewController adViewController, Map<String, String> paramsMap) {
            super(adViewController);
            mParamsMap = paramsMap;
        }

        public void execute() {
            AdViewController adViewController = mWeakAdView.get();
            if (adViewController == null || adViewController.isDestroyed()) {
                return;
            }

            adViewController.setNotLoading();
            MoPubView moPubView = adViewController.getMoPubView();

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

    static class HtmlAdLoadTask extends AdLoadTask {

        protected String mData;
        private HtmlAdLoadTask(AdViewController adViewController, String data) {
            super(adViewController);
            mData = data;
        }

        public void execute() {
            AdViewController adViewController = mWeakAdView.get();
            if (adViewController == null || adViewController.isDestroyed()) {
                return;
            }

            if (mData == null) {
                return;
            }

            adViewController.setResponseString(mData);
            adViewController.loadResponseString(mData);
        }

        public void cleanup() {
            mData = null;
        }

    }
}
