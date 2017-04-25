package org.carlspring.strongbox.security.certificates;

import org.apache.commons.lang3.StringUtils;
import org.carlspring.commons.io.resource.ResourceCloser;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Authenticator;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.Socket;
import java.net.URL;
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class KeyStoreManager
{

    private static final Logger logger = LoggerFactory.getLogger(KeyStoreManager.class);

    @Inject
    private ProxyAuthenticator proxyAuthenticator;


    public KeyStoreManager()
    {
    }

    @PostConstruct
    public void init()
    {
        Authenticator.setDefault(proxyAuthenticator);
    }

    private KeyStore load(File fileName,
                          char[] password)
            throws KeyStoreException,
                   IOException,
                   CertificateException,
                   NoSuchAlgorithmException
    {
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        if (fileName == null)
        {
            keyStore.load(null, password);
            return keyStore;
        }

        InputStream is = new FileInputStream(fileName);
        try
        {
            keyStore.load(is, password);
            return keyStore;
        }
        finally
        {
            ResourceCloser.close(is, logger);
        }
    }

    private KeyStore store(File fileName,
                           char[] password,
                           KeyStore keyStore)
            throws IOException,
                   CertificateException,
                   NoSuchAlgorithmException,
                   KeyStoreException
    {
        OutputStream os = new FileOutputStream(fileName);
        try
        {
            keyStore.store(os, password);
            return keyStore;
        }
        finally
        {
            ResourceCloser.close(os, logger);
        }
    }

    public KeyStore createNew(File fileName,
                              char[] password)
            throws KeyStoreException,
                   CertificateException,
                   NoSuchAlgorithmException,
                   IOException
    {
        return store(fileName, password, load(null, password));
    }

    public KeyStore changePassword(File fileName,
                                   char[] oldPassword,
                                   char[] newPassword)
            throws KeyStoreException,
                   IOException,
                   CertificateException,
                   NoSuchAlgorithmException
    {
        return store(fileName, newPassword, load(fileName, oldPassword));
    }

    public Map<String, Certificate> listCertificates(File fileName,
                                                     char[] password)
            throws KeyStoreException,
                   IOException,
                   CertificateException,
                   NoSuchAlgorithmException
    {
        KeyStore keyStore = load(fileName, password);
        Map<String, Certificate> certificates = new HashMap<>();
        Enumeration<String> aliases = keyStore.aliases();

        while (aliases.hasMoreElements())
        {
            String alias = aliases.nextElement();
            certificates.put(alias, keyStore.getCertificate(alias));
        }

        return certificates;
    }

    public KeyStore removeCertificates(File fileName,
                                       char[] password,
                                       InetAddress host,
                                       int port)
            throws KeyStoreException,
                   IOException,
                   CertificateException,
                   NoSuchAlgorithmException
    {
        KeyStore keyStore = load(fileName, password);
        String prefix = host.getCanonicalHostName() + ":" + Integer.toString(port);

        Enumeration<String> aliases = keyStore.aliases();
        while (aliases.hasMoreElements())
        {
            String alias = aliases.nextElement();
            if (StringUtils.startsWithIgnoreCase(alias, prefix))
            {
                keyStore.deleteEntry(alias);
            }
        }

        return store(fileName, password, keyStore);
    }

    public KeyStore addCertificates(File fileName,
                                    char[] password,
                                    InetAddress host,
                                    int port)
            throws KeyStoreException,
                   IOException,
                   CertificateException,
                   NoSuchAlgorithmException,
                   KeyManagementException
    {
        KeyStore keyStore = load(fileName, password);
        String prefix = host.getCanonicalHostName() + ":" + Integer.toString(port);
        X509Certificate[] chain = remoteCertificateChain(host, port);

        for (X509Certificate cert : chain)
        {
            keyStore.setCertificateEntry(prefix + "_" + cert.getSubjectDN().getName(), cert);
        }

        return store(fileName, password, keyStore);
    }

    public KeyStore addHttpsCertificates(File fileName,
                                         char[] password,
                                         Proxy httpProxy,
                                         PasswordAuthentication credentials,
                                         String host,
                                         int port)
            throws CertificateException,
                   NoSuchAlgorithmException,
                   KeyStoreException,
                   IOException,
                   KeyManagementException
    {
        KeyStore keyStore = load(fileName, password);
        String prefix = host + ":" + Integer.toString(port);
        ChainCaptureTrustManager tm = new ChainCaptureTrustManager();
        SSLContext ctx = SSLContext.getInstance("TLS");
        ctx.init(null, new TrustManager[]{ tm }, null);

        URL url = new URL("https", host, port, "/");
        HttpsURLConnection conn = (HttpsURLConnection) (httpProxy != null ?
                                                        url.openConnection(httpProxy) :
                                                        url.openConnection());
        conn.setSSLSocketFactory(ctx.getSocketFactory());

        if (credentials != null)
        {
            proxyAuthenticator.getCredentials().set(credentials);
        }

        try
        {
            conn.connect();
            for (X509Certificate cert : tm.chain)
            {
                keyStore.setCertificateEntry(prefix + "_" + cert.getSubjectDN().getName(), cert);
            }
            return store(fileName, password, keyStore);
        }
        finally
        {
            proxyAuthenticator.getCredentials().remove();
            conn.disconnect();
        }

    }

    public KeyStore addSslCertificates(File fileName,
                                       char[] password,
                                       Proxy socksProxy,
                                       PasswordAuthentication credentials,
                                       String host,
                                       int port)
            throws KeyStoreException,
                   IOException,
                   CertificateException,
                   NoSuchAlgorithmException,
                   KeyManagementException
    {
        KeyStore keyStore = load(fileName, password);
        String prefix = host + ":" + Integer.toString(port);
        X509Certificate[] chain = remoteCertificateChain(socksProxy, credentials, host, port);

        for (X509Certificate cert : chain)
        {
            keyStore.setCertificateEntry(prefix + "_" + cert.getSubjectDN().getName(), cert);
        }

        return store(fileName, password, keyStore);
    }

    private X509Certificate[] remoteCertificateChain(InetAddress address,
                                                     int port)
            throws NoSuchAlgorithmException,
                   IOException,
                   KeyStoreException,
                   KeyManagementException
    {
        ChainCaptureTrustManager tm = new ChainCaptureTrustManager();
        SSLContext ctx = SSLContext.getInstance("TLS");
        ctx.init(null, new TrustManager[]{ tm }, null);
        SSLSocket socket = (SSLSocket) ctx.getSocketFactory().createSocket(address, port);

        try
        {
            socket.startHandshake();
            socket.close();
        }
        catch (SSLException ignore) // non trusted certificates should be returned as well
        {
        }

        return tm.chain;
    }

    private X509Certificate[] remoteCertificateChain(Proxy socksProxy,
                                                     PasswordAuthentication credentials,
                                                     String host,
                                                     int port)
            throws NoSuchAlgorithmException,
                   IOException,
                   KeyStoreException,
                   KeyManagementException
    {
        ChainCaptureTrustManager tm = new ChainCaptureTrustManager();
        SSLContext ctx = SSLContext.getInstance("TLS");
        ctx.init(null, new TrustManager[]{ tm }, null);

        if (credentials != null)
        {
            proxyAuthenticator.getCredentials().set(credentials);
        }

        Socket proxySocket = socksProxy != null ? new Socket(socksProxy) : null;

        if (proxySocket != null)
        {
            proxySocket.connect(new InetSocketAddress(host, port));
        }

        try
        {
            handshake(ctx, proxySocket, host, port);
            return tm.chain;
        }
        finally
        {
            proxyAuthenticator.getCredentials().remove();
            ResourceCloser.close(proxySocket, logger);
        }
    }

    private void handshake(SSLContext ctx,
                           Socket proxySocket,
                           String host,
                           int port)
            throws IOException
    {
        SSLSocket socket = (SSLSocket) (proxySocket == null ?
                                        ctx.getSocketFactory().createSocket(host, port) :
                                        ctx.getSocketFactory().createSocket(proxySocket, host, port, true));

        try
        {
            socket.startHandshake();
        }
        catch (SSLException ignore) // non trusted certificates should be returned as well
        {
        }
        finally
        {
            ResourceCloser.close(socket, logger);
        }
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
                throws CertificateException
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain,
                                       String authType)
                throws CertificateException
        {
            this.chain = chain;
        }
    }

}