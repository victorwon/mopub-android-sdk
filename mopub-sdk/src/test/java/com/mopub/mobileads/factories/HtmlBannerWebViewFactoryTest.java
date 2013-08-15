package com.mopub.mobileads.factories;

import android.app.Activity;
import com.mopub.mobileads.HtmlBannerWebView;
import com.mopub.mobileads.test.support.SdkTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.mopub.mobileads.CustomEventBanner.CustomEventBannerListener;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@RunWith(SdkTestRunner.class)
public class HtmlBannerWebViewFactoryTest {

    private HtmlBannerWebViewFactory subject;
    private CustomEventBannerListener customEventBannerListener;

    @Before
    public void setUp() throws Exception {
        subject = new HtmlBannerWebViewFactory();
        Activity context = new Activity();
        HtmlBannerWebViewFactory.initialize(context);
        customEventBannerListener = mock(CustomEventBannerListener.class);
    }

    @Test
    public void internalCreate_shouldCreateHtmlWebView() throws Exception {
        HtmlBannerWebView htmlBannerWebView = subject.internalCreate(customEventBannerListener, false, null, null);
        assertThat(htmlBannerWebView).isNotNull();
    }

    @Test
    public void shouldBeAbleToReinitialize() throws Exception {
        HtmlBannerWebViewFactory.setInstance(subject);
        HtmlBannerWebViewFactory.initialize(new Activity());

        assertThat(HtmlBannerWebViewFactory.create(null, false, "", "")).isNotNull();

        HtmlBannerWebViewFactory.cleanup();
        HtmlBannerWebViewFactory.initialize(new Activity());

        assertThat(HtmlBannerWebViewFactory.create(null, false, "", "")).isNotNull();
    }

}
