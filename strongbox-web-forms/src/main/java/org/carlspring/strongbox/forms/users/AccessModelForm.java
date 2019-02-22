package org.carlspring.strongbox.forms.users;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Przemyslaw Fusik
 */
public class AccessModelForm
{

    private List<String> apiAcess = new ArrayList<>();
    
    @Valid
    private List<RepositoryAccessModelForm> repositoriesAccess = new ArrayList<>();

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

    public List<String> getApiAcess()
    {
        return apiAcess;
    }

    public void setApiAcess(List<String> apiAcess)
    {
        this.apiAcess = apiAcess;
    }
    
    public void addApiAccess(String privelegie) {
        if (apiAcess == null) {
            apiAcess = new ArrayList<>();
        }
        
        apiAcess.add(privelegie);
    }
    
}
