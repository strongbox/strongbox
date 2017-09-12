package org.carlspring.strongbox.services;

import org.carlspring.strongbox.services.support.TrustStoreCertificationAdditionException;

import java.io.IOException;

/**
 * @author Przemyslaw Fusik
 */
public interface TrustStoreService
{

    void addSslCertificatesToTrustStore(String host)
            throws IOException, TrustStoreCertificationAdditionException;

}
