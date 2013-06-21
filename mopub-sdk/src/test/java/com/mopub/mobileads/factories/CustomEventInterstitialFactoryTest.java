package com.mopub.mobileads.factories;

import com.mopub.mobileads.CustomEventInterstitial;
import com.mopub.mobileads.test.support.SdkTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.mopub.mobileads.AdTypeTranslator.HTML_INTERSTITIAL;
import static com.mopub.mobileads.AdTypeTranslator.MRAID_INTERSTITIAL;
import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(SdkTestRunner.class)
public class CustomEventInterstitialFactoryTest {

    private CustomEventInterstitialFactory subject;

    @Before
    public void setup() {
        subject = new CustomEventInterstitialFactory();
    }

    @Test
    public void create_shouldCreateInterstitials() throws Exception {
        assertCustomEventClassCreated(MRAID_INTERSTITIAL);
        assertCustomEventClassCreated(HTML_INTERSTITIAL);
    }

    private void assertCustomEventClassCreated(String customEventName) throws Exception {
        CustomEventInterstitial customEventInterstitial = subject.internalCreate(customEventName);
        assertThat(customEventInterstitial.getClass().getName()).isEqualTo(customEventName);
    }
}
