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
import com.mopub.mobileads.test.support.TestMraidViewFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.*;

import static com.mopub.mobileads.MraidCommandFactory.MraidJavascriptCommand.GET_RESIZE_PROPERTIES;
import static com.mopub.mobileads.MraidCommandFactory.MraidJavascriptCommand.RESIZE;
import static com.mopub.mobileads.MraidCommandFactory.MraidJavascriptCommand.SET_RESIZE_PROPERTIES;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;

@RunWith(SdkTestRunner.class)
public class MraidCommandResizeTest {
    private MraidCommandResize subjectResize;
    private MraidCommandGetResizeProperties subjectGetResizeProperties;
    private MraidCommandSetResizeProperties subjectSetResizeProperties;
    private MraidView mraidView;
    @Before
    public void setup() {
        mraidView = TestMraidViewFactory.getSingletonMock();
        subjectResize = new MraidCommandResize(new HashMap<String, String>(), mraidView);
        subjectGetResizeProperties = new MraidCommandGetResizeProperties(new HashMap<String, String>(), mraidView);
        subjectSetResizeProperties = new MraidCommandSetResizeProperties(new HashMap<String, String>(), mraidView);
    }

    @Test
    public void mraidCommandResizeExecute_shouldFireErrorEvent() throws Exception {
        reset(mraidView);
        subjectResize.execute();
        verify(mraidView).fireErrorEvent(eq(RESIZE), any(String.class));
    }

    @Test
    public void mraidCommandSetResizePropertiesExecute_shouldFireErrorEvent() throws Exception {
        reset(mraidView);
        subjectSetResizeProperties.execute();
        verify(mraidView).fireErrorEvent(eq(SET_RESIZE_PROPERTIES), any(String.class));
    }

    @Test
    public void mraidCommandGetResizePropertiesExecute_shouldFireErrorEvent() throws Exception {
        reset(mraidView);
        subjectGetResizeProperties.execute();
        verify(mraidView).fireErrorEvent(eq(GET_RESIZE_PROPERTIES), any(String.class));
    }
}
