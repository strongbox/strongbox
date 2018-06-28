package org.carlspring.strongbox.storage.validation.version;

import org.carlspring.strongbox.StorageApiTestConfig;
import org.carlspring.strongbox.data.CacheManagerTestExecutionListener;
import org.carlspring.strongbox.services.VersionValidatorService;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static org.junit.Assert.assertFalse;

/**
 * @author mtodorov
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = StorageApiTestConfig.class)
@TestExecutionListeners(listeners = { CacheManagerTestExecutionListener.class }, mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
public class VersionValidatorServiceTest
{

    @Inject
    private VersionValidatorService versionValidatorService;


    @Test
    public void testValidationService()
    {
        assertFalse(versionValidatorService.getVersionValidators().isEmpty());
    }

}
