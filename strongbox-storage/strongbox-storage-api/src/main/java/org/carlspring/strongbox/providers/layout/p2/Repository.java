package org.carlspring.strongbox.providers.layout.p2;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Repository
{

    private Artifacts artifacts;

    @XmlElement(name = "artifacts")
    public Artifacts getArtifacts()
    {
        return artifacts;
    }

    public void setArtifacts(Artifacts artifacts)
    {
        this.artifacts = artifacts;
    }

}
