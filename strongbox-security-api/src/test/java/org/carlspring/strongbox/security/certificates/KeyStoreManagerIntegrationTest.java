package org.carlspring.strongbox.security.certificates;

import org.carlspring.strongbox.net.ConnectionChecker;
import org.carlspring.strongbox.testing.AssignedPorts;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class KeyStoreManagerIntegrationTest
{

    @org.springframework.context.annotation.Configuration
    @ComponentScan(basePackages = { "org.carlspring.strongbox.security",
                                    "org.carlspring.strongbox.testing" })
    public static class SpringConfig { }

    @Inject
    private AssignedPorts assignedPorts;

    private static final String PROXY_USERNAME = "testuser";

    private static final String PROXY_PASSWORD = "password";

    private static final String KEYSTORE_PASSWORD = "password";

    private static final String PROXY_HOST = "localhost";

    private static final String SOCKS_HOST = "192.168.100.1";

    private static final int PROXY_SOCKS_PORT = 15035;

    private static int PROXY_HTTP_PORT;

    private final Proxy PROXY_SOCKS = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(PROXY_HOST, PROXY_SOCKS_PORT));

    private final Proxy PROXY_HTTP = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(PROXY_HOST, PROXY_HTTP_PORT));

    private static final PasswordAuthentication credentials = new PasswordAuthentication(PROXY_USERNAME, PROXY_PASSWORD.toCharArray());

    public static int LDAPS_PORT;

    private File f;

    @Inject
    KeyStoreManager keyStoreManager;


    @Before
    public void init()
            throws IOException,
                   CertificateException,
                   NoSuchAlgorithmException,
                   KeyStoreException
    {
        //noinspection ResultOfMethodCallIgnored
        new File("target/test-resources").mkdirs();
        f = new File("target/test-resources/test.jks");

        PROXY_HTTP_PORT = assignedPorts.getPort("port.littleproxy");
        LDAPS_PORT = assignedPorts.getPort("port.unboundid");
    }

    @Test
    public void testWithoutProxy()
            throws IOException,
                   CertificateException,
                   NoSuchAlgorithmException,
                   KeyStoreException,
                   KeyManagementException
    {
        keyStoreManager.createNew(f, KEYSTORE_PASSWORD.toCharArray());
        final KeyStore ks = keyStoreManager.addCertificates(f,
                                                            KEYSTORE_PASSWORD.toCharArray(),
                                                            InetAddress.getLocalHost(),
                                                            LDAPS_PORT);

        assertEquals("localhost should have three certificates in the chain", 1, ks.size());

        Map<String, Certificate> certs = keyStoreManager.listCertificates(f, KEYSTORE_PASSWORD.toCharArray());
        for (final Map.Entry<String, Certificate> cert : certs.entrySet())
        {
            System.out.println(cert.getKey() + " : " + ((X509Certificate)cert.getValue()).getSubjectDN());
        }

        final String newPassword = "newpassword";

        keyStoreManager.changePassword(f, KEYSTORE_PASSWORD.toCharArray(), newPassword.toCharArray());
        keyStoreManager.removeCertificates(f, newPassword.toCharArray(), InetAddress.getLocalHost(), LDAPS_PORT);
        certs = keyStoreManager.listCertificates(f, newPassword.toCharArray());

        assertTrue(certs.isEmpty());
    }

    @Test
    public void testSocksProxy()
            throws IOException,
                   CertificateException,
                   NoSuchAlgorithmException,
                   KeyStoreException,
                   KeyManagementException
    {
        if (!ConnectionChecker.checkServiceAvailability(SOCKS_HOST, PROXY_SOCKS_PORT, 5000))
        {
            System.out.println("WARN: Skipping the testSocks() test, as the proxy server is unreachable.");
            return;
        }

        keyStoreManager.createNew(f, KEYSTORE_PASSWORD.toCharArray());
        final KeyStore ks = keyStoreManager.addSslCertificates(f,
                                                               KEYSTORE_PASSWORD.toCharArray(),
                                                               PROXY_SOCKS,
                                                               credentials,
                                                               "google.com",
                                                               443);

        assertEquals("localhost should have one certificate in the chain", 1, ks.size());

        Map<String, Certificate> certs = keyStoreManager.listCertificates(f, KEYSTORE_PASSWORD.toCharArray());
        for (final Map.Entry<String, Certificate> cert : certs.entrySet())
        {
            System.out.println(cert.getKey() + " : " + ((X509Certificate) cert.getValue()).getSubjectDN());
        }

        final String newPassword = "newpassword";

        keyStoreManager.changePassword(f, KEYSTORE_PASSWORD.toCharArray(), newPassword.toCharArray());
        keyStoreManager.removeCertificates(f, newPassword.toCharArray(), InetAddress.getLocalHost(), LDAPS_PORT);
        certs = keyStoreManager.listCertificates(f, newPassword.toCharArray());

        assertTrue(certs.isEmpty());
    }

    // @Ignore
    @Test
    public void testHttpProxy()
            throws IOException,
                   CertificateException,
                   NoSuchAlgorithmException,
                   KeyStoreException,
                   KeyManagementException
    {
        if (!ConnectionChecker.checkServiceAvailability(PROXY_HOST, PROXY_HTTP_PORT, 5000))
        {
            System.out.println("WARN: Skipping the testHttp() test, as the proxy server is unreachable.");
            return;
        }

        System.out.println("Executing HTTP proxy test...");

        keyStoreManager.createNew(f, KEYSTORE_PASSWORD.toCharArray());
        final KeyStore ks = keyStoreManager.addHttpsCertificates(f,
                                                                 KEYSTORE_PASSWORD.toCharArray(),
                                                                 PROXY_HTTP,
                                                                 credentials,
                                                                 "google.com",
                                                                 443);

        assertEquals("google.com should have three certificate in the chain", 3, ks.size());

        Map<String, Certificate> certs = keyStoreManager.listCertificates(f, KEYSTORE_PASSWORD.toCharArray());
        for (final Map.Entry<String, Certificate> cert : certs.entrySet())
        {
            System.out.println(cert.getKey() + " : " + ((X509Certificate) cert.getValue()).getSubjectDN());
        }
    }

}
