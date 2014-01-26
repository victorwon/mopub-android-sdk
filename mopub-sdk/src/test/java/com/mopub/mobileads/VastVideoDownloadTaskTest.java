package com.mopub.mobileads;

import android.app.Activity;
import android.net.Uri;
import com.mopub.mobileads.test.support.SdkTestRunner;
import com.mopub.mobileads.test.support.StreamUtils;
import com.mopub.mobileads.test.support.TestHttpResponseWithHeaders;
import com.mopub.mobileads.util.Streams;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;

import java.io.*;

import static com.mopub.mobileads.VastVideoDownloadTask.OnDownloadCompleteListener;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@RunWith(SdkTestRunner.class)
public class VastVideoDownloadTaskTest {
    private OnDownloadCompleteListener onDownloadCompleteListener;
    private DiskLruCache diskLruCache;
    private VastVideoDownloadTask subject;
    private String videoUrl;
    private TestHttpResponseWithHeaders response;

    @Before
    public void setUp() throws Exception {
        onDownloadCompleteListener = mock(OnDownloadCompleteListener.class);
        Activity context = new Activity();
        diskLruCache = new DiskLruCache(context, "test_cache_directory", 1000);

        videoUrl = "http://www.video.com";
        response = new TestHttpResponseWithHeaders(200, "responseBody");
        Robolectric.addPendingHttpResponse(response);

        subject = new VastVideoDownloadTask(onDownloadCompleteListener, diskLruCache);
    }

    @After
    public void tearDown() throws Exception {
        diskLruCache.evictAll();
    }

    @Test
    public void execute_shouldAddToCacheAndSignalDownloadSuccess() throws Exception {
        subject.execute(videoUrl);

        Uri uri = diskLruCache.getUri(videoUrl);
        File file = new File(uri.toString());

        assertThat(file.exists()).isTrue();
        assertThat(file.length()).isEqualTo("responseBody".length());

        verify(onDownloadCompleteListener).onDownloadSuccess();
        verify(onDownloadCompleteListener, never()).onDownloadFailed();
    }

    @Test
    public void execute_withMultipleUrls_shouldParseTheFirstOne() throws Exception {
        String ignoredUrl = "ignored";
        subject.execute(videoUrl, ignoredUrl);

        Uri uri = diskLruCache.getUri(videoUrl);
        File expectedFile = new File(uri.toString());
        Uri ignoredUri = diskLruCache.getUri(ignoredUrl);

        assertThat(expectedFile.exists()).isTrue();
        assertThat(expectedFile.length()).isEqualTo("responseBody".length());
        assertThat(ignoredUri).isNull();

        verify(onDownloadCompleteListener).onDownloadSuccess();
        verify(onDownloadCompleteListener, never()).onDownloadFailed();
    }

    @Test
    public void execute_whenUrlArrayIsNull_shouldSignalDownloadFailed() throws Exception {
        subject.execute((String) null);

        verify(onDownloadCompleteListener).onDownloadFailed();
        verify(onDownloadCompleteListener, never()).onDownloadSuccess();
    }

    @Test
    public void execute_whenFirstElementOfUrlArrayIsNull_shouldSignalDownloadFailed() throws Exception {
        subject.execute(null, "ignored");

        verify(onDownloadCompleteListener).onDownloadFailed();
        verify(onDownloadCompleteListener, never()).onDownloadSuccess();
    }

    @Test
    public void onPostExecute_whenOnDownloadCompleteListenerIsNull_shouldNotBlowUp() throws Exception {
        subject = new VastVideoDownloadTask(null, diskLruCache);

        subject.onPostExecute(true);
        subject.onPostExecute(false);

        // pass
    }

    @Test
    public void connectToUrl_shouldReturnInputStreamFromHttpConnection() throws Exception {
        InputStream result = subject.connectToUrl(videoUrl);

        assertThat(inputStreamToString(result)).isEqualTo("responseBody");
    }

    @Test
    public void connectToUrl_whenVideoUrlIsNull_shouldThrowIOException() throws Exception {
        try {
            subject.connectToUrl(null);
            fail("Expected IOException due to null videoUrl");
        } catch (IOException exception) {
            // pass
        }
    }

    @Test
    public void copyInputStream_withLessThan25MB_shouldCreateFile() throws Exception {
        InputStream inputStream = StreamUtils.createByteArrayInputStream(25 * 1000 * 1000 - 1);

        File result = subject.copyInputStreamToTempFile(inputStream);

        assertThat(result.exists());
        assertThat(result.isFile());
        assertThat(result.getName()).matches("mopub-vast\\d+\\.tmp");
        assertThat(result.getParentFile()).isEqualTo(diskLruCache.getCacheDirectory());
        assertThat(result.length()).isEqualTo(25 * 1000 * 1000 - 1);

        result.delete();
    }

    @Test
    public void copyInputStream_withMoreThan25MB_shouldThrowIOException() throws Exception {
        InputStream inputStream = StreamUtils.createByteArrayInputStream(25 * 1000 * 1000 + 1);

        try {
            subject.copyInputStreamToTempFile(inputStream);
            fail();
        } catch (IOException exception) {
            // pass
        }
    }

    @Test
    public void copyInputStreamToTempFile_whenInputStreamIsNull_shouldThrowIOExpcetion() throws Exception {
        try {
            subject.copyInputStreamToTempFile(null);
            fail("Expected IOException due to null InputStream");
        } catch (IOException exception) {
            // pass
        }
    }

    @Test
    public void copyTempFileIntoCache_shouldReturnTrueOnSuccess() throws Exception {
        ByteArrayInputStream byteArrayInputStream = StreamUtils.createByteArrayInputStream(20);

        File tempFile = File.createTempFile("something", null, diskLruCache.getCacheDirectory());
        FileOutputStream fileOutputStream = new FileOutputStream(tempFile);
        fileOutputStream.write(byteArrayInputStream.read());
        Streams.closeStream(fileOutputStream);

        boolean result = subject.copyTempFileIntoCache(videoUrl, tempFile);

        assertThat(result).isTrue();
        assertThat(diskLruCache.getUri(videoUrl)).isNotNull();

        tempFile.delete();
    }

    @Ignore("pending")
    @Test
    public void copyTempFileIntoCache_whenUnableToPutInCache_shouldReturnFalse() throws Exception {
        ByteArrayInputStream byteArrayInputStream = StreamUtils.createByteArrayInputStream(20);

//        File tempFile = File.createTempFile("something", null, diskLruCache.getCacheDirectory());
//        new FileOutputStream(tempFile).write(byteArrayInputStream.read());

//        File tempFile = new File("/tmp/blah.mp4");
//        tempFile.createNewFile();

        File tempFile = File.createTempFile("mopub-vast", null, diskLruCache.getCacheDirectory());

        diskLruCache.put(videoUrl, tempFile);
        diskLruCache.put(Utils.sha1(videoUrl), tempFile);
        diskLruCache.put(Utils.sha1(Utils.sha1(videoUrl)), tempFile);

        boolean result = subject.copyTempFileIntoCache(videoUrl, tempFile);
        assertThat(result).isFalse();

        tempFile.delete();
    }

    @Test
    public void copyTempFileIntoCache_whenFileNotFound_shouldThrowFileNotFound() throws Exception {
        try {
            subject.copyTempFileIntoCache(videoUrl, new File(""));
            fail("Expected FileNotFound exception");
        } catch (FileNotFoundException exception) {
            // pass
        }
    }

    private String inputStreamToString(InputStream inputStream) {
        if (inputStream == null) {
            return null;
        }

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[65536];
        int read;

        try {
            while ((read = inputStream.read(buffer, 0, buffer.length)) != -1) {
                byteArrayOutputStream.write(buffer, 0, read);
            }
        } catch (IOException exception) {
            return null;
        }

        return new String(byteArrayOutputStream.toByteArray());
    }
}
