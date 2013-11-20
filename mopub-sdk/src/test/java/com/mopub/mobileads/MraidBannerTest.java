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
import com.mopub.mobileads.test.support.SdkTestRunner;
import com.mopub.mobileads.test.support.TestMraidViewFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;

import java.util.*;

import static com.mopub.mobileads.AdFetcher.HTML_RESPONSE_BODY_KEY;
import static com.mopub.mobileads.CustomEventBanner.CustomEventBannerListener;
import static com.mopub.mobileads.MoPubErrorCode.MRAID_LOAD_ERROR;
import static com.mopub.mobileads.MraidView.MraidListener;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
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

        context = new Activity();
        bannerListener = mock(CustomEventBanner.CustomEventBannerListener.class);
        localExtras = new HashMap<String, Object>();
        serverExtras = new HashMap<String, String>();
        serverExtras.put(HTML_RESPONSE_BODY_KEY, INPUT_HTML_DATA);
    }

    @Test
    public void loadBanner_whenExtrasAreMalformed_shouldNotifyBannerListenerAndReturn() throws Exception {
        serverExtras.remove(HTML_RESPONSE_BODY_KEY);

        subject.loadBanner(context, bannerListener, localExtras, serverExtras);

        verify(bannerListener).onBannerFailed(eq(MRAID_LOAD_ERROR));
        verify(mraidView, never()).loadHtmlData(any(String.class));
        verify(mraidView, never()).setMraidListener(any(MraidListener.class));
    }

    @Test
    public void loadBanner_shouldLoadHtmlDataAndInitializeListeners() throws Exception {
        subject.loadBanner(context, bannerListener, localExtras, serverExtras);

        verify(mraidView).loadHtmlData(EXPECTED_HTML_DATA);

        verify(mraidView).setMraidListener(any(MraidListener.class));
    }

    @Test
    public void invalidate_shouldDestroyMraidView() throws Exception {
        subject.loadBanner(context, bannerListener, localExtras, serverExtras);
        subject.onInvalidate();

        verify(mraidView).destroy();
    }

    @Test
    public void bannerMraidListener_onReady_shouldNotifyBannerLoaded() throws Exception {
        MraidListener mraidListener = captureMraidListener();
        mraidListener.onReady(null);

        verify(bannerListener).onBannerLoaded(eq(mraidView));
    }

    @Test
    public void bannerMraidListener_onFailure_shouldNotifyBannerFailed() throws Exception {
        MraidListener mraidListener = captureMraidListener();
        mraidListener.onFailure(null);

        verify(bannerListener).onBannerFailed(eq(MRAID_LOAD_ERROR));
    }

    @Test
    public void bannerMraidListener_onExpand_shouldNotifyBannerExpandedAndClicked() throws Exception {
        MraidListener mraidListener = captureMraidListener();
        mraidListener.onExpand(null);

        verify(bannerListener).onBannerExpanded();
        verify(bannerListener).onBannerClicked();
    }

    @Test
    public void bannerMraidListener_onClose_shouldNotifyBannerCollapsed() throws Exception {
        MraidListener mraidListener = captureMraidListener();
        mraidListener.onClose(null, null);

        verify(bannerListener).onBannerCollapsed();
    }

    private MraidListener captureMraidListener() {
        subject.loadBanner(context, bannerListener, localExtras, serverExtras);
        ArgumentCaptor<MraidListener> listenerCaptor = ArgumentCaptor.forClass(MraidListener.class);
        verify(mraidView).setMraidListener(listenerCaptor.capture());

        return listenerCaptor.getValue();
    }
}
