package org.carlspring.strongbox.storage.validation.resource;

import org.carlspring.commons.io.RandomInputStream;
import org.carlspring.strongbox.StorageApiTestConfig;
import org.carlspring.strongbox.data.CacheManagerTestExecutionListener;
import org.carlspring.strongbox.services.ConfigurationManagementService;
import org.carlspring.strongbox.storage.ArtifactResolutionException;

import javax.inject.Inject;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * @author Kate Novik.
 */
@ExtendWith(SpringExtension.class)
@ActiveProfiles(profiles = "test")
@ContextConfiguration(classes = StorageApiTestConfig.class)
@TestExecutionListeners(listeners = { CacheManagerTestExecutionListener.class },
                        mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
public class ArtifactOperationsValidatorTest
{

    private static final String STORAGE_ID = "storage0";

    private static final String REPOSITORY_ID = "releases";

    private static MockMultipartFile multipartFile;

    private static InputStream is;

    @Inject
    private ArtifactOperationsValidator artifactOperationsValidator;

    @Inject
    private ConfigurationManagementService configurationManagementService;

    private String REPOSITORY_BASEDIR = new File("target/strongbox-vault/storages/storage0/releases").getAbsolutePath();

    @BeforeEach
    public void setUp()
            throws Exception
    {
        File baseDir = new File(REPOSITORY_BASEDIR + "/" + REPOSITORY_ID);
        if (!baseDir.exists())
        {
            //noinspection ResultOfMethodCallIgnored
            baseDir.mkdirs();
        }

        is = new RandomInputStream(20480000);

        multipartFile = new MockMultipartFile("artifact",
                                              "strongbox-validate-8.1.jar",
                                              "application/octet-stream",
                                              is);
    }

    @AfterEach
    public void tearDown()
    {
        configurationManagementService.setArtifactMaxSize(STORAGE_ID, REPOSITORY_ID, 0L);
    }

    @Test
    public void checkArtifactSizeTest()
            throws IOException
    {
        long size = multipartFile.getSize();

        configurationManagementService.setArtifactMaxSize(STORAGE_ID, REPOSITORY_ID, size + 1000L);

        try
        {
            artifactOperationsValidator.checkArtifactSize(STORAGE_ID, REPOSITORY_ID, multipartFile);
        }
        catch (ArtifactResolutionException e)
        {

        }

        configurationManagementService.setArtifactMaxSize(STORAGE_ID, REPOSITORY_ID, size - 10L);

        try
        {
            artifactOperationsValidator.checkArtifactSize(STORAGE_ID, REPOSITORY_ID, multipartFile);

            fail("Should have thrown an ArtifactResolutionException.");
        }
        catch (ArtifactResolutionException e)
        {
            assertTrue(true);
        }

        configurationManagementService.setArtifactMaxSize(STORAGE_ID, REPOSITORY_ID, 0L);

        try
        {
            artifactOperationsValidator.checkArtifactSize(STORAGE_ID, REPOSITORY_ID, multipartFile);
        }
        catch (ArtifactResolutionException e)
        {

        }

        File file = new File(REPOSITORY_BASEDIR + "validate-test.jar");
        //noinspection ResultOfMethodCallIgnored
        file.getParentFile().mkdirs();
        //noinspection ResultOfMethodCallIgnored
        file.createNewFile();

        MockMultipartFile emptyFile = new MockMultipartFile("artifact",
                                                            "strongbox-validate-empty.jar",
                                                            "application/octet-stream",
                                                            new FileInputStream(file));

        try
        {
            artifactOperationsValidator.checkArtifactSize(STORAGE_ID, REPOSITORY_ID, emptyFile);

            fail("Should have thrown an ArtifactResolutionException.");
        }
        catch (ArtifactResolutionException e)
        {
            assertTrue(true);
        }
    }


}
