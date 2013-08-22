package com.mopub.mobileads;

import java.util.Map;

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
