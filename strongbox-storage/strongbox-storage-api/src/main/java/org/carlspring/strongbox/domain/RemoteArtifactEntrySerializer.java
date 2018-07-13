package org.carlspring.strongbox.domain;

import org.carlspring.strongbox.data.domain.EntitySerializer;
import org.springframework.stereotype.Component;

@Component
public class RemoteArtifactEntrySerializer extends EntitySerializer<RemoteArtifactEntry>
{

    @Override
    public int getTypeId()
    {
        return 50;
    }

    @Override
    public Class<RemoteArtifactEntry> getEntityClass()
    {
        return RemoteArtifactEntry.class;
    }

}
