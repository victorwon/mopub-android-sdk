package com.mopub.mobileads;

import android.app.Activity;
import com.mopub.mobileads.test.support.SdkTestRunner;
import com.mopub.mobileads.test.support.StreamUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;

import java.io.*;

import static com.mopub.mobileads.VastVideoDownloadTask.OnDownloadCompleteListener;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.stub;
import static org.mockito.Mockito.verify;

@RunWith(SdkTestRunner.class)
public class VastVideoDownloadTaskTest {
    private OnDownloadCompleteListener onDownloadCompleteListener;
    private DiskLruCache diskLruCache;
    private VastVideoDownloadTask subject;
    private String videoUrl;
    private File cacheDirectory;

    @Before
    public void setUp() throws Exception {
        onDownloadCompleteListener = mock(OnDownloadCompleteListener.class);
        diskLruCache = mock(DiskLruCache.class);
        Activity context = new Activity();

        cacheDirectory = new File(context.getFilesDir(), "test_cache_directory");
        cacheDirectory.mkdirs();
        stub(diskLruCache.getCacheDirectory()).toReturn(cacheDirectory);

        videoUrl = "http://www.video.com";

        subject = new VastVideoDownloadTask(onDownloadCompleteListener, diskLruCache);
    }

    @After
    public void tearDown() throws Exception {
        cacheDirectory.delete();
    }

    @Ignore("pending")
    @Test
    public void execute_shouldAddToCacheAndSignalDownloadSuccess() throws Exception {
        subject.execute(videoUrl);

        ArgumentCaptor<InputStream> inputStreamCaptor = ArgumentCaptor.forClass(InputStream.class);
        verify(diskLruCache).putStream(eq(videoUrl), inputStreamCaptor.capture());
        InputStream inputStream = inputStreamCaptor.getValue();

//        assertThat(inputStreamToString(inputStream)).isEqualTo("");
    }

    @Ignore("pending")
    @Test
    public void execute_withMultipleUrls_shouldParseTheFirstOne() throws Exception {
        subject.execute(videoUrl, "ignored");
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

    @Ignore("pending")
    @Test
    public void connectToUrl_shouldReturnInputStreamFromHttpConnection() throws Exception {
        InputStream result = subject.connectToUrl(videoUrl);

        String response = inputStreamToString(result);
        assertThat(response).isNotNull();
        assertThat(response).isNotEmpty();
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
        assertThat(result.getParentFile()).isEqualTo(cacheDirectory);
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
        stub(diskLruCache.putStream(anyString(), any(InputStream.class))).toReturn(true);

        File tempFile = File.createTempFile("something", null, cacheDirectory);
        new FileOutputStream(tempFile).write(byteArrayInputStream.read());

        boolean result = subject.copyTempFileIntoCache(videoUrl, tempFile);

        assertThat(result).isTrue();
        verify(diskLruCache).putStream(eq(videoUrl), any(InputStream.class));

        tempFile.delete();
    }

    @Test
    public void copyTempFileIntoCache_whenUnableToPutInCache_shouldReturnFalse() throws Exception {
        ByteArrayInputStream byteArrayInputStream = StreamUtils.createByteArrayInputStream(20);
        stub(diskLruCache.putStream(anyString(), any(InputStream.class))).toReturn(false);

        File tempFile = File.createTempFile("something", null, cacheDirectory);
        new FileOutputStream(tempFile).write(byteArrayInputStream.read());

        boolean result = subject.copyTempFileIntoCache(videoUrl, tempFile);

        assertThat(result).isFalse();
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
