package org.carlspring.strongbox.forms;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Przemyslaw Fusik
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class ExampleForm
{

    @NotNull
    @Size(max = 64)
    private String username;

    @Size(min = 6, message = "This field is less than 6 characters long!")
    @Pattern(regexp = ".*[A-Z].*[A-Z].*", message = "This field requires at least 2 capital letters")
    private String password;

    public String getUsername()
    {
        return username;
    }

    public void setUsername(final String username)
    {
        this.username = username;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(final String password)
    {
        this.password = password;
    }
}
