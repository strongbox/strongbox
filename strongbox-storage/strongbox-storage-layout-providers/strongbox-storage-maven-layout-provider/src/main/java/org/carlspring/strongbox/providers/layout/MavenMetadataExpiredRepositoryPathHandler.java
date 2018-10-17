package org.carlspring.strongbox.providers.layout;

import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.io.RepositoryPathResolver;
import org.carlspring.strongbox.providers.repository.ProxyRepositoryProvider;
import org.carlspring.strongbox.providers.repository.RepositoryProvider;
import org.carlspring.strongbox.providers.repository.RepositoryProviderRegistry;
import org.carlspring.strongbox.storage.checksum.ChecksumCacheManager;
import org.carlspring.strongbox.storage.metadata.MetadataHelper;
import org.carlspring.strongbox.storage.repository.Repository;

import javax.inject.Inject;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import static org.carlspring.strongbox.providers.layout.MavenMetadataExpiredRepositoryPathHandler.Decision.I_DONT_KNOW;
import static org.carlspring.strongbox.providers.layout.MavenMetadataExpiredRepositoryPathHandler.Decision.NO_LEAVE_IT;
import static org.carlspring.strongbox.providers.layout.MavenMetadataExpiredRepositoryPathHandler.Decision.YES_FETCH;

/**
 * @author Przemyslaw Fusik
 */
@Component
public class MavenMetadataExpiredRepositoryPathHandler
        implements MavenExpiredRepositoryPathHandler
{

    private static final Logger logger = LoggerFactory.getLogger(MavenMetadataExpiredRepositoryPathHandler.class);

    @Inject
    private RepositoryProviderRegistry repositoryProviderRegistry;

    @Inject
    private RepositoryPathResolver repositoryPathResolver;

    @Inject
    private ChecksumCacheManager checksumCacheManager;

    @Inject
    private ProxyRepositoryProvider proxyRepositoryProvider;

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
    public RepositoryPath handleExpiration(final RepositoryPath repositoryPath)
            throws IOException
    {
        Decision refetchMetadata = determineMetadataRefetch(repositoryPath, "sha1");
        if (refetchMetadata == I_DONT_KNOW)
        {
            refetchMetadata = determineMetadataRefetch(repositoryPath, "md5");
        }
        if (refetchMetadata == NO_LEAVE_IT)
        {
            // checksums match - do nothing
            logger.debug("Local and remote checksums match - no need to re-fetch maven-metadata.xml.");
            return repositoryPath;
        }
        if (refetchMetadata == I_DONT_KNOW)
        {
            logger.debug("maven-metadata.xml will be re-fetched. Checksum comparison process was not helpful.");
        }
        if (refetchMetadata == YES_FETCH)
        {
            logger.debug("maven-metadata.xml will be re-fetched. Checksums differ.");
        }
        return proxyRepositoryProvider.resolvePathForceFetch(repositoryPath);
    }

    private Decision determineMetadataRefetch(final RepositoryPath repositoryPath,
                                              final String checksumAlgorithm)
            throws IOException
    {
        final String currentChecksum = checksumCacheManager.getArtifactChecksum(repositoryPath, checksumAlgorithm);
        if (currentChecksum == null)
        {
            return I_DONT_KNOW;
        }

        proxyRepositoryProvider.resolvePathForceFetch(
                repositoryPath.resolveSibling(repositoryPath.getFileName().toString() + "." + checksumAlgorithm));
        final String newRemoteChecksum = checksumCacheManager.getArtifactChecksum(repositoryPath,
                                                                                  checksumAlgorithm);

        if (newRemoteChecksum == null)
        {
            return I_DONT_KNOW;
        }

        return currentChecksum.equals(newRemoteChecksum) ? NO_LEAVE_IT : YES_FETCH;
    }

    enum Decision
    {
        I_DONT_KNOW, YES_FETCH, NO_LEAVE_IT;
    }
}
