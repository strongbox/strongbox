package org.carlspring.strongbox.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.inject.Inject;
import javax.xml.bind.JAXBException;

import org.apache.maven.artifact.Artifact;

import org.carlspring.maven.commons.io.filters.JarFilenameFilter;
import org.carlspring.maven.commons.util.ArtifactUtils;
import org.carlspring.strongbox.artifact.ArtifactNotFoundException;
import org.carlspring.strongbox.artifact.ArtifactTag;
import org.carlspring.strongbox.artifact.coordinates.MavenArtifactCoordinates;
import org.carlspring.strongbox.artifact.generator.MavenArtifactGenerator;
import org.carlspring.strongbox.config.Maven2LayoutProviderTestConfig;
import org.carlspring.strongbox.domain.ArtifactEntry;
import org.carlspring.strongbox.providers.io.RepositoryFiles;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.io.RepositoryPathResolver;
import org.carlspring.strongbox.providers.io.RepositoryStreamSupport.RepositoryInputStream;
import org.carlspring.strongbox.providers.layout.Maven2LayoutProvider;
import org.carlspring.strongbox.repository.MavenRepositoryFeatures;
import org.carlspring.strongbox.storage.ArtifactResolutionException;
import org.carlspring.strongbox.storage.ArtifactStorageException;
import org.carlspring.strongbox.storage.repository.MavenRepositoryFactory;
import org.carlspring.strongbox.storage.repository.MutableRepository;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.RepositoryPolicyEnum;
import org.carlspring.strongbox.storage.repository.RepositoryTypeEnum;
import org.carlspring.strongbox.testing.MavenRepositorySetup;
import org.carlspring.strongbox.testing.TestCaseWithMavenArtifactGenerationAndIndexing;
import org.carlspring.strongbox.testing.artifact.ArtifactManagementTestExecutionListener;
import org.carlspring.strongbox.testing.artifact.TestArtifact;
import org.carlspring.strongbox.testing.storage.repository.RepositoryManagementTestExecutionListener;
import org.carlspring.strongbox.testing.storage.repository.TestRepository;

import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

/**
 * @author mtodorov
 */
@SpringBootTest
@ActiveProfiles(profiles = "test")
@ContextConfiguration(classes = Maven2LayoutProviderTestConfig.class)
@Execution(CONCURRENT)
public class ArtifactManagementServiceImplTest
        extends TestCaseWithMavenArtifactGenerationAndIndexing
{

    private static final Logger logger = LoggerFactory.getLogger(ArtifactManagementServiceImplTest.class);

    private static final int CONTENT_SIZE = 40000;

    private DateFormat formatter = new SimpleDateFormat("yyyyMMdd.HHmmss");

    private static final String AMSI_RELEASES_WITHOUT_DEPLOYMENT = "amsi-releases-without-deployment";

    private static final String AMSI_RELEASES_WITHOUT_DELETES = "amsi-releases-without-deletes";

    private static final String TDRADAGR_RELEASES = "tdradagr-releases";

    private static final String TDRADAGR_GROUP = "tdradagr-group";

    private static final String TARFG_RELEASES = "tarfg-releases";

    private static final String TARFG_GROUP = "tarfg-group";

    private static final String TFD_RELEASE_WITHOUT_DELETE = "tfd-release-without-delete";

    private static final String TFD_RELEASE_WITH_TRASH = "tfd-release-with-trash";

    private static final String TRTS_SNAPSHOTS = "trts-snapshots";

    private static final String TCRW_RELEASES_WITH_LOCK = "tcrw-releases-with-lock";

    private static final String LAST_VERSION_RELEASES = "last-version-releases";

    @Inject
    private ArtifactManagementService mavenArtifactManagementService;

    @Inject
    private ArtifactEntryService artifactEntryService;

    @Inject
    private ArtifactResolutionService artifactResolutionService;

    @Inject
    private MavenRepositoryFeatures mavenRepositoryFeatures;

    @Inject
    private ArtifactMetadataService artifactMetadataService;

    @Inject
    private MavenRepositoryFactory mavenRepositoryFactory;

    @Inject
    private RepositoryPathResolver repositoryPathResolver;

    @ExtendWith({ RepositoryManagementTestExecutionListener.class, ArtifactManagementTestExecutionListener.class })
    @Test
    public void testDeploymentToRepositoryWithForbiddenDeployments(@TestRepository(storage = STORAGE0,
                                                                                   repository = AMSI_RELEASES_WITHOUT_DEPLOYMENT,
                                                                                   layout = Maven2LayoutProvider.ALIAS,
                                                                                   setup = MavenRepositorySetup.MavenRepositorySetupWithForbiddenDeleteAndDeloyment.class)
                                                                   Repository repositoryWithoutDeployment,
                                                                   @TestArtifact(repository = AMSI_RELEASES_WITHOUT_DEPLOYMENT,
                                                                                   id = "org.carlspring.strongbox:strongbox-utils",
                                                                                   versions = { "8.0" },
                                                                                   generator = MavenArtifactGenerator.class)
                                                                   List<Path> repositoryArtifact)
            throws Exception
    {
        String repositoryWithoutDeploymentId = repositoryWithoutDeployment.getId();

        File repositoryDir = getRepositoryBasedir(STORAGE0, repositoryWithoutDeploymentId);

        String gavtc = "org.carlspring.strongbox:strongbox-utils:8.0:jar";

        //noinspection EmptyCatchBlock
        try (InputStream is = generateArtifactInputStream(repositoryDir.toPath().getParent().toAbsolutePath().toString(),
                                                          repositoryWithoutDeploymentId,
                                                          gavtc,
                                                          true))
        {
            Artifact artifact = ArtifactUtils.getArtifactFromGAVTC(gavtc);
            RepositoryPath repositoryPath = repositoryPathResolver.resolve(STORAGE0,
                                                                           repositoryWithoutDeploymentId,
                                                                           ArtifactUtils.convertArtifactToPath(artifact));
            mavenArtifactManagementService.validateAndStore(repositoryPath, is);

            fail("Failed to deny artifact operation for repository with disallowed deployments.");
        }

        catch (ArtifactStorageException e)
        {
            // This is the expected correct behavior
        }
    }

    @ExtendWith({ RepositoryManagementTestExecutionListener.class, ArtifactManagementTestExecutionListener.class })
    @Test
    public void testRedeploymentToRepositoryWithForbiddenRedeployments(@TestRepository(storage = STORAGE0,
                                                                                       repository = AMSI_RELEASES_WITHOUT_DEPLOYMENT,
                                                                                       layout = Maven2LayoutProvider.ALIAS,
                                                                                       setup = MavenRepositorySetup.MavenRepositorySetupWithForbiddenRedeloyment.class)
                                                                       Repository repositoryWithoutDeployment,
                                                                       @TestArtifact(repository = AMSI_RELEASES_WITHOUT_DEPLOYMENT,
                                                                                     id = "org.carlspring.strongbox:strongbox-utils",
                                                                                     versions = { "8.1" },
                                                                                     generator = MavenArtifactGenerator.class)
                                                                       List<Path> repositoryArtifact2)
            throws Exception
    {
        String repositoryWithoutDeploymentId = repositoryWithoutDeployment.getId();

        String gavtc = "org.carlspring.strongbox:strongbox-utils:8.1:jar";

        File repositoryBasedir = getRepositoryBasedir(STORAGE0, repositoryWithoutDeploymentId);

        //noinspection EmptyCatchBlock
        try (InputStream is = generateArtifactInputStream(repositoryBasedir.toPath().getParent().toAbsolutePath().toString(),
                                                          repositoryWithoutDeployment.getId(),
                                                          gavtc,
                                                          true))
        {
            Artifact artifact = ArtifactUtils.getArtifactFromGAVTC(gavtc);
            RepositoryPath repositoryPath = repositoryPathResolver.resolve(STORAGE0,
                                                                           repositoryWithoutDeployment.getId(),
                                                                           ArtifactUtils.convertArtifactToPath(artifact));
            mavenArtifactManagementService.validateAndStore(repositoryPath, is);

            fail("Failed to deny artifact operation for repository with disallowed re-deployments.");
        }
        catch (ArtifactStorageException e)
        {
            // This is the expected correct behavior
        }
    }

    @ExtendWith({ RepositoryManagementTestExecutionListener.class, ArtifactManagementTestExecutionListener.class })
    @Test
    public void testDeletionFromRepositoryWithForbiddenDeletes(@TestRepository(storage = STORAGE0,
                                                                               repository = AMSI_RELEASES_WITHOUT_DELETES,
                                                                               layout = Maven2LayoutProvider.ALIAS,
                                                                               setup = MavenRepositorySetup.MavenRepositorySetupWithForbiddenDeletes.class)
                                                               Repository repositoryWithoutDeployment,
                                                               @TestArtifact(repository = AMSI_RELEASES_WITHOUT_DELETES,
                                                                               id = "org.carlspring.strongbox:strongbox-utils",
                                                                               versions = { "8.2", "8.6" },
                                                                               generator = MavenArtifactGenerator.class)
                                                               List<Path> repositoryArtifact)
            throws Exception
    {
        String repositoryWithoutDeploymentId = repositoryWithoutDeployment.getId();

        //noinspection EmptyCatchBlock
        try
        {
            String gavtc = "org.carlspring.strongbox:strongbox-utils:8.2:jar";

            Artifact artifact = ArtifactUtils.getArtifactFromGAVTC(gavtc);
            RepositoryPath repositoryPath = repositoryPathResolver.resolve(STORAGE0,
                                                                           repositoryWithoutDeploymentId,
                                                                           ArtifactUtils.convertArtifactToPath(artifact));
            mavenArtifactManagementService.delete(repositoryPath, false);

            fail("Failed to deny artifact operation for repository with disallowed deletions.");
        }
        catch (ArtifactStorageException e)
        {
            // This is the expected correct behavior
        }
    }

    @ExtendWith({ RepositoryManagementTestExecutionListener.class, ArtifactManagementTestExecutionListener.class })
    @Test
    public void testDeploymentRedeploymentAndDeletionAgainstGroupRepository(@TestRepository(storage = STORAGE0,
                                                                                            repository = TDRADAGR_RELEASES,
                                                                                            layout = Maven2LayoutProvider.ALIAS,
                                                                                            setup = MavenRepositorySetup.MavenRepositorySetupWithForbiddenDeletes.class)
                                                                            Repository repository,
                                                                            @TestRepository.Group({ TDRADAGR_RELEASES })
                                                                            @TestRepository(storage = STORAGE0,
                                                                                            repository = TDRADAGR_GROUP,
                                                                                            layout = Maven2LayoutProvider.ALIAS,
                                                                                            setup = MavenRepositorySetup.MavenRepositorySetupWithForbiddenDeleteDeploymentForceDelete.class)
                                                                            Repository repositoryGroup,
                                                                            @TestArtifact(repository = TDRADAGR_RELEASES,
                                                                                          id = "org.carlspring.strongbox:strongbox-utils",
                                                                                          versions = { "8.3" },
                                                                                          generator = MavenArtifactGenerator.class)
                                                                            List<Path> repositoryArtifact)
            throws Exception
    {
        String repositoryGroupId = repositoryGroup.getId();

        String gavtc = "org.carlspring.strongbox:strongbox-utils:8.3:jar";

        Artifact artifact = ArtifactUtils.getArtifactFromGAVTC(gavtc);

        File repositoryDir = getRepositoryBasedir(STORAGE0, repositoryGroupId);

        try (InputStream is = generateArtifactInputStream(repositoryDir.toPath().getParent().toAbsolutePath().toString(),
                                                          repositoryGroupId,
                                                          gavtc,
                                                          true))
        {
            //noinspection EmptyCatchBlock
            try
            {
                RepositoryPath repositoryPath = repositoryPathResolver.resolve(STORAGE0,
                                                                               repositoryGroupId,
                                                                               ArtifactUtils.convertArtifactToPath(artifact));
                mavenArtifactManagementService.validateAndStore(repositoryPath, is);

                fail("Failed to deny artifact operation for repository with disallowed deployments.");
            }
            catch (ArtifactStorageException e)
            {
                // This is the expected correct behavior
            }

            //noinspection EmptyCatchBlock
            try
            {
                RepositoryPath repositoryPath = repositoryPathResolver.resolve(STORAGE0,
                                                                               repositoryGroupId,
                                                                               ArtifactUtils.convertArtifactToPath(artifact));
                mavenArtifactManagementService.validateAndStore(repositoryPath, is);

                fail("Failed to deny artifact operation for repository with disallowed re-deployments.");
            }
            catch (ArtifactStorageException e)
            {
                // This is the expected correct behavior
            }

            RepositoryPath repositoryPath = repositoryPathResolver.resolve(STORAGE0,
                                                                           repositoryGroupId,
                                                                           ArtifactUtils.convertArtifactToPath(artifact));

            // Delete: Case 1: No forcing
            //noinspection EmptyCatchBlock
            try
            {
                mavenArtifactManagementService.delete(repositoryPath, false);

                fail("Failed to deny artifact operation for repository with disallowed deletions (non-forced test).");
            }
            catch (ArtifactStorageException e)
            {
                // This is the expected correct behavior
            }

            // Delete: Case 2: Force delete
            //noinspection EmptyCatchBlock
            try
            {
                mavenArtifactManagementService.delete(repositoryPath, true);

                fail("Failed to deny artifact operation for repository with disallowed deletions (forced test).");
            }
            catch (ArtifactStorageException e)
            {
                // This is the expected correct behavior
            }
        }
    }

    @ExtendWith({ RepositoryManagementTestExecutionListener.class, ArtifactManagementTestExecutionListener.class })
    @Test
    public void testArtifactResolutionFromGroup(@TestRepository(storage = STORAGE0,
                                                                repository = TARFG_RELEASES,
                                                                layout = Maven2LayoutProvider.ALIAS,
                                                                setup = MavenRepositorySetup.MavenRepositorySetupWithForbiddenDeletes.class)
                                                Repository repository,
                                                @TestRepository.Group({ TARFG_RELEASES })
                                                @TestRepository(storage = STORAGE0,
                                                                repository = TARFG_GROUP,
                                                                layout = Maven2LayoutProvider.ALIAS,
                                                                setup = MavenRepositorySetup.MavenRepositorySetupWithForbiddenDeleteDeploymentForceDelete.class)
                                                Repository repositoryGroup,
                                                @TestArtifact(repository = TARFG_RELEASES,
                                                              id = "org.carlspring.strongbox:strongbox-utils",
                                                              versions = { "8.0.5" },
                                                              generator = MavenArtifactGenerator.class)
                                                List<Path> repositoryArtifact)
            throws Exception
    {
        RepositoryPath path = (RepositoryPath)repositoryArtifact.get(0);

        try (InputStream is = artifactResolutionService.getInputStream(path))
        {
            assertNotNull(is, "Failed to resolve artifact from group repository!");
            assertTrue(is.available() > 0, "Failed to resolve artifact from group repository!");
        }
    }

    @ExtendWith({ RepositoryManagementTestExecutionListener.class, ArtifactManagementTestExecutionListener.class })
    @Test
    public void testForceDelete(@TestRepository(storage = STORAGE0,
                                                repository = TFD_RELEASE_WITHOUT_DELETE,
                                                layout = Maven2LayoutProvider.ALIAS,
                                                policy = RepositoryPolicyEnum.RELEASE)
                                Repository repository1,
                                @TestArtifact(repository = TFD_RELEASE_WITHOUT_DELETE,
                                              id = "org.carlspring.strongbox:strongbox-utils",
                                              versions = { "7.0" },
                                              generator = MavenArtifactGenerator.class)
                                List<Path> repositoryArtifact1,
                                @TestRepository(storage = STORAGE0,
                                              repository = TFD_RELEASE_WITH_TRASH,
                                              layout = Maven2LayoutProvider.ALIAS,
                                              policy = RepositoryPolicyEnum.RELEASE,
                                              cleanup = false,
                                              setup = MavenRepositorySetup.MavenRepositorySetupWithTrashEnabled.class)
                                Repository repository2,
                                @TestArtifact(repository = TFD_RELEASE_WITH_TRASH,
                                              id = "org.carlspring.strongbox:strongbox-utils",
                                              versions = { "7.2" },
                                              generator = MavenArtifactGenerator.class)
                                List<Path> repositoryArtifact2)
            throws Exception
    {

        String repositoryid = repository1.getId();

        String repositoryWithTrashId = repository2.getId();

        final String artifactPath = "org/carlspring/strongbox/strongbox-utils/7.0/strongbox-utils-7.0.jar";

        RepositoryPath repositoryPath = (RepositoryPath)repositoryArtifact1.get(0);

        mavenArtifactManagementService.delete(repositoryPath, true);

        assertFalse(new File(getRepositoryBasedir(STORAGE0, repositoryid), artifactPath).exists(),
                    "Failed to delete artifact during a force delete operation!");

        final String artifactPath2 = "org/carlspring/strongbox/strongbox-utils/7.2/strongbox-utils-7.2.jar";

        repositoryPath = (RepositoryPath)repositoryArtifact2.get(0);

        mavenArtifactManagementService.delete(repositoryPath, true);

        final File repositoryDir = new File(getStorageBasedir(STORAGE0), repositoryWithTrashId + "/.trash");

        assertTrue(new File(repositoryDir, artifactPath2).exists(),
                   "Should have moved the artifact to the trash during a force delete operation, " +
                   "when allowsForceDeletion is not enabled!");
    }

    @ExtendWith({ RepositoryManagementTestExecutionListener.class })
    @Test
    public void testRemoveTimestampedSnapshots(@TestRepository(storage = STORAGE0,
                                                               repository = TRTS_SNAPSHOTS,
                                                               layout = Maven2LayoutProvider.ALIAS,
                                                               policy = RepositoryPolicyEnum.SNAPSHOT)
                                               Repository repository,
                                               TestInfo testInfo)
            throws Exception
    {
        String repositoryid = repository.getId();

        String repositoryBasedir = getRepositoryBasedir(STORAGE0, repositoryid).getAbsolutePath();

        String artifactPath = repositoryBasedir + "/org/carlspring/strongbox/timestamped";

        File artifactVersionBaseDir = new File(artifactPath, "2.0-SNAPSHOT");

        assertFalse(artifactVersionBaseDir.exists());

        createTimestampedSnapshotArtifact(getRepositoryBasedir(STORAGE0, repositoryid).getAbsolutePath(),
                                          "org.carlspring.strongbox",
                                          "timestamped",
                                          "2.0",
                                          "jar",
                                          null,
                                          3);

        assertEquals(3,
                     artifactVersionBaseDir.listFiles(new JarFilenameFilter()).length,
                     "Amount of timestamped snapshots doesn't equal 3.");

        artifactMetadataService.rebuildMetadata(STORAGE0, repositoryid, "org/carlspring/strongbox/timestamped");

        //To check removing timestamped snapshot with numberToKeep = 1
        mavenRepositoryFeatures.removeTimestampedSnapshots(STORAGE0,
                                                           repositoryid,
                                                           "org/carlspring/strongbox/timestamped",
                                                           1,
                                                           0);

        File[] files = artifactVersionBaseDir.listFiles(new JarFilenameFilter());

        assertEquals(1, files.length, "Amount of timestamped snapshots doesn't equal 1.");
        assertTrue(files[0].toString().endsWith("-3.jar"));

        //Creating timestamped snapshot with another timestamp

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -5);
        String timestamp = formatter.format(cal.getTime());

        createTimestampedSnapshot(repositoryBasedir,
                                  "org.carlspring.strongbox",
                                  "timestamped",
                                  "2.0",
                                  "jar",
                                  null,
                                  2,
                                  timestamp);

        artifactMetadataService.rebuildMetadata(STORAGE0, repositoryid, "org/carlspring/strongbox/timestamped");

        assertEquals(2,
                     artifactVersionBaseDir.listFiles(new JarFilenameFilter()).length,
                     "Amount of timestamped snapshots doesn't equal 2.");

        // To check removing timestamped snapshot with keepPeriod = 3 and numberToKeep = 0
        mavenRepositoryFeatures.removeTimestampedSnapshots(STORAGE0,
                                                           repositoryid,
                                                           "org/carlspring/strongbox/timestamped",
                                                           0,
                                                           3);

        files = artifactVersionBaseDir.listFiles(new JarFilenameFilter());

        assertEquals(1, files.length, "Amount of timestamped snapshots doesn't equal 1.");
        assertTrue(files[0].toString().endsWith("-3.jar"));
    }

    @Disabled // TODO FIXME ASAP
    @ExtendWith({ RepositoryManagementTestExecutionListener.class })
    @Test
    public void testConcurrentReadWrite(@TestRepository(storage = STORAGE0,
                                                        repository = TCRW_RELEASES_WITH_LOCK,
                                                        layout = Maven2LayoutProvider.ALIAS)
                                        Repository repository,
                                        TestInfo testInfo)
            throws Exception
    {
        int concurrency = 64;

        Random random = new Random();

        byte[][] loremIpsumContentArray = new byte[concurrency][];
        for (int i = 0; i < loremIpsumContentArray.length; i++)
        {
            random.nextBytes(loremIpsumContentArray[i] = new byte[CONTENT_SIZE]);
        }

        String path = "org/carlspring/strongbox/locked-artifact/12.2.0.1/locked-artifact-12.2.0.1.pom";

        RepositoryPath repositoryPath = repositoryPathResolver.resolve(repository, path);

        // when
        AtomicBoolean aBoolean = new AtomicBoolean(true);
        List<Long> resultList = IntStream.range(0, concurrency * 2)
                                         .parallel()
                                         .mapToObj(i -> getResult(i,
                                                                  aBoolean,
                                                                  repositoryPath,
                                                                  loremIpsumContentArray))
                                         .collect(Collectors.toList());

        // then
        for (int i = 0; i < resultList.size(); i++)
        {
            assertEquals(Long.valueOf(CONTENT_SIZE),
                         resultList.get(i),
                         String.format("Operation [%s:%s] content size don't match.",
                                       i % 2 == 0 ? "write" : "read",
                                       i));
        }

        RepositoryPath repositoryPathResult = repositoryPathResolver.resolve(repository, path);
        ArtifactEntry artifactEntry = repositoryPathResult.getArtifactEntry();

        assertNotNull(artifactEntry);
        assertEquals(Integer.valueOf(concurrency), artifactEntry.getDownloadCount());

        byte[] repositoryPathContent = Files.readAllBytes(repositoryPath);

        assertTrue(Arrays.stream(loremIpsumContentArray)
                         .map(c -> Arrays.equals(repositoryPathContent, c))
                         .reduce((r1,
                                  r2) -> r1 || r2)
                         .get());

    }

    @ExtendWith({ RepositoryManagementTestExecutionListener.class })
    @Test
    public void testLastVersionManagement(@TestRepository(storage = STORAGE0,
                                                          repository = LAST_VERSION_RELEASES,
                                                          layout = Maven2LayoutProvider.ALIAS,
                                                          setup = MavenRepositorySetup.MavenHostedRepositorySetup.class)
                                          Repository repository,
                                          TestInfo testInfo)
            throws Exception
    {
        String repositoryId = repository.getId();

        // store the file without classifier
        String gavtc = "org.carlspring.strongbox:strongbox-lv-artifact:1.0:jar";
        Artifact artifact = ArtifactUtils.getArtifactFromGAVTC(gavtc);
        String artifactPath = ArtifactUtils.convertArtifactToPath(artifact);

        try (InputStream is = new ByteArrayInputStream("strongbox-lv-artifact-content".getBytes(StandardCharsets.UTF_8)))
        {
            RepositoryPath repositoryPath = repositoryPathResolver.resolve(STORAGE0,
                                                                           repositoryId,
                                                                           artifactPath);
            mavenArtifactManagementService.validateAndStore(repositoryPath, is);
        }

        // confirm it has last-version tag
        ArtifactEntry artifactEntry = artifactEntryService.findOneArtifact(STORAGE0, repositoryId, artifactPath);
        MatcherAssert.assertThat(artifactEntry.getTagSet(), CoreMatchers.notNullValue());
        MatcherAssert.assertThat(artifactEntry.getTagSet().size(), CoreMatchers.equalTo(1));
        MatcherAssert.assertThat(artifactEntry.getTagSet().iterator().next().getName(),
                                 CoreMatchers.equalTo(ArtifactTag.LAST_VERSION));

        // store the file with classifier
        String gavtcWithClassifier = "org.carlspring.strongbox:strongbox-lv-artifact:1.0:jar:sources";
        Artifact artifactWithClassifier = ArtifactUtils.getArtifactFromGAVTC(gavtcWithClassifier);
        String artifactPathWithClassifier = ArtifactUtils.convertArtifactToPath(artifactWithClassifier);

        try (InputStream is = new ByteArrayInputStream("strongbox-lv-artifact-content".getBytes(StandardCharsets.UTF_8)))
        {
            RepositoryPath repositoryPath = repositoryPathResolver.resolve(STORAGE0,
                                                                           repositoryId,
                                                                           artifactPathWithClassifier);
            mavenArtifactManagementService.validateAndStore(repositoryPath, is);
        }

        // confirm it has last-version tag
        ArtifactEntry artifactEntryWithClassifier = artifactEntryService.findOneArtifact(STORAGE0,
                                                                                         repositoryId,
                                                                                         artifactPathWithClassifier);

        MatcherAssert.assertThat(artifactEntryWithClassifier.getTagSet(), CoreMatchers.notNullValue());
        MatcherAssert.assertThat(artifactEntryWithClassifier.getTagSet().size(), CoreMatchers.equalTo(1));
        MatcherAssert.assertThat(artifactEntryWithClassifier.getTagSet().iterator().next().getName(),
                                 CoreMatchers.equalTo(ArtifactTag.LAST_VERSION));

        // re-fetch the artifact without classifier
        // and confirm it still has the last version tag
        artifactEntry = artifactEntryService.findOneArtifact(STORAGE0, repositoryId, artifactPath);
        MatcherAssert.assertThat(artifactEntry.getTagSet(), CoreMatchers.notNullValue());
        MatcherAssert.assertThat(artifactEntry.getTagSet().size(), CoreMatchers.equalTo(1));
        MatcherAssert.assertThat(artifactEntry.getTagSet().iterator().next().getName(),
                                 CoreMatchers.equalTo(ArtifactTag.LAST_VERSION));

        // store the newest version of file without classifier
        String gavtcV2 = "org.carlspring.strongbox:strongbox-lv-artifact:2.0:jar";
        Artifact artifactV2 = ArtifactUtils.getArtifactFromGAVTC(gavtcV2);
        String artifactPathV2 = ArtifactUtils.convertArtifactToPath(artifactV2);

        try (InputStream is = new ByteArrayInputStream("strongbox-lv-artifact-content".getBytes(StandardCharsets.UTF_8)))
        {
            RepositoryPath repositoryPath = repositoryPathResolver.resolve(STORAGE0,
                                                                           repositoryId,
                                                                           artifactPathV2);
            mavenArtifactManagementService.validateAndStore(repositoryPath, is);
        }

        // confirm it has last-version tag
        ArtifactEntry artifactEntryV2 = artifactEntryService.findOneArtifact(STORAGE0,
                                                                             repositoryId,
                                                                             artifactPathV2);

        MatcherAssert.assertThat(artifactEntryV2.getTagSet(), CoreMatchers.notNullValue());
        MatcherAssert.assertThat(artifactEntryV2.getTagSet().size(), CoreMatchers.equalTo(1));
        MatcherAssert.assertThat(artifactEntryV2.getTagSet().iterator().next().getName(),
                                 CoreMatchers.equalTo(ArtifactTag.LAST_VERSION));

        // re-fetch the artifact without classifier
        // and confirm it no longer has the last version tag
        artifactEntry = artifactEntryService.findOneArtifact(STORAGE0, repositoryId, artifactPath);
        MatcherAssert.assertThat(artifactEntry.getTagSet(), CoreMatchers.notNullValue());
        MatcherAssert.assertThat(artifactEntry.getTagSet().size(), CoreMatchers.equalTo(0));

        // confirm it no longer has last-version tag
        artifactEntryWithClassifier = artifactEntryService.findOneArtifact(STORAGE0,
                                                                           repositoryId,
                                                                           artifactPathWithClassifier);

        MatcherAssert.assertThat(artifactEntryWithClassifier.getTagSet(), CoreMatchers.notNullValue());
        MatcherAssert.assertThat(artifactEntryWithClassifier.getTagSet().size(), CoreMatchers.equalTo(0));
    }

    @Test
    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    public void testChecksumsStorage(TestInfo testInfo,
                                     @TestRepository (layout = MavenArtifactCoordinates.LAYOUT_NAME,
                                                      repositoryId = "checksums-storage")
                                             Repository repository,
                                     @TestArtifact(resource = "org/carlspring/strongbox/strongbox-checksum-test/8.4/strongbox-checksum-test-8.4.jar",
                                             generator = MavenArtifactGenerator.class)
                                             Path artifact)
            throws Exception
    {
        RepositoryPath repositoryPath = repositoryPathResolver.resolve(repository).resolve("org/carlspring/strongbox/strongbox-checksum-test/8.4/strongbox-checksum-test-8.4.jar");

        try (InputStream is = Files.newInputStream(artifact))
        {
            mavenArtifactManagementService.store(repositoryPath, is);
        }

        // Obtains two expected checksums
        String sha1Checksum = new String(Files.readAllBytes(artifact.resolveSibling(artifact.getFileName()+ ".sha1")));
        String md5Checksum = new String(Files.readAllBytes(artifact.resolveSibling(artifact.getFileName()+ ".md5")));

        Map <String, String> expectedChecksums = new HashMap<>();
        expectedChecksums.put("SHA-1", sha1Checksum);
        expectedChecksums.put("MD5", md5Checksum);

        String path = RepositoryFiles.relativizePath(repositoryPath);
        ArtifactEntry artifactEntry = artifactEntryService.findOneArtifact(STORAGE0, repository.getId(), path);

        assertNotNull(artifactEntry);

        Map<String, String> actualChecksums = artifactEntry.getChecksums();

        assertNotNull(actualChecksums);
        assertEquals(expectedChecksums, actualChecksums);
    }


    private Long getResult(int i,
                           AtomicBoolean aBoolean,
                           RepositoryPath repositoryPath,
                           byte[][] loremIpsumContentArray)
    {
        try
        {
            Repository repository = repositoryPath.getRepository();
            String path = RepositoryFiles.relativizePath(repositoryPath);
            return aBoolean.getAndSet(!aBoolean.get()) ?
                   new Store(new ByteArrayInputStream(loremIpsumContentArray[i / 2]), repository, path).call() :
                   new Fetch(repository, path).call();
        }
        catch (IOException e)
        {
            logger.error("Unexpected IOException while getting result:", e);
            e.printStackTrace();

            return 0L;
        }
    }

    private class Store
            implements Callable<Long>
    {

        private final Repository repository;

        private final String path;

        private final InputStream is;

        private Store(InputStream is,
                      Repository repository,
                      String path)
        {
            this.path = path;
            this.repository = repository;
            this.is = is;
        }

        @Override
        public Long call()
        {
            RepositoryPath repositoryPath = repositoryPathResolver.resolve(repository, path);

            try
            {
                return mavenArtifactManagementService.store(repositoryPath, is);
            }
            catch (Exception ex)
            {
                logger.error(String.format("Failed to store artifact [%s]", repositoryPath), ex);

                return 0L;
            }
        }
    }

    private class Fetch
            implements Callable<Long>
    {

        private final Repository repository;
        private final String path;
        private int attempts = 0;

        private Fetch(Repository repository,
                      String path)
        {
            this.path = path;
            this.repository = repository;
        }

        @Override
        public Long call()
        {
            RepositoryPath repositoryPath = repositoryPathResolver.resolve(repository, path);

            long result = 0;

            byte[] buffer = new byte[1024];
            try (RepositoryInputStream is = artifactResolutionService.getInputStream(repositoryPath))
            {
                while (true)
                {
                    int n = is.read(buffer);
                    if (n < 0)
                    {
                        break;
                    }

                    result += n;
                }
            }
            catch (ArtifactResolutionException | ArtifactNotFoundException e)
            {
                try
                {
                    Thread.sleep(100);
                }
                catch (InterruptedException e1)
                {
                    return 0L;
                }

                if (attempts++ > 5)
                {
                    return 0L;
                }

                return this.call();
            }
            catch (Exception ex)
            {
                logger.error(String.format("Failed to read artifact [%s]", repositoryPath), ex);

                return 0L;
            }

            return result;
        }
    }
}
