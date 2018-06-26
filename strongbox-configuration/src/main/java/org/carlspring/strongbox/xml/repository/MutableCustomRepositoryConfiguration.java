package org.carlspring.strongbox.xml.repository;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * @author carlspring
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public abstract class MutableCustomRepositoryConfiguration
        implements RepositoryConfiguration
{

    @JsonIgnore
    public abstract CustomRepositoryConfiguration getImmutable();

}
