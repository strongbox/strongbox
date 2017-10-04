package org.carlspring.strongbox.event.artifact;

import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.services.ArtifactResolutionService;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.RepositoryLayoutEnum;

import javax.inject.Inject;
import java.io.File;

import org.apache.commons.io.FilenameUtils;
import org.apache.maven.index.locator.Locator;
import org.apache.maven.index.locator.MetadataLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author Przemyslaw Fusik
 */
@Component
public class MavenArtifactDownloadedEventListener
        implements ArtifactEventListener
{

    private static final Logger logger = LoggerFactory.getLogger(MavenArtifactDownloadedEventListener.class);

    @Inject
    private ConfigurationManager configurationManager;

    @Inject
    private ArtifactResolutionService artifactResolutionService;

    private Locator metadataLocator = new MetadataLocator();

    @Override
    public void handle(ArtifactEvent event)
    {
        if (event.getType() != ArtifactEventTypeEnum.EVENT_ARTIFACT_FILE_DOWNLOADED.getType())
        {
            return;
        }

        Repository repository = configurationManager.getConfiguration().getStorage(event.getStorageId()).getRepository(
                event.getRepositoryId());

        if (!RepositoryLayoutEnum.MAVEN_2.getLayout().equals(repository.getLayout()))
        {
            return;
        }

        resolveArtifactMetadataFile(event);
    }

    private void resolveArtifactMetadataFile(ArtifactEvent event)
    {
        try
        {
            final File metadataFile = metadataLocator.locate(new File(event.getPath()));

            artifactResolutionService.getInputStream(event.getStorageId(), event.getRepositoryId(),
                                                     FilenameUtils.separatorsToUnix(metadataFile.getPath()));
        }
        catch (Exception e)
        {
            logger.error("Unable to resolve artifact metadata of file " + event.getPath() + " of repository " +
                         event.getRepositoryId(), e);
        }
    }
}
