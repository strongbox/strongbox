package org.carlspring.strongbox.services.impl;

import static org.assertj.core.api.Assertions.assertThat;

import javax.inject.Inject;
import javax.transaction.Transactional;

import org.carlspring.strongbox.StorageApiTestConfig;
import org.carlspring.strongbox.data.CacheManagerTestExecutionListener;
import org.carlspring.strongbox.domain.ArtifactIdGroupEntity;
import org.carlspring.strongbox.repositories.ArtifactIdGroupRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;

/**
 * @author Przemyslaw Fusik
 */
@SpringBootTest
@ActiveProfiles(profiles = "test")
@ContextConfiguration(classes = StorageApiTestConfig.class)
@TestExecutionListeners(listeners = { CacheManagerTestExecutionListener.class },
                        mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
@Transactional
class RepositoryArtifactIdGroupServiceImplTest
{

    @Inject
    private ArtifactIdGroupRepository artifactIdGroupRepository;

    @Test
    public void repositoryArtifactIdGroupShouldBeProtectedByIndex()
    {
        ArtifactIdGroupEntity g1 = new ArtifactIdGroupEntity("s1", "r1", "a1");
        g1 = artifactIdGroupRepository.save(g1);
        
        ArtifactIdGroupEntity g2 = new ArtifactIdGroupEntity("s1", "r1", "a1");
        g2 = artifactIdGroupRepository.save(g2);
        assertThat(g2.getNativeId()).isEqualTo(g1.getNativeId());
    }
}
