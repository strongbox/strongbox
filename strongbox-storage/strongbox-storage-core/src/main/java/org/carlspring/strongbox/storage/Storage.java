package org.carlspring.strongbox.storage;

import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.MutableRepository;

import javax.annotation.concurrent.Immutable;
import java.util.Collections;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import static java.util.stream.Collectors.toMap;

/**
 * @author Przemyslaw Fusik
 */
@Immutable
public class Storage
{

    private final String id;

    private final String basedir;

    private final Map<String, Repository> repositories;

    public Storage(final MutableStorage delegate)
    {
        this.id = delegate.getId();
        this.basedir = delegate.getBasedir();
        this.repositories = immuteRepositories(delegate.getRepositories());
    }

    private Map<String, Repository> immuteRepositories(final Map<String, MutableRepository> source)
    {
        return source != null ? ImmutableMap.copyOf(source.entrySet().stream().collect(
                toMap(Map.Entry::getKey, e -> new Repository(e.getValue(), this)))) : Collections.emptyMap();
    }

    public Repository getRepository(final String repositoryId)
    {
        return repositories.get(repositoryId);
    }

    public boolean containsRepository(final String repository)
    {
        return repositories.containsKey(repository);
    }

    public String getId()
    {
        return id;
    }

    public String getBasedir()
    {
        return basedir;
    }

    public Map<String, Repository> getRepositories()
    {
        return repositories;
    }
}
