package org.carlspring.strongbox.services.impl;

import java.util.Optional;

import javax.inject.Inject;

import org.carlspring.strongbox.artifact.ArtifactTag;
import org.carlspring.strongbox.data.CacheName;
import org.carlspring.strongbox.domain.ArtifactTagEntity;
import org.carlspring.strongbox.repositories.ArtifactTagRepository;
import org.carlspring.strongbox.services.ArtifactTagService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ArtifactTagServiceImpl implements ArtifactTagService
{

    @Inject
    private ArtifactTagRepository artifactTagRepository;

    @Override
    @Cacheable(value = CacheName.Artifact.TAGS, key = "#name")
    public synchronized ArtifactTag findOneOrCreate(String name)
    {
        Optional<ArtifactTag> result = artifactTagRepository.findById(name);

        return result.orElseGet(() -> {
            ArtifactTagEntity artifactTagEntry = new ArtifactTagEntity();
            artifactTagEntry.setName(name);
            return artifactTagRepository.save(artifactTagEntry);
        });
    }

}
