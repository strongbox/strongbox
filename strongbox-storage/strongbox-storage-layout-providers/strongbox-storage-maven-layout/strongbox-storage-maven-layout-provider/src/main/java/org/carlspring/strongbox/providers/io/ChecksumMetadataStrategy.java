package org.carlspring.strongbox.providers.io;

import org.carlspring.commons.encryption.EncryptionAlgorithmsEnum;
import org.carlspring.strongbox.providers.repository.proxied.ProxyRepositoryArtifactResolver;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import static org.carlspring.strongbox.providers.io.MetadataStrategy.Decision.*;

@Component
public class ChecksumMetadataStrategy implements MetadataStrategy
{

    private static final Logger logger = LoggerFactory.getLogger(ChecksumMetadataStrategy.class);

    @Inject
    private ProxyRepositoryArtifactResolver proxyRepositoryArtifactResolver;

    public Decision determineMetadataRefetch(final RepositoryPath repositoryPath) throws IOException
    {

        RepositoryPath checksumRepositoryPath = resolveSiblingChecksum(repositoryPath, EncryptionAlgorithmsEnum.SHA1);
        String currentChecksum = readChecksum(checksumRepositoryPath);
        if (currentChecksum == null)
        {
            checksumRepositoryPath = resolveSiblingChecksum(repositoryPath, EncryptionAlgorithmsEnum.MD5);
            currentChecksum = readChecksum(checksumRepositoryPath);
            if (currentChecksum == null)
            {
                logger.debug("Unable to read local checksum for {}, will refetch metadata", repositoryPath.getTarget());
                return I_DONT_KNOW;
            }
        }

        proxyRepositoryArtifactResolver.fetchRemoteResource(checksumRepositoryPath);
        final String newRemoteChecksum = readChecksum(checksumRepositoryPath);

        if (newRemoteChecksum == null)
        {
            logger.debug("Unable to fetch remote checksum for {}, will refetch metadata", repositoryPath.getTarget());
            return I_DONT_KNOW;
        }

        if (currentChecksum.equals(newRemoteChecksum))
        {
            logger.debug("Local and remote checksums match for {}, no need to refetch metadata",
                         repositoryPath.getTarget());
            return NO_LEAVE_IT;
        }
        else
        {
            logger.debug("Local and remote checksums differ for {}, will refetch metadata",
                         repositoryPath.getTarget());
            return YES_FETCH;
        }
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
