package com.mopub.mobileads;

import android.app.Activity;
import android.os.Build;
import android.view.MotionEvent;
import android.view.View;
import com.mopub.mobileads.test.support.SdkTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.shadows.ShadowWebView;

import static android.webkit.WebSettings.PluginState;
import static com.mopub.mobileads.util.VersionCode.HONEYCOMB_MR2;
import static com.mopub.mobileads.util.VersionCode.ICE_CREAM_SANDWICH;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.robolectric.Robolectric.shadowOf;

@RunWith(SdkTestRunner.class)
public class BaseHtmlWebViewTest {

    private BaseHtmlWebView subject;
    private MotionEvent touchDown;
    private MotionEvent touchUp;

    @Before
    public void setUp() throws Exception {
        subject = new BaseHtmlWebView(new Activity());

        touchDown = createMotionEvent(MotionEvent.ACTION_DOWN);
        touchUp = createMotionEvent(MotionEvent.ACTION_UP);
    }

    @Test
    public void shouldEnablePluginsBasedOnApiLevel() throws Exception {
        Robolectric.Reflection.setFinalStaticField(Build.VERSION.class, "SDK_INT", ICE_CREAM_SANDWICH.getApiLevel());
        subject = new BaseHtmlWebView(new Activity());
        assertThat(subject.getSettings().getPluginState()).isEqualTo(PluginState.ON);

        Robolectric.Reflection.setFinalStaticField(Build.VERSION.class, "SDK_INT", HONEYCOMB_MR2.getApiLevel());
        subject = new BaseHtmlWebView(new Activity());
        assertThat(subject.getSettings().getPluginState()).isEqualTo(PluginState.OFF);
    }

    @Test
    public void init_shouldSetWebViewScrollability() throws Exception {
        subject.init(false);
        assertThat(shadowOf(subject).getOnTouchListener()).isNotNull();

        subject.init(true);
        assertThat(shadowOf(subject).getOnTouchListener()).isNotNull();
    }

    @Test
    public void loadUrl_shouldAcceptNullParameter() throws Exception {
        subject.loadUrl(null);
        // pass
    }

    @Test
    public void loadUrl_whenUrlIsJavascript_shouldCallSuperLoadUrl() throws Exception {
        String javascriptUrl = "javascript:function() {alert(\"guy\")};";
        subject.loadUrl(javascriptUrl);

        assertThat(shadowOf(subject).getLastLoadedUrl()).isEqualTo(javascriptUrl);
    }

    @Test
    public void loadHtmlResponse_shouldCallLoadDataWithBaseURL() throws Exception {
        String htmlResponse = "some random html response";
        subject.loadHtmlResponse(htmlResponse);

        ShadowWebView.LoadDataWithBaseURL lastLoadData = shadowOf(subject).getLastLoadDataWithBaseURL();
        assertThat(lastLoadData.baseUrl).isEqualTo("http://ads.mopub.com/");
        assertThat(lastLoadData.data).isEqualTo(htmlResponse);
        assertThat(lastLoadData.mimeType).isEqualTo("text/html");
        assertThat(lastLoadData.encoding).isEqualTo("utf-8");
        assertThat(lastLoadData.historyUrl).isNull();
    }

    @Test
    public void sendTouchEvent_withScrollingDisabled_shouldSetUserClicked() throws Exception {
        assertThat(subject.hasUserClicked()).isFalse();

        subject.initializeOnTouchListener(false);
        View.OnTouchListener onTouchListener = shadowOf(subject).getOnTouchListener();

        onTouchListener.onTouch(subject, touchUp);
        assertThat(subject.hasUserClicked()).isTrue();
    }

    @Test
    public void sendTouchEvent_withScrollingEnabled_shouldSetUserClicked() throws Exception {
        assertThat(subject.hasUserClicked()).isFalse();

        subject.initializeOnTouchListener(true);
        View.OnTouchListener onTouchListener = shadowOf(subject).getOnTouchListener();

        onTouchListener.onTouch(subject, touchUp);
        assertThat(subject.hasUserClicked()).isTrue();
    }

    @Test
    public void sendTouchEvent_withScrollingDisabled_withLotsOfRandomMotionEvents_shouldEventuallySetUserClicked() throws Exception {
        subject.initializeOnTouchListener(false);
        View.OnTouchListener onTouchListener = shadowOf(subject).getOnTouchListener();

        onTouchListener.onTouch(subject, touchDown);
        assertThat(subject.hasUserClicked()).isFalse();
        onTouchListener.onTouch(subject, createMotionEvent(MotionEvent.ACTION_CANCEL));
        assertThat(subject.hasUserClicked()).isFalse();
        onTouchListener.onTouch(subject, createMotionEvent(MotionEvent.ACTION_MOVE));
        assertThat(subject.hasUserClicked()).isFalse();

        onTouchListener.onTouch(subject, touchUp);
        assertThat(subject.hasUserClicked()).isTrue();

        onTouchListener.onTouch(subject, touchDown);
        assertThat(subject.hasUserClicked()).isTrue();
        onTouchListener.onTouch(subject, createMotionEvent(MotionEvent.ACTION_CANCEL));
        assertThat(subject.hasUserClicked()).isTrue();
        onTouchListener.onTouch(subject, createMotionEvent(MotionEvent.ACTION_MOVE));
        assertThat(subject.hasUserClicked()).isTrue();
    }

    @Test
    public void sendTouchEvent_withScrollingEnabled_withLotsOfRandomMotionEvents_shouldEventuallySetUserClicked() throws Exception {
        subject.initializeOnTouchListener(true);
        View.OnTouchListener onTouchListener = shadowOf(subject).getOnTouchListener();

        onTouchListener.onTouch(subject, touchDown);
        assertThat(subject.hasUserClicked()).isFalse();
        onTouchListener.onTouch(subject, createMotionEvent(MotionEvent.ACTION_CANCEL));
        assertThat(subject.hasUserClicked()).isFalse();
        onTouchListener.onTouch(subject, createMotionEvent(MotionEvent.ACTION_MOVE));
        assertThat(subject.hasUserClicked()).isFalse();

        onTouchListener.onTouch(subject, touchUp);
        assertThat(subject.hasUserClicked()).isTrue();

        onTouchListener.onTouch(subject, touchDown);
        assertThat(subject.hasUserClicked()).isTrue();
        onTouchListener.onTouch(subject, createMotionEvent(MotionEvent.ACTION_CANCEL));
        assertThat(subject.hasUserClicked()).isTrue();
        onTouchListener.onTouch(subject, createMotionEvent(MotionEvent.ACTION_MOVE));
        assertThat(subject.hasUserClicked()).isTrue();
    }

    @Test
    public void resetUserClicked_shouldResetUserClicked() throws Exception {
        subject.initializeOnTouchListener(false);
        View.OnTouchListener onTouchListener = shadowOf(subject).getOnTouchListener();

        onTouchListener.onTouch(subject, touchDown);
        onTouchListener.onTouch(subject, touchUp);
        assertThat(subject.hasUserClicked()).isTrue();

        subject.resetUserClicked();
        assertThat(subject.hasUserClicked()).isFalse();
    }

    @Test
    public void resetUserClicked_whenTouchStateIsUnset_shouldKeepTouchStateUnset() throws Exception {
        subject.initializeOnTouchListener(false);
        assertThat(subject.hasUserClicked()).isFalse();

        subject.resetUserClicked();
        assertThat(subject.hasUserClicked()).isFalse();
    }

    @Test
    public void setWebViewScrollingEnabled_whenScrollableIsTrue_onTouchListenerShouldAlwaysReturnFalse() throws Exception {
        subject.initializeOnTouchListener(true);

        View.OnTouchListener onTouchListener = shadowOf(subject).getOnTouchListener();
        boolean shouldConsumeTouch = onTouchListener.onTouch(subject, createMotionEvent(MotionEvent.ACTION_MOVE));

        assertThat(shouldConsumeTouch).isFalse();
    }

    @Test
    public void setWebViewScrollingEnabled_whenScrollableIsFalse_whenActionMove_onTouchListenerShouldReturnTrue() throws Exception {
        subject.initializeOnTouchListener(false);

        View.OnTouchListener onTouchListener = shadowOf(subject).getOnTouchListener();
        boolean shouldConsumeTouch = onTouchListener.onTouch(subject, createMotionEvent(MotionEvent.ACTION_MOVE));

        assertThat(shouldConsumeTouch).isTrue();
    }

    @Test
    public void setWebViewScrollingEnabled_whenScrollableIsFalse_whenMotionEventIsNotActionMove_onTouchListenerShouldReturnFalse() throws Exception {
        subject.initializeOnTouchListener(false);

        View.OnTouchListener onTouchListener = shadowOf(subject).getOnTouchListener();

        boolean shouldConsumeTouch = onTouchListener.onTouch(subject, touchUp);
        assertThat(shouldConsumeTouch).isFalse();

        shouldConsumeTouch = onTouchListener.onTouch(subject, touchDown);
        assertThat(shouldConsumeTouch).isFalse();

        shouldConsumeTouch = onTouchListener.onTouch(subject, createMotionEvent(MotionEvent.ACTION_CANCEL));
        assertThat(shouldConsumeTouch).isFalse();
    }

    private static MotionEvent createMotionEvent(int action) {
        return MotionEvent.obtain(0, 0, action, 0, 0, 0);
    }
}
