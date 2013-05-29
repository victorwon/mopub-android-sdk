package com.mopub.mobileads;

import com.mopub.mobileads.test.support.SdkTestRunner;
import com.mopub.mobileads.test.support.TestHttpResponseWithHeaders;
import org.apache.http.HttpResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;

import java.util.HashMap;
import java.util.Map;

import static com.mopub.mobileads.AdFetcher.*;
import static com.mopub.mobileads.AdTypeTranslator.MRAID_BANNER;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
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
    public void extractCustomEventMraidAdLoadTask_shouldCreateAnEncodedJsonString() throws Exception {
        String expectedJson = "{\"Mraid-Html-Data\":\"%3Chtml%3E%3C%2Fhtml%3E\"}";
        AdFetchTask adFetchTask = new AdFetchTask(subject);
        String htmlData = "<html></html>";
        response = new TestHttpResponseWithHeaders(200, htmlData);
        response.addHeader(AD_TYPE_HEADER, "mraid");

        CustomEventAdLoadTask customEventTask = (CustomEventAdLoadTask) adFetchTask.extractCustomEventMraidAdLoadTask(response, MRAID_BANNER);
        assertThat(customEventTask.mParamsMap.get(CUSTOM_EVENT_NAME_HEADER)).isEqualTo(MRAID_BANNER);
        assertThat(customEventTask.mParamsMap.get(CUSTOM_EVENT_DATA_HEADER)).isEqualTo(expectedJson);
    }
}
