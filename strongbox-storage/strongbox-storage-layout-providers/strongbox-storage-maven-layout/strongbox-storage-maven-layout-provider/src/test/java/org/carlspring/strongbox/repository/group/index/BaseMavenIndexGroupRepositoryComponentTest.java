package org.carlspring.strongbox.repository.group.index;

import java.io.IOException;
import java.util.Set;

import javax.inject.Inject;

import org.carlspring.strongbox.providers.io.RootRepositoryPath;
import org.carlspring.strongbox.repository.group.BaseMavenGroupRepositoryComponentTest;
import org.carlspring.strongbox.services.ArtifactIndexesService;
import org.carlspring.strongbox.storage.repository.ImmutableRepository;
import org.carlspring.strongbox.storage.repository.MutableRepository;

abstract class BaseMavenIndexGroupRepositoryComponentTest
        extends BaseMavenGroupRepositoryComponentTest
{

    @Inject
    ArtifactIndexesService artifactIndexesService;

    void rebuildIndexes(Set<MutableRepository> repositories)
            throws IOException
    {
        for (MutableRepository repository : repositories)
        {
            RootRepositoryPath repositoryPath = repositoryPathResolver.resolve(new ImmutableRepository(repository));
            artifactIndexesService.rebuildIndex(repositoryPath);
        }
    }
}
