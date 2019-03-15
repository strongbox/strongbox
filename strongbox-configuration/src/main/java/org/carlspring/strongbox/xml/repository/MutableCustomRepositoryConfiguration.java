package org.carlspring.strongbox.xml.repository;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * @author carlspring
 * @author Pablo Tirado
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
public abstract class MutableCustomRepositoryConfiguration
        implements RepositoryConfiguration
{

    @JsonIgnore
    public abstract CustomRepositoryConfiguration getImmutable();

}
