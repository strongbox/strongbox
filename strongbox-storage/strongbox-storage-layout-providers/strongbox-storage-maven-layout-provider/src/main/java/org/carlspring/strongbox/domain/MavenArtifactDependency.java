package org.carlspring.strongbox.domain;

import java.io.Serializable;

/**
 * @author carlspring
 */
public class MavenArtifactDependency extends ArtifactDependency<MavenArtifactDependency>
        implements Serializable
{

    @Override
    public Class<MavenArtifactDependency> getEntityClass()
    {
        return MavenArtifactDependency.class;
    }

}
