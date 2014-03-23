package org.carlspring.strongbox.security.certificates;

import javax.net.ssl.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;

public class KeyStores
{

    public static KeyStore createNewStore(final File fileName,
                                          final char[] password,
                                          final Map<String, Certificate> certificates)
            throws KeyStoreException,
                   CertificateException,
                   NoSuchAlgorithmException,
                   IOException
    {
        final KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(null, password);

        for (final Map.Entry<String, Certificate> entry : certificates.entrySet())
        {
            keyStore.setCertificateEntry(entry.getKey(), entry.getValue());
        }
        final OutputStream os = new FileOutputStream(fileName);
        try
        {
            keyStore.store(os, password);
        }
        finally
        {
            os.close();
        }

        return keyStore;
    }

    public static X509Certificate[] remoteCertificateChain(final InetAddress address,
                                                           final int port)
            throws NoSuchAlgorithmException,
                   IOException,
                   KeyStoreException,
                   KeyManagementException
    {
        final ChainCaptureTrustManager tm = new ChainCaptureTrustManager();
        final SSLContext ctx = SSLContext.getInstance("TLS");
        ctx.init(null, new TrustManager[]{ tm }, null);
        final SSLSocket socket = (SSLSocket) ctx.getSocketFactory().createSocket(address, port);

        try
        {
            socket.startHandshake();
            socket.close();
        }
        catch (final SSLException ignore) // non trusted certificates should be returned as well
        {
        }

        return tm.chain;
    }

    private static class ChainCaptureTrustManager
            implements X509TrustManager
    {

        private X509Certificate[] chain;

        @Override
        public X509Certificate[] getAcceptedIssuers()
        {
            return new X509Certificate[0];
        }

        @Override
        public void checkClientTrusted(final X509Certificate[] chain,
                                       final String authType)
                throws CertificateException
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public void checkServerTrusted(final X509Certificate[] chain,
                                       final String authType)
                throws CertificateException
        {
            this.chain = chain;
        }
    }
}