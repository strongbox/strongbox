package org.carlspring.strongbox.storage.repository;

import org.carlspring.strongbox.xml.CustomTag;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * @author carlspring
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
/*
@JsonSubTypes({
        @Type(value = MutableAwsConfiguration.class, name = "awsConfiguration"),
        @Type(value = MutableGoogleCloudConfiguration.class, name = "awsConfiguration")
})
*/
public abstract class MutableCustomConfiguration
        implements CustomTag
{

    @JsonIgnore
    public abstract CustomConfiguration getImmutable();
}
