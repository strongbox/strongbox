package org.carlspring.strongbox.artifact;

import org.carlspring.strongbox.data.domain.DomainObject;

public interface ArtifactTag extends DomainObject
{

    String LAST_VERSION = "last-version";
    String RELEASE = "release";

    default String getName()
    {
        return getUuid();
    }

}
