package org.carlspring.strongbox.domain;

import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
import org.carlspring.strongbox.data.domain.GenericEntity;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author carlspring
 */
public class ArtifactDependency extends GenericEntity
        implements Serializable
{

    private ArtifactCoordinates artifactCoordinates;

    private Map<String, ArtifactDependency> artifactDependencies = new LinkedHashMap<>();


    public ArtifactDependency()
    {
    }

    public ArtifactCoordinates getArtifactCoordinates()
    {
        return artifactCoordinates;
    }

    public void setArtifactCoordinates(ArtifactCoordinates artifactCoordinates)
    {
        this.artifactCoordinates = artifactCoordinates;
    }

    public void putAll(Collection<? extends ArtifactDependency> dependencies)
    {
        for (ArtifactDependency dependency : dependencies)
        {
            artifactDependencies.put(dependency.toString(), dependency);
        }
    }

    public ArtifactDependency remove(Object key)
    {
        return artifactDependencies.remove(key);
    }

    public ArtifactDependency put(ArtifactDependency dependency)
    {
        return artifactDependencies.put(dependency.toString(), dependency);
    }

    public Map<String, ArtifactDependency> getArtifactDependencies()
    {
        return artifactDependencies;
    }

    public void setArtifactDependencies(Map<String, ArtifactDependency> artifactDependencies)
    {
        this.artifactDependencies = artifactDependencies;
    }

}
