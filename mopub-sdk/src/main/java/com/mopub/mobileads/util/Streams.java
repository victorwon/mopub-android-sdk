package com.mopub.mobileads.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Streams {
    public static void copyContent(InputStream in, OutputStream out) {
        byte[] buffer = new byte[65536];
        int len;
        try {
            while((len = in.read(buffer)) != -1){
                out.write(buffer, 0, len);
            }
        } catch (IOException e) {
            // too bad
        }
    }
}
