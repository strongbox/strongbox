package org.carlspring.strongbox.services.impl;

import org.carlspring.strongbox.security.certificates.KeyStoreManager;
import org.carlspring.strongbox.services.TrustStoreService;
import org.carlspring.strongbox.services.support.TrustStoreCertificationAdditionException;

import javax.inject.Inject;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

/**
 * @author Przemyslaw Fusik
 */
@Service
public class TrustStoreServiceImpl
        implements TrustStoreService
{

    private static final String PASSWORD = "password";

    @Value("${strongbox.home}/etc/ssl/truststore.jks")
    private Resource trustStore;

    @Inject
    private KeyStoreManager keyStoreManager;


    @Override
    public void addSslCertificatesToTrustStore(String host)
            throws IOException, TrustStoreCertificationAdditionException
    {
        final URL url = new URL(host);
        final String urlHost = url.getHost();
        final int urlPort = url.getPort() != -1 ? url.getPort() : url.getDefaultPort();

        try
        {
            keyStoreManager.addCertificates(trustStore.getFile(), PASSWORD.toCharArray(),
                                            InetAddress.getByName(urlHost), urlPort);
        }
        catch (IOException | CertificateException | NoSuchAlgorithmException | KeyStoreException | KeyManagementException ex)
        {
            throw new TrustStoreCertificationAdditionException(ex);
        }
    }
}
