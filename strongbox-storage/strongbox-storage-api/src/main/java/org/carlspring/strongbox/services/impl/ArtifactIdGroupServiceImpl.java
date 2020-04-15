package org.carlspring.strongbox.services.impl;

import java.util.Optional;

import javax.inject.Inject;

import org.carlspring.strongbox.artifact.ArtifactTag;
import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
import org.carlspring.strongbox.data.CacheName;
import org.carlspring.strongbox.domain.Artifact;
import org.carlspring.strongbox.domain.ArtifactIdGroup;
import org.carlspring.strongbox.domain.ArtifactIdGroupEntity;
import org.carlspring.strongbox.domain.ArtifactTagEntity;
import org.carlspring.strongbox.repositories.ArtifactIdGroupRepository;
import org.carlspring.strongbox.repositories.ArtifactRepository;
import org.carlspring.strongbox.services.ArtifactIdGroupService;
import org.carlspring.strongbox.services.ArtifactTagService;
import org.janusgraph.core.SchemaViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
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
    
    @Inject
    private ArtifactRepository artifactRepository;

    @Override
    public ArtifactCoordinates addArtifactToGroup(ArtifactIdGroup artifactGroup,
                                   Artifact artifact)
    {
        ArtifactCoordinates coordinates = artifact.getArtifactCoordinates();
        Assert.notNull(coordinates, "coordinates should not be null");

        ArtifactTag lastVersionTag = artifactTagService.findOneOrCreate(ArtifactTagEntity.LAST_VERSION);

        artifact.getTagSet().add(lastVersionTag);
        artifactGroup.addArtifact(artifact);

        Artifact lastVersionArtifact = artifactGroup.getArtifacts()
                                                    .stream()
                                                    .filter(e -> e.getTagSet().contains(lastVersionTag))
                                                    .reduce((a1,
                                                             a2) -> reduceByLastVersionTag(a1, a2, lastVersionTag))
                                                    .get();
        
        return lastVersionArtifact.getArtifactCoordinates();
    }

    private Artifact reduceByLastVersionTag(Artifact a1,
                                            Artifact a2,
                                            ArtifactTag lastVersionTag)
    {
        int artifactCoordinatesComparison = a1.getArtifactCoordinates()
                                              .compareTo(a2.getArtifactCoordinates());
        if (artifactCoordinatesComparison > 0)
        {
            removeLastVersionTag(a2, lastVersionTag);

            return a1;
        }
        else if (artifactCoordinatesComparison < 0)
        {
            removeLastVersionTag(a1, lastVersionTag);

            return a2;
        }

        return a1;
    }

    private Artifact removeLastVersionTag(Artifact artifact,
                                      ArtifactTag lastVersionTag)
    {
        artifact.getTagSet().remove(lastVersionTag);
        if (artifact.getNativeId() != null)
        {
            artifactRepository.merge(artifact);
        }
        
        return artifact;
    }
    
    public ArtifactIdGroup findOneOrCreate(String storageId,
                                           String repositoryId,
                                           String artifactId)
    {
        Optional<ArtifactIdGroup> optional = artifactIdGroupRepository.findOne(storageId, repositoryId, artifactId);
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
            optional = artifactIdGroupRepository.findOne(storageId, repositoryId, artifactId);
            if (optional.isPresent())
            {
                return optional.get();
            }
            throw ex;
        }
    }

    protected ArtifactIdGroup create(String storageId,
                                     String repositoryId,
                                     String artifactId)
    {
        return new ArtifactIdGroupEntity(storageId, repositoryId, artifactId);
    }

}
