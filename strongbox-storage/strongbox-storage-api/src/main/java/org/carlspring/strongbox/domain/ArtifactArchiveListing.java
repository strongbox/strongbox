package org.carlspring.strongbox.domain;

import java.io.Serializable;

public interface ArtifactArchiveListing extends Serializable
{

    String getFileName();

    void setFileName(String fileName);

}
