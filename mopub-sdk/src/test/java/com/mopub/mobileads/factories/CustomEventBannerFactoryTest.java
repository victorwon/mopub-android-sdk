package com.mopub.mobileads.factories;

import com.mopub.mobileads.CustomEventBanner;
import com.mopub.mobileads.test.support.SdkTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.mopub.mobileads.AdTypeTranslator.HTML_BANNER;
import static com.mopub.mobileads.AdTypeTranslator.MRAID_BANNER;
import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(SdkTestRunner.class)
public class CustomEventBannerFactoryTest {

    private CustomEventBannerFactory subject;

    @Before
    public void setup() {
        subject = new CustomEventBannerFactory();
    }

    @Test
    public void create_shouldCreateBanners() throws Exception {
        assertCustomEventClassCreated(MRAID_BANNER);
        assertCustomEventClassCreated(HTML_BANNER);
    }

    private void assertCustomEventClassCreated(String customEventName) throws Exception {
        CustomEventBanner customEventBanner = subject.internalCreate(customEventName);
        assertThat(customEventBanner.getClass().getName()).isEqualTo(customEventName);
    }
}
