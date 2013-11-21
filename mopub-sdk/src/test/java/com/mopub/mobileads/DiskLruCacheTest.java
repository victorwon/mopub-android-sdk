package com.mopub.mobileads;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;

import com.mopub.mobileads.test.support.SdkTestRunner;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.*;

import static com.mopub.mobileads.test.support.StreamUtils.createByteArrayInputStream;
import static junit.framework.Assert.fail;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.stub;

@RunWith(SdkTestRunner.class)
public class DiskLruCacheTest {

    private Context context;
    private String cacheDirectoryName;
    private int maxSizeBytes;
    private DiskLruCache subject;

    @Before
    public void setUp() throws Exception {
        context = new Activity();
        cacheDirectoryName = "mopub_video_cache";
        maxSizeBytes = 1000;

        subject = new DiskLruCache(context, cacheDirectoryName, maxSizeBytes);
    }

    @After
    public void tearDown() throws Exception {
        File[] files = subject.getCacheDirectory().listFiles();

        if (files != null) {
            for (final File file : files) {
                file.delete();
            }
        }
    }

    @Test
    public void constructor_shouldSetVideoCacheDirectory() throws Exception {
        File videoCacheDirectory = subject.getCacheDirectory();

        assertThat(videoCacheDirectory.exists()).isTrue();
        assertThat(videoCacheDirectory.isDirectory()).isTrue();
        assertThat(videoCacheDirectory.getAbsolutePath()).isEqualTo(context.getFilesDir() + "/mopub_video_cache");
    }

    @Test
    public void constructor_whenNullContext_shouldThrowIllegalArgumentException() throws Exception {
        try {
            subject = new DiskLruCache(null, cacheDirectoryName, maxSizeBytes);
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException exception) {
            // pass
        }
    }

    @Test
    public void constructor_whenNullCacheDirectoryName_shouldThrowIllegalArgumentException() throws Exception {
        try {
            subject = new DiskLruCache(context, null, maxSizeBytes);
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException exception) {
            // pass
        }
    }

    @Test
    public void constructor_whenCacheDirectoryNameUsesSpecialCharacters_shouldNotBlowUp() throws Exception {
        subject = new DiskLruCache(context, "//  ///`~!@#$ %^&*( )_+-=[]{}\\|;:'\",<.....>/?", maxSizeBytes);

        subject.putStream("test1.txt", new ByteArrayInputStream("string".getBytes()));

        // pass
    }

    @Test
    public void constructor_whenCacheDirectoryNameIsEmpty_shouldNotBlowUp() throws Exception {
        subject = new DiskLruCache(context, "", maxSizeBytes);

        subject.putStream("test1.txt", new ByteArrayInputStream("string".getBytes()));

        // pass
    }

    @Test
    public void constructor_whenMaxSizeBytes_shouldThrowIllegalArgumentException() throws Exception {
        try {
            subject = new DiskLruCache(context, cacheDirectoryName, -1000);
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException exception) {
            // pass
        }
    }

    @Test
    public void constructor_whenCacheDirectoryExistsAsAFile_shouldThrowIOException() throws Exception {
        File file = new File(subject.getCacheDirectory().getAbsolutePath());
        file.delete();
        file.createNewFile();

        try {
            subject = new DiskLruCache(context, cacheDirectoryName, maxSizeBytes);
            fail("Should throw IOException");
        } catch (IOException exception) {
            // pass
        } finally {
            file.delete();
        }
    }
    
    @Test
    public void constructor_whenFilesExistInCacheDirectory_shouldInitializeCache() throws Exception {
        subject.putStream("dog.txt", createByteArrayInputStream(11));
        subject.putStream("cat.txt", createByteArrayInputStream(23));

        DiskLruCache newCache = new DiskLruCache(context, cacheDirectoryName, maxSizeBytes);

        File dogFile = new File(String.valueOf(newCache.getUri("dog.txt")));
        File catFile = new File(String.valueOf(newCache.getUri("cat.txt")));

        assertThat(newCache.snapshot().size()).isEqualTo(2);
        assertThat(dogFile.length()).isEqualTo(11);
        assertThat(catFile.length()).isEqualTo(23);
    }

    @Test
    public void constructor_whenFilesExistInADifferentCacheDirectory_shouldInitializeEmptyCache() throws Exception {
        subject.putStream("dog.txt", createByteArrayInputStream(11));
        subject.putStream("cat.txt", createByteArrayInputStream(23));

        DiskLruCache newCache = new DiskLruCache(context, "a_different_cache", maxSizeBytes);

        assertThat(newCache.snapshot().size()).isEqualTo(0);

        newCache.evictAll();
    }

    @Test
    public void constructor_whenNoFilesExistInCacheDirectory_shouldBeEmptyLruCache() throws Exception {
        assertThat(subject.snapshot().isEmpty()).isTrue();
    }

    @Test
    public void getUri_whenValueInCache_shouldReturnUriRepresentationOfFile() throws Exception {
        ByteArrayInputStream byteArrayInputStream = createByteArrayInputStream(100);
        Uri expectedUri = Uri.parse(context.getFilesDir() + "/mopub_video_cache/" + Utils.sha1("file1.gif"));

        subject.putStream("file1.gif", byteArrayInputStream);

        assertThat(subject.getUri("file1.gif").equals(expectedUri)).isTrue();
    }

    @Test
    public void getUri_whenValueNotInCache_shouldReturnNull() throws Exception {
        subject.evictAll();

        assertThat(subject.getUri("this_file_does_not_exist.bat")).isNull();
    }

    @Test
    public void putStream_withValidFileNameAndInputStream_shouldAddKeyValuePairToCache() throws Exception {
        assertThat(subject.get("some_file")).isNull();

        boolean success = subject.putStream("some_file", createByteArrayInputStream(123));

        assertThat(success).isTrue();
        assertThat(subject.snapshot().size()).isEqualTo(1);
        assertThat(subject.getUri("some_file")).isNotNull();
    }

    @Test
    public void putStream_withEmptyFileName_shoulAddKeyValuePairToCache() throws Exception {
        assertThat(subject.get("")).isNull();

        boolean success = subject.putStream("", createByteArrayInputStream(234));

        assertThat(success).isTrue();
        assertThat(subject.snapshot().size()).isEqualTo(1);
        assertThat(subject.getUri("")).isNotNull();
    }

    @Test
    public void putStream_withEmptyInputStream_shouldAddKeyValuePairToCache() throws Exception {
        assertThat(subject.get("some_file")).isNull();

        boolean success = subject.putStream("some_file", createByteArrayInputStream(0));

        assertThat(success).isTrue();
        assertThat(subject.snapshot().size()).isEqualTo(1);
        assertThat(subject.getUri("some_file")).isNotNull();
    }

    @Test
    public void putStream_withNullFileName_shouldNotAddKeyValuePairToCache() throws Exception {
        boolean success = subject.putStream(null, createByteArrayInputStream(1));

        assertThat(success).isFalse();
        assertThat(subject.snapshot().isEmpty()).isTrue();
    }

    @Test
    public void putStream_withNullInputStream_shouldNotAddKeyValuePairToCache() throws Exception {
        boolean success = subject.putStream("some_file", (InputStream) null);

        assertThat(success).isFalse();
        assertThat(subject.snapshot().isEmpty()).isTrue();
    }

    @Test
    public void putStream_canHandleFileNamesWithSymbols() throws Exception {
        String fileNameWithSymbols = " ~`!@#$%^&*()_+-={}|[]\\:\";'<>,.?/";
        boolean success = subject.putStream(fileNameWithSymbols, createByteArrayInputStream(1));

        assertThat(success).isTrue();
        assertThat(subject.snapshot().size()).isEqualTo(1);
        assertThat(subject.getUri(fileNameWithSymbols)).isNotNull();
    }

    @Test
    public void putStream_whenGoingOverSizeLimit_shouldRemoveLeastRecentlyUsedItem() throws Exception {
        subject.putStream("file0", createByteArrayInputStream(700));
        subject.putStream("file1", createByteArrayInputStream(200));
        subject.putStream("file2", createByteArrayInputStream(50));

        // We should be safe, with 950 kb / 1000 kb allotted.

        assertThat(subject.snapshot().size()).isEqualTo(3);
        assertThat(subject.getUri("file0")).isNotNull();
        assertThat(subject.getUri("file1")).isNotNull();
        assertThat(subject.getUri("file2")).isNotNull();

        // We go over the edge with file3

        subject.putStream("file3", createByteArrayInputStream(60));

        assertThat(subject.snapshot().size()).isEqualTo(3);
        assertThat(subject.getUri("file0")).isNull();
        assertThat(subject.getUri("file1")).isNotNull();
        assertThat(subject.getUri("file2")).isNotNull();
        assertThat(subject.getUri("file3")).isNotNull();
    }

    @Test
    public void put_whenGoingOverSizeLimit_canCauseMultipleEvictions() throws Exception {
        subject.putStream("file0", createByteArrayInputStream(100));
        subject.putStream("file1", createByteArrayInputStream(101));

        assertThat(subject.snapshot().size()).isEqualTo(2);
        assertThat(subject.getUri("file0")).isNotNull();
        assertThat(subject.getUri("file1")).isNotNull();

        subject.putStream("file2", createByteArrayInputStream(999));

        assertThat(subject.snapshot().size()).isEqualTo(1);
        assertThat(subject.getUri("file0")).isNull();
        assertThat(subject.getUri("file1")).isNull();
        assertThat(subject.getUri("file2")).isNotNull();
    }

    @Test
    public void removeStream_shouldRemoveBackingFileFromFilesystem() throws Exception {
        ByteArrayInputStream byteArrayInputStream = createByteArrayInputStream(660);
        File file = new File(context.getFilesDir() + "/mopub_video_cache/" + Utils.sha1("this_will_be_removed.exe"));

        subject.putStream("this_will_be_removed.exe", byteArrayInputStream);

        assertFileLength(file, 660);

        subject.removeStream("this_will_be_removed.exe");

        assertFileDoesNotExist(file);
    }

    @Test
    public void remove_whenRemovingAFileThatDoesNotExist_shouldDoNothing() throws Exception {
        File untouchedFile = new File(context.getFilesDir() + "/mopub_video_cache/" + Utils.sha1("cat_pic.gif"));

        subject.putStream("cat_pic.gif", createByteArrayInputStream(27));

        assertFileLength(untouchedFile, 27);

        subject.removeStream("something_else.jpg");

        assertFileLength(untouchedFile, 27);
    }

    @Test
    public void sizeOf_shouldReturnSizeOfTheFileAsInteger() throws Exception {
        File file = createMockFile(1000);

        assertThat(subject.sizeOf(null, file)).isEqualTo(1000);
    }
    
    @Test
    public void sizeOf_whenFileIsNullNonExistentOrEmpty_shouldReturnDefaultOne() throws Exception {
        assertThat(subject.sizeOf(null, null)).isEqualTo(1);

        File file = mock(File.class);
        stub(file.exists()).toReturn(false);

        assertThat(subject.sizeOf(null, file)).isEqualTo(1);

        file = createMockFile(-1);

        assertThat(subject.sizeOf(null, file)).isEqualTo(1);
    }

    private void assertFileLength(File file, long length) {
        assertThat(file.exists()).isTrue();
        assertThat(file.length()).isEqualTo(length);
    }

    private void assertFileDoesNotExist(File file) {
        assertThat(file.exists()).isFalse();
        assertThat(file.length()).isEqualTo(0);
    }

    private File createMockFile(int size) {
        File file = mock(File.class);
        stub(file.exists()).toReturn(true);
        stub(file.length()).toReturn((long) size);

        return file;
    }
}
