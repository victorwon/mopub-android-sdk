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

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@RunWith(SdkTestRunner.class)
public class MraidCommandFactoryTest {
    private MraidCommandFactory subject;
    private Map params;
    private MraidView mraidView;

    @Before
    public void setUp() throws Exception {
        subject = new MraidCommandFactory();
        params = mock(Map.class);
        mraidView = mock(MraidView.class);

    }

    @Test
    public void create_withCommandName_shouldInstantiateCorrectSubclass() throws Exception {
        assertMraidCommandFactoryCreatesCorrectMraidCommandSubclass("close", MraidCommandClose.class);
        assertMraidCommandFactoryCreatesCorrectMraidCommandSubclass("expand", MraidCommandExpand.class);
        assertMraidCommandFactoryCreatesCorrectMraidCommandSubclass("usecustomclose", MraidCommandUseCustomClose.class);
        assertMraidCommandFactoryCreatesCorrectMraidCommandSubclass("open", MraidCommandOpen.class);
        assertMraidCommandFactoryCreatesCorrectMraidCommandSubclass("resize", MraidCommandResize.class);
        assertMraidCommandFactoryCreatesCorrectMraidCommandSubclass("getResizeProperties", MraidCommandGetResizeProperties.class);
        assertMraidCommandFactoryCreatesCorrectMraidCommandSubclass("setResizeProperties", MraidCommandSetResizeProperties.class);
        assertMraidCommandFactoryCreatesCorrectMraidCommandSubclass("playVideo", MraidCommandPlayVideo.class);
        assertMraidCommandFactoryCreatesCorrectMraidCommandSubclass("storePicture", MraidCommandStorePicture.class);
        assertMraidCommandFactoryCreatesCorrectMraidCommandSubclass("getCurrentPosition", MraidCommandGetCurrentPosition.class);
        assertMraidCommandFactoryCreatesCorrectMraidCommandSubclass("getDefaultPosition", MraidCommandGetDefaultPosition.class);
        assertMraidCommandFactoryCreatesCorrectMraidCommandSubclass("getMaxSize", MraidCommandGetMaxSize.class);
        assertMraidCommandFactoryCreatesCorrectMraidCommandSubclass("getScreenSize", MraidCommandGetScreenSize.class);
        assertMraidCommandFactoryCreatesCorrectMraidCommandSubclass("createCalendarEvent", MraidCommandCreateCalendarEvent.class);
    }

    @Test
    public void create_withInvalidCommandString_shouldReturnNull() throws Exception {
        MraidCommand command = MraidCommandFactory.create("dog", params, mraidView);

        assertThat(command).isNull();
    }

    @Test
    public void create_withNullCommandString_shouldReturnNull() throws Exception {
        MraidCommand command = MraidCommandFactory.create(null, params, mraidView);

        assertThat(command).isNull();
    }

    @Test
    public void create_withNullParams_shouldNotBlowUp() throws Exception {
        MraidCommand command = MraidCommandFactory.create("close", null, mraidView);

        // pass
    }

    @Test
    public void create_withNullMraidView_shouldNotBlowUp() throws Exception {
        MraidCommand command = MraidCommandFactory.create("close", params, null);

        // pass
    }

    private void assertMraidCommandFactoryCreatesCorrectMraidCommandSubclass(String command, Class type) {
        MraidCommand mraidCommand = MraidCommandFactory.create(command, params, mraidView);

        assertThat(mraidCommand).isNotNull();
        assertThat(mraidCommand).isInstanceOf(type);
    }
}
