package com.mopub.mobileads.util;

import com.mopub.mobileads.test.support.SdkTestRunner;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ClientConnectionManager;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.stub;
import static org.mockito.Mockito.verify;

@RunWith(SdkTestRunner.class)
public class HttpClientsTest {

    private HttpClient httpClient;
    private ClientConnectionManager clientConnectionManager;

    @Before
    public void setUp() throws Exception {
        httpClient = mock(HttpClient.class);
        clientConnectionManager = mock(ClientConnectionManager.class);
        stub(httpClient.getConnectionManager()).toReturn(clientConnectionManager);
    }

    @Ignore("pending")
    @Test
    public void safeShutdown_shouldShutdownHttpClient() throws Exception {
        HttpClients.safeShutdown(httpClient);

        Robolectric.runBackgroundTasks();

        verify(clientConnectionManager).shutdown();
    }

    @Test
    public void safeShutdown_withNullHttpClient_shouldNotBlowUp() throws Exception {
        HttpClients.safeShutdown(null);

        Robolectric.runBackgroundTasks();

        verify(clientConnectionManager, never()).shutdown();
    }

    @Test
    public void safeShutdown_withNullConnectionManager_shouldNotBlowUp() throws Exception {
        stub(httpClient.getConnectionManager()).toReturn(null);
        HttpClients.safeShutdown(httpClient);

        Robolectric.runBackgroundTasks();

        verify(clientConnectionManager, never()).shutdown();
    }
}
