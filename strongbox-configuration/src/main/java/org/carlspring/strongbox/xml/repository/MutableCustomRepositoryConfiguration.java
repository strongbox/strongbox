package org.carlspring.strongbox.xml.repository;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * @author carlspring
 * @author Pablo Tirado
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "type")
/*
@JsonSubTypes({
        @Type(value = MutableMavenRepositoryConfiguration.class, name = "mavenRepositoryConfiguration"),
        @Type(value = MutableNpmRepositoryConfiguration.class, name = "npmRepositoryConfiguration"),
        @Type(value = MutableNugetRepositoryConfiguration.class, name = "nugetRepositoryConfiguration"),
        @Type(value = MutableRawRepositoryConfiguration.class, name = "rawRepositoryConfiguration")
})
*/
public abstract class MutableCustomRepositoryConfiguration
        implements RepositoryConfiguration
{

    @JsonIgnore
    public abstract CustomRepositoryConfiguration getImmutable();

}
