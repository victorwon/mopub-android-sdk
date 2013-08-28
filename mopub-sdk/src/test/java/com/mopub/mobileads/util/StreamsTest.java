package com.mopub.mobileads.util;

import com.mopub.mobileads.test.support.SdkTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(SdkTestRunner.class)
public class StreamsTest {
    @Test
    public void copyStream_shouldCopyContentsOfOneStreamToAnother() throws Exception {
        File inFile = new File("etc/expectedFile.jpg");
        FileInputStream in = new FileInputStream(inFile);
        File tempFile = File.createTempFile("foo", "bar");
        FileOutputStream out = new FileOutputStream(tempFile);

        Streams.copyContent(in, out);

        assertThat(inFile.length()).isEqualTo(tempFile.length());
    }
}
