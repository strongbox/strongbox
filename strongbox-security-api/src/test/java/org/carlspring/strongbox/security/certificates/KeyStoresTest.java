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
    public void init() throws IOException, CertificateException, NoSuchAlgorithmException, KeyStoreException
    {
        f = File.createTempFile("test", ".jks");
        ks = KeyStores.createNewStore(f, "12345".toCharArray(), Collections.<String, Certificate>emptyMap());
    }

    @After
    public void cleanup()
    {
        f.delete();
    }

    @Test
    public void testAddDeleteList() throws IOException, CertificateException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException
    {
        final X509Certificate [] certs = KeyStores.remoteCertificateChain(InetAddress.getByName("www.google.com"), 443);
        assertEquals("www.google.com should have three certificates in the chain", certs.length, 3);

        for (final X509Certificate cert : certs)
            ks.setCertificateEntry(cert.getSerialNumber().toString(16), cert);
        assertEquals(ks.size(), 3);

        ks.deleteEntry(certs[2].getSerialNumber().toString(16));
        assertEquals(ks.size(), 2);

        final Enumeration<String> aliases = ks.aliases();
        for (int i = 0; aliases.hasMoreElements(); i++) {
            String alias = aliases.nextElement();
            System.out.println(alias);
            assertEquals(certs[i].getSerialNumber().toString(16), alias);
        }
    }

    @Test
    public void testChangePwd() throws IOException, CertificateException, NoSuchAlgorithmException, KeyStoreException
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
