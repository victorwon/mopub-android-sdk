package com.mopub.mobileads.test.support;

import com.mopub.mobileads.util.Streams;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class FileUtils {
    public static void copyFile(String sourceFile, String destinationFile) {
        try {
            Streams.copyContent(new FileInputStream(sourceFile), new FileOutputStream(destinationFile));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
