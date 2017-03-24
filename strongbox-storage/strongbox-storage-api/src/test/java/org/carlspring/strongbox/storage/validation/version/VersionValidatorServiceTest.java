package org.carlspring.strongbox.storage.validation.version;

import org.carlspring.strongbox.config.CommonConfig;
import org.carlspring.strongbox.config.StorageCoreConfig;
import org.carlspring.strongbox.services.VersionValidatorService;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static org.junit.Assert.assertFalse;

/**
 * @author mtodorov
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class VersionValidatorServiceTest
{

    @org.springframework.context.annotation.Configuration
    @Import({
                    StorageCoreConfig.class,
                    CommonConfig.class
    })
    public static class SpringConfig { }

    @Inject
    private VersionValidatorService versionValidatorService;


    @Test
    public void testValidationService()
    {
        assertFalse(versionValidatorService.getVersionValidators().isEmpty());
    }

}
