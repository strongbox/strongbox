package org.carlspring.strongbox.security.certificates;

import org.carlspring.strongbox.SecurityApiTestConfig;
import org.carlspring.strongbox.net.ConnectionChecker;
import org.carlspring.strongbox.testing.AssignedPorts;

import javax.inject.Inject;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.parallel.ExecutionMode.SAME_THREAD;

@SpringBootTest
@ActiveProfiles(profiles = "test")
@ContextConfiguration(classes = SecurityApiTestConfig.class)
@TestExecutionListeners(mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
@Execution(SAME_THREAD)
public class KeyStoreManagerTestIT
{

    @Inject
    private AssignedPorts assignedPorts;

    private static final String PROXY_USERNAME = "testuser";

    private static final String PROXY_PASSWORD = "password";

    private static final String PROXY_HOST = "localhost";

    private static final String SOCKS_HOST = "192.168.100.1";

    private static final int PROXY_SOCKS_PORT = 15035;

    private static int PROXY_HTTP_PORT;

    private static int LDAPS_PORT;

    private final Proxy PROXY_SOCKS = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(PROXY_HOST, PROXY_SOCKS_PORT));

    private final Proxy PROXY_HTTP = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(PROXY_HOST, PROXY_HTTP_PORT));

    private static final PasswordAuthentication credentials = new PasswordAuthentication(PROXY_USERNAME,
                                                                                         PROXY_PASSWORD.toCharArray());

    @Inject
    private KeyStoreManager keyStoreManager;

    @BeforeEach
    public void init()
    {
        PROXY_HTTP_PORT = assignedPorts.getPort("port.littleproxy");
        LDAPS_PORT = assignedPorts.getPort("port.unboundid");
    }

    @Test
    public void testWithoutProxy()
            throws Exception
    {
        keyStoreManager.addCertificates(InetAddress.getLocalHost(), LDAPS_PORT);

        assertThat(keyStoreManager.size()).isEqualTo(2);

        Map<String, Certificate> certs = keyStoreManager.listCertificates();
        assertThat(certs).hasSize(2);

        for (final Map.Entry<String, Certificate> cert : certs.entrySet())
        {
            assertThat(cert.getKey()).contains("localhost");
            System.out.println(cert.getKey() + " : " + ((X509Certificate) cert.getValue()).getSubjectDN());
        }

        keyStoreManager.removeCertificates(InetAddress.getLocalHost(), LDAPS_PORT);
        certs = keyStoreManager.listCertificates();

        assertThat(certs).hasSize(1);
        assertThat(certs.keySet().iterator().next()).isEqualTo("localhost");
    }

    @Test
    public void testSocksProxy()
            throws Exception
    {
        if (!ConnectionChecker.checkServiceAvailability(SOCKS_HOST, PROXY_SOCKS_PORT, 5000))
        {
            System.out.println("WARN: Skipping the testSocks() test, as the proxy server is unreachable.");

            return;
        }

        keyStoreManager.addCertificates(PROXY_SOCKS,
                                        credentials,
                                        InetAddress.getByName("google.com"),
                                        443);

        assertEquals(1, keyStoreManager.size(), "localhost should have one certificate in the chain");

        Map<String, Certificate> certs = keyStoreManager.listCertificates();
        for (final Map.Entry<String, Certificate> cert : certs.entrySet())
        {
            System.out.println(cert.getKey() + " : " + ((X509Certificate) cert.getValue()).getSubjectDN());
        }

        keyStoreManager.removeCertificates(InetAddress.getLocalHost(), LDAPS_PORT);
        certs = keyStoreManager.listCertificates();

        assertTrue(certs.isEmpty());
    }

    @Test
    public void testHttpProxy()
            throws Exception
    {
        InetAddress googleInetAddress = InetAddress.getByName("google.com");

        keyStoreManager.addCertificates(PROXY_HTTP,
                                        credentials,
                                        googleInetAddress,
                                        443);

        assertThat(keyStoreManager.size()).isEqualTo(3);

        Map<String, Certificate> certs = keyStoreManager.listCertificates();
        certs.remove("localhost");
        assertThat(certs.size()).isEqualTo(2);

        for (final Map.Entry<String, Certificate> cert : certs.entrySet())
        {
            assertThat(cert.getKey()).contains("google");
            System.out.println(cert.getKey() + " : " + ((X509Certificate) cert.getValue()).getSubjectDN());
        }

        keyStoreManager.removeCertificates(googleInetAddress, 443);
        certs = keyStoreManager.listCertificates();

        assertThat(certs).hasSize(1);
        assertThat(certs.keySet().iterator().next()).isEqualTo("localhost");
    }

}
