package com.mopub.mobileads;

import android.os.Build;
import com.mopub.mobileads.factories.AdFetchTaskFactory;
import com.mopub.mobileads.test.support.SdkTestRunner;
import com.mopub.mobileads.test.support.TestAdFetchTaskFactory;
import com.mopub.mobileads.test.support.TestHttpResponseWithHeaders;
import org.apache.http.HttpResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

import static com.mopub.mobileads.AdFetcher.*;
import static com.mopub.mobileads.util.VersionCode.HONEYCOMB_MR2;
import static com.mopub.mobileads.util.VersionCode.ICE_CREAM_SANDWICH;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.stub;
import static org.mockito.Mockito.verify;

@RunWith(SdkTestRunner.class)
public class AdFetcherTest {
    private AdFetcher subject;
    private AdViewController adViewController;
    private MoPubView moPubView;
    private HttpResponse response;

    @Before
    public void setup() {
        adViewController = mock(AdViewController.class);
        moPubView = mock(MoPubView.class);
        stub(adViewController.getMoPubView()).toReturn(moPubView);

        subject = new AdFetcher(adViewController, "expected userAgent");
        response = new TestHttpResponseWithHeaders(200, "yahoo!!!");
    }

    @Test
    public void shouldSendResponseToAdView() {
        Robolectric.addPendingHttpResponse(response);

        subject.fetchAdForUrl("url");

        verify(adViewController).configureUsingHttpResponse(eq(response));
    }

    @Test
    public void fetchAdForUrl_shouldRouteMillennialBannerToCustomEventHandling() throws Exception {
        String json = "{\"adWidth\": 320, \"adHeight\": 50, \"adUnitID\": \"44310\"}";
        response.addHeader(AD_TYPE_HEADER, "millennial_native");
        response.addHeader(NATIVE_PARAMS_HEADER, json);
        Robolectric.addPendingHttpResponse(response);

        subject.fetchAdForUrl("ignored_url");

        Map<String, String> paramsMap = new HashMap<String, String>();
        paramsMap.put(CUSTOM_EVENT_NAME_HEADER, "com.mopub.mobileads.MillennialBanner");
        paramsMap.put(CUSTOM_EVENT_DATA_HEADER, json);

        verify(moPubView).loadCustomEvent(eq(paramsMap));
    }

    @Test
    public void fetchAdForUrl_shouldRouteMillennialInterstitialToCustomEventHandling() throws Exception {
        AdViewController interstitialAdViewController = mock(AdViewController.class);
        MoPubInterstitial.MoPubInterstitialView moPubInterstitialView = mock(MoPubInterstitial.MoPubInterstitialView.class);
        stub(interstitialAdViewController.getMoPubView()).toReturn(moPubInterstitialView);
        subject = new AdFetcher(interstitialAdViewController, "expected userAgent");

        String json = "{\"adWidth\": 320, \"adHeight\": 480, \"adUnitID\": \"44310\"}";
        response.addHeader(AD_TYPE_HEADER, "interstitial");
        response.addHeader(FULL_AD_TYPE_HEADER, "millennial_full");
        response.addHeader(NATIVE_PARAMS_HEADER, json);
        Robolectric.addPendingHttpResponse(response);

        subject.fetchAdForUrl("ignored_url");

        Map<String, String> paramsMap = new HashMap<String, String>();
        paramsMap.put(CUSTOM_EVENT_NAME_HEADER, "com.mopub.mobileads.MillennialInterstitial");
        paramsMap.put(CUSTOM_EVENT_DATA_HEADER, json);

        verify(moPubInterstitialView).loadCustomEvent(eq(paramsMap));
    }

    @Test
    public void fetchAdForUrl_whenApiLevelIsAtLeastICS_shouldExecuteUsingAnExecutor() throws Exception {
        Robolectric.Reflection.setFinalStaticField(Build.VERSION.class, "SDK_INT", ICE_CREAM_SANDWICH.getApiLevel());
        AdFetchTaskFactory.setInstance(new TestAdFetchTaskFactory());
        AdFetchTask adFetchTask = TestAdFetchTaskFactory.getSingletonMock();

        subject.fetchAdForUrl("some url");

        verify(adFetchTask).executeOnExecutor(eq(AdFetchTask.THREAD_POOL_EXECUTOR), eq("some url"));
        verify(adFetchTask, never()).execute(anyString());
    }

    @Test
    public void fetchAdForUrl_whenApiLevelIsBelowICS_shouldExecuteWithoutAnExecutor() throws Exception {
        Robolectric.Reflection.setFinalStaticField(Build.VERSION.class, "SDK_INT", HONEYCOMB_MR2.getApiLevel());
        AdFetchTaskFactory.setInstance(new TestAdFetchTaskFactory());
        AdFetchTask adFetchTask = TestAdFetchTaskFactory.getSingletonMock();

        subject.fetchAdForUrl("some url");

        verify(adFetchTask, never()).executeOnExecutor(any(Executor.class), anyString());
        verify(adFetchTask).execute(eq("some url"));
    }
}
