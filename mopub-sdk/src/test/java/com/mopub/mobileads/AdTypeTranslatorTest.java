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
    public void getCustomEventNameForAdType_whenSendingNonsense_shouldReturnNull() throws Exception {
        customEventName = AdTypeTranslator.getCustomEventNameForAdType(null, null, null);

        assertThat(customEventName).isNull();
    }
}
