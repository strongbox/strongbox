package org.carlspring.strongbox.providers.io;

import org.carlspring.commons.encryption.EncryptionAlgorithmsEnum;
import org.carlspring.strongbox.providers.repository.proxied.ProxyRepositoryArtifactResolver;
import org.carlspring.strongbox.storage.repository.Repository;

import javax.inject.Inject;
import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.nio.file.Files;

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
    private ProxyRepositoryArtifactResolver proxyRepositoryArtifactResolver;

    @Override
    public boolean supports(final RepositoryPath repositoryPath)
    {
        if (repositoryPath == null)
        {
            return false;
        }
        try
        {
            if (!RepositoryFiles.isMetadata(repositoryPath))
            {
                return false;
            }
        }
        catch (IOException e)
        {
            throw new UndeclaredThrowableException(e);
        }

        Repository repository = repositoryPath.getRepository();
        return repository.getRemoteRepository() != null;
    }

    @Override
    public void handleExpiration(final RepositoryPath repositoryPath)
            throws IOException
    {
        Decision refetchMetadata = determineMetadataRefetch(repositoryPath,
                                                            EncryptionAlgorithmsEnum.SHA1);
        if (refetchMetadata == I_DONT_KNOW)
        {
            refetchMetadata = determineMetadataRefetch(repositoryPath,
                                                       EncryptionAlgorithmsEnum.MD5);
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
                                              final EncryptionAlgorithmsEnum checksumAlgorithm)
            throws IOException
    {

        final RepositoryPath checksumRepositoryPath = resolveSiblingChecksum(repositoryPath, checksumAlgorithm);
        final String currentChecksum = readChecksum(checksumRepositoryPath);
        if (currentChecksum == null)
        {
            return I_DONT_KNOW;
        }

        proxyRepositoryArtifactResolver.fetchRemoteResource(checksumRepositoryPath);
        final String newRemoteChecksum = readChecksum(checksumRepositoryPath);

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

    private RepositoryPath resolveSiblingChecksum(final RepositoryPath repositoryPath,
                                                  final EncryptionAlgorithmsEnum checksumAlgorithm)
    {
        return repositoryPath.resolveSibling(
                repositoryPath.getFileName().toString() + checksumAlgorithm.getExtension());
    }

    private String readChecksum(final RepositoryPath checksumRepositoryPath)
            throws IOException
    {
        if (!Files.exists(checksumRepositoryPath))
        {
            return null;
        }

        return Files.readAllLines(checksumRepositoryPath).stream().findFirst().orElse(null);
    }

}
