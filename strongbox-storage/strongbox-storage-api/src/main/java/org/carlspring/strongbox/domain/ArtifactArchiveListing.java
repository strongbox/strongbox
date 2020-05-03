package org.carlspring.strongbox.domain;

import org.carlspring.strongbox.data.domain.DomainObject;

public interface ArtifactArchiveListing extends DomainObject
{

    String getFileName();

    void setFileName(String fileName);

}
