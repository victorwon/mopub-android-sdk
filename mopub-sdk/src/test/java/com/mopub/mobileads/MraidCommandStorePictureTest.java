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

import com.mopub.mobileads.test.support.FileUtils;
import com.mopub.mobileads.test.support.SdkTestRunner;
import com.mopub.mobileads.test.support.TestMraidViewFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.*;

import static com.mopub.mobileads.MraidCommand.URI_KEY;
import static com.mopub.mobileads.MraidCommandFactory.MraidJavascriptCommand.STORE_PICTURE;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.stub;
import static org.mockito.Mockito.verify;

@RunWith(SdkTestRunner.class)
public class MraidCommandStorePictureTest {

    public static final String EXPECTED_FILE = "file://tmp/expectedFile.jpg";
    private MraidCommandStorePicture subject;
    private MraidView mraidView;
    private Map<String,String> params;
    private MraidDisplayController mraidDisplayController;

    @Before
    public void setUp() {
        FileUtils.copyFile("etc/expectedFile.jpg", "/tmp/expectedFile.jpg");

        mraidView = TestMraidViewFactory.getSingletonMock();
        mraidDisplayController = mock(MraidDisplayController.class);
        stub(mraidView.getDisplayController()).toReturn(mraidDisplayController);

        params = new HashMap<String, String>();
        subject = new MraidCommandStorePicture(params, mraidView);
    }

    @Test
    public void execute_withImageUri_shouldShowUserDownloadImageAlert() throws Exception {
        params.put(URI_KEY, EXPECTED_FILE);

        subject.execute();

        verify(mraidDisplayController).showUserDownloadImageAlert(eq(EXPECTED_FILE));
    }

    @Test
    public void execute_withEmptyUriKey_shouldFireErrorEvent() throws Exception {
        params.put(URI_KEY, "");

        subject.execute();

        verify(mraidView).fireErrorEvent(
                eq(STORE_PICTURE),
                anyString());
        verify(mraidDisplayController, never()).showUserDownloadImageAlert(anyString());
    }

    @Test
     public void execute_withMissingUriKey_shouldFireErrorEvent() throws Exception {
        // don't add URI_KEY to params

        subject.execute();

        verify(mraidView).fireErrorEvent(
                eq(STORE_PICTURE),
                anyString());
        verify(mraidDisplayController, never()).showUserDownloadImageAlert(anyString());
    }
}
