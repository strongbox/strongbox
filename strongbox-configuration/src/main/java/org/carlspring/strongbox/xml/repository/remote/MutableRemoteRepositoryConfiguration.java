package org.carlspring.strongbox.xml.repository.remote;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public abstract class MutableRemoteRepositoryConfiguration implements RemoteRepositoryConfiguration
{

    @JsonIgnore
    public abstract CustomRemoteRepositoryConfiguration getImmutable();
    
}
