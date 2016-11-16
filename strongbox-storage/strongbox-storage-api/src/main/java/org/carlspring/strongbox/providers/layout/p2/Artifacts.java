package org.carlspring.strongbox.providers.layout.p2;

import javax.xml.bind.annotation.XmlElement;
import java.util.ArrayList;
import java.util.List;

public class Artifacts
{

    private Integer size;

    private List<Artifact> artifacts = new ArrayList();

    @XmlElement(name = "artifact")
    public List<Artifact> getArtifacts()
    {
        return artifacts;
    }

    public void setArtifacts(List<Artifact> artifacts)
    {
        this.artifacts = artifacts;
    }
}