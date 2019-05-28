package org.carlspring.strongbox.security.certificates;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.net.ssl.*;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.*;
import java.security.GeneralSecurityException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.*;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;

public abstract class AbstractKeyStoreManager
{

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected abstract String getKeyStoreType();

    protected abstract FileSystemResource getKeyStoreResource();

    protected abstract char[] getKeyStorePassword();

    @Inject
    private ProxyAuthenticator proxyAuthenticator;

    @PostConstruct
    public void init()
    {
        Authenticator.setDefault(proxyAuthenticator);
    }

    public synchronized Map<String, Certificate> listCertificates()
            throws GeneralSecurityException, IOException
    {
        KeyStore keyStore = load();

        Map<String, Certificate> certificates = new HashMap<>();
        Enumeration<String> aliases = keyStore.aliases();

        while (aliases.hasMoreElements())
        {
            String alias = aliases.nextElement();
            certificates.put(alias, keyStore.getCertificate(alias));
        }

        return certificates;
    }

    public synchronized void removeCertificates(InetAddress host,
                                                int port)
            throws GeneralSecurityException, IOException
    {
        KeyStore keyStore = load();

        String prefix = aliasPrefix(host, port);

        Enumeration<String> aliases = keyStore.aliases();
        List<String> aliasesToDelete = new ArrayList<>();

        while (aliases.hasMoreElements())
        {
            String alias = aliases.nextElement();
            if (StringUtils.startsWithIgnoreCase(alias, prefix))
            {
                aliasesToDelete.add(alias);
            }
        }

        if (aliasesToDelete.size() > 0)
        {
            for (String alias : aliasesToDelete)
            {
                keyStore.deleteEntry(alias);
            }
            store(keyStore);
        }
    }

    public void addCertificates(InetAddress host,
                                int port)
            throws GeneralSecurityException,
                   IOException
    {
        addCertificates(null, null, host, port);
    }

    public void addCertificates(Proxy proxy,
                                PasswordAuthentication credentials,
                                InetAddress host,
                                int port)
            throws
            GeneralSecurityException,
            IOException
    {
        X509Certificate[] chain = remoteCertificateChain(proxy, credentials, host, port);
        storeCertificateEntries(chain, host, port);
    }

    public synchronized int size()
            throws GeneralSecurityException, IOException
    {
        KeyStore keyStore = load();
        return keyStore.size();
    }

    private synchronized void storeCertificateEntries(X509Certificate[] chain,
                                                      InetAddress host,
                                                      int port)
            throws IOException, GeneralSecurityException
    {
        KeyStore keyStore = load();
        String prefix = aliasPrefix(host, port);

        for (X509Certificate cert : chain)
        {
            keyStore.setCertificateEntry(prefix + "_" + cert.getSubjectDN().getName(), cert);
        }

        store(keyStore);
    }

    private void store(KeyStore keyStore)
    {
        try (final OutputStream os = getKeyStoreResource().getOutputStream())
        {
            keyStore.store(os, getKeyStorePassword());
        }
        catch (Exception ex)
        {
            throw new UndeclaredThrowableException(ex);
        }
    }

    public KeyStore load()
            throws IOException, GeneralSecurityException
    {
        KeyStore keyStore = KeyStore.getInstance(getKeyStoreType());
        try (final InputStream inputStream = new BufferedInputStream(getKeyStoreResource().getInputStream()))
        {
            keyStore.load(inputStream, getKeyStorePassword());
        }
        return keyStore;
    }

    private String aliasPrefix(InetAddress host,
                               int port)
    {
        return host.getCanonicalHostName() + ":" + port;
    }

    private X509Certificate[] remoteCertificateChain(Proxy socksProxy,
                                                     PasswordAuthentication credentials,
                                                     InetAddress host,
                                                     int port)
            throws NoSuchAlgorithmException,
                   IOException,
                   KeyManagementException
    {
        if (credentials != null)
        {
            proxyAuthenticator.getCredentials().set(credentials);
        }

        try (Socket proxySocket = socksProxy != null ? new Socket(socksProxy) : null)
        {
            if (proxySocket != null)
            {
                proxySocket.connect(new InetSocketAddress(host, port));
            }

            try
            {
                return handshake(proxySocket, host, port);
            }
            finally
            {
                proxyAuthenticator.getCredentials().remove();
            }
        }
    }

    private X509Certificate[] handshake(Socket proxySocket,
                                        InetAddress host,
                                        int port)
            throws IOException, KeyManagementException, NoSuchAlgorithmException
    {
        CertificatesChainContext certificatesChainContext = new CertificatesChainContext();
        SSLContext ctx = certificatesChainContext.getSSLContext();

        try (SSLSocket socket = (SSLSocket) (proxySocket == null ?
                                             ctx.getSocketFactory().createSocket(host, port) :
                                             ctx.getSocketFactory().createSocket(proxySocket,
                                                                                 host.getHostName(),
                                                                                 port,
                                                                                 true)))
        {
            try
            {
                socket.startHandshake();
            }
            catch (SSLException ignore) // non trusted certificates should be returned as well
            {
            }
        }

        return certificatesChainContext.getChainTrustManager().chain;
    }

    private class ChainCaptureTrustManager
            implements X509TrustManager
    {

        private X509Certificate[] chain;

        @Override
        public X509Certificate[] getAcceptedIssuers()
        {
            return new X509Certificate[0];
        }

        @Override
        public void checkClientTrusted(X509Certificate[] chain,
                                       String authType)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain,
                                       String authType)
        {
            this.chain = chain;
        }
    }

    private class CertificatesChainContext
    {

        private final ChainCaptureTrustManager tm;
        private final SSLContext ctx;

        private CertificatesChainContext()
                throws NoSuchAlgorithmException, KeyManagementException
        {
            this.tm = new ChainCaptureTrustManager();
            this.ctx = SSLContext.getInstance("TLS");
            this.ctx.init(null, new TrustManager[]{ tm }, null);
        }

        private ChainCaptureTrustManager getChainTrustManager()
        {
            return tm;
        }

        private SSLContext getSSLContext()
        {
            return ctx;
        }
    }
}
