package org.carlspring.strongbox.yaml.repository;

import javax.annotation.concurrent.Immutable;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * @author Przemyslaw Fusik
 */
@Immutable
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "layout")
public abstract class CustomRepositoryConfiguration
{

}
