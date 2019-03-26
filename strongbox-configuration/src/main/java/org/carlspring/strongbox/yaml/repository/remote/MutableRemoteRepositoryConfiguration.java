package org.carlspring.strongbox.yaml.repository.remote;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * @author Pablo Tirado
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
public abstract class MutableRemoteRepositoryConfiguration
        implements Serializable
{

    @JsonIgnore
    public abstract CustomRemoteRepositoryConfiguration getImmutable();

}
