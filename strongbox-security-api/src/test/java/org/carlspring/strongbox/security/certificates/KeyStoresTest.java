package org.carlspring.strongbox.security.certificates;

import java.io.*;
import java.net.*;
import java.security.*;
import java.security.cert.*;
import org.junit.*;
import static org.junit.Assert.*;

public class KeyStoresTest
{
    @Test
    public void testRemoteCertificates() throws IOException, CertificateException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException
    {
        final File f = File.createTempFile("test", ".jks");
        try
        {
            final KeyStore ks = KeyStores.createNewStore("12345", f);
            final X509Certificate [] certs = KeyStores.remoteCertificateChain(ks, InetAddress.getByName("www.google.com"), 443);
            assertEquals("www.google.com should have three certificates in the chain", certs.length, 3);
            for (int i = 0; i < certs.length; i++)
            {
                ks.setCertificateEntry(certs[i].getSerialNumber().toString(16), certs[i]);
            }
            final OutputStream os = new FileOutputStream(f);
            try
            {
                ks.store(os, "12345".toCharArray());
            } finally {
                os.close();
            }
        }
        finally
        {
            f.delete();
        }
    }
}
