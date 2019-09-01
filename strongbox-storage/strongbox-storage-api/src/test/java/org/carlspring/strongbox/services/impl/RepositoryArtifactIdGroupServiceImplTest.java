package org.carlspring.strongbox.services.impl;

import org.carlspring.strongbox.StorageApiTestConfig;
import org.carlspring.strongbox.data.CacheManagerTestExecutionListener;
import org.carlspring.strongbox.domain.RepositoryArtifactIdGroupEntry;
import org.carlspring.strongbox.services.RepositoryArtifactIdGroupService;

import javax.inject.Inject;

import com.orientechnologies.orient.core.storage.ORecordDuplicatedException;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * @author Przemyslaw Fusik
 */
@SpringBootTest
@ActiveProfiles(profiles = "test")
@ContextConfiguration(classes = StorageApiTestConfig.class)
@TestExecutionListeners(listeners = { CacheManagerTestExecutionListener.class },
                        mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
class RepositoryArtifactIdGroupServiceImplTest
{

    private static final Logger logger = LoggerFactory.getLogger(RepositoryArtifactIdGroupServiceImplTest.class);

    @Inject
    private RepositoryArtifactIdGroupService repositoryArtifactIdGroupService;

    @Test
    public void repositoryArtifactIdGroupShouldBeProtectedByIndex()
    {
        RepositoryArtifactIdGroupEntry g1 = new RepositoryArtifactIdGroupEntry();
        g1.setName("a1");
        g1.setRepositoryId("r1");
        g1.setStorageId("s1");
        RepositoryArtifactIdGroupEntry savedG1 = repositoryArtifactIdGroupService.save(g1);
        logger.debug(savedG1.getObjectId());

        assertThatExceptionOfType(ORecordDuplicatedException.class)
                .isThrownBy(() -> {
                    RepositoryArtifactIdGroupEntry g2 = new RepositoryArtifactIdGroupEntry();
                    g2.setName("a1");
                    g2.setRepositoryId("r1");
                    g2.setStorageId("s1");
                    RepositoryArtifactIdGroupEntry savedG2 = repositoryArtifactIdGroupService.save(g2);
            logger.debug(savedG2.getObjectId());
        });
    }
}
