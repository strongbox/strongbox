package org.carlspring.strongbox.storage.repository.aws;

import org.carlspring.strongbox.storage.repository.CustomConfiguration;

import javax.annotation.concurrent.Immutable;

/**
 * @author Przemyslaw Fusik
 */
@Immutable
public class AwsConfiguration
        extends CustomConfiguration
{

    private final String bucket;

    private final String key;

    public AwsConfiguration(final MutableAwsConfiguration delegate)
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
