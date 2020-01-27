package org.carlspring.strongbox.services.impl;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.Optional;

import javax.inject.Inject;

import org.apache.tinkerpop.gremlin.structure.Graph;
import org.carlspring.strongbox.artifact.ArtifactTag;
import org.carlspring.strongbox.data.CacheName;
import org.carlspring.strongbox.domain.ArtifactTagEntity;
import org.carlspring.strongbox.gremlin.dsl.EntityTraversalSource;
import org.carlspring.strongbox.repositories.ArtifactTagRepository;
import org.carlspring.strongbox.services.ArtifactTagService;
import org.janusgraph.core.JanusGraph;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ArtifactTagServiceImpl implements ArtifactTagService
{

    @Inject
    private ArtifactTagRepository artifactTagRepository;
    @Inject
    private JanusGraph janusGraph;

    @Override
    @Cacheable(value = CacheName.Artifact.TAGS, key = "#name", sync = true)
    public ArtifactTag findOneOrCreate(String name)
    {
        Optional<ArtifactTag> optionalResult = artifactTagRepository.findById(name);

        return optionalResult.orElseGet(() -> {
            ArtifactTagEntity artifactTagEntry = new ArtifactTagEntity();
            artifactTagEntry.setName(name);

            Graph g = janusGraph.tx().createThreadedTx();
            try
            {
                ArtifactTagEntity result = artifactTagRepository.save(() -> g.traversal(EntityTraversalSource.class), artifactTagEntry);
                g.tx().commit();

                return result;
            }
            catch (Exception e)
            {
                g.tx().rollback();
                throw new UndeclaredThrowableException(e);
            }
            finally
            {
                g.tx().close();
            }
        });
    }

}
