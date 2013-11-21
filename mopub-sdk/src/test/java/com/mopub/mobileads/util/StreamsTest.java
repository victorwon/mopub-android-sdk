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

package com.mopub.mobileads.util;

import com.mopub.mobileads.test.support.SdkTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.*;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.fail;

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

    @Test
    public void copyStream_withMaxBytes_belowThreshold_shouldCopyContentsOfOneStreamToAnother() throws Exception {
        File inFile = new File("etc/expectedFile.jpg");
        FileInputStream in = new FileInputStream(inFile);
        File tempFile = File.createTempFile("foo", "bar");
        FileOutputStream out = new FileOutputStream(tempFile);

        Streams.copyContent(in, out, 1000000);

        assertThat(inFile.length()).isEqualTo(tempFile.length());
    }

    @Test
    public void copyStream_withMaxBytes_aboveThreshold_shouldThrowIOException() throws Exception {
        InputStream in = new ByteArrayInputStream("this is a pretty long stream".getBytes());

        File tempFile = File.createTempFile("foo", "bar");
        FileOutputStream out = new FileOutputStream(tempFile);

        try {
            Streams.copyContent(in, out, 10);
            fail("Expected IOException.");
        } catch (IOException e) {
            // pass
        }
    }
}
