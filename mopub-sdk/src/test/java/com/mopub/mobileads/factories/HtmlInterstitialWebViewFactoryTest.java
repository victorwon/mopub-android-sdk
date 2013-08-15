package com.mopub.mobileads.factories;

import android.app.Activity;
import com.mopub.mobileads.test.support.SdkTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.fest.assertions.api.Assertions.assertThat;


@RunWith(SdkTestRunner.class)
public class HtmlInterstitialWebViewFactoryTest {
    @Before
    public void setup() {
        HtmlInterstitialWebViewFactory.setInstance(new HtmlInterstitialWebViewFactory());
    }

    @Test
    public void shouldBeAbleToReinitialize() throws Exception {
        HtmlInterstitialWebViewFactory.initialize(new Activity());

        assertThat(HtmlInterstitialWebViewFactory.create(null, false, "", "")).isNotNull();

        HtmlInterstitialWebViewFactory.cleanup();
        HtmlInterstitialWebViewFactory.initialize(new Activity());

        assertThat(HtmlInterstitialWebViewFactory.create(null, false, "", "")).isNotNull();
    }
}
