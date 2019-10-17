package org.carlspring.strongbox.controllers.layout.npm;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

@JsonIgnoreProperties(ignoreUnknown = true)
public class NpmUser
{

    private String id;

    private String name;

    private String password;

    private String type;

    private String[] roles;

    private String date;

    private String email;

    @JsonCreator
    public NpmUser()
    {
    }

    @JsonCreator
    public NpmUser(@JsonProperty(value = "_id", required = true) String id,
                   @JsonProperty(value = "name", required = true) String name,
                   @JsonProperty(value = "password", required = true) String password,
                   @JsonProperty(value = "type", required = true) String type,
                   @JsonProperty(value = "roles", required = true) String[] roles,
                   @JsonProperty(value = "date", required = true) String date,
                   @JsonProperty(value = "email") String email)
    {
        this.id = id;
        this.name = name;
        this.password = password;
        this.type = type;
        this.roles = roles;
        this.date = date;
        this.email = email;
    }

    public String getName()
    {
        return name;
    }

    public String getPassword()
    {
        return password;
    }

    public String getType()
    {
        return type;
    }

    public String[] getRoles()
    {
        return roles;
    }

    public String getDate()
    {
        return date;
    }

    public String getId()
    {
        return id;
    }

    @JsonSetter("_id")
    public void setId(String id)
    {
        this.id = id;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public void setRoles(String[] roles)
    {
        this.roles = roles;
    }

    public void setDate(String date)
    {
        this.date = date;
    }
}
