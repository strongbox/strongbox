package org.carlspring.strongbox.repository.group.index;

import java.io.IOException;
import java.util.Set;

import javax.inject.Inject;

import org.carlspring.strongbox.providers.io.RootRepositoryPath;
import org.carlspring.strongbox.repository.group.BaseMavenGroupRepositoryComponentTest;
import org.carlspring.strongbox.services.ArtifactIndexesService;
import org.carlspring.strongbox.storage.repository.RepositoryData;
import org.carlspring.strongbox.storage.repository.RepositoryDto;

abstract class BaseMavenIndexGroupRepositoryComponentTest
        extends BaseMavenGroupRepositoryComponentTest
{

    @Inject
    ArtifactIndexesService artifactIndexesService;

    void rebuildIndexes(Set<RepositoryDto> repositories)
            throws IOException
    {
        for (RepositoryDto repository : repositories)
        {
            RootRepositoryPath repositoryPath = repositoryPathResolver.resolve(new RepositoryData(repository));
            artifactIndexesService.rebuildIndex(repositoryPath);
        }
    }
}
