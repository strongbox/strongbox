package org.carlspring.strongbox.providers.io;

import org.carlspring.strongbox.providers.repository.proxied.ProxyRepositoryArtifactResolver;
import org.carlspring.strongbox.storage.repository.MetadataStrategyEnum;
import org.carlspring.strongbox.storage.repository.RepositoryData;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.yaml.configuration.repository.MavenRepositoryConfiguration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.io.IOException;

import static org.carlspring.strongbox.providers.io.MetadataStrategy.Decision.*;

/**
 * @author Przemyslaw Fusik
 */
@Component
public class MavenMetadataExpiredRepositoryPathHandler
        implements MavenExpiredRepositoryPathHandler
{

    private static final Logger logger = LoggerFactory.getLogger(MavenMetadataExpiredRepositoryPathHandler.class);

    @Inject
    private ProxyRepositoryArtifactResolver proxyRepositoryArtifactResolver;

    @Inject
    private ChecksumMetadataStrategy checksumMetadataStrategy;

    @Inject
    private RefreshMetadataStrategy refreshMetadataStrategy;

    @Override
    public boolean supports(final RepositoryPath repositoryPath)
            throws IOException
    {
        if (repositoryPath == null)
        {
            return false;
        }

        if (!RepositoryFiles.isMetadata(repositoryPath))
        {
            return false;
        }

        Repository repository = repositoryPath.getRepository();
        return ((RepositoryData)repository).getRemoteRepository() != null;
    }

    @Override
    public void handleExpiration(final RepositoryPath repositoryPath)
            throws IOException
    {
        MetadataStrategy metadataStrategy = getMetadataStrategy(repositoryPath);
        MetadataStrategy.Decision refetchMetadata = metadataStrategy.determineMetadataRefetch(repositoryPath);

        if (refetchMetadata == NO_LEAVE_IT)
        {
            return;
        }
        proxyRepositoryArtifactResolver.fetchRemoteResource(repositoryPath);
    }

    private MetadataStrategy getMetadataStrategy(final RepositoryPath repositoryPath)
    {
        MavenRepositoryConfiguration repositoryConfiguration =
                (MavenRepositoryConfiguration) repositoryPath.getRepository().getRepositoryConfiguration();
        MetadataStrategyEnum configuredStrategy =
                MetadataStrategyEnum.ofStrategy(repositoryConfiguration.getMetadataStrategy());
        if (MetadataStrategyEnum.REFRESH.equals(configuredStrategy))
        {
            return refreshMetadataStrategy;
        }
        else
        {
            return checksumMetadataStrategy;
        }
    }

}
