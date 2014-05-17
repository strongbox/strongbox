package org.carlspring.strongbox.security.certificates;

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
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

public class KeyStoresTest
{


    private static final String PROXY_USERNAME = "testuser";

    private static final String PROXY_PASSWORD = "password";

    private static final Proxy PROXY_SOCKS = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress("192.168.100.1", 15035));

    private static final Proxy PROXY_HTTP = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("192.168.100.1", 15036));

    private static final PasswordAuthentication credentials = new PasswordAuthentication(PROXY_USERNAME, PROXY_PASSWORD.toCharArray());

    private static final String KEYSTORE_PASSWORD = "password";

    private File f;


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
    }

    @Ignore
    public void testSocks()
            throws IOException,
                   CertificateException,
                   NoSuchAlgorithmException,
                   KeyStoreException,
                   KeyManagementException
    {
        KeyStores.createNew(f, "12345".toCharArray());
        //final KeyStore ks = KeyStores.addSslCertificates(f, "12345".toCharArray(), null, null, "localhost", 40636);
        final KeyStore ks = KeyStores.addSslCertificates(f,
                                                         KEYSTORE_PASSWORD.toCharArray(),
                                                         PROXY_SOCKS,
                                                         credentials,
                                                         "localhost",
                                                         40636);

        assertEquals("localhost should have one certificate in the chain", 1, ks.size());

        Map<String, Certificate> certs = KeyStores.listCertificates(f, "12345".toCharArray());
        for (final Map.Entry<String, Certificate> cert : certs.entrySet())
        {
            System.out.println(cert.getKey() + " : " + ((X509Certificate) cert.getValue()).getSubjectDN());
            System.out.println(cert.getKey() + " : " + ((X509Certificate) cert.getValue()).getSubjectDN());
        }

        KeyStores.changePassword(f, KEYSTORE_PASSWORD.toCharArray(), "666".toCharArray());
        KeyStores.removeCertificates(f, "666".toCharArray(), InetAddress.getLocalHost(), 40636);
        certs = KeyStores.listCertificates(f, "666".toCharArray());

        assertTrue(certs.isEmpty());
    }


    @Test
    public void testHttp()
            throws IOException,
                   CertificateException,
                   NoSuchAlgorithmException,
                   KeyStoreException,
                   KeyManagementException
    {
        KeyStores.createNew(f, KEYSTORE_PASSWORD.toCharArray());
        final KeyStore ks = KeyStores.addHttpsCertificates(f,
                                                           KEYSTORE_PASSWORD.toCharArray(),
                                                           PROXY_HTTP,
                                                           credentials,
                                                           "google.com",
                                                           443);

        assertEquals("google.com should have three certificate in the chain", 3, ks.size());

        Map<String, Certificate> certs = KeyStores.listCertificates(f, KEYSTORE_PASSWORD.toCharArray());
        for (final Map.Entry<String, Certificate> cert : certs.entrySet())
        {
            System.out.println(cert.getKey() + " : " + ((X509Certificate) cert.getValue()).getSubjectDN());
        }
    }

}
