package org.carlspring.strongbox.domain;

import org.carlspring.strongbox.data.domain.EntitySerializer;
import org.springframework.stereotype.Component;

@Component
public class ArtifactTagSerializer extends EntitySerializer<ArtifactTagEntry>
{

    @Override
    public int getTypeId()
    {
        return 20;
    }

    @Override
    public Class<ArtifactTagEntry> getEntityClass()
    {
        return ArtifactTagEntry.class;
    }

}
