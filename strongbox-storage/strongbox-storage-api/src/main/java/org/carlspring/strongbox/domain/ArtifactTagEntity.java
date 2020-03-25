package org.carlspring.strongbox.domain;

import org.carlspring.strongbox.artifact.ArtifactTag;
import org.carlspring.strongbox.data.domain.DomainEntity;
import org.carlspring.strongbox.db.schema.Vertices;
import org.neo4j.ogm.annotation.NodeEntity;

/**
 * @author Sergey Bespalov
 *
 */
@NodeEntity(Vertices.ARTIFACT_TAG)
public class ArtifactTagEntity
        extends DomainEntity
        implements ArtifactTag
{

    public ArtifactTagEntity()
    {
    }
    
    public ArtifactTagEntity(String name)
    {
        setName(name);
    }

    public void setName(String name)
    {
        setUuid(name);
    }

}
