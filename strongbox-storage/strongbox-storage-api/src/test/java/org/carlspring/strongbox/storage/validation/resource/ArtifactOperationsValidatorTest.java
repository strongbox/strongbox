package org.carlspring.strongbox.storage.validation.resource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.inject.Inject;

import org.carlspring.commons.io.RandomInputStream;
import org.carlspring.strongbox.StorageApiTestConfig;
import org.carlspring.strongbox.data.CacheManagerTestExecutionListener;
import org.carlspring.strongbox.services.ConfigurationManagementService;
import org.carlspring.strongbox.storage.ArtifactResolutionException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

/**
 * @author Kate Novik.
 */
@SpringBootTest
@ActiveProfiles(profiles = "test")
@ContextConfiguration(classes = StorageApiTestConfig.class)
@TestExecutionListeners(listeners = { CacheManagerTestExecutionListener.class },
                        mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
public class ArtifactOperationsValidatorTest
{

    private static final String STORAGE_ID = "storage0";

    private static final String REPOSITORY_ID = "releases";

    private static MockMultipartFile multipartFile;

    @Inject
    private ArtifactOperationsValidator artifactOperationsValidator;

    @Inject
    private ConfigurationManagementService configurationManagementService;

    private String REPOSITORY_BASEDIR = new File("target/strongbox-vault/storages/storage0/releases").getAbsolutePath();

    @BeforeEach
    public void setUp()
            throws Exception
    {
        Path basePath = Paths.get(REPOSITORY_BASEDIR, REPOSITORY_ID);
        if (Files.notExists(basePath))
        {
            Files.createDirectories(basePath);
        }

        InputStream is = new RandomInputStream(20480000);

        multipartFile = new MockMultipartFile("artifact",
                                              "strongbox-validate-8.1.jar",
                                              MediaType.APPLICATION_OCTET_STREAM_VALUE,
                                              is);
    }

    @AfterEach
    public void tearDown() throws IOException
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
            assertThat(true).isTrue();
        }

        configurationManagementService.setArtifactMaxSize(STORAGE_ID, REPOSITORY_ID, 0L);

        try
        {
            artifactOperationsValidator.checkArtifactSize(STORAGE_ID, REPOSITORY_ID, multipartFile);
        }
        catch (ArtifactResolutionException e)
        {

        }

        Path path = Paths.get(REPOSITORY_BASEDIR, "validate-test.jar");
        Files.createDirectories(path.getParent());
        Files.createFile(path);

        MockMultipartFile emptyFile = new MockMultipartFile("artifact",
                                                            "strongbox-validate-empty.jar",
                                                            MediaType.APPLICATION_OCTET_STREAM_VALUE,
                                                            Files.newInputStream(path));

        try
        {
            artifactOperationsValidator.checkArtifactSize(STORAGE_ID, REPOSITORY_ID, emptyFile);

            fail("Should have thrown an ArtifactResolutionException.");
        }
        catch (ArtifactResolutionException e)
        {
            assertThat(true).isTrue();
        }
        Files.delete(path);
    }


}
