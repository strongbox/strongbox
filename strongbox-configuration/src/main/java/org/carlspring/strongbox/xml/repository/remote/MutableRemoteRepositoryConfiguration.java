package org.carlspring.strongbox.xml.repository.remote;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
/*
@JsonSubTypes({
        @Type(value = MutableMavenRemoteRepositoryConfiguration.class, name = "mavenRemoteRepositoryConfiguration"),
        @Type(value = MutableNpmRemoteRepositoryConfiguration.class, name = "npmRemoteRepositoryConfiguration"),
        @Type(value = MutableNugetRemoteRepositoryConfiguration.class, name = "nugetRemoteRepositoryConfiguration"),
        @Type(value = MutableRawRemoteRepositoryConfiguration.class, name = "rawRemoteRepositoryConfiguration")
})
*/
public abstract class MutableRemoteRepositoryConfiguration implements RemoteRepositoryConfiguration
{

    @JsonIgnore
    public abstract CustomRemoteRepositoryConfiguration getImmutable();
    
}
