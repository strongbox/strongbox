package org.carlspring.strongbox.storage.validation.resource;

import org.carlspring.strongbox.configuration.Configuration;
import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.storage.ArtifactResolutionException;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.testing.TestCaseWithArtifactGeneration;

import javax.inject.Inject;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Kate Novik.
 */
@RunWith(SpringJUnit4ClassRunner.class)
public class ArtifactOperationsValidatorTest
        extends TestCaseWithArtifactGeneration
{

    private static final Logger logger = LoggerFactory.getLogger(ArtifactOperationsValidatorTest.class);

    private static final File REPOSITORY_BASEDIR = new File(ConfigurationResourceResolver.getVaultDirectory() +
                                                            "/storages/storage0/releases");

    private static final String storageId = "storage0";

    private static final String repositoryId = "releases";

    private static MockMultipartFile multipartFile;

    private static InputStream is;

    @org.springframework.context.annotation.Configuration
    @ComponentScan(basePackages = { "org.carlspring.strongbox" })
    public static class SpringConfig
    {

    }

    @Inject
    private ArtifactOperationsValidator artifactOperationsValidator;

    @Inject
    private ConfigurationManager configurationManager;

    @Before
    public void setUp()
            throws Exception
    {
        String gavtc = "org.carlspring.strongbox:strongbox-validate:8.1:jar";
        is = generateArtifactInputStream(REPOSITORY_BASEDIR.getAbsolutePath(), repositoryId, gavtc, true);

        multipartFile = new MockMultipartFile("artifact", "strongbox-validate-8.1.jar", "application/octet-stream", is);

    }

    @Test
    public void checkArtifactSizeTest()
            throws IOException
    {
        long size = multipartFile.getSize();
        Repository repository = getConfiguration().getStorage(storageId)
                                                  .getRepository(repositoryId);
        repository.setArtifactMaxSize(size + 1000L);

        logger.debug("Size of repository is " + repository.getArtifactMaxSize());
        logger.debug("Size of uploaded file is " + size);

        try
        {
            artifactOperationsValidator.checkArtifactSize(storageId, repositoryId, multipartFile);
        }
        catch (ArtifactResolutionException e)
        {

        }

        repository.setArtifactMaxSize(size - 10L);

        logger.debug("Size of repository is " + repository.getArtifactMaxSize());
        logger.debug("Size of uploaded file is " + size);

        try
        {
            artifactOperationsValidator.checkArtifactSize(storageId, repositoryId, multipartFile);
            fail("Should have thrown an ArtifactResolutionException.");
        }
        catch (ArtifactResolutionException e)
        {
            assertTrue(true);
        }

        repository.setArtifactMaxSize(0L);
        logger.debug("Size of repository is " + repository.getArtifactMaxSize());
        logger.debug("Size of uploaded file is " + size);
        try
        {
            artifactOperationsValidator.checkArtifactSize(storageId, repositoryId, multipartFile);
        }
        catch (ArtifactResolutionException e)
        {

        }

        File file = new File(REPOSITORY_BASEDIR.getAbsolutePath() + "validate-test.jar");
        file.getParentFile()
            .mkdirs();
        file.createNewFile();

        MockMultipartFile emptyFile = new MockMultipartFile("artifact", "strongbox-validate-empty.jar",
                                                            "application/octet-stream", new FileInputStream(file));
        size = emptyFile.getSize();
        logger.debug("Size of repository is " + repository.getArtifactMaxSize());
        logger.debug("Size of uploaded file is " + size);
        try
        {
            artifactOperationsValidator.checkArtifactSize(storageId, repositoryId, emptyFile);
            fail("Should have thrown an ArtifactResolutionException.");
        }
        catch (ArtifactResolutionException e)
        {
            assertTrue(true);
        }
    }

    public Configuration getConfiguration()
    {
        return configurationManager.getConfiguration();
    }


}
