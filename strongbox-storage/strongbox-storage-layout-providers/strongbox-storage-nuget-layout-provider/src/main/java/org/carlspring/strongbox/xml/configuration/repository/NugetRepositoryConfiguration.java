package org.carlspring.strongbox.xml.configuration.repository;

import org.carlspring.strongbox.xml.repository.CustomRepositoryConfiguration;

import javax.persistence.Embeddable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author carlspring
 */
@Embeddable
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "nuget-repository-configuration")
public class NugetRepositoryConfiguration
        extends CustomRepositoryConfiguration
{

    @XmlAttribute(name = "feed-version")
    private String feedVersion = "2.0";
    
    @XmlAttribute(name = "remote-feed-page-size")
    private Integer remoteFeedPageSize;


    public NugetRepositoryConfiguration()
    {
    }

    public String getFeedVersion()
    {
        return feedVersion;
    }

    public void setFeedVersion(String feedVersion)
    {
        this.feedVersion = feedVersion;
    }

    public Integer getRemoteFeedPageSize()
    {
        return remoteFeedPageSize;
    }

    public void setRemoteFeedPageSize(Integer remoteFeedPageSize)
    {
        this.remoteFeedPageSize = remoteFeedPageSize;
    }
    
}
