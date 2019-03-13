package org.carlspring.strongbox.xml.repository.remote;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * @author Pablo Tirado
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public abstract class MutableRemoteRepositoryConfiguration implements RemoteRepositoryConfiguration
{

    @JsonIgnore
    public abstract CustomRemoteRepositoryConfiguration getImmutable();
    
}
