package com.mopub.mobileads;

import android.content.ComponentName;
import android.content.Intent;
import android.view.View;
import android.widget.RelativeLayout;
import com.mopub.mobileads.test.support.SdkTestRunner;
import com.mopub.mobileads.test.support.TestMoPubViewFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.robolectric.Robolectric;

import static android.widget.RelativeLayout.LayoutParams;
import static com.mopub.mobileads.BaseActivity.SOURCE_KEY;
import static com.mopub.mobileads.MoPubActivity.AD_UNIT_ID_KEY;
import static com.mopub.mobileads.MoPubActivity.CLICKTHROUGH_URL_KEY;
import static com.mopub.mobileads.MoPubActivity.KEYWORDS_KEY;
import static com.mopub.mobileads.MoPubActivity.TIMEOUT_KEY;
import static com.mopub.mobileads.MoPubView.BannerAdListener;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;

@RunWith(SdkTestRunner.class)
public class MoPubActivityTest extends BaseActivityTest {
    public static final String EXPECTED_AD_UNIT_ID = "expected ad unit id";
    public static final String EXPECTED_KEYWORDS = "keywords";
    public static final String EXPECTED_CLICKTHROUGH_URL = "http://expected_url";
    public static final int EXPECTED_TIMEOUT = 10;

    private MoPubView moPubView;

    @Before
    public void setUp() throws Exception {
        super.setup();
        Intent moPubActivityIntent = createMoPubActivityIntent(EXPECTED_SOURCE);
        moPubView = TestMoPubViewFactory.getSingletonMock();
        resetMockedView(moPubView);
        subject = Robolectric.buildActivity(MoPubActivity.class).withIntent(moPubActivityIntent).create().get();
        reset(moPubView);
        resetMockedView(moPubView);
    }

    @Test
    public void onCreate_shouldLayoutMoPubView() throws Exception {
        subject.onCreate(null);

        ArgumentCaptor<RelativeLayout.LayoutParams> captor = ArgumentCaptor.forClass(RelativeLayout.LayoutParams.class);
        verify(moPubView).setLayoutParams(captor.capture());
        RelativeLayout.LayoutParams actualLayoutParams = captor.getValue();

        assertThat(actualLayoutParams.width).isEqualTo(RelativeLayout.LayoutParams.FILL_PARENT);
        assertThat(actualLayoutParams.height).isEqualTo(RelativeLayout.LayoutParams.WRAP_CONTENT);
        assertOnlyOneRuleSet(actualLayoutParams, RelativeLayout.CENTER_IN_PARENT);
    }

    @Test
    public void getAdView_shouldReturnPopulatedMoPubView() throws Exception {
        View adView = subject.getAdView();

        assertThat(adView).isSameAs(moPubView);
        verify(moPubView).setAdUnitId(EXPECTED_AD_UNIT_ID);
        verify(moPubView).setKeywords(EXPECTED_KEYWORDS);
        verify(moPubView).setClickthroughUrl(EXPECTED_CLICKTHROUGH_URL);
        verify(moPubView).setTimeout(EXPECTED_TIMEOUT);

        verify(moPubView).setBannerAdListener(any(BannerAdListener.class));

        verify(moPubView).loadHtmlString(EXPECTED_SOURCE);
    }

    @Test
    public void getAdView_whenSourceHasImpressionTracking_shouldRemoveImpressionTracking() throws Exception {
        Intent intent = createMoPubActivityIntent("this is some cool source http://ads.mopub.com/m/imp !!");
        subject.setIntent(intent);
        subject.getAdView();

        verify(moPubView).loadHtmlString(eq("this is some cool source mopub://null !!"));
    }

    @Test
    public void onDestroy_shouldDestroyMoPubView() throws Exception {
        subject.onCreate(null);
        subject.onDestroy();

        verify(moPubView).destroy();
        assertThat(getContentView(subject).getChildCount()).isEqualTo(0);
    }

    private Intent createMoPubActivityIntent(String expectedSource) {
        Intent moPubActivityIntent = new Intent();
        moPubActivityIntent.setComponent(new ComponentName("", ""));
        moPubActivityIntent.putExtra(AD_UNIT_ID_KEY, EXPECTED_AD_UNIT_ID);
        moPubActivityIntent.putExtra(KEYWORDS_KEY, EXPECTED_KEYWORDS);
        moPubActivityIntent.putExtra(CLICKTHROUGH_URL_KEY, EXPECTED_CLICKTHROUGH_URL);
        moPubActivityIntent.putExtra(TIMEOUT_KEY, EXPECTED_TIMEOUT);
        moPubActivityIntent.putExtra(SOURCE_KEY, expectedSource);
        return moPubActivityIntent;
    }

    private void assertOnlyOneRuleSet(LayoutParams layoutParams, int desiredRule) {
        int[] rules = layoutParams.getRules();
        for (int ruleIndex = 0; ruleIndex < rules.length; ruleIndex++) {
            int currentRule = rules[ruleIndex];
            if (ruleIndex == desiredRule) {
                assertThat(currentRule).isNotEqualTo(0);
            } else {
                assertThat(currentRule).isEqualTo(0);
            }
        }
    }
}

