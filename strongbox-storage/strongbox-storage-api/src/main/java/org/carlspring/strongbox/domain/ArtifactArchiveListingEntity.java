package org.carlspring.strongbox.domain;

import static org.carlspring.strongbox.db.schema.Vertices.ARTIFACT_ARCHIVE_LISTING;

import org.carlspring.strongbox.data.domain.DomainEntity;

import org.neo4j.ogm.annotation.NodeEntity;

/**
 * @author ankit.tomar
 */
@NodeEntity(ARTIFACT_ARCHIVE_LISTING)
public class ArtifactArchiveListingEntity extends DomainEntity implements ArtifactArchiveListing
{

    private String fileName;

    public ArtifactArchiveListingEntity()
    {
        super();
    }

    public ArtifactArchiveListingEntity(String storageId,
                                        String repositoryId,
                                        String fileName)
    {
        this.fileName = fileName;
        // setUuid(String.format("%s/%s/%s", getStorageId(), getRepositoryId(), getFileName()));
    }

    @Override
    public String getFileName()
    {
        return fileName;
    }

    @Override
    public void setFileName(String fileName)
    {
        this.fileName = fileName;
    }
}
