package org.carlspring.strongbox.storage.repository;

import org.carlspring.strongbox.yaml.CustomTag;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * @author carlspring
 * @author Pablo Tirado
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
public abstract class MutableCustomConfiguration
        implements CustomTag
{

    @JsonIgnore
    public abstract CustomConfiguration getImmutable();
}
