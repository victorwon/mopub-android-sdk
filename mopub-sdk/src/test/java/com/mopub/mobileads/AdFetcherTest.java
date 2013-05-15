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
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.stub;
import static org.mockito.Mockito.verify;

@RunWith(SdkTestRunner.class)
public class AdFetcherTest {
    private AdFetcher subject;
    private AdView adView;
    private MoPubView moPubView;
    private HttpResponse response;

    @Before
    public void setup() {
        adView = mock(AdView.class);
        moPubView = mock(MoPubView.class);
        stub(adView.getMoPubView()).toReturn(moPubView);

        subject = new AdFetcher(adView, "expected userAgent");
        response = new TestHttpResponseWithHeaders(200, "yahoo!!!");
    }

    @Test
    public void shouldSendResponseToAdView() {
        Robolectric.addPendingHttpResponse(response);

        subject.fetchAdForUrl("url");

        verify(adView).configureUsingHttpResponse(eq(response));
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
        AdView interstitialAdView = mock(AdView.class);
        MoPubInterstitial.MoPubInterstitialView moPubInterstitialView = mock(MoPubInterstitial.MoPubInterstitialView.class);
        stub(interstitialAdView.getMoPubView()).toReturn(moPubInterstitialView);
        subject = new AdFetcher(interstitialAdView, "expected userAgent");

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
}
