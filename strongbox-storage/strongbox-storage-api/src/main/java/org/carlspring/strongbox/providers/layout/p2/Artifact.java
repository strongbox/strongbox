package org.carlspring.strongbox.providers.layout.p2;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Artifact
{

    private String version;
    private String id;
    private String classifier;

    @XmlAttribute
    public String getVersion()
    {
        return version;
    }

    public void setVersion(String version)
    {
        this.version = version;
    }

    @XmlAttribute
    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    @XmlAttribute
    public String getClassifier()
    {
        return classifier;
    }

    public void setClassifier(String classifier)
    {
        this.classifier = classifier;
    }
}
