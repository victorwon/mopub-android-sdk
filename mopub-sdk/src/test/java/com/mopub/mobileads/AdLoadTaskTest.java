/*
 * Copyright (c) 2010-2013, MoPub Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *  Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 *  Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 *  Neither the name of 'MoPub Inc.' nor the names of its contributors
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

import com.mopub.mobileads.test.support.SdkTestRunner;
import com.mopub.mobileads.test.support.TestHttpResponseWithHeaders;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.mopub.mobileads.AdTypeTranslator.ADMOB_BANNER;
import static com.mopub.mobileads.AdTypeTranslator.HTML_BANNER;
import static com.mopub.mobileads.AdTypeTranslator.HTML_INTERSTITIAL;
import static com.mopub.mobileads.AdTypeTranslator.MRAID_BANNER;
import static com.mopub.mobileads.AdTypeTranslator.MRAID_INTERSTITIAL;
import static com.mopub.mobileads.util.ResponseHeader.AD_TYPE;
import static com.mopub.mobileads.util.ResponseHeader.CLICKTHROUGH_URL;
import static com.mopub.mobileads.util.ResponseHeader.CUSTOM_EVENT_DATA;
import static com.mopub.mobileads.util.ResponseHeader.CUSTOM_EVENT_NAME;
import static com.mopub.mobileads.util.ResponseHeader.CUSTOM_SELECTOR;
import static com.mopub.mobileads.util.ResponseHeader.NATIVE_PARAMS;
import static com.mopub.mobileads.util.ResponseHeader.REDIRECT_URL;
import static com.mopub.mobileads.util.ResponseHeader.SCROLLABLE;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.stub;

@RunWith(SdkTestRunner.class)
public class AdLoadTaskTest {

    private AdViewController adViewController;
    private HttpResponse response;
    private String standardExpectedJson;
    private AdConfiguration adConfiguration;

    @Before
    public void setup() {
        adViewController = mock(AdViewController.class);
        adConfiguration = mock(AdConfiguration.class);
        stub(adViewController.getAdConfiguration()).toReturn(adConfiguration);
        response = new TestHttpResponseWithHeaders(200, "");
        standardExpectedJson = "{\"Scrollable\":\"false\",\"Redirect-Url\":\"redirect\",\"Clickthrough-Url\":\"clickthrough\",\"Html-Response-Body\":\"%3Chtml%3E%3C%2Fhtml%3E\"}";
    }

    @Test
    public void fromHttpResponse_whenCustomEvent_shouldGetNameAndData() throws Exception {
        String expectedCustomData = "Custom data";
        response.addHeader(AD_TYPE.getKey(), "custom");
        String expectedCustomEventName = "custom event name";
        response.addHeader(CUSTOM_EVENT_NAME.getKey(), expectedCustomEventName);
        response.addHeader(CUSTOM_EVENT_DATA.getKey(), expectedCustomData);

        AdLoadTask.CustomEventAdLoadTask customEventTask = (AdLoadTask.CustomEventAdLoadTask) AdLoadTask.fromHttpResponse(response, adViewController);
        assertThat(customEventTask.getParamsMap().get(CUSTOM_EVENT_NAME.getKey())).isEqualTo(expectedCustomEventName);
        assertThat(customEventTask.getParamsMap().get(CUSTOM_EVENT_DATA.getKey())).isEqualTo(expectedCustomData);
    }

    @Test
    public void fromHttpResponse_whenNoCustomEventName_shouldCreateLegacyCustomEventAdLoadTaskWithAHeader() throws Exception {
        String expectedCustomData = "Custom data";
        String expectedHeaderValue = "some stuff";
        response.addHeader(AD_TYPE.getKey(), "custom");
        response.addHeader(CUSTOM_EVENT_DATA.getKey(), expectedCustomData);
        response.addHeader(CUSTOM_SELECTOR.getKey(), expectedHeaderValue);

        AdLoadTask.LegacyCustomEventAdLoadTask customEventTask = (AdLoadTask.LegacyCustomEventAdLoadTask) AdLoadTask.fromHttpResponse(response, adViewController);
        Header taskHeader = customEventTask.getHeader();
        assertThat(taskHeader).isNotNull();
        assertThat(taskHeader.getName()).isEqualTo(CUSTOM_SELECTOR.getKey());
        assertThat(taskHeader.getValue()).isEqualTo(expectedHeaderValue);
    }

    @Test
    public void fromHttpResponse_whenMraidBanner_shouldCreateAnEncodedJsonString() throws Exception {
        String htmlData = "<html></html>";
        response = new TestHttpResponseWithHeaders(200, htmlData);
        addExpectedResponseHeaders("mraid");

        AdLoadTask.CustomEventAdLoadTask customEventTask = (AdLoadTask.CustomEventAdLoadTask) AdLoadTask.fromHttpResponse(response, adViewController);
        assertThat(customEventTask.getParamsMap().get(CUSTOM_EVENT_NAME.getKey())).isEqualTo(MRAID_BANNER);
        assertThat(customEventTask.getParamsMap().get(CUSTOM_EVENT_DATA.getKey())).isEqualTo(standardExpectedJson);
    }

    @Test
    public void fromHttpResponse_whenMraidInterstitial_shouldCreateAnEncodedJsonString() throws Exception {
        String htmlData = "<html></html>";
        response = new TestHttpResponseWithHeaders(200, htmlData);
        addExpectedResponseHeaders("mraid");
        stub(adViewController.getMoPubView()).toReturn(mock(MoPubInterstitial.MoPubInterstitialView.class));

        AdLoadTask.CustomEventAdLoadTask customEventTask = (AdLoadTask.CustomEventAdLoadTask) AdLoadTask.fromHttpResponse(response, adViewController);
        assertThat(customEventTask.getParamsMap().get(CUSTOM_EVENT_NAME.getKey())).isEqualTo(MRAID_INTERSTITIAL);
        assertThat(customEventTask.getParamsMap().get(CUSTOM_EVENT_DATA.getKey())).isEqualTo(standardExpectedJson);
    }

    @Test
    public void fromHttpResponse_whenCustomEventDelegate_shouldConvertAdMobToCustomEvent() throws Exception {
        String expectedNativeParams = "{\"this is a json\":\"map\",\"whee\":\"look at me\"}";
        response.addHeader(AD_TYPE.getKey(), "admob_native");
        response.addHeader(NATIVE_PARAMS.getKey(), expectedNativeParams);

        AdLoadTask.CustomEventAdLoadTask customEventTask = (AdLoadTask.CustomEventAdLoadTask) AdLoadTask.fromHttpResponse(response, adViewController);
        assertThat(customEventTask.getParamsMap().get(CUSTOM_EVENT_NAME.getKey())).isEqualTo(ADMOB_BANNER);
        assertThat(customEventTask.getParamsMap().get(CUSTOM_EVENT_DATA.getKey())).isEqualTo(expectedNativeParams);
    }

    @Test
    public void fromHttpResponse_whenHtmlBanner_shouldConvertToCustomEventBanner() throws Exception {
        String htmlData = "<html></html>";
        response = new TestHttpResponseWithHeaders(200, htmlData);
        addExpectedResponseHeaders("html");

        AdLoadTask.CustomEventAdLoadTask customEventTask = (AdLoadTask.CustomEventAdLoadTask) AdLoadTask.fromHttpResponse(response, adViewController);
        assertThat(customEventTask.getParamsMap().get(CUSTOM_EVENT_NAME.getKey())).isEqualTo(HTML_BANNER);
        assertThat(customEventTask.getParamsMap().get(CUSTOM_EVENT_DATA.getKey())).isEqualTo(standardExpectedJson);
    }

    @Test
    public void fromHttpResponse_whenHtmlInterstitial_shouldConvertToCustomEventInterstitial() throws Exception {
        String htmlData = "<html></html>";
        response = new TestHttpResponseWithHeaders(200, htmlData);
        addExpectedResponseHeaders("html");
        stub(adViewController.getMoPubView()).toReturn(mock(MoPubInterstitial.MoPubInterstitialView.class));

        AdLoadTask.CustomEventAdLoadTask customEventTask = (AdLoadTask.CustomEventAdLoadTask) AdLoadTask.fromHttpResponse(response, adViewController);
        assertThat(customEventTask.getParamsMap().get(CUSTOM_EVENT_NAME.getKey())).isEqualTo(HTML_INTERSTITIAL);
        assertThat(customEventTask.getParamsMap().get(CUSTOM_EVENT_DATA.getKey())).isEqualTo(standardExpectedJson);
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
        response.addHeader(AD_TYPE.getKey(), "html");

        AdLoadTask.CustomEventAdLoadTask customEventTask = (AdLoadTask.CustomEventAdLoadTask) AdLoadTask.fromHttpResponse(response, adViewController);
        assertThat(customEventTask.getParamsMap().get(CUSTOM_EVENT_NAME.getKey())).isEqualTo(HTML_BANNER);
        assertThat(customEventTask.getParamsMap().get(CUSTOM_EVENT_DATA.getKey())).isEqualTo(expectedJson);
    }

    @Test
    public void fromHttpResponse_whenScrollableIsOne_shouldBeReflectedInJson() throws Exception {
        String expectedJson = "{\"Scrollable\":\"true\",\"Html-Response-Body\":\"\"}";
        response.addHeader(SCROLLABLE.getKey(), "1");
        response.addHeader(AD_TYPE.getKey(), "html");


        AdLoadTask.CustomEventAdLoadTask customEventTask = (AdLoadTask.CustomEventAdLoadTask) AdLoadTask.fromHttpResponse(response, adViewController);
        assertThat(customEventTask.getParamsMap().get(CUSTOM_EVENT_NAME.getKey())).isEqualTo(HTML_BANNER);
        assertThat(customEventTask.getParamsMap().get(CUSTOM_EVENT_DATA.getKey())).isEqualTo(expectedJson);
    }

    @Test
    public void fromHttpResponse_whenScrollableIsNotSpecified_shouldDefaultToFalseInJson() throws Exception {
        String expectedJson = "{\"Scrollable\":\"false\",\"Html-Response-Body\":\"\"}";
        response.addHeader(AD_TYPE.getKey(), "html");

        AdLoadTask.CustomEventAdLoadTask customEventTask = (AdLoadTask.CustomEventAdLoadTask) AdLoadTask.fromHttpResponse(response, adViewController);
        assertThat(customEventTask.getParamsMap().get(CUSTOM_EVENT_NAME.getKey())).isEqualTo(HTML_BANNER);
        assertThat(customEventTask.getParamsMap().get(CUSTOM_EVENT_DATA.getKey())).isEqualTo(expectedJson);
    }

    private void addExpectedResponseHeaders(String adType) {
        response.addHeader(SCROLLABLE.getKey(), "0");
        response.addHeader(AD_TYPE.getKey(), adType);
        response.addHeader(REDIRECT_URL.getKey(), "redirect");
        response.addHeader(CLICKTHROUGH_URL.getKey(), "clickthrough");
    }
}
