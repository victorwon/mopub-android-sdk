package com.mopub.mobileads;

import com.mopub.mobileads.test.support.SdkTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(SdkTestRunner.class)
public class AdTypeTranslatorTest {
    private AdTypeTranslator subject;
    private String customEventName;

    @Before
    public void setUp() throws Exception {
        subject = new AdTypeTranslator();
    }

    @Test
    public void getAdMobBanner() throws Exception {
        customEventName = AdTypeTranslator.getCustomEventNameForAdType("admob_native", null);

        assertThat(customEventName).isEqualTo(AdTypeTranslator.ADMOB_BANNER);
    }

    @Test
    public void getAdMobInterstitial() throws Exception {
        customEventName = AdTypeTranslator.getCustomEventNameForAdType("interstitial", "admob_full");

        assertThat(customEventName).isEqualTo(AdTypeTranslator.ADMOB_INTERSTITIAL);
    }

    @Test
    public void getMillennialBanner() throws Exception {
        customEventName = AdTypeTranslator.getCustomEventNameForAdType("millennial_native", null);

        assertThat(customEventName).isEqualTo(AdTypeTranslator.MILLENNIAL_BANNER);
    }

    @Test
    public void getMillennnialInterstitial() throws Exception {
        customEventName = AdTypeTranslator.getCustomEventNameForAdType("interstitial", "millennial_full");

        assertThat(customEventName).isEqualTo(AdTypeTranslator.MILLENNIAL_INTERSTITIAL);
    }

    @Test
    public void getMraidBanner() throws Exception {
        customEventName = AdTypeTranslator.getCustomEventNameForAdType("mraid", null);

        assertThat(customEventName).isEqualTo(AdTypeTranslator.MRAID_BANNER);
    }

    @Test
    public void getMraidInterstitial() throws Exception {
        customEventName = AdTypeTranslator.getCustomEventNameForAdType("interstitial", "mraid");

        assertThat(customEventName).isEqualTo(AdTypeTranslator.MRAID_INTERSTITIAL);
    }

    @Test
    public void getCustomEventNameForAdType_whenSendingNonsense_shouldReturnNull() throws Exception {
        customEventName = AdTypeTranslator.getCustomEventNameForAdType(null, null);

        assertThat(customEventName).isNull();
    }
}
