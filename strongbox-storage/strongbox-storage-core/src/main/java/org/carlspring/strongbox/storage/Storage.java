package org.carlspring.strongbox.storage;

import org.carlspring.strongbox.json.MapValuesJsonSerializer;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.RepositoryDto;
import org.carlspring.strongbox.storage.repository.RepositoryData;

import javax.annotation.concurrent.Immutable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import java.util.Collections;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.ImmutableSortedMap;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import static java.util.stream.Collectors.toMap;

/**
 * @author Przemyslaw Fusik
 */
@Immutable
@XmlAccessorType(XmlAccessType.FIELD)
@SuppressFBWarnings(value = "AJCIP_FIELD_ISNT_FINAL_IN_IMMUTABLE_CLASS")
public class Storage implements StorageData
{

    @JsonView(Views.ShortStorage.class)
    private String id;

    @JsonView(Views.ShortStorage.class)
    private String basedir;

    @JsonView(Views.LongStorage.class)
    @JsonSerialize(using = MapValuesJsonSerializer.class)
    @JsonDeserialize(using = RepositoryArrayToMapJsonDeserializer.class)
    private Map<String, ? extends RepositoryData> repositories;

    Storage()
    {

    }

    public Storage(final StorageData delegate)
    {
        this.id = delegate.getId();
        this.basedir = delegate.getBasedir();
        this.repositories = immuteRepositories(delegate.getRepositories());
    }

    private Map<String, ? extends RepositoryData> immuteRepositories(final Map<String, ? extends RepositoryData> source)
    {
        return source != null ? ImmutableSortedMap.copyOf(source.entrySet().stream().collect(
                toMap(Map.Entry::getKey, e -> new Repository(e.getValue(), this)))) : Collections.emptyMap();
    }

    @Override
    public RepositoryData getRepository(final String repositoryId)
    {
        return repositories.get(repositoryId);
    }

    public boolean containsRepository(final String repository)
    {
        return repositories.containsKey(repository);
    }

    @Override
    public String getId()
    {
        return id;
    }

    @Override
    public String getBasedir()
    {
        return basedir;
    }

    @Override
    public Map<String, ? extends RepositoryData> getRepositories()
    {
        return repositories;
    }
}
