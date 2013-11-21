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
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@RunWith(SdkTestRunner.class)
public class AdTypeTranslatorTest {
    private AdTypeTranslator subject;
    private String customEventName;
    private MoPubView moPubView;
    private MoPubInterstitial.MoPubInterstitialView moPubInterstitialView;

    @Before
    public void setUp() throws Exception {
        subject = new AdTypeTranslator();
        moPubView = mock(MoPubView.class);
        moPubInterstitialView = mock(MoPubInterstitial.MoPubInterstitialView.class);
    }

    @Test
    public void getAdMobBanner() throws Exception {
        customEventName = AdTypeTranslator.getCustomEventNameForAdType(moPubView, "admob_native", null);

        assertThat(customEventName).isEqualTo(AdTypeTranslator.ADMOB_BANNER);
    }

    @Test
    public void getAdMobInterstitial() throws Exception {
        customEventName = AdTypeTranslator.getCustomEventNameForAdType(moPubInterstitialView, "interstitial", "admob_full");

        assertThat(customEventName).isEqualTo(AdTypeTranslator.ADMOB_INTERSTITIAL);
    }

    @Test
    public void getMillennialBanner() throws Exception {
        customEventName = AdTypeTranslator.getCustomEventNameForAdType(moPubView, "millennial_native", null);

        assertThat(customEventName).isEqualTo(AdTypeTranslator.MILLENNIAL_BANNER);
    }

    @Test
    public void getMillennnialInterstitial() throws Exception {
        customEventName = AdTypeTranslator.getCustomEventNameForAdType(moPubInterstitialView, "interstitial", "millennial_full");

        assertThat(customEventName).isEqualTo(AdTypeTranslator.MILLENNIAL_INTERSTITIAL);
    }

    @Test
    public void getMraidBanner() throws Exception {
        customEventName = AdTypeTranslator.getCustomEventNameForAdType(moPubView, "mraid", null);

        assertThat(customEventName).isEqualTo(AdTypeTranslator.MRAID_BANNER);
    }

    @Test
    public void getMraidInterstitial() throws Exception {
        customEventName = AdTypeTranslator.getCustomEventNameForAdType(moPubInterstitialView, "mraid", null);

        assertThat(customEventName).isEqualTo(AdTypeTranslator.MRAID_INTERSTITIAL);
    }

    @Test
    public void getHtmlBanner() throws Exception {
        customEventName = AdTypeTranslator.getCustomEventNameForAdType(moPubView, "html", null);

        assertThat(customEventName).isEqualTo(AdTypeTranslator.HTML_BANNER);
    }

    @Test
    public void getHtmlInterstitial() throws Exception {
        customEventName = AdTypeTranslator.getCustomEventNameForAdType(moPubInterstitialView, "html", null);

        assertThat(customEventName).isEqualTo(AdTypeTranslator.HTML_INTERSTITIAL);
    }

    @Test
    public void getVastInterstitial() throws Exception {
        customEventName = AdTypeTranslator.getCustomEventNameForAdType(moPubInterstitialView, "interstitial", "vast");

        assertThat(customEventName).isEqualTo(AdTypeTranslator.VAST_VIDEO_INTERSTITIAL);
    }

    @Test
    public void getCustomEventNameForAdType_whenSendingNonsense_shouldReturnNull() throws Exception {
        customEventName = AdTypeTranslator.getCustomEventNameForAdType(null, null, null);

        assertThat(customEventName).isNull();
    }
}
