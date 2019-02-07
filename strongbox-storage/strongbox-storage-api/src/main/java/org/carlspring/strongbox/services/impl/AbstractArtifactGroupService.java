package org.carlspring.strongbox.services.impl;

import java.util.Optional;

import org.carlspring.strongbox.data.service.CommonCrudService;
import org.carlspring.strongbox.domain.ArtifactEntry;
import org.carlspring.strongbox.domain.ArtifactGroup;
import org.carlspring.strongbox.services.ArtifactGroupService;

import com.orientechnologies.common.concur.ONeedRetryException;

/**
 * @author sbespalov
 *
 * @param <T>
 */
public abstract class AbstractArtifactGroupService<T extends ArtifactGroup> extends CommonCrudService<T> implements ArtifactGroupService<T>
{

    public T findOneOrCreate(ArtifactEntry repositoryPath)
    {
        Optional<T> optional = tryFind(repositoryPath);
        if (optional.isPresent())
        {
            return optional.get();
        }

        T artifactGroup = create(repositoryPath);

        try
        {
            return save(artifactGroup);
        }
        catch (ONeedRetryException ex)
        {
            optional = tryFind(repositoryPath);
            if (optional.isPresent())
            {
                return optional.get();
            }
            throw ex;
        }
    }

    protected abstract T create(ArtifactEntry repositoryPath);

    protected abstract Optional<T> tryFind(ArtifactEntry repositoryPath);
    
}
