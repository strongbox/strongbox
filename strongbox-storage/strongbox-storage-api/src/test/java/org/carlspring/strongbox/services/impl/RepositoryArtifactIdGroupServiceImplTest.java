package org.carlspring.strongbox.services.impl;

import org.carlspring.strongbox.StorageApiTestConfig;
import org.carlspring.strongbox.data.CacheManagerTestExecutionListener;
import org.carlspring.strongbox.domain.RepositoryArtifactIdGroupEntry;
import org.carlspring.strongbox.services.RepositoryArtifactIdGroupService;

import javax.inject.Inject;

import com.orientechnologies.orient.core.storage.ORecordDuplicatedException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * @author Przemyslaw Fusik
 */
@ExtendWith(SpringExtension.class)
@ActiveProfiles(profiles = "test")
@ContextConfiguration(classes = StorageApiTestConfig.class)
@TestExecutionListeners(listeners = { CacheManagerTestExecutionListener.class }, mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
class RepositoryArtifactIdGroupServiceImplTest
{

    @Inject
    RepositoryArtifactIdGroupService repositoryArtifactIdGroupService;

    @Test
    public void repositoryArtifactIdGroupShouldBeProtectedByIndex()
    {
        RepositoryArtifactIdGroupEntry g1 = new RepositoryArtifactIdGroupEntry();
        g1.setName("a1");
        g1.setRepositoryId("r1");
        g1.setStorageId("s1");
        System.out.println(repositoryArtifactIdGroupService.save(g1).getObjectId());
        
        Assertions.assertThrows(ORecordDuplicatedException.class, () -> {
            RepositoryArtifactIdGroupEntry g2 = new RepositoryArtifactIdGroupEntry();
            g2.setName("a1");
            g2.setRepositoryId("r1");
            g2.setStorageId("s1");
            System.out.println(repositoryArtifactIdGroupService.save(g2).getObjectId());
        });
    }
}