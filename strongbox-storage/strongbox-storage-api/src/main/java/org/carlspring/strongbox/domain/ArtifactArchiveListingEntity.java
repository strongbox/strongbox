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

    private String storageId;

    private String repositoryId;

    public ArtifactArchiveListingEntity()
    {
        super();
    }

    public ArtifactArchiveListingEntity(String fileName,
                                        String storageId,
                                        String repositoryId)
    {
        this.fileName = fileName;
        this.storageId = storageId;
        this.repositoryId = repositoryId;

        setUuid(String.format("%s/%s/%s", getStorageId(), getRepositoryId(), getFileName()));
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

    @Override
    public String getStorageId()
    {
        return storageId;
    }

    public void setStorageId(String storageId)
    {
        this.storageId = storageId;
    }

    @Override
    public String getRepositoryId()
    {
        return repositoryId;
    }

    public void setRepositoryId(String repositoryId)
    {
        this.repositoryId = repositoryId;
    }

}
