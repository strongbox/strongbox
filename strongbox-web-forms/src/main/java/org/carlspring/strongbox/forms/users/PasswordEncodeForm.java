package org.carlspring.strongbox.forms.users;

import static org.carlspring.strongbox.db.schema.Properties.PASSWORD;

import java.io.Serializable;

import org.carlspring.strongbox.validation.users.Password;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PasswordEncodeForm
        implements Serializable
{

    @Password(min = 8)
    @JsonProperty(PASSWORD)
    private String password;

    @JsonCreator
    public PasswordEncodeForm(@JsonProperty(PASSWORD) String password)
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
