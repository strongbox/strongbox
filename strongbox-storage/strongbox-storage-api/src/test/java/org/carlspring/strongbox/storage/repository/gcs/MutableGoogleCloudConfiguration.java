package org.carlspring.strongbox.storage.repository.gcs;

import org.carlspring.strongbox.storage.repository.CustomConfiguration;
import org.carlspring.strongbox.storage.repository.MutableCustomConfiguration;
import org.carlspring.strongbox.xml.CustomTag;

/**
 * @author carlspring
 * @author Pablo Tirado
 */
public class MutableGoogleCloudConfiguration
        extends MutableCustomConfiguration
        implements CustomTag
{

    private String bucket;

    private String key;

    public String getBucket()
    {
        return bucket;
    }

    public void setBucket(String bucket)
    {
        this.bucket = bucket;
    }

    public String getKey()
    {
        return key;
    }

    public void setKey(String key)
    {
        this.key = key;
    }

    @Override
    public CustomConfiguration getImmutable()
    {
        return new GoogleCloudConfiguration(this);
    }
}
