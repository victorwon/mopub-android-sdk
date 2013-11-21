package com.mopub.mobileads.test.support;

import java.io.*;
import java.util.*;

public class StreamUtils {
    public static ByteArrayInputStream createByteArrayInputStream(int size) {
        byte[] buffer = new byte[size];
        new Random().nextBytes(buffer);

        return new ByteArrayInputStream(buffer);
    }
}
