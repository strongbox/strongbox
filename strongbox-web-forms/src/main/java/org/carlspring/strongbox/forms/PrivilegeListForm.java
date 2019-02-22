package org.carlspring.strongbox.forms;

import java.util.List;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import org.carlspring.strongbox.users.domain.Privileges;

/**
 * @author Pablo Tirado
 */
public class PrivilegeListForm
{

    @NotEmpty
    private List<@NotNull Privileges> privileges;

    public List<Privileges> getPrivileges()
    {
        return privileges;
    }

    public void setPrivileges(List<Privileges> privileges)
    {
        this.privileges = privileges;
    }

}
