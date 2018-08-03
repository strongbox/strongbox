package org.carlspring.strongbox.validation.users.support;

import org.carlspring.strongbox.validation.users.Password;

import java.io.Serializable;

public class PasswordAnnotationTestClass
{

    @Password(groups = { NewUser.class }, min = 8, max = 9)
    @Password(groups = { ExistingUser.class }, allowNull = true, min = 8, max = 9)
    private String password;

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public interface NewUser
            extends Serializable
    {

    }

    public interface ExistingUser
            extends Serializable
    {

    }
}
