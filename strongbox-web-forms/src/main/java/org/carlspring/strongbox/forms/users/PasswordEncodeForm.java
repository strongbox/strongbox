package org.carlspring.strongbox.forms.users;

import org.carlspring.strongbox.db.schema.Properties;
import org.carlspring.strongbox.validation.users.Password;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PasswordEncodeForm
        implements Serializable
{

    @Password(min = 8)
    @JsonProperty(Properties.PASSWORD)
    private String password;

    @JsonCreator
    public PasswordEncodeForm(@JsonProperty(Properties.PASSWORD) String password)
    {
        this.password = password;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

}
