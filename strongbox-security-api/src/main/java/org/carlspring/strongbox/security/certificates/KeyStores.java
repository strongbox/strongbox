package org.carlspring.strongbox.security.certificates;

import javax.net.ssl.*;
import java.io.*;
import java.net.InetAddress;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class KeyStores
{
    private static KeyStore load(final File fileName,
                                 final char[] password)
            throws KeyStoreException,
                   IOException,
                   CertificateException,
                   NoSuchAlgorithmException
    {
        final KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        if (fileName == null) {
            keyStore.load(null, password);
            return keyStore;
        }

        final InputStream is = new FileInputStream(fileName);
        try
        {
            keyStore.load(is, password);
            return keyStore;
        }
        finally
        {
            is.close();
        }
    }

    private static KeyStore store(final File fileName,
                                  final char[] password,
                                  final KeyStore keyStore)
            throws IOException,
                   CertificateException,
                   NoSuchAlgorithmException,
                   KeyStoreException {
        final OutputStream os = new FileOutputStream(fileName);
        try
        {
            keyStore.store(os, password);
            return keyStore;
        }
        finally
        {
            os.close();
        }
    }

    public static KeyStore createNew(final File fileName,
                                          final char[] password)
            throws KeyStoreException,
                   CertificateException,
                   NoSuchAlgorithmException,
                   IOException
    {
        return store(fileName, password, load(null, password));
    }

    public static KeyStore changePassword(final File fileName,
                                          final char[] oldPassword,
                                          final char[] newPassword)
            throws KeyStoreException,
                   IOException,
                   CertificateException,
                   NoSuchAlgorithmException {
        return store(fileName, newPassword, load(fileName, oldPassword));
    }

    public static Map<String, Certificate> listCertificates(final File fileName, final char[] password)
            throws KeyStoreException,
                   IOException,
                   CertificateException,
                   NoSuchAlgorithmException
    {
        final KeyStore keyStore = load(fileName, password);
        final Map<String, Certificate> certificates = new HashMap<String, Certificate>();
        final Enumeration<String> aliases = keyStore.aliases();

        while (aliases.hasMoreElements())
        {
            final String alias = aliases.nextElement();
            certificates.put(alias, keyStore.getCertificate(alias));
        }
        return certificates;
    }

    public static KeyStore removeCertificates(final File fileName,
                                              final char[] password,
                                              final InetAddress host,
                                              final int port)
            throws KeyStoreException,
                   IOException,
                   CertificateException,
                   NoSuchAlgorithmException
    {
        final KeyStore keyStore = load(fileName, password);
        final String prefix = host.getCanonicalHostName() + ":" + Integer.toString(port);

        final Enumeration<String> aliases = keyStore.aliases();
        while (aliases.hasMoreElements())
        {
            final String alias = aliases.nextElement();
            if (alias.startsWith(prefix))
                keyStore.deleteEntry(alias);
        }
        return store(fileName, password, keyStore);
    }

    public static KeyStore addCertificates(final File fileName,
                                           final char[] password,
                                           final InetAddress host,
                                           final int port)
            throws KeyStoreException,
                   IOException,
                   CertificateException,
                   NoSuchAlgorithmException,
                   KeyManagementException
    {
        final KeyStore keyStore = load(fileName, password);
        final String prefix = host.getCanonicalHostName() + ":" + Integer.toString(port);
        final X509Certificate [] chain = remoteCertificateChain(host, port);

        for (final X509Certificate cert : chain)
        {
            keyStore.setCertificateEntry(prefix + "_" + cert.getSubjectDN().getName(), cert);
        }

        return store(fileName, password, keyStore);
    }

    private static X509Certificate[] remoteCertificateChain(final InetAddress address,
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