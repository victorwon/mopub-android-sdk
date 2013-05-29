package com.mopub.mobileads;

import android.app.Activity;
import com.mopub.mobileads.test.support.SdkTestRunner;
import com.mopub.mobileads.test.support.TestMraidViewFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.Map;

import static com.mopub.mobileads.AdFetcher.MRAID_HTML_DATA;
import static com.mopub.mobileads.CustomEventBanner.CustomEventBannerListener;
import static com.mopub.mobileads.MoPubErrorCode.MRAID_LOAD_ERROR;
import static com.mopub.mobileads.MraidView.OnCloseListener;
import static com.mopub.mobileads.MraidView.OnExpandListener;
import static com.mopub.mobileads.MraidView.OnFailureListener;
import static com.mopub.mobileads.MraidView.OnReadyListener;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.notNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;

@RunWith(SdkTestRunner.class)
public class MraidBannerTest {
    private MraidBanner subject;
    private MraidView mraidView;
    private Activity context;
    private Map<String, Object> localExtras;
    private Map<String, String> serverExtras;
    private CustomEventBannerListener bannerListener;
    private static final String INPUT_HTML_DATA = "%3Chtml%3E%3C%2Fhtml%3E";
    private static final String EXPECTED_HTML_DATA = "<html></html>";

    @Before
    public void setUp() throws Exception {
        subject = new MraidBanner();
        mraidView = TestMraidViewFactory.getSingletonMock();
        reset(mraidView);

        context = new Activity();
        bannerListener = mock(CustomEventBanner.CustomEventBannerListener.class);
        localExtras = new HashMap<String, Object>();
        serverExtras = new HashMap<String, String>();
        serverExtras.put(MRAID_HTML_DATA, INPUT_HTML_DATA);
    }

    @Test
    public void loadBanner_whenExtrasAreMalformed_shouldNotifyBannerListenerAndReturn() throws Exception {
        serverExtras.remove(MRAID_HTML_DATA);
        subject.loadBanner(context, bannerListener, localExtras, serverExtras);

        verify(bannerListener).onBannerFailed(eq(MRAID_LOAD_ERROR));

        verify(mraidView, never()).loadHtmlData(any(String.class));

        verify(mraidView, never()).setOnReadyListener(notNull(OnReadyListener.class));
        verify(mraidView, never()).setOnExpandListener(notNull(OnExpandListener.class));
        verify(mraidView, never()).setOnCloseListener(notNull(OnCloseListener.class));
        verify(mraidView, never()).setOnFailureListener(notNull(OnFailureListener.class));
    }

    @Test
    public void loadBanner_shouldLoadHtmlDataAndInitializeListeners() throws Exception {
        subject.loadBanner(context, bannerListener, localExtras, serverExtras);

        verify(mraidView).loadHtmlData(EXPECTED_HTML_DATA);

        verify(mraidView).setOnReadyListener(notNull(OnReadyListener.class));
        verify(mraidView).setOnExpandListener(notNull(OnExpandListener.class));
        verify(mraidView).setOnCloseListener(notNull(OnCloseListener.class));
        verify(mraidView).setOnFailureListener(notNull(OnFailureListener.class));
    }

    @Test
    public void invalidate_shouldDestroyMraidView() throws Exception {
        subject.loadBanner(context, bannerListener, localExtras, serverExtras);
        subject.onInvalidate();

        verify(mraidView).destroy();
    }

    @Test
    public void onReady_shouldNotifyBannerLoaded() throws Exception {
        subject.loadBanner(context, bannerListener, localExtras, serverExtras);
        subject.onReady();

        verify(bannerListener).onBannerLoaded(eq(mraidView));
    }

    @Test
    public void onFail_shouldNotifyBannerFailed() throws Exception {
        subject.loadBanner(context, bannerListener, localExtras, serverExtras);
        subject.onFail();

        verify(bannerListener).onBannerFailed(eq(MRAID_LOAD_ERROR));
    }

    @Test
    public void onExpand_shouldNotifyBannerExpandedAndClicked() throws Exception {
        subject.loadBanner(context, bannerListener, localExtras, serverExtras);
        subject.onExpand();

        verify(bannerListener).onBannerExpanded();
        verify(bannerListener).onBannerClicked();
    }

    @Test
    public void onClose_shouldNotifyBannerCollapsed() throws Exception {
        subject.loadBanner(context, bannerListener, localExtras, serverExtras);
        subject.onClose();

        verify(bannerListener).onBannerCollapsed();
    }
}
