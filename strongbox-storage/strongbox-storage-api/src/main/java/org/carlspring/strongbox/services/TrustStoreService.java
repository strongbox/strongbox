package org.carlspring.strongbox.services;

import org.carlspring.strongbox.services.support.TrustStoreCertificateOperationException;

import java.io.IOException;

/**
 * @author Przemyslaw Fusik
 */
public interface TrustStoreService
{

    void addSslCertificatesToTrustStore(String host)
            throws IOException, TrustStoreCertificateOperationException;

}
