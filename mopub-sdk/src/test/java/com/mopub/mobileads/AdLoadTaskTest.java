package com.mopub.mobileads;

import com.mopub.mobileads.test.support.SdkTestRunner;
import com.mopub.mobileads.test.support.TestHttpResponseWithHeaders;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.mopub.mobileads.AdFetcher.*;
import static com.mopub.mobileads.AdTypeTranslator.*;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.stub;

@RunWith(SdkTestRunner.class)
public class AdLoadTaskTest {

    private AdViewController adViewController;
    private HttpResponse response;
    private String standardExpectedJson;

    @Before
    public void setup() {
        adViewController = mock(AdViewController.class);
        response = new TestHttpResponseWithHeaders(200, "");
        standardExpectedJson = "{\"Scrollable\":\"false\",\"Redirect-Url\":\"redirect\",\"Clickthrough-Url\":\"clickthrough\",\"Html-Response-Body\":\"%3Chtml%3E%3C%2Fhtml%3E\"}";
    }

    @Test
    public void fromHttpResponse_whenCustomEvent_shouldGetNameAndData() throws Exception {
        String expectedCustomData = "Custom data";
        response.addHeader(AD_TYPE_HEADER, "custom");
        String expectedCustomEventName = "custom event name";
        response.addHeader(CUSTOM_EVENT_NAME_HEADER, expectedCustomEventName);
        response.addHeader(CUSTOM_EVENT_DATA_HEADER, expectedCustomData);

        AdLoadTask.CustomEventAdLoadTask customEventTask = (AdLoadTask.CustomEventAdLoadTask) AdLoadTask.fromHttpResponse(response, adViewController);
        assertThat(customEventTask.getParamsMap().get(CUSTOM_EVENT_NAME_HEADER)).isEqualTo(expectedCustomEventName);
        assertThat(customEventTask.getParamsMap().get(CUSTOM_EVENT_DATA_HEADER)).isEqualTo(expectedCustomData);
    }

    @Test
    public void fromHttpResponse_whenNoCustomEventName_shouldCreateLegacyCustomEventAdLoadTaskWithAHeader() throws Exception {
        String expectedCustomData = "Custom data";
        String expectedHeaderValue = "some stuff";
        response.addHeader(AD_TYPE_HEADER, "custom");
        response.addHeader(CUSTOM_EVENT_DATA_HEADER, expectedCustomData);
        response.addHeader(CUSTOM_SELECTOR_HEADER, expectedHeaderValue);

        AdLoadTask.LegacyCustomEventAdLoadTask customEventTask = (AdLoadTask.LegacyCustomEventAdLoadTask) AdLoadTask.fromHttpResponse(response, adViewController);
        Header taskHeader = customEventTask.getHeader();
        assertThat(taskHeader).isNotNull();
        assertThat(taskHeader.getName()).isEqualTo(CUSTOM_SELECTOR_HEADER);
        assertThat(taskHeader.getValue()).isEqualTo(expectedHeaderValue);
    }

    @Test
    public void fromHttpResponse_whenMraidBanner_shouldCreateAnEncodedJsonString() throws Exception {
        String htmlData = "<html></html>";
        response = new TestHttpResponseWithHeaders(200, htmlData);
        addExpectedResponseHeaders("mraid");

        AdLoadTask.CustomEventAdLoadTask customEventTask = (AdLoadTask.CustomEventAdLoadTask) AdLoadTask.fromHttpResponse(response, adViewController);
        assertThat(customEventTask.getParamsMap().get(CUSTOM_EVENT_NAME_HEADER)).isEqualTo(MRAID_BANNER);
        assertThat(customEventTask.getParamsMap().get(CUSTOM_EVENT_DATA_HEADER)).isEqualTo(standardExpectedJson);
    }

    @Test
    public void fromHttpResponse_whenMraidInterstitial_shouldCreateAnEncodedJsonString() throws Exception {
        String htmlData = "<html></html>";
        response = new TestHttpResponseWithHeaders(200, htmlData);
        addExpectedResponseHeaders("mraid");
        stub(adViewController.getMoPubView()).toReturn(mock(MoPubInterstitial.MoPubInterstitialView.class));

        AdLoadTask.CustomEventAdLoadTask customEventTask = (AdLoadTask.CustomEventAdLoadTask) AdLoadTask.fromHttpResponse(response, adViewController);
        assertThat(customEventTask.getParamsMap().get(CUSTOM_EVENT_NAME_HEADER)).isEqualTo(MRAID_INTERSTITIAL);
        assertThat(customEventTask.getParamsMap().get(CUSTOM_EVENT_DATA_HEADER)).isEqualTo(standardExpectedJson);
    }

    @Test
    public void fromHttpResponse_whenCustomEventDelegate_shouldConvertAdMobToCustomEvent() throws Exception {
        String expectedNativeParams = "{\"this is a json\":\"map\",\"whee\":\"look at me\"}";
        response.addHeader(AD_TYPE_HEADER, "admob_native");
        response.addHeader(NATIVE_PARAMS_HEADER, expectedNativeParams);

        AdLoadTask.CustomEventAdLoadTask customEventTask = (AdLoadTask.CustomEventAdLoadTask) AdLoadTask.fromHttpResponse(response, adViewController);
        assertThat(customEventTask.getParamsMap().get(CUSTOM_EVENT_NAME_HEADER)).isEqualTo(ADMOB_BANNER);
        assertThat(customEventTask.getParamsMap().get(CUSTOM_EVENT_DATA_HEADER)).isEqualTo(expectedNativeParams);
    }

    @Test
    public void fromHttpResponse_whenHtmlBanner_shouldConvertToCustomEventBanner() throws Exception {
        String htmlData = "<html></html>";
        response = new TestHttpResponseWithHeaders(200, htmlData);
        addExpectedResponseHeaders("html");

        AdLoadTask.CustomEventAdLoadTask customEventTask = (AdLoadTask.CustomEventAdLoadTask) AdLoadTask.fromHttpResponse(response, adViewController);
        assertThat(customEventTask.getParamsMap().get(CUSTOM_EVENT_NAME_HEADER)).isEqualTo(HTML_BANNER);
        assertThat(customEventTask.getParamsMap().get(CUSTOM_EVENT_DATA_HEADER)).isEqualTo(standardExpectedJson);
    }

    @Test
    public void fromHttpResponse_whenHtmlInterstitial_shouldConvertToCustomEventInterstitial() throws Exception {
        String htmlData = "<html></html>";
        response = new TestHttpResponseWithHeaders(200, htmlData);
        addExpectedResponseHeaders("html");
        stub(adViewController.getMoPubView()).toReturn(mock(MoPubInterstitial.MoPubInterstitialView.class));

        AdLoadTask.CustomEventAdLoadTask customEventTask = (AdLoadTask.CustomEventAdLoadTask) AdLoadTask.fromHttpResponse(response, adViewController);
        assertThat(customEventTask.getParamsMap().get(CUSTOM_EVENT_NAME_HEADER)).isEqualTo(HTML_INTERSTITIAL);
        assertThat(customEventTask.getParamsMap().get(CUSTOM_EVENT_DATA_HEADER)).isEqualTo(standardExpectedJson);
    }

    @Test
    public void fromHttpResponse_whenEntityIsNull_shouldCreateMinimumJsonString() throws Exception {
        String htmlData = "<html></html>";
        String expectedJson = "{\"Scrollable\":\"false\",\"Html-Response-Body\":\"\"}";
        response = new TestHttpResponseWithHeaders(200, htmlData) {
            @Override
            public HttpEntity getEntity() {
                return null;
            }
        };
        response.addHeader(AD_TYPE_HEADER, "html");

        AdLoadTask.CustomEventAdLoadTask customEventTask = (AdLoadTask.CustomEventAdLoadTask) AdLoadTask.fromHttpResponse(response, adViewController);
        assertThat(customEventTask.getParamsMap().get(CUSTOM_EVENT_NAME_HEADER)).isEqualTo(HTML_BANNER);
        assertThat(customEventTask.getParamsMap().get(CUSTOM_EVENT_DATA_HEADER)).isEqualTo(expectedJson);
    }

    @Test
    public void fromHttpResponse_whenScrollableIsOne_shouldBeReflectedInJson() throws Exception {
        String expectedJson = "{\"Scrollable\":\"true\",\"Html-Response-Body\":\"\"}";
        response.addHeader(SCROLLABLE_HEADER, "1");
        response.addHeader(AD_TYPE_HEADER, "html");


        AdLoadTask.CustomEventAdLoadTask customEventTask = (AdLoadTask.CustomEventAdLoadTask) AdLoadTask.fromHttpResponse(response, adViewController);
        assertThat(customEventTask.getParamsMap().get(CUSTOM_EVENT_NAME_HEADER)).isEqualTo(HTML_BANNER);
        assertThat(customEventTask.getParamsMap().get(CUSTOM_EVENT_DATA_HEADER)).isEqualTo(expectedJson);
    }

    @Test
    public void fromHttpResponse_whenScrollableIsNotSpecified_shouldDefaultToFalseInJson() throws Exception {
        String expectedJson = "{\"Scrollable\":\"false\",\"Html-Response-Body\":\"\"}";
        response.addHeader(AD_TYPE_HEADER, "html");

        AdLoadTask.CustomEventAdLoadTask customEventTask = (AdLoadTask.CustomEventAdLoadTask) AdLoadTask.fromHttpResponse(response, adViewController);
        assertThat(customEventTask.getParamsMap().get(CUSTOM_EVENT_NAME_HEADER)).isEqualTo(HTML_BANNER);
        assertThat(customEventTask.getParamsMap().get(CUSTOM_EVENT_DATA_HEADER)).isEqualTo(expectedJson);
    }

    private void addExpectedResponseHeaders(String adType) {
        response.addHeader(SCROLLABLE_HEADER, "0");
        response.addHeader(AD_TYPE_HEADER, adType);
        response.addHeader(REDIRECT_URL_HEADER, "redirect");
        response.addHeader(CLICKTHROUGH_URL_HEADER, "clickthrough");
    }
}
