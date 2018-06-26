package org.carlspring.strongbox.storage.repository;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.carlspring.strongbox.xml.CustomTag;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * @author carlspring
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public abstract class MutableCustomConfiguration
        implements CustomTag
{

    @JsonIgnore
    public abstract CustomConfiguration getImmutable();
}
