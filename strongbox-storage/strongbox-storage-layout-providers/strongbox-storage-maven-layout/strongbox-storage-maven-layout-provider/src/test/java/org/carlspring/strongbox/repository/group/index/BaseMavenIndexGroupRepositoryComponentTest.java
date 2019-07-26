package org.carlspring.strongbox.repository.group.index;

import org.carlspring.strongbox.providers.io.RootRepositoryPath;
import org.carlspring.strongbox.repository.group.BaseMavenGroupRepositoryComponentTest;
import org.carlspring.strongbox.services.ArtifactIndexesService;
import org.carlspring.strongbox.storage.repository.Repository;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Set;

/**
 * @author Pablo Tirado
 */
abstract class BaseMavenIndexGroupRepositoryComponentTest
        extends BaseMavenGroupRepositoryComponentTest
{

    @Inject
    protected ArtifactIndexesService artifactIndexesService;

    void rebuildIndexes(Set<Repository> repositories)
            throws IOException
    {
        for (Repository repository : repositories)
        {
            RootRepositoryPath repositoryPath = repositoryPathResolver.resolve(repository);
            artifactIndexesService.rebuildIndex(repositoryPath);
        }
    }
}
