package org.carlspring.repositoryunit.storage;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import com.thoughtworks.xstream.annotations.XStreamOmitField;
import org.carlspring.repositoryunit.storage.repository.Repository;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author mtodorov
 */
public class Storage
{

    @XStreamAlias(value = "basedir")
    private String basedir;

    @XStreamAlias(value = "repositories")
    private Map<String, Repository> repositories = new LinkedHashMap<String, Repository>();


    public Storage()
    {
    }

    public Storage(String basedir)
    {
        this.basedir = basedir;
    }

    public String getBasedir()
    {
        return basedir;
    }

    public void setBasedir(String basedir)
    {
        this.basedir = basedir;
    }

    public Map<String, Repository> getRepositories()
    {
        return repositories;
    }

    public void setRepositories(Map<String, Repository> repositories)
    {
        this.repositories = repositories;
    }

    public void addRepository(Repository repository)
    {
        repositories.put(repository.getName(), repository);
    }

    public void removeRepository(Repository repository)
    {
        repositories.remove(repository.getName());
    }

    public boolean hasRepositories()
    {
        return repositories.size() > 0;
    }

}
