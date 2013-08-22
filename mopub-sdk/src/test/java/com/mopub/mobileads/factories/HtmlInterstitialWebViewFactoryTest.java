package com.mopub.mobileads.factories;

import com.mopub.mobileads.MoPubActivity;
import com.mopub.mobileads.test.support.SdkTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;

import static org.fest.assertions.api.Assertions.assertThat;


@RunWith(SdkTestRunner.class)
public class HtmlInterstitialWebViewFactoryTest {
    @Before
    public void setup() {
        HtmlInterstitialWebViewFactory.setInstance(new HtmlInterstitialWebViewFactory());
    }

    @Test
    public void shouldBeAbleToReinitialize() throws Exception {
        HtmlInterstitialWebViewFactory.initialize(Robolectric.buildActivity(MoPubActivity.class).get());

        assertThat(HtmlInterstitialWebViewFactory.create(null, false, "", "")).isNotNull();

        HtmlInterstitialWebViewFactory.cleanup();
        HtmlInterstitialWebViewFactory.initialize(Robolectric.buildActivity(MoPubActivity.class).get());

        assertThat(HtmlInterstitialWebViewFactory.create(null, false, "", "")).isNotNull();

        HtmlInterstitialWebViewFactory.cleanup();
    }

    @Test
    public void create_withTooManyCleanUps_shouldNotReturnNull() throws Exception {
        HtmlInterstitialWebViewFactory.initialize(Robolectric.buildActivity(MoPubActivity.class).get());
        HtmlInterstitialWebViewFactory.cleanup();
        HtmlInterstitialWebViewFactory.cleanup();

        // pass
        HtmlInterstitialWebViewFactory.create(null, false, "", "");
    }
}
