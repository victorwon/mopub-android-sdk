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

import com.mopub.mobileads.test.support.SdkTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.*;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.stub;
import static org.mockito.Mockito.verify;

@RunWith(SdkTestRunner.class)
public class CustomEventAdLoadTaskTest {

    private AdViewController adViewController;
    private AdLoadTask.CustomEventAdLoadTask subject;
    private Map<String, String> paramsMap;
    private MoPubView moPubView;

    @Before
    public void setup() {
        moPubView = mock(MoPubView.class);
        adViewController = mock(AdViewController.class);
        stub(adViewController.getMoPubView()).toReturn(moPubView);
        paramsMap = new HashMap<String, String>();
        subject = new AdLoadTask.CustomEventAdLoadTask(adViewController, paramsMap);
    }

    @Test
    public void execute_shouldCallLoadCustomEvent() throws Exception {
        subject.execute();

        verify(adViewController).setNotLoading();
        verify(moPubView).loadCustomEvent(eq(paramsMap));
    }

    @Test
    public void execute_whenAdViewControllerIsNull_shouldDoNothing() throws Exception {
        subject = new AdLoadTask.CustomEventAdLoadTask(null, paramsMap);

        subject.execute();
        // pass
    }

    @Test
    public void execute_whenAdViewControllerIsDestroyed_shouldDoNothing() throws Exception {
        stub(adViewController.isDestroyed()).toReturn(true);

        subject.execute();

        verify(adViewController, never()).setNotLoading();
        verify(moPubView, never()).loadCustomEvent(eq(paramsMap));
    }

    @Test
    public void execute_whenParamsMapIsNull_shouldLoadNullParamsMap() throws Exception {
        subject = new AdLoadTask.CustomEventAdLoadTask(adViewController, null);

        subject.execute();

        verify(adViewController).setNotLoading();
        verify(moPubView).loadCustomEvent((Map<String, String>) eq(null));
    }

    @Test
    public void execute_afterCleanup_shouldLoadNullParamsMap() throws Exception {
        subject.cleanup();
        subject.execute();

        verify(adViewController).setNotLoading();
        verify(moPubView).loadCustomEvent((Map<String, String>) eq(null));
    }
}
