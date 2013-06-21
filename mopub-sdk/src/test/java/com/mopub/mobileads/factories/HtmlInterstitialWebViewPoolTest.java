package com.mopub.mobileads.factories;

import android.app.Activity;
import com.mopub.mobileads.CustomEventInterstitial;
import com.mopub.mobileads.HtmlInterstitialWebView;
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
public class HtmlInterstitialWebViewPoolTest {
    private HtmlInterstitialWebViewPool subject;
    private Activity context;
    private CustomEventInterstitial.CustomEventInterstitialListener customEventInterstitialListener;
    private boolean isScrollable;
    private String redirectUrl;
    private String clickthroughUrl;

    @Before
    public void setUp() throws Exception {
        context = new Activity();
        subject = new HtmlInterstitialWebViewPool(context);
        customEventInterstitialListener = mock(CustomEventInterstitial.CustomEventInterstitialListener.class);
        isScrollable = false;
        redirectUrl = "redirectUrl";
        clickthroughUrl = "clickthroughUrl";
    }

    @Test
    public void getNextHtmlWebView_shouldReturnHtmlWebView() throws Exception {
        HtmlInterstitialWebView returnValue = subject.getNextHtmlWebView(customEventInterstitialListener, isScrollable, redirectUrl, clickthroughUrl);

        assertThat(returnValue).isNotNull();
        assertThat(returnValue).isInstanceOf(HtmlInterstitialWebView.class);
        assertThat(shadowOf(returnValue).getWebViewClient()).isNotNull();
    }

    @Test
    public void getNextHtmlWebView_shouldContinuouslyReturnUniqueHtmlWebViews() throws Exception {
        Set<HtmlInterstitialWebView> htmlInterstitialWebViews = new HashSet<HtmlInterstitialWebView>();
        int expectedNewHtmlWebViewCount = HtmlInterstitialWebViewPool.POOL_SIZE * 2;

        for (int i = 0; i < expectedNewHtmlWebViewCount; i++) {
            htmlInterstitialWebViews.add(subject.getNextHtmlWebView(customEventInterstitialListener, isScrollable, redirectUrl, clickthroughUrl));
        }

        assertThat(htmlInterstitialWebViews.size()).isEqualTo(expectedNewHtmlWebViewCount);
    }
}
