package org.carlspring.strongbox.storage.checksum;

import org.carlspring.strongbox.client.ArtifactTransportException;
import org.carlspring.strongbox.io.ArtifactInputStream;
import org.carlspring.strongbox.providers.ProviderImplementationException;
import org.carlspring.strongbox.providers.layout.LayoutProvider;
import org.carlspring.strongbox.providers.layout.LayoutProviderRegistry;
import org.carlspring.strongbox.providers.storage.StorageProviderRegistry;
import org.carlspring.strongbox.services.ArtifactResolutionService;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.UnknownRepositoryTypeException;
import org.carlspring.strongbox.util.ArtifactFileUtils;
import org.carlspring.strongbox.util.MessageDigestUtils;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import edu.emory.mathcs.backport.java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import static org.carlspring.strongbox.providers.layout.LayoutProviderRegistry.getLayoutProvider;

/**
 * @author Kate Novik.
 */
@Component
public class NugetChecksumManager
{

    private static final Logger logger = LoggerFactory.getLogger(NugetChecksumManager.class);

    @Inject
    private ArtifactResolutionService artifactResolutionService;

    @Inject
    private LayoutProviderRegistry layoutProviderRegistry;

    @Inject
    protected StorageProviderRegistry storageProviderRegistry;

    public NugetChecksumManager()
    {
    }

    /**
     * Generate a checksum for an artifact.
     *
     * @param repository         Repository
     * @param path               String
     * @param versionDirectories List<File>
     * @param forceRegeneration  boolean
     * @throws IOException
     * @throws ProviderImplementationException
     * @throws NoSuchAlgorithmException
     * @throws UnknownRepositoryTypeException
     * @throws ArtifactTransportException
     */
    public void generateChecksum(Repository repository,
                                 String path,
                                 List<File> versionDirectories,
                                 boolean forceRegeneration)
            throws IOException,
                   NoSuchAlgorithmException,
                   ProviderImplementationException,
                   UnknownRepositoryTypeException,
                   ArtifactTransportException
    {
        LayoutProvider layoutProvider = getLayoutProvider(repository, layoutProviderRegistry);
        if (layoutProvider.containsPath(repository, path))
        {
            logger.debug("Artifact checksum generation triggered for " + path +
                         " in '" + repository.getStorage()
                                             .getId() + ":" + repository.getId() + "'" +
                         " [policy: " + repository.getPolicy() + "].");

            if (!versionDirectories.isEmpty())
            {
                versionDirectories.forEach(file ->
                                           {
                                               String versionBasePath = file.getPath();
                                               try
                                               {
                                                   storeChecksum(versionBasePath, repository, forceRegeneration);
                                               }
                                               catch (IOException |
                                                              NoSuchAlgorithmException |
                                                              ArtifactTransportException |
                                                              ProviderImplementationException e)
                                               {
                                                   logger.error(e.getMessage(), e);
                                               }

                                               logger.debug("Generated Nuget checksum for " + versionBasePath + ".");
                                           });
            }
        }
        else
        {
            logger.error("Artifact checksum generation failed: " + path + ".");
        }

    }

    private void storeChecksum(String basePath,
                               Repository repository,
                               boolean forceRegeneration)
            throws ProviderImplementationException,
                   NoSuchAlgorithmException,
                   IOException,
                   ArtifactTransportException
    {

        File file = new File(basePath);

        LayoutProvider layoutProvider = getLayoutProvider(repository, layoutProviderRegistry);

        List<File> list = Arrays.asList(file.listFiles());

        list.stream()
            .filter(File::isFile)
            .filter(e -> !ArtifactFileUtils.isChecksum(e.getPath()))
            .forEach(e ->
                     {
                         if (!layoutProvider.isExistChecksum(repository, e.getPath()) || forceRegeneration)
                         {
                             ArtifactInputStream is = null;
                             try
                             {
                                 String artifactPath = e.getPath()
                                                        .substring(repository.getBasedir()
                                                                             .length() + 1);
                                 is = (ArtifactInputStream) artifactResolutionService.getInputStream(
                                         repository.getStorage()
                                                   .getId(),
                                         repository.getId(), artifactPath);
                             }
                             catch (IOException |
                                            NoSuchAlgorithmException |
                                            ArtifactTransportException |
                                            ProviderImplementationException e1)
                             {
                                 logger.error(e1.getMessage(), e1);
                             }

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
                             String checksum = is.getMessageDigestAsHexadecimalString(e.toString());
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

}
