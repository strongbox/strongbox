package org.carlspring.strongbox.security.certificates;

import java.io.*;
import java.net.*;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.*;
import java.util.*;
import org.junit.*;
import static org.junit.Assert.*;

public class KeyStoresTest
{

    private File f;
    private KeyStore ks;


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
        ks = KeyStores.createNewStore(f, "12345".toCharArray(), Collections.<String, Certificate>emptyMap());
    }

    @Test
    public void testAddDeleteList()
            throws IOException,
                   CertificateException,
                   NoSuchAlgorithmException,
                   KeyStoreException,
                   KeyManagementException
    {
        final X509Certificate [] certs = KeyStores.remoteCertificateChain(InetAddress.getByName("localhost"), 40636);
        assertEquals("localhost should have three certificates in the chain", 1, certs.length);

        for (final X509Certificate cert : certs)
        {
            ks.setCertificateEntry(cert.getSerialNumber().toString(16), cert);
        }

        final Enumeration<String> aliases = ks.aliases();
        for (int i = 0; aliases.hasMoreElements(); i++)
        {
            String alias = aliases.nextElement();
            System.out.println(" " + certs[i].getSubjectDN().getName());
            assertEquals(certs[i].getSerialNumber().toString(16), alias);
        }

        assertEquals(ks.size(), 1);

        ks.deleteEntry(certs[0].getSerialNumber().toString(16));
        assertEquals(ks.size(), 0);
    }

    @Test
    public void testChangePwd()
            throws IOException,
                   CertificateException,
                   NoSuchAlgorithmException,
                   KeyStoreException
    {
        final OutputStream os = new FileOutputStream(f);
        try
        {
            ks.store(os, "xxx".toCharArray());
        }
        finally
        {
            os.close();
        }
    }

}
