package org.carlspring.strongbox.storage.repository;

import javax.xml.bind.annotation.*;

/**
 * @author carlspring
 */
@XmlRootElement(name = "aws-configuration")
public class AwsConfiguration extends CustomConfiguration
{

    @XmlAttribute
    private String bucket;

    @XmlAttribute
    private String key;


    public AwsConfiguration()
    {
    }

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

}
