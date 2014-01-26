/*
 * Copyright (c) 2010-2013, MoPub Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *  Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 *  Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 *  Neither the name of 'MoPub Inc.' nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.mopub.mobileads;

import android.app.Activity;
import android.os.Build;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
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
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.robolectric.Robolectric.shadowOf;

@RunWith(SdkTestRunner.class)
public class BaseHtmlWebViewTest {

    private BaseHtmlWebView subject;
    private MotionEvent touchDown;
    private MotionEvent touchUp;
    private AdConfiguration adConfiguration;

    @Before
    public void setUp() throws Exception {
        adConfiguration = mock(AdConfiguration.class);
        subject = new BaseHtmlWebView(new Activity(), adConfiguration);

        touchDown = createMotionEvent(MotionEvent.ACTION_DOWN);
        touchUp = createMotionEvent(MotionEvent.ACTION_UP);
    }

    @Test
    public void shouldEnablePluginsBasedOnApiLevel() throws Exception {
        Robolectric.Reflection.setFinalStaticField(Build.VERSION.class, "SDK_INT", ICE_CREAM_SANDWICH.getApiLevel());
        subject = new BaseHtmlWebView(new Activity(), adConfiguration);
        assertThat(subject.getSettings().getPluginState()).isEqualTo(PluginState.ON);

        Robolectric.Reflection.setFinalStaticField(Build.VERSION.class, "SDK_INT", HONEYCOMB_MR2.getApiLevel());
        subject = new BaseHtmlWebView(new Activity(), adConfiguration);
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
        assertThat(subject.wasClicked()).isFalse();

        subject.initializeOnTouchListener(false);
        View.OnTouchListener onTouchListener = shadowOf(subject).getOnTouchListener();

        onTouchListener.onTouch(subject, touchUp);
        assertThat(subject.wasClicked()).isTrue();
    }

    @Test
    public void sendTouchEvent_withScrollingEnabled_shouldSetUserClicked() throws Exception {
        assertThat(subject.wasClicked()).isFalse();

        subject.initializeOnTouchListener(true);
        View.OnTouchListener onTouchListener = shadowOf(subject).getOnTouchListener();

        onTouchListener.onTouch(subject, touchUp);
        assertThat(subject.wasClicked()).isTrue();
    }

    @Test
    public void sendTouchEvent_withScrollingDisabled_withLotsOfRandomMotionEvents_shouldEventuallySetUserClicked() throws Exception {
        subject.initializeOnTouchListener(false);
        View.OnTouchListener onTouchListener = shadowOf(subject).getOnTouchListener();

        onTouchListener.onTouch(subject, touchDown);
        assertThat(subject.wasClicked()).isFalse();
        onTouchListener.onTouch(subject, createMotionEvent(MotionEvent.ACTION_CANCEL));
        assertThat(subject.wasClicked()).isFalse();
        onTouchListener.onTouch(subject, createMotionEvent(MotionEvent.ACTION_MOVE));
        assertThat(subject.wasClicked()).isFalse();

        onTouchListener.onTouch(subject, touchUp);
        assertThat(subject.wasClicked()).isTrue();

        onTouchListener.onTouch(subject, touchDown);
        assertThat(subject.wasClicked()).isTrue();
        onTouchListener.onTouch(subject, createMotionEvent(MotionEvent.ACTION_CANCEL));
        assertThat(subject.wasClicked()).isTrue();
        onTouchListener.onTouch(subject, createMotionEvent(MotionEvent.ACTION_MOVE));
        assertThat(subject.wasClicked()).isTrue();
    }

    @Test
    public void sendTouchEvent_withScrollingEnabled_withLotsOfRandomMotionEvents_shouldEventuallySetUserClicked() throws Exception {
        subject.initializeOnTouchListener(true);
        View.OnTouchListener onTouchListener = shadowOf(subject).getOnTouchListener();

        onTouchListener.onTouch(subject, touchDown);
        assertThat(subject.wasClicked()).isFalse();
        onTouchListener.onTouch(subject, createMotionEvent(MotionEvent.ACTION_CANCEL));
        assertThat(subject.wasClicked()).isFalse();
        onTouchListener.onTouch(subject, createMotionEvent(MotionEvent.ACTION_MOVE));
        assertThat(subject.wasClicked()).isFalse();

        onTouchListener.onTouch(subject, touchUp);
        assertThat(subject.wasClicked()).isTrue();

        onTouchListener.onTouch(subject, touchDown);
        assertThat(subject.wasClicked()).isTrue();
        onTouchListener.onTouch(subject, createMotionEvent(MotionEvent.ACTION_CANCEL));
        assertThat(subject.wasClicked()).isTrue();
        onTouchListener.onTouch(subject, createMotionEvent(MotionEvent.ACTION_MOVE));
        assertThat(subject.wasClicked()).isTrue();
    }

    @Test
    public void onResetClicked_shouldonResetClicked() throws Exception {
        subject.initializeOnTouchListener(false);
        View.OnTouchListener onTouchListener = shadowOf(subject).getOnTouchListener();

        onTouchListener.onTouch(subject, touchDown);
        onTouchListener.onTouch(subject, touchUp);
        assertThat(subject.wasClicked()).isTrue();

        subject.onResetUserClick();
        assertThat(subject.wasClicked()).isFalse();
    }

    @Test
    public void onResetClicked_whenTouchStateIsUnset_shouldKeepTouchStateUnset() throws Exception {
        subject.initializeOnTouchListener(false);
        assertThat(subject.wasClicked()).isFalse();

        subject.onResetUserClick();
        assertThat(subject.wasClicked()).isFalse();
    }

    @Test
    public void setWebViewScrollingEnabled_whenScrollableIsTrue_onTouchListenerShouldAlwaysReturnFalse() throws Exception {
        subject.initializeOnTouchListener(true);

        View.OnTouchListener onTouchListener = shadowOf(subject).getOnTouchListener();
        // ACTION_DOWN is guaranteed to be run before ACTION_MOVE
        onTouchListener.onTouch(subject, createMotionEvent(MotionEvent.ACTION_DOWN));
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

    @Test
    public void destroy_shouldRemoveSelfFromParent() throws Exception {
        ViewGroup parentView = mock(ViewGroup.class);
        ShadowWebView shadow = shadowOf(subject);
        shadow.setMyParent(parentView);

        subject.destroy();

        verify(parentView).removeView(eq(subject));
        assertThat(shadow.wasDestroyCalled());
    }
    
    private static MotionEvent createMotionEvent(int action) {
        return MotionEvent.obtain(0, 0, action, 0, 0, 0);
    }
}
