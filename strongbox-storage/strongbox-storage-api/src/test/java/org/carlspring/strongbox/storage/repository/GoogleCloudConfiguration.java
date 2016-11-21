package org.carlspring.strongbox.storage.repository;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author carlspring
 */
@XmlRootElement(name = "google-cloud-configuration")
public class GoogleCloudConfiguration
        extends CustomConfiguration
{

    @XmlAttribute
    private String bucket;

    @XmlAttribute
    private String key;


    public GoogleCloudConfiguration()
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
