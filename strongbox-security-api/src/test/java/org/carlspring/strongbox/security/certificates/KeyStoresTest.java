package org.carlspring.strongbox.security.certificates;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class KeyStoresTest
{

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

    @Test
    public void testAll()
            throws IOException,
                   CertificateException,
                   NoSuchAlgorithmException,
                   KeyStoreException,
                   KeyManagementException
    {
        KeyStores.createNew(f, "12345".toCharArray());
        final KeyStore ks = KeyStores.addCertificates(f, "12345".toCharArray(), InetAddress.getLocalHost(), 40636);
        assertEquals("localhost should have three certificates in the chain", 1, ks.size());

        Map<String, Certificate> certs = KeyStores.listCertificates(f, "12345".toCharArray());
        for (final Map.Entry<String, Certificate> cert : certs.entrySet())
        {
            System.out.println(cert.getKey() + " : " + ((X509Certificate) cert.getValue()).getSubjectDN());
        }

        KeyStores.changePassword(f, "12345".toCharArray(), "666".toCharArray());
        KeyStores.removeCertificates(f, "666".toCharArray(), InetAddress.getLocalHost(), 40636);
        certs = KeyStores.listCertificates(f, "666".toCharArray());
        assertTrue(certs.isEmpty());
    }

}
