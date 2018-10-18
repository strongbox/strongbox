package org.carlspring.strongbox.xml.repository.remote;

import javax.annotation.concurrent.Immutable;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

@Immutable
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "layout")
public abstract class CustomRemoteRepositoryConfiguration implements RemoteRepositoryConfiguration
{

}
