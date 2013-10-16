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

import org.junit.Test;

import java.util.*;

import static com.mopub.mobileads.MraidCommandRegistry.createCommand;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class MraidCommandRegistryTest {
    @Test
    public void createCommand_shouldReturnTheRightKindOfCommand() throws Exception {
        assertThat(createCommand("bogus", null, null)).isNull();

        assertThat(createCommand(MraidCommandRegistry.MRAID_JAVASCRIPT_COMMAND_CLOSE, null, null)).isInstanceOf(MraidCommandClose.class);
        assertThat(createCommand(MraidCommandRegistry.MRAID_JAVASCRIPT_COMMAND_CLOSE, null, null)).isNotSameAs(createCommand("close", null, null));

        assertThat(createCommand(MraidCommandRegistry.MRAID_JAVASCRIPT_COMMAND_EXPAND, null, null)).isInstanceOf(MraidCommandExpand.class);
        assertThat(createCommand(MraidCommandRegistry.MRAID_JAVASCRIPT_COMMAND_USECUSTOMCLOSE, null, null)).isInstanceOf(MraidCommandUseCustomClose.class);
        assertThat(createCommand(MraidCommandRegistry.MRAID_JAVASCRIPT_COMMAND_OPEN, null, null)).isInstanceOf(MraidCommandOpen.class);
        assertThat(createCommand(MraidCommandRegistry.MRAID_JAVASCRIPT_COMMAND_RESIZE, null, null)).isInstanceOf(MraidCommandResize.class);
        assertThat(createCommand(MraidCommandRegistry.MRAID_JAVASCRIPT_COMMAND_SET_RESIZE_PROPERTIES, null, null)).isInstanceOf(MraidCommandSetResizeProperties.class);
        assertThat(createCommand(MraidCommandRegistry.MRAID_JAVASCRIPT_COMMAND_GET_RESIZE_PROPERTIES, null, null)).isInstanceOf(MraidCommandGetResizeProperties.class);

        assertThat(createCommand(MraidCommandRegistry.MRAID_JAVASCRIPT_COMMAND_PLAY_VIDEO, null, null)).isInstanceOf(MraidCommandPlayVideo.class);
        assertThat(createCommand(MraidCommandRegistry.MRAID_JAVASCRIPT_COMMAND_STORE_PICTURE, null, null)).isInstanceOf(MraidCommandStorePicture.class);

        assertThat(createCommand(MraidCommandRegistry.MRAID_JAVASCRIPT_COMMAND_GET_CURRENT_POSITION, null, null)).isInstanceOf(MraidCommandGetCurrentPosition.class);
        assertThat(createCommand(MraidCommandRegistry.MRAID_JAVASCRIPT_COMMAND_GET_DEFAULT_POSITION, null, null)).isInstanceOf(MraidCommandGetDefaultPosition.class);
        assertThat(createCommand(MraidCommandRegistry.MRAID_JAVASCRIPT_COMMAND_GET_MAX_SIZE, null, null)).isInstanceOf(MraidCommandGetMaxSize.class);
        assertThat(createCommand(MraidCommandRegistry.MRAID_JAVASCRIPT_COMMAND_GET_SCREEN_SIZE, null, null)).isInstanceOf(MraidCommandGetScreenSize.class);
        assertThat(createCommand(MraidCommandRegistry.MRAID_JAVASCRIPT_COMMAND_CREATE_CALENDAR_EVENT, null, null)).isInstanceOf(MraidCommandCreateCalendarEvent.class);
    }

    @Test
    public void createCommand_shouldPassParameters() throws Exception {
        MraidView expectedView = mock(MraidView.class);
        Map<String, String> expectedMap = mock(Map.class);

        MraidCommand command = createCommand(MraidCommandRegistry.MRAID_JAVASCRIPT_COMMAND_EXPAND, expectedMap, expectedView);
        assertThat(command.mParams).isEqualTo(expectedMap);
        assertThat(command.mView).isEqualTo(expectedView);
    }

    @Test
    public void createCommand_shouldCreateCommandWith() throws Exception {
        assertThat(createCommand(MraidCommandRegistry.MRAID_JAVASCRIPT_COMMAND_CLOSE, null, null).isCommandDependentOnUserClick()).isFalse();
        assertThat(createCommand(MraidCommandRegistry.MRAID_JAVASCRIPT_COMMAND_EXPAND, null, null).isCommandDependentOnUserClick()).isTrue();
        assertThat(createCommand(MraidCommandRegistry.MRAID_JAVASCRIPT_COMMAND_USECUSTOMCLOSE, null, null).isCommandDependentOnUserClick()).isFalse();
        assertThat(createCommand(MraidCommandRegistry.MRAID_JAVASCRIPT_COMMAND_OPEN, null, null).isCommandDependentOnUserClick()).isTrue();
        assertThat(createCommand(MraidCommandRegistry.MRAID_JAVASCRIPT_COMMAND_RESIZE, null, null).isCommandDependentOnUserClick()).isFalse();
        assertThat(createCommand(MraidCommandRegistry.MRAID_JAVASCRIPT_COMMAND_SET_RESIZE_PROPERTIES, null, null).isCommandDependentOnUserClick()).isFalse();
        assertThat(createCommand(MraidCommandRegistry.MRAID_JAVASCRIPT_COMMAND_GET_RESIZE_PROPERTIES, null, null).isCommandDependentOnUserClick()).isFalse();
        assertThat(createCommand(MraidCommandRegistry.MRAID_JAVASCRIPT_COMMAND_PLAY_VIDEO, null, null).isCommandDependentOnUserClick()).isFalse();
        assertThat(createCommand(MraidCommandRegistry.MRAID_JAVASCRIPT_COMMAND_STORE_PICTURE, null, null).isCommandDependentOnUserClick()).isTrue();
        assertThat(createCommand(MraidCommandRegistry.MRAID_JAVASCRIPT_COMMAND_GET_CURRENT_POSITION, null, null).isCommandDependentOnUserClick()).isFalse();
        assertThat(createCommand(MraidCommandRegistry.MRAID_JAVASCRIPT_COMMAND_GET_DEFAULT_POSITION, null, null).isCommandDependentOnUserClick()).isFalse();
        assertThat(createCommand(MraidCommandRegistry.MRAID_JAVASCRIPT_COMMAND_GET_MAX_SIZE, null, null).isCommandDependentOnUserClick()).isFalse();
        assertThat(createCommand(MraidCommandRegistry.MRAID_JAVASCRIPT_COMMAND_GET_SCREEN_SIZE, null, null).isCommandDependentOnUserClick()).isFalse();
        assertThat(createCommand(MraidCommandRegistry.MRAID_JAVASCRIPT_COMMAND_CREATE_CALENDAR_EVENT, null, null).isCommandDependentOnUserClick()).isTrue();
    }
}
