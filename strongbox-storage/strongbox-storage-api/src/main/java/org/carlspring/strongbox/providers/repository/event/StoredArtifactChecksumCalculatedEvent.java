package org.carlspring.strongbox.providers.repository.event;

import org.carlspring.strongbox.event.artifact.ArtifactEvent;

import java.nio.file.Path;
import java.util.Map;

import static org.carlspring.strongbox.event.artifact.ArtifactEventTypeEnum.EVENT_ARTIFACT_FILE_STORED_CHECKSUM_CALCULATED;

public class StoredArtifactChecksumCalculatedEvent extends ArtifactEvent
{
    private final Map<String, String> digestMap;

    public StoredArtifactChecksumCalculatedEvent(Path sourcePath,
                                                 Map<String, String> digestMap)
    {
        super(sourcePath, EVENT_ARTIFACT_FILE_STORED_CHECKSUM_CALCULATED.getType());
        this.digestMap = digestMap;
    }

    public Map<String, String> getDigestMap()
    {
        return digestMap;
    }
}
