package org.carlspring.strongbox.security.certificates;

import java.io.*;
import java.net.*;
import java.security.*;
import java.security.cert.*;
import javax.net.ssl.*;

public class KeyStores {
    public static KeyStore createNewStore(final String password, final File fileName) throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException {
        final KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        final char [] passwordChars = password.toCharArray();
        keyStore.load(null, passwordChars);
        try (final OutputStream os = new FileOutputStream(fileName)) {
            keyStore.store(os, passwordChars);
        }
        return keyStore;
    }

    public static X509Certificate [] remoteCertificateChain(final KeyStore keyStore, final InetAddress address, final int port) throws NoSuchAlgorithmException, IOException, KeyStoreException, KeyManagementException {
        final SSLContext context = SSLContext.getInstance("TLS");
        final TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(keyStore);
        final X509TrustManager defaultTrustManager = (X509TrustManager)tmf.getTrustManagers()[0];
        final SavingTrustManager tm = new SavingTrustManager(defaultTrustManager);
        context.init(null, new TrustManager[] { tm }, null);
        final SSLSocketFactory factory = context.getSocketFactory();
        final SSLSocket socket = (SSLSocket)factory.createSocket(address, port);
        try {
            socket.startHandshake();
            socket.close();
        } catch (final SSLException ignore) { // non trusted certificates should be returned as well
        }
        return tm.chain;
    }

    private static class SavingTrustManager implements X509TrustManager {
        private final X509TrustManager tm;
        private X509Certificate [] chain;

        SavingTrustManager(final X509TrustManager tm) { this.tm = tm; }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }

        @Override
        public void checkClientTrusted(final X509Certificate [] chain, final String authType) throws CertificateException {
            throw new UnsupportedOperationException();
        }

        @Override
        public void checkServerTrusted(final X509Certificate [] chain, final String authType) throws CertificateException {
            this.chain = chain;
            this.tm.checkServerTrusted(chain, authType);
        }
    }
}