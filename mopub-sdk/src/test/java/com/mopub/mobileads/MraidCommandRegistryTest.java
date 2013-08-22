package com.mopub.mobileads;

import org.junit.Test;

import java.util.Map;

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
    }

    @Test
    public void createCommand_shouldPassParameters() throws Exception {
        MraidView expectedView = mock(MraidView.class);
        Map<String, String> expectedMap = mock(Map.class);

        MraidCommand command = createCommand(MraidCommandRegistry.MRAID_JAVASCRIPT_COMMAND_EXPAND, expectedMap, expectedView);
        assertThat(command.mParams).isEqualTo(expectedMap);
        assertThat(command.mView).isEqualTo(expectedView);
    }
}
