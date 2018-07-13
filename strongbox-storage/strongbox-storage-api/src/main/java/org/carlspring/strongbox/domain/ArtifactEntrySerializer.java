package org.carlspring.strongbox.domain;

import org.carlspring.strongbox.data.domain.EntitySerializer;
import org.springframework.stereotype.Component;

@Component
public class ArtifactEntrySerializer extends EntitySerializer<ArtifactEntry>
{

    @Override
    public int getTypeId()
    {
        return 10;
    }

    @Override
    public Class<ArtifactEntry> getEntityClass()
    {
        return ArtifactEntry.class;
    }

}
