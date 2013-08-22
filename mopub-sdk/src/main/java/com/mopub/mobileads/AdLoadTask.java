package com.mopub.mobileads;

import android.app.Activity;
import android.net.Uri;
import android.util.Log;
import com.mopub.mobileads.util.Strings;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static com.mopub.mobileads.AdFetcher.*;
import static com.mopub.mobileads.util.HttpResponses.extractBooleanHeader;
import static com.mopub.mobileads.util.HttpResponses.extractHeader;

abstract class AdLoadTask {
    WeakReference<AdViewController> mWeakAdViewController;
    AdLoadTask(AdViewController adViewController) {
        mWeakAdViewController = new WeakReference<AdViewController>(adViewController);
    }

    abstract void execute();

    /*
     * The AsyncTask thread pool often appears to keep references to these
     * objects, preventing GC. This method should be used to release
     * resources to mitigate the GC issue.
     */
    abstract void cleanup();

    static AdLoadTask fromHttpResponse(HttpResponse response, AdViewController adViewController) throws IOException {
        return new TaskExtractor(response, adViewController).extract();
    }

    private static class TaskExtractor {
        private final HttpResponse response;
        private final AdViewController adViewController;
        private String adType;
        private String adTypeCustomEventName;
        private String fullAdType;

        TaskExtractor(HttpResponse response, AdViewController adViewController){
            this.response = response;
            this.adViewController = adViewController;
        }

        AdLoadTask extract() throws IOException {
            adType = extractHeader(response, AD_TYPE_HEADER);
            fullAdType = extractHeader(response, FULL_AD_TYPE_HEADER);

            adTypeCustomEventName = AdTypeTranslator.getCustomEventNameForAdType(
                    adViewController.getMoPubView(), adType, fullAdType);

            if ("custom".equals(adType)) {
                return extractCustomEventAdLoadTask();
            } else if (eventDataIsInResponseBody(adType)) {
                return extractCustomEventAdLoadTaskFromResponseBody();
            } else {
                return extractCustomEventAdLoadTaskFromNativeParams();
            }
        }

        private AdLoadTask extractCustomEventAdLoadTask() {
            Log.i("MoPub", "Performing custom event.");

            // If applicable, try to invoke the new custom event system (which uses custom classes)
            adTypeCustomEventName = extractHeader(response, CUSTOM_EVENT_NAME_HEADER);
            if (adTypeCustomEventName != null) {
                String customEventData = extractHeader(response, CUSTOM_EVENT_DATA_HEADER);
                return createCustomEventAdLoadTask(customEventData);
            }

            // Otherwise, use the (deprecated) legacy custom event system for older clients
            Header oldCustomEventHeader = response.getFirstHeader(CUSTOM_SELECTOR_HEADER);
            return new AdLoadTask.LegacyCustomEventAdLoadTask(adViewController, oldCustomEventHeader);
        }

        private AdLoadTask extractCustomEventAdLoadTaskFromResponseBody() throws IOException {
            HttpEntity entity = response.getEntity();
            String htmlData = entity != null ? Strings.fromStream(entity.getContent()) : "";
            String redirectUrl = extractHeader(response, REDIRECT_URL_HEADER);
            String clickthroughUrl = extractHeader(response, CLICKTHROUGH_URL_HEADER);
            boolean scrollingEnabled = extractBooleanHeader(response, SCROLLABLE_HEADER, false);

            Map<String, String> eventDataMap = new HashMap<String, String>();
            eventDataMap.put(HTML_RESPONSE_BODY_KEY, Uri.encode(htmlData));
            eventDataMap.put(SCROLLABLE_KEY, Boolean.toString(scrollingEnabled));
            if (redirectUrl != null) {
                eventDataMap.put(REDIRECT_URL_KEY, redirectUrl);
            }
            if (clickthroughUrl != null) {
                eventDataMap.put(CLICKTHROUGH_URL_KEY, clickthroughUrl);
            }

            String eventData = Utils.mapToJsonString(eventDataMap);
            return createCustomEventAdLoadTask(eventData);
        }

        private AdLoadTask extractCustomEventAdLoadTaskFromNativeParams() throws IOException {
            String eventData = extractHeader(response, AdFetcher.NATIVE_PARAMS_HEADER);

            return createCustomEventAdLoadTask(eventData);
        }

        private AdLoadTask createCustomEventAdLoadTask(String customEventData) {
            Map<String, String> paramsMap = new HashMap<String, String>();
            paramsMap.put(CUSTOM_EVENT_NAME_HEADER, adTypeCustomEventName);

            if (customEventData != null) {
                paramsMap.put(CUSTOM_EVENT_DATA_HEADER, customEventData);
            }

            return new AdLoadTask.CustomEventAdLoadTask(adViewController, paramsMap);
        }

        private boolean eventDataIsInResponseBody(String adType) {
            return "mraid".equals(this.adType) || "html".equals(adType);
        }
    }

    /*
     * This is the new way of performing Custom Events. This will  be invoked on new clients when
     * X-Adtype is "custom" and the X-Custom-Event-Class-Name header is specified.
     */
    static class CustomEventAdLoadTask extends AdLoadTask {
        private Map<String,String> mParamsMap;

        public CustomEventAdLoadTask(AdViewController adViewController, Map<String, String> paramsMap) {
            super(adViewController);
            mParamsMap = paramsMap;
        }

        @Override
        void execute() {
            AdViewController adViewController = mWeakAdViewController.get();

            if (adViewController == null || adViewController.isDestroyed()) {
                return;
            }

            adViewController.setNotLoading();
            adViewController.getMoPubView().loadCustomEvent(mParamsMap);
        }

        @Override
        void cleanup() {
            mParamsMap = null;
        }

        @Deprecated // for testing
        Map<String, String> getParamsMap() {
            return mParamsMap;
        }
    }

    /*
     * This is the old way of performing Custom Events, and is now deprecated. This will still be
     * invoked on old clients when X-Adtype is "custom" and the new X-Custom-Event-Class-Name header
     * is not specified (legacy custom events parse the X-Customselector header instead).
     */
    @Deprecated
    static class LegacyCustomEventAdLoadTask extends AdLoadTask {
        private Header mHeader;

        public LegacyCustomEventAdLoadTask(AdViewController adViewController, Header header) {
            super(adViewController);
            mHeader = header;
        }

        @Override
        void execute() {
            AdViewController adViewController = mWeakAdViewController.get();
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
            } catch (Exception e) {
                Log.d("MoPub", "Couldn't perform custom method named " + methodName);
                mpv.loadFailUrl(MoPubErrorCode.ADAPTER_NOT_FOUND);
            }
        }

        @Override
        void cleanup() {
            mHeader = null;
        }

        @Deprecated // for testing
        Header getHeader() {
            return mHeader;
        }
    }
}
