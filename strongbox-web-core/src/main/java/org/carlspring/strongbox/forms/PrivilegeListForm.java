package org.carlspring.strongbox.forms;

import javax.validation.Valid;
import java.util.List;

/**
 * @author Pablo Tirado
 */
public class PrivilegeListForm
{

    @Valid
    private List<PrivilegeForm> privileges;

    public List<PrivilegeForm> getPrivileges()
    {
        return privileges;
    }

    public void setPrivileges(List<PrivilegeForm> privileges)
    {
        this.privileges = privileges;
    }
}