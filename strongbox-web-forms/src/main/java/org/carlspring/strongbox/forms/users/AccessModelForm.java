package org.carlspring.strongbox.forms.users;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Przemyslaw Fusik
 */
public class AccessModelForm
{

    @Valid
    private List<RepositoryAccessModelForm> repositoriesAccess;

    public List<RepositoryAccessModelForm> getRepositoriesAccess()
    {
        return repositoriesAccess;
    }

    public void setRepositoriesAccess(final List<RepositoryAccessModelForm> repositoriesAccess)
    {
        this.repositoriesAccess = repositoriesAccess;
    }

    public void addRepositoryAccess(RepositoryAccessModelForm repositoryAccess)
    {
        if (repositoriesAccess == null)
        {
            repositoriesAccess = new ArrayList<>();
        }
        repositoriesAccess.add(repositoryAccess);
    }
}
