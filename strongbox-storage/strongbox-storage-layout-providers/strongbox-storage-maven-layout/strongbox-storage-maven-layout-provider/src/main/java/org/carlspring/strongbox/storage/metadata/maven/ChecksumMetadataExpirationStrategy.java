package org.carlspring.strongbox.storage.metadata.maven;

import org.carlspring.commons.encryption.EncryptionAlgorithmsEnum;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.repository.proxied.ProxyRepositoryArtifactResolver;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import static org.carlspring.strongbox.storage.metadata.maven.MetadataExpirationStrategy.Decision.*;

@Component
public class ChecksumMetadataExpirationStrategy
        implements MetadataExpirationStrategy
{

    private static final Logger logger = LoggerFactory.getLogger(ChecksumMetadataExpirationStrategy.class);

    @Inject
    private ProxyRepositoryArtifactResolver proxyRepositoryArtifactResolver;

    public Decision decide(final RepositoryPath repositoryPath) throws IOException
    {
        Decision decision = decideUsingChecksumAlgorithm(repositoryPath, EncryptionAlgorithmsEnum.SHA1);
        if (UNDECIDED.equals(decision))
        {
            decision = decideUsingChecksumAlgorithm(repositoryPath, EncryptionAlgorithmsEnum.MD5);
        }
        return decision;
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

    private Decision decideUsingChecksumAlgorithm(final RepositoryPath repositoryPath,
                                                  final EncryptionAlgorithmsEnum checksumAlgorithm) throws IOException
    {
        RepositoryPath checksumRepositoryPath = resolveSiblingChecksum(repositoryPath, checksumAlgorithm);
        String currentChecksum = readChecksum(checksumRepositoryPath);
        if (currentChecksum == null)
        {
            logger.debug("Unable to read local {} checksum for {}, returning " + UNDECIDED.name(),
                         checksumAlgorithm,
                         repositoryPath.normalize());
            return UNDECIDED;
        }

        proxyRepositoryArtifactResolver.fetchRemoteResource(checksumRepositoryPath);
        final String newRemoteChecksum = readChecksum(checksumRepositoryPath);

        if (newRemoteChecksum == null)
        {
            logger.debug("Unable to fetch remote {} checksum for {}, returning " + UNDECIDED.name(),
                         checksumAlgorithm,
                         repositoryPath.normalize());
            return UNDECIDED;
        }

        if (currentChecksum.equals(newRemoteChecksum))
        {
            logger.debug("Local and remote {} checksums match for {}, no need to refetch metadata",
                         checksumAlgorithm,
                         repositoryPath.normalize());
            return USABLE;
        }
        else
        {
            logger.debug("Local and remote {} checksums differ for {}, will refetch metadata",
                         checksumAlgorithm,
                         repositoryPath.normalize());
            return EXPIRED;
        }
    }

}
