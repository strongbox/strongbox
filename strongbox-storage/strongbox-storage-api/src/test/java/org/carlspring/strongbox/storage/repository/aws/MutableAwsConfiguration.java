package org.carlspring.strongbox.storage.repository.aws;

import org.carlspring.strongbox.storage.repository.MutableCustomConfiguration;
import org.carlspring.strongbox.storage.repository.CustomConfiguration;
import org.carlspring.strongbox.xml.CustomTag;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author carlspring
 */
@XmlRootElement(name = "aws-configuration")
public class MutableAwsConfiguration
        extends MutableCustomConfiguration
        implements CustomTag
{

    @XmlAttribute
    private String bucket;

    @XmlAttribute
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
        return new AwsConfiguration(this);
    }
}
