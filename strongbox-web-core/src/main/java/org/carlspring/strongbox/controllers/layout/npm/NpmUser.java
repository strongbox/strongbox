package org.carlspring.strongbox.controllers.layout.npm;

import org.carlspring.strongbox.db.schema.Properties;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

@JsonIgnoreProperties(ignoreUnknown = true)
public class NpmUser
{

    private String name;

    private String password;

    @JsonCreator
    public NpmUser()
    {
    }

    @JsonCreator
    public NpmUser(@JsonProperty(value = Properties.NAME, required = true) String name,
                   @JsonProperty(value = Properties.PASSWORD, required = true) String password)
    {
        this.name = name;
        this.password = password;
    }

    public String getName()
    {
        return name;
    }

    public String getPassword()
    {
        return password;
    }
}