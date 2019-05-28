package org.carlspring.strongbox.security.certificates;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;

/**
 * @author Przemyslaw Fusik
 */
public class TrustStoreManager
        extends AbstractKeyStoreManager
{

    @Value("${server.ssl.trust-store-type:PKCS12}")
    private String keyStoreType;

    @Value("${server.ssl.trust-store-password:password}")
    private String keyStorePassword;

    @Value("${server.ssl.trust-store:truststore.p12}")
    private FileSystemResource keyStoreResource;

    @Override
    protected String getKeyStoreType()
    {
        return keyStoreType;
    }

    @Override
    protected FileSystemResource getKeyStoreResource()
    {
        return keyStoreResource;
    }

    @Override
    protected char[] getKeyStorePassword()
    {
        return keyStorePassword.toCharArray();
    }

}
