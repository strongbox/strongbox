package org.carlspring.strongbox.providers.io;

import org.carlspring.strongbox.providers.repository.proxied.ProxyRepositoryArtifactResolver;
import org.carlspring.strongbox.storage.checksum.ChecksumCacheManager;
import org.carlspring.strongbox.storage.metadata.MetadataHelper;
import org.carlspring.strongbox.storage.repository.Repository;

import javax.inject.Inject;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import static org.carlspring.strongbox.providers.io.MavenMetadataExpiredRepositoryPathHandler.Decision.*;

/**
 * @author Przemyslaw Fusik
 */
@Component
public class MavenMetadataExpiredRepositoryPathHandler
        implements MavenExpiredRepositoryPathHandler
{

    private static final Logger logger = LoggerFactory.getLogger(MavenMetadataExpiredRepositoryPathHandler.class);

    @Inject
    private ChecksumCacheManager checksumCacheManager;

    @Inject
    private ProxyRepositoryArtifactResolver proxyRepositoryArtifactResolver;

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
        return repository.getRemoteRepository() != null;
    }

    @Override
    public void handleExpiration(final RepositoryPath repositoryPath)
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
            return;
        }
        if (refetchMetadata == I_DONT_KNOW)
        {
            logger.debug("maven-metadata.xml will be re-fetched. Checksum comparison process was not helpful.");
        }
        if (refetchMetadata == YES_FETCH)
        {
            logger.debug("maven-metadata.xml will be re-fetched. Checksums differ.");
        }
        proxyRepositoryArtifactResolver.fetchRemoteResource(repositoryPath);
    }

    private Decision determineMetadataRefetch(final RepositoryPath repositoryPath,
                                              final String checksumAlgorithm)
            throws IOException
    {
        final String currentChecksum = checksumCacheManager.get(repositoryPath, checksumAlgorithm);
        if (currentChecksum == null)
        {
            return I_DONT_KNOW;
        }

        proxyRepositoryArtifactResolver.fetchRemoteResource(
                repositoryPath.resolveSibling(repositoryPath.getFileName().toString() + "." + checksumAlgorithm));
        final String newRemoteChecksum = checksumCacheManager.get(repositoryPath,
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
