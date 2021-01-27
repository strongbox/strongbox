package org.carlspring.strongbox.providers.repository;

import org.carlspring.strongbox.artifact.AsyncArtifactEntryHandler;
import org.carlspring.strongbox.domain.Artifact;
import org.carlspring.strongbox.event.artifact.ArtifactEventTypeEnum;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.util.LocalDateTimeInstance;

import java.io.IOException;
import java.nio.file.Files;

import org.springframework.stereotype.Component;

@Component
public class ArtifactUpdatedEventHandler extends AsyncArtifactEntryHandler
{

    public ArtifactUpdatedEventHandler()
    {
        super(ArtifactEventTypeEnum.EVENT_ARTIFACT_FILE_UPDATED);
    }

    @Override
    protected Artifact handleEvent(RepositoryPath repositoryPath) throws IOException
    {
        long size = Files.size(repositoryPath);

        Artifact artifactEntry = repositoryPath.getArtifactEntry();
        artifactEntry.setLastUpdated(LocalDateTimeInstance.now());
        artifactEntry.setSizeInBytes(size);
        
        return artifactEntry;
    }

}
