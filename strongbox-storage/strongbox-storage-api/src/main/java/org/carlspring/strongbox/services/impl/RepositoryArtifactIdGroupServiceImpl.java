package org.carlspring.strongbox.services.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;

import org.carlspring.strongbox.artifact.ArtifactTag;
import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
import org.carlspring.strongbox.domain.ArtifactEntry;
import org.carlspring.strongbox.domain.ArtifactTagEntry;
import org.carlspring.strongbox.domain.RepositoryArtifactIdGroupEntry;
import org.carlspring.strongbox.services.ArtifactEntryService;
import org.carlspring.strongbox.services.ArtifactTagService;
import org.carlspring.strongbox.services.RepositoryArtifactIdGroupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.orientechnologies.common.concur.ONeedRetryException;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;

/**
 * @author Przemyslaw Fusik
 * @author sbespalov
 */
@Service
@Transactional
public class RepositoryArtifactIdGroupServiceImpl
        extends AbstractArtifactGroupService<RepositoryArtifactIdGroupEntry>
        implements RepositoryArtifactIdGroupService
{

    private static final Logger logger = LoggerFactory.getLogger(RepositoryArtifactIdGroupEntry.class);
    
    @Inject
    private ArtifactTagService artifactTagService;

    @Override
    public void addArtifactToGroup(RepositoryArtifactIdGroupEntry artifactGroup,
                                   ArtifactEntry artifactEntry)
    {
        ArtifactCoordinates coordinates = artifactEntry.getArtifactCoordinates();
        Assert.notNull(coordinates, "coordinates should not be null");

        ArtifactTag lastVersionTag = artifactTagService.findOneOrCreate(ArtifactTagEntry.LAST_VERSION);

        artifactEntry.getTagSet().add(lastVersionTag);
        artifactGroup.putArtifactEntry(artifactEntry);
        
        artifactGroup.getArtifactEntries()
                     .stream()
                     .filter(e -> e.getTagSet().contains(lastVersionTag))
                     .sorted((e1,
                              e2) -> e1.getArtifactCoordinates().compareTo(e2.getArtifactCoordinates()))
                     .forEach(e -> checkAndUpdateLastVersionTagIfNeeded(e, artifactEntry, lastVersionTag));
        
        
        save(artifactGroup);
    }

    private <S extends ArtifactEntry> Optional<S> checkAndUpdateLastVersionTagIfNeeded(S lastVersionEntry,
                                                                                       S entity,
                                                                                       ArtifactTag lastVersionTag)
    {
        Optional<S> result = Optional.empty();
        ArtifactCoordinates coordinates = entity.getArtifactCoordinates();
        
        int artifactCoordinatesComparison = entity.getArtifactCoordinates()
                                                  .compareTo(lastVersionEntry.getArtifactCoordinates());
        if (artifactCoordinatesComparison == 0)
        {
            logger.debug(String.format("Set [%s] last version to [%s]",
                                       entity.getArtifactPath(),
                                       coordinates.getVersion()));
            entity.getTagSet().add(lastVersionTag);
        }
        else if (artifactCoordinatesComparison > 0)
        {
            logger.debug(String.format("Update [%s] last version from [%s] to [%s]",
                                       entity.getArtifactPath(),
                                       lastVersionEntry.getArtifactCoordinates().getVersion(),
                                       coordinates.getVersion()));
            entity.getTagSet().add(lastVersionTag);

            lastVersionEntry.getTagSet().remove(lastVersionTag);
            result  = Optional.of(lastVersionEntry);
        }
        else
        {
            logger.debug(String.format("Keep [%s] last version [%s]",
                                       entity.getArtifactPath(),
                                       lastVersionEntry.getArtifactCoordinates().getVersion()));
            entity.getTagSet().remove(lastVersionTag);
        }
        
        return result;
    }
    
    public RepositoryArtifactIdGroupEntry findOneOrCreate(String storageId,
                                                     String repositoryId,
                                                     String artifactId)
    {
        Optional<RepositoryArtifactIdGroupEntry> optional = tryFind(storageId, repositoryId, artifactId);
        if (optional.isPresent())
        {
            return optional.get();
        }

        RepositoryArtifactIdGroupEntry artifactGroup = create(storageId, repositoryId, artifactId);

        try
        {
            return save(artifactGroup);
        }
        catch (ONeedRetryException ex)
        {
            optional = tryFind(storageId, repositoryId, artifactId);
            if (optional.isPresent())
            {
                return optional.get();
            }
            throw ex;
        }
    }

    protected Optional<RepositoryArtifactIdGroupEntry> tryFind(String storageId,
                                                        String repositoryId,
                                                        String artifactId)
    {
        return Optional.ofNullable(findOne(storageId, repositoryId, artifactId));
    }

    protected RepositoryArtifactIdGroupEntry create(String storageId,
                                               String repositoryId,
                                               String artifactId)
    {
        return new RepositoryArtifactIdGroupEntry(storageId, repositoryId, artifactId);
    }

    public RepositoryArtifactIdGroupEntry findOne(String storageId,
                                             String repositoryId,
                                             String artifactId)
    {
        Map<String, String> params = new HashMap<>();
        params.put("storageId", storageId);
        params.put("repositoryId", repositoryId);
        params.put("name", artifactId);

        String sQuery = buildQuery(params);

        OSQLSynchQuery<ODocument> oQuery = new OSQLSynchQuery<>(sQuery);
        oQuery.setLimit(1);

        List<RepositoryArtifactIdGroupEntry> resultList = getDelegate().command(oQuery)
                                                                  .execute(params);
        return resultList.stream().findFirst().orElse(null);
    }

}
