package org.carlspring.strongbox.services.impl;

import org.carlspring.strongbox.StorageApiTestConfig;
import org.carlspring.strongbox.data.CacheManagerTestExecutionListener;
import org.carlspring.strongbox.domain.RepositoryArtifactIdGroup;
import org.carlspring.strongbox.services.ArtifactGroupService;

import javax.inject.Inject;

import com.orientechnologies.orient.core.storage.ORecordDuplicatedException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
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
class ArtifactGroupServiceImplTest
{

    @Inject
    ArtifactGroupService artifactGroupService;

    @Test
    @Disabled // requires https://github.com/strongbox/strongbox-db/pull/5/files
    public void artifactIdGroupShouldBeProtectedByNameIndex()
    {
        Assertions.assertThrows(ORecordDuplicatedException.class, () -> {
            RepositoryArtifactIdGroup g1 = new RepositoryArtifactIdGroup();
            g1.setId("strongbox-art");
            g1.setRepositoryId("strongbox-rId");
            g1.setStorageId("strongbox-sId");
            artifactGroupService.save(g1);

            RepositoryArtifactIdGroup g2 = new RepositoryArtifactIdGroup();
            g2.setId("strongbox-art");
            g2.setRepositoryId("strongbox-rId");
            g2.setStorageId("strongbox-sId");
            artifactGroupService.save(g2);
        });
    }
}