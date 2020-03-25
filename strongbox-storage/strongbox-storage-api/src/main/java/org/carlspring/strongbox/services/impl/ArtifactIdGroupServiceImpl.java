package org.carlspring.strongbox.services.impl;

import java.util.Optional;

import javax.inject.Inject;

import org.carlspring.strongbox.artifact.ArtifactTag;
import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
import org.carlspring.strongbox.domain.Artifact;
import org.carlspring.strongbox.domain.ArtifactIdGroup;
import org.carlspring.strongbox.domain.ArtifactIdGroupEntity;
import org.carlspring.strongbox.domain.ArtifactTagEntity;
import org.carlspring.strongbox.repositories.ArtifactIdGroupRepository;
import org.carlspring.strongbox.services.ArtifactIdGroupService;
import org.carlspring.strongbox.services.ArtifactTagService;
import org.janusgraph.core.SchemaViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/**
 * @author Przemyslaw Fusik
 * @author sbespalov
 */
@Service
@Transactional
public class ArtifactIdGroupServiceImpl
        extends AbstractArtifactGroupService<ArtifactIdGroup>
        implements ArtifactIdGroupService
{

    private static final Logger logger = LoggerFactory.getLogger(ArtifactIdGroupEntity.class);

    @Inject
    private ArtifactTagService artifactTagService;

    @Inject
    private ArtifactIdGroupRepository artifactIdGroupRepository;

    @Override
    public void addArtifactToGroup(ArtifactIdGroup artifactGroup,
                                   Artifact artifact)
    {
        ArtifactCoordinates coordinates = artifact.getArtifactCoordinates();
        Assert.notNull(coordinates, "coordinates should not be null");

        ArtifactTag lastVersionTag = artifactTagService.findOneOrCreate(ArtifactTagEntity.LAST_VERSION);

        artifact.getTagSet().add(lastVersionTag);
        artifactGroup.addArtifact(artifact);

        artifactGroup.getArtifacts()
                     .stream()
                     .filter(e -> e.getTagSet().contains(lastVersionTag))
                     .sorted((e1,
                              e2) -> e1.getArtifactCoordinates().compareTo(e2.getArtifactCoordinates()))
                     .forEach(e -> checkAndUpdateLastVersionTagIfNeeded(e, artifact, lastVersionTag));

        artifactIdGroupRepository.save(artifactGroup);
    }

    private Optional<Artifact> checkAndUpdateLastVersionTagIfNeeded(Artifact lastVersionEntry,
                                                                    Artifact entity,
                                                                    ArtifactTag lastVersionTag)
    {
        Optional<Artifact> result = Optional.empty();
        ArtifactCoordinates coordinates = entity.getArtifactCoordinates();

        int artifactCoordinatesComparison = entity.getArtifactCoordinates()
                                                  .compareTo(lastVersionEntry.getArtifactCoordinates());
        if (artifactCoordinatesComparison == 0)
        {
            logger.debug("Set [{}] last version to [{}]",
                         entity.getArtifactPath(),
                         coordinates.getVersion());
            entity.getTagSet().add(lastVersionTag);
        }
        else if (artifactCoordinatesComparison > 0)
        {
            logger.debug("Update [{}] last version from [{}] to [{}]",
                         entity.getArtifactPath(),
                         lastVersionEntry.getArtifactCoordinates().getVersion(),
                         coordinates.getVersion());
            entity.getTagSet().add(lastVersionTag);

            lastVersionEntry.getTagSet().remove(lastVersionTag);
            result = Optional.of(lastVersionEntry);
        }
        else
        {
            logger.debug("Keep [{}] last version [{}]",
                         entity.getArtifactPath(),
                         lastVersionEntry.getArtifactCoordinates().getVersion());
            entity.getTagSet().remove(lastVersionTag);
        }

        return result;
    }

    public ArtifactIdGroup findOneOrCreate(String storageId,
                                                 String repositoryId,
                                                 String artifactId)
    {
        Optional<ArtifactIdGroup> optional = tryFind(storageId, repositoryId, artifactId);
        if (optional.isPresent())
        {
            return optional.get();
        }

        ArtifactIdGroup artifactGroup = create(storageId, repositoryId, artifactId);

        try
        {
            return artifactIdGroupRepository.save(artifactGroup);
        }
        catch (SchemaViolationException ex)
        {
            optional = tryFind(storageId, repositoryId, artifactId);
            if (optional.isPresent())
            {
                return optional.get();
            }
            throw ex;
        }
    }

    protected Optional<ArtifactIdGroup> tryFind(String storageId,
                                                String repositoryId,
                                                String artifactId)
    {
        return Optional.ofNullable(artifactIdGroupRepository.findOne(storageId, repositoryId, artifactId));
    }

    protected ArtifactIdGroup create(String storageId,
                                     String repositoryId,
                                     String artifactId)
    {
        return new ArtifactIdGroupEntity(storageId, repositoryId, artifactId);
    }

}
