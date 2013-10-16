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

import java.util.*;

class MraidCommandRegistry {

    public static final String MRAID_JAVASCRIPT_COMMAND_CLOSE = "close";
    public static final String MRAID_JAVASCRIPT_COMMAND_EXPAND = "expand";
    public static final String MRAID_JAVASCRIPT_COMMAND_USECUSTOMCLOSE = "usecustomclose";
    public static final String MRAID_JAVASCRIPT_COMMAND_OPEN = "open";
    public static final String MRAID_JAVASCRIPT_COMMAND_RESIZE = "resize";
    public static final String MRAID_JAVASCRIPT_COMMAND_GET_RESIZE_PROPERTIES = "getResizeProperties";
    public static final String MRAID_JAVASCRIPT_COMMAND_SET_RESIZE_PROPERTIES = "setResizeProperties";
    public static final String MRAID_JAVASCRIPT_COMMAND_PLAY_VIDEO = "playVideo";
    public static final String MRAID_JAVASCRIPT_COMMAND_STORE_PICTURE = "storePicture";
    public static final String MRAID_JAVASCRIPT_COMMAND_GET_CURRENT_POSITION = "getCurrentPosition";
    public static final String MRAID_JAVASCRIPT_COMMAND_GET_DEFAULT_POSITION = "getDefaultPosition";
    public static final String MRAID_JAVASCRIPT_COMMAND_GET_MAX_SIZE = "getMaxSize";
    public static final String MRAID_JAVASCRIPT_COMMAND_GET_SCREEN_SIZE = "getScreenSize";
    public static final String MRAID_JAVASCRIPT_COMMAND_CREATE_CALENDAR_EVENT = "createCalendarEvent";

    static MraidCommand createCommand(String command, Map<String, String> params, MraidView view) {
        if (MRAID_JAVASCRIPT_COMMAND_CLOSE.equals(command)) {
            return new MraidCommandClose(params, view);
        }
        if (MRAID_JAVASCRIPT_COMMAND_EXPAND.equals(command)) {
            return new MraidCommandExpand(params, view);
        }
        if (MRAID_JAVASCRIPT_COMMAND_USECUSTOMCLOSE.equals(command)) {
            return new MraidCommandUseCustomClose(params, view);
        }
        if (MRAID_JAVASCRIPT_COMMAND_OPEN.equals(command)) {
            return new MraidCommandOpen(params, view);
        }
        if (MRAID_JAVASCRIPT_COMMAND_RESIZE.equals(command)) {
            return new MraidCommandResize(params, view);
        }
        if (MRAID_JAVASCRIPT_COMMAND_GET_RESIZE_PROPERTIES.equals(command)) {
            return new MraidCommandGetResizeProperties(params, view);
        }
        if (MRAID_JAVASCRIPT_COMMAND_SET_RESIZE_PROPERTIES.equals(command)) {
            return new MraidCommandSetResizeProperties(params, view);
        }
        if (MRAID_JAVASCRIPT_COMMAND_PLAY_VIDEO.equals(command)) {
            return new MraidCommandPlayVideo(params, view);
        }
        if (MRAID_JAVASCRIPT_COMMAND_STORE_PICTURE.equals(command)) {
            return new MraidCommandStorePicture(params, view);
        }
        if (MRAID_JAVASCRIPT_COMMAND_GET_CURRENT_POSITION.equals(command)) {
            return new MraidCommandGetCurrentPosition(params, view);
        }
        if (MRAID_JAVASCRIPT_COMMAND_GET_DEFAULT_POSITION.equals(command)) {
            return new MraidCommandGetDefaultPosition(params, view);
        }
        if (MRAID_JAVASCRIPT_COMMAND_GET_MAX_SIZE.equals(command)) {
            return new MraidCommandGetMaxSize(params, view);
        }
        if (MRAID_JAVASCRIPT_COMMAND_GET_SCREEN_SIZE.equals(command)) {
            return new MraidCommandGetScreenSize(params, view);
        }
        if (MRAID_JAVASCRIPT_COMMAND_CREATE_CALENDAR_EVENT.equals(command)) {
            return new MraidCommandCreateCalendarEvent(params, view);
        }

        return null;
    }
}
