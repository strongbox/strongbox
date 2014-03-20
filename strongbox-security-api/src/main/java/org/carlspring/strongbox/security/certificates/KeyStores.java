package org.carlspring.strongbox.security.certificates;

import java.io.*;
import java.net.*;
import java.security.*;
import java.security.cert.*;
import javax.net.ssl.*;

public class KeyStores {
    public static KeyStore createNewStore(final String password, final File fileName) throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException
    {
        final KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        final char [] passwordChars = password.toCharArray();
        keyStore.load(null, passwordChars);
        final OutputStream os = new FileOutputStream(fileName);
        try
        {
            keyStore.store(os, passwordChars);
        }
        finally
        {
            os.close();
        }
        return keyStore;
    }

    public static X509Certificate [] remoteCertificateChain(final KeyStore keyStore, final InetAddress address, final int port) throws NoSuchAlgorithmException, IOException, KeyStoreException, KeyManagementException
    {
        final ChainCaptureTrustManager tm = new ChainCaptureTrustManager();
        final SSLContext ctx = SSLContext.getInstance("TLS");
        ctx.init(null, new TrustManager[] { tm }, null);
        final SSLSocket socket = (SSLSocket)ctx.getSocketFactory().createSocket(address, port);
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

    private static class ChainCaptureTrustManager implements X509TrustManager
    {
        private X509Certificate [] chain;

        @Override
        public X509Certificate [] getAcceptedIssuers()
        {
            return new X509Certificate[0];
        }

        @Override
        public void checkClientTrusted(final X509Certificate [] chain, final String authType) throws CertificateException
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public void checkServerTrusted(final X509Certificate [] chain, final String authType) throws CertificateException
        {
            this.chain = chain;
        }
    }
}