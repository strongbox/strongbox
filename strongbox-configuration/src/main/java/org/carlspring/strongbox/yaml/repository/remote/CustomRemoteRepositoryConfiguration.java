package org.carlspring.strongbox.yaml.repository.remote;

import javax.annotation.concurrent.Immutable;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

@Immutable
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "layout")
public abstract class CustomRemoteRepositoryConfiguration implements Serializable
{

}
