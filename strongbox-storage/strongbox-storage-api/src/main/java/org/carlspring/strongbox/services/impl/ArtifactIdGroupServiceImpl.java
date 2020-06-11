package org.carlspring.strongbox.services.impl;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.carlspring.strongbox.artifact.ArtifactTag;
import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
import org.carlspring.strongbox.domain.Artifact;
import org.carlspring.strongbox.domain.ArtifactIdGroup;
import org.carlspring.strongbox.domain.ArtifactIdGroupEntity;
import org.carlspring.strongbox.domain.ArtifactTagEntity;
import org.carlspring.strongbox.providers.io.RepositoryPathLock;
import org.carlspring.strongbox.repositories.ArtifactIdGroupRepository;
import org.carlspring.strongbox.repositories.ArtifactRepository;
import org.carlspring.strongbox.services.ArtifactIdGroupService;
import org.carlspring.strongbox.services.ArtifactTagService;
import org.carlspring.strongbox.storage.repository.Repository;
import org.janusgraph.core.SchemaViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.google.common.collect.Iterables;

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
    private RepositoryPathLock repositoryPathLock;

    @Override
    public void saveArtifacts(Repository repository,
                              Set<Artifact> artifactToSaveSet)
    {
        Map<String, List<Artifact>> artifactByGroupIdMap = artifactToSaveSet.stream()
                                                                            .collect(Collectors.groupingBy(a -> a.getArtifactCoordinates()
                                                                                                                 .getId()));
        for (Entry<String, List<Artifact>> artifactIdGroupEntry : artifactByGroupIdMap.entrySet())
        {
            List<Artifact> artifacts = artifactIdGroupEntry.getValue();
            String artifactGroupId = artifactIdGroupEntry.getKey();
            ArtifactTag lastVersionTag = artifactTagService.findOneOrCreate(ArtifactTagEntity.LAST_VERSION);
            for (List<Artifact> artifacstBatch : Iterables.partition(artifacts, 50))
            {
                Lock lock = repositoryPathLock.lock(artifactGroupId).writeLock();
                lock.lock();
                try
                {
                    ArtifactIdGroup artifactGroup = artifactIdGroupRepository.findArtifactsGroupWithTag(repository.getStorage()
                                                                                                                  .getId(),
                                                                                                        repository.getId(),
                                                                                                        artifactGroupId,
                                                                                                        Optional.of(lastVersionTag))
                                                                             .orElseGet(() -> create(repository.getStorage().getId(),
                                                                                                     repository.getId(),
                                                                                                     artifactGroupId));

                    ArtifactCoordinates lastVersion = addArtifactsToGroup(artifacstBatch, artifactGroup);
                    logger.debug("Last version for group [{}] is [{}] with [{}]",
                                 artifactGroup.getName(),
                                 lastVersion.getVersion(),
                                 lastVersion.getPath());

                    artifactIdGroupRepository.merge(artifactGroup);
                }
                finally
                {
                    lock.unlock();
                }
            }
        }
    }

    private ArtifactCoordinates addArtifactsToGroup(List<Artifact> artifacts,
                                                    ArtifactIdGroup artifactGroup)
    {
        ArtifactCoordinates lastVersion = null;
        for (Artifact artifact : artifacts)
        {
            lastVersion = addArtifactToGroup(artifactGroup, artifact);
        }
        return lastVersion;
    }

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
            a2.getTagSet().remove(lastVersionTag);
            return a1;
        }
        else if (artifactCoordinatesComparison < 0)
        {
            a1.getTagSet().remove(lastVersionTag);
            return a2;
        }

        return a1;
    }

    protected ArtifactIdGroup create(String storageId,
                                     String repositoryId,
                                     String artifactId)
    {
        return new ArtifactIdGroupEntity(storageId, repositoryId, artifactId);
    }

}
