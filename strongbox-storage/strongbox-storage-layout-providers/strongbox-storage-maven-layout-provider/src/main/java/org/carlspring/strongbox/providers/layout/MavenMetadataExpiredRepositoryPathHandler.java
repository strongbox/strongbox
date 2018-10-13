package org.carlspring.strongbox.providers.layout;

import org.carlspring.strongbox.providers.io.ExpiredRepositoryPathHandler;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.io.RepositoryPathResolver;
import org.carlspring.strongbox.providers.repository.ProxyRepositoryProvider;
import org.carlspring.strongbox.providers.repository.RepositoryProvider;
import org.carlspring.strongbox.providers.repository.RepositoryProviderRegistry;
import org.carlspring.strongbox.storage.metadata.MetadataHelper;
import org.carlspring.strongbox.storage.repository.Repository;

import javax.inject.Inject;

/**
 * @author Przemyslaw Fusik
 */
public class MavenMetadataExpiredRepositoryPathHandler
        implements ExpiredRepositoryPathHandler
{

    @Inject
    private RepositoryProviderRegistry repositoryProviderRegistry;

    @Inject
    private RepositoryPathResolver repositoryPathResolver;

    @Override
    public boolean supports(final RepositoryPath repositoryPath)
    {
        if (repositoryPath == null)
        {
            return false;
        }
        if (!MetadataHelper.MAVEN_METADATA_XML.equals(repositoryPath.getFileName().toString()))
        {
            return false;
        }

        Repository repository = repositoryPath.getRepository();
        RepositoryProvider provider = repositoryProviderRegistry.getProvider(repository.getType());

        if (!(provider instanceof ProxyRepositoryProvider))
        {
            return false;
        }

        return true;
    }

    @Override
    public void handleExpiration(final RepositoryPath repositoryPath)
    {
        Repository repository = repositoryPath.getRepository();
        ProxyRepositoryProvider provider = (ProxyRepositoryProvider) repositoryProviderRegistry.getProvider(
                repository.getType());

        // TODO fetch checksum
        // repositoryPathResolver.resolve()
    }
}
