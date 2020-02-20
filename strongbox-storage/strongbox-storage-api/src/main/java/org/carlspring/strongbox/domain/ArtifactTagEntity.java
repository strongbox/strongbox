package org.carlspring.strongbox.domain;

import org.apache.commons.lang3.StringUtils;
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

    private String name;

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    @Override
    public int hashCode()
    {
        if (name == null)
        {
            return 0;
        }
        return name.hashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (!(obj instanceof ArtifactTagEntity))
        {
            return false;
        }
        return StringUtils.equals(name, ((ArtifactTagEntity) obj).name);
    }

}
