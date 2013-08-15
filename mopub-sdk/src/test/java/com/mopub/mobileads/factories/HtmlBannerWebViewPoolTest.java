package com.mopub.mobileads.factories;

import android.app.Activity;
import com.mopub.mobileads.CustomEventBanner;
import com.mopub.mobileads.HtmlBannerWebView;
import com.mopub.mobileads.test.support.SdkTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashSet;
import java.util.Set;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.robolectric.Robolectric.shadowOf;

@RunWith(SdkTestRunner.class)
public class HtmlBannerWebViewPoolTest {
    private HtmlBannerWebViewPool subject;
    private Activity context;
    private CustomEventBanner.CustomEventBannerListener customEventBannerListener;
    private boolean isScrollable;
    private String redirectUrl;
    private String clickthroughUrl;

    @Before
    public void setUp() throws Exception {
        context = new Activity();
        subject = new HtmlBannerWebViewPool(context);
        customEventBannerListener = mock(CustomEventBanner.CustomEventBannerListener.class);
        isScrollable = false;
        redirectUrl = "redirectUrl";
        clickthroughUrl = "clickthroughUrl";
    }

    @Test
    public void getNextHtmlWebView_shouldReturnHtmlWebView() throws Exception {
        HtmlBannerWebView returnValue = subject.getNextHtmlWebView(customEventBannerListener, isScrollable, redirectUrl, clickthroughUrl);

        assertThat(returnValue).isNotNull();
        assertThat(returnValue).isInstanceOf(HtmlBannerWebView.class);
        assertThat(shadowOf(returnValue).getWebViewClient()).isNotNull();
    }

    @Test
    public void getNextHtmlWebView_shouldContinuouslyReturnUniqueHtmlWebViews() throws Exception {
        Set<HtmlBannerWebView> htmlBannerWebViews = new HashSet<HtmlBannerWebView>();
        int expectedNewHtmlWebViewCount = HtmlBannerWebViewPool.POOL_SIZE * 2;

        for (int i = 0; i < expectedNewHtmlWebViewCount; i++) {
            htmlBannerWebViews.add(subject.getNextHtmlWebView(customEventBannerListener, isScrollable, redirectUrl, clickthroughUrl));
        }

        assertThat(htmlBannerWebViews.size()).isEqualTo(expectedNewHtmlWebViewCount);
    }
}
