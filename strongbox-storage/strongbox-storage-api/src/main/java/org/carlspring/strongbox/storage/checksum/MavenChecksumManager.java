package org.carlspring.strongbox.storage.checksum;

import org.carlspring.maven.commons.util.ArtifactUtils;
import org.carlspring.strongbox.client.ArtifactTransportException;
import org.carlspring.strongbox.io.ArtifactInputStream;
import org.carlspring.strongbox.io.RepositoryPath;
import org.carlspring.strongbox.providers.ProviderImplementationException;
import org.carlspring.strongbox.providers.layout.LayoutProvider;
import org.carlspring.strongbox.providers.layout.LayoutProviderRegistry;
import org.carlspring.strongbox.providers.storage.StorageProviderRegistry;
import org.carlspring.strongbox.storage.metadata.VersionCollectionRequest;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.RepositoryPolicyEnum;
import org.carlspring.strongbox.storage.repository.UnknownRepositoryTypeException;
import org.carlspring.strongbox.util.MessageDigestUtils;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import edu.emory.mathcs.backport.java.util.Arrays;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.metadata.Versioning;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import static org.carlspring.strongbox.providers.layout.LayoutProviderRegistry.getLayoutProvider;

/**
 * @author Kate Novik.
 */
@Component
public class MavenChecksumManager
{

    private static final Logger logger = LoggerFactory.getLogger(MavenChecksumManager.class);

    @Inject
    private LayoutProviderRegistry layoutProviderRegistry;

    @Inject
    protected StorageProviderRegistry storageProviderRegistry;

    public MavenChecksumManager()
    {
    }

    /**
     * Generate a checksum for an artifact.
     *
     * @param repository Repository
     * @throws IOException
     * @throws ProviderImplementationException
     * @throws NoSuchAlgorithmException
     * @throws UnknownRepositoryTypeException
     * @throws ArtifactTransportException
     */
    public void generateChecksum(Repository repository,
                                 String path,
                                 VersionCollectionRequest request,
                                 boolean forceRegeneration)
            throws IOException, NoSuchAlgorithmException, ProviderImplementationException,
                   UnknownRepositoryTypeException, ArtifactTransportException
    {
        LayoutProvider layoutProvider = getLayoutProvider(repository, layoutProviderRegistry);
        if (layoutProvider.containsPath(repository, path))
        {
            logger.debug("Artifact checksum generation triggered for " + path +
                         " in '" + repository.getStorage()
                                             .getId() + ":" + repository.getId() + "'" +
                         " [policy: " + repository.getPolicy() + "].");
            Versioning versioning = request.getVersioning();

            /**
             * In the repository we need to generate checksum for files in the artifactBasePath and
             * for each version directory.
             */
            if (!versioning.getVersions()
                           .isEmpty())
            {
                Artifact artifact = ArtifactUtils.convertPathToArtifact(path);
                // Generate and write additional snapshot metadata.
                for (String version : versioning.getVersions())
                {
                    artifact.setVersion(version);
                    Path versionBasePath = Paths.get(request.getArtifactBasePath()
                                                            .toString(),
                                                     getVersionDirectoryName(repository, version));

                    storeChecksum(versionBasePath, repository, artifact, forceRegeneration);
                    logger.debug("Generated Maven checksum for " + versionBasePath + ".");
                }
                storeChecksum(request.getArtifactBasePath(), repository, artifact, forceRegeneration);
            }
        }
        else
        {
            logger.error("Artifact checksum generation failed: " + path + ".");
        }

    }

    private void storeChecksum(Path path,
                               Repository repository,
                               Artifact artifact,
                               boolean forceRegeneration)
            throws ProviderImplementationException, NoSuchAlgorithmException, IOException, ArtifactTransportException
    {

        String basePath = path.toString();

        File file = new File(basePath);

        String artifactPath = ArtifactUtils.convertArtifactToPath(artifact);

        LayoutProvider layoutProvider = getLayoutProvider(repository, layoutProviderRegistry);
        ArtifactInputStream is = layoutProvider.getInputStream(repository.getStorage()
                                                                         .getId(), repository.getId(), artifactPath);

        List<File> list = Arrays.asList(file.listFiles());

        list.stream()
            .filter(File::isFile)
            .forEach(e ->
                     {
                         if (!layoutProvider.isExistChecksum(repository, e.getPath()) || forceRegeneration)
                         {
                             writeChecksum(layoutProvider, is, e.getPath());
                         }
                     });
    }

    private void writeChecksum(LayoutProvider provider,
                               ArtifactInputStream is,
                               String filePath)

    {
        provider.getDigestAlgorithmSet()
                .stream()
                .forEach(e ->
                                       {
                                           String checksum = getChecksum(is, filePath, e.toString());
                                           String checksumExtension = "." + e.toString()
                                                                             .toLowerCase()
                                                                             .replaceAll("-", "");
                                           try
                                           {
                                               MessageDigestUtils.writeChecksum(new File(filePath), checksumExtension,
                                                                                checksum);
                                           }
                                           catch (IOException e1)
                                           {
                                               logger.error(
                                                       String.format("Failed to write checksum: alg-[%s]; path-[%s];",
                                                                     e,
                                                                     filePath + "." + checksumExtension));
                                           }
                                       });

    }

    private String getChecksum(ArtifactInputStream is,
                               String path,
                               String algorithm)
    {

        if (ArtifactUtils.isArtifact(path))
        {
            return is.getMessageDigestAsHexadecimalString(algorithm);
        }
        return MessageDigestUtils.convertToHexadecimalString(is.getMessageDigest(algorithm));
    }

    private String getVersionDirectoryName(Repository repository,
                                           String version)
    {
        if (repository.getPolicy()
                      .equals(RepositoryPolicyEnum.SNAPSHOT.getPolicy()))
        {
            return ArtifactUtils.getSnapshotBaseVersion(version);
        }
        else
        {
            return version;
        }
    }

}
