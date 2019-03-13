package org.carlspring.strongbox.xml.repository;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * @author carlspring
 * @author Pablo Tirado
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public abstract class MutableCustomRepositoryConfiguration
        implements RepositoryConfiguration
{

    @JsonIgnore
    public abstract CustomRepositoryConfiguration getImmutable();

}
