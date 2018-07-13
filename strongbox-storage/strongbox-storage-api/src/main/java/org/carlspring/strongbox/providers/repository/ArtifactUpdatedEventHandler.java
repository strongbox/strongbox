package org.carlspring.strongbox.providers.repository;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Date;

import org.carlspring.strongbox.artifact.AsyncArtifactEntryHandler;
import org.carlspring.strongbox.domain.ArtifactEntry;
import org.carlspring.strongbox.event.artifact.ArtifactEventTypeEnum;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.springframework.stereotype.Component;

@Component
public class ArtifactUpdatedEventHandler extends AsyncArtifactEntryHandler
{

    public ArtifactUpdatedEventHandler()
    {
        super(ArtifactEventTypeEnum.EVENT_ARTIFACT_FILE_UPDATED);
    }

    @Override
    protected ArtifactEntry handleEvent(RepositoryPath repositoryPath) throws IOException
    {
        ArtifactEntry artifactEntry = repositoryPath.getArtifactEntry();
        artifactEntry.setLastUpdated(new Date());
        
        long size = Files.size(repositoryPath);
        artifactEntry.setSizeInBytes(size);
        
        return artifactEntry;
    }

}
