package org.carlspring.strongbox.forms;

import javax.validation.constraints.NotEmpty;

/**
 * @author Pablo Tirado
 */
public class PrivilegeForm
{

    @NotEmpty(message = "This field must not be empty.")
    private String name;

    private String description;

    public PrivilegeForm()
    {
    }

    public PrivilegeForm(String name,
                         String description)
    {
        this.name = name;
        this.description = description;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }
}
