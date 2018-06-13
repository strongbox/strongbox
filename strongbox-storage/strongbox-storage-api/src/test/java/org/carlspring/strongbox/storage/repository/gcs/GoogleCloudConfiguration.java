package org.carlspring.strongbox.storage.repository.gcs;

import org.carlspring.strongbox.storage.repository.CustomConfiguration;

import javax.annotation.concurrent.Immutable;

/**
 * @author Przemyslaw Fusik
 */
@Immutable
public class GoogleCloudConfiguration
        extends CustomConfiguration
{

    private final String bucket;

    private final String key;

    public GoogleCloudConfiguration(final MutableGoogleCloudConfiguration delegate)
    {
        this.bucket = delegate.getBucket();
        this.key = delegate.getKey();
    }

    public String getBucket()
    {
        return bucket;
    }

    public String getKey()
    {
        return key;
    }
}
