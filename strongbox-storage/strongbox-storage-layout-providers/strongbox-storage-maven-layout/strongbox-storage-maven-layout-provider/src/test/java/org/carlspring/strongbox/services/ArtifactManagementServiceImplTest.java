package org.carlspring.strongbox.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.apache.maven.artifact.Artifact;
import org.carlspring.strongbox.artifact.ArtifactTag;
import org.carlspring.strongbox.artifact.MavenArtifactUtils;
import org.carlspring.strongbox.config.Maven2LayoutProviderTestConfig;
import org.carlspring.strongbox.providers.io.RepositoryFiles;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.io.RepositoryPathResolver;
import org.carlspring.strongbox.providers.io.RepositoryStreamSupport.RepositoryInputStream;
import org.carlspring.strongbox.repositories.ArtifactRepository;
import org.carlspring.strongbox.repository.MavenRepositoryFeatures;
import org.carlspring.strongbox.storage.ArtifactStorageException;
import org.carlspring.strongbox.storage.metadata.MavenSnapshotManager;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.RepositoryPolicyEnum;
import org.carlspring.strongbox.testing.artifact.ArtifactManagementTestExecutionListener;
import org.carlspring.strongbox.testing.artifact.MavenArtifactTestUtils;
import org.carlspring.strongbox.testing.artifact.MavenTestArtifact;
import org.carlspring.strongbox.testing.repository.MavenRepository;
import org.carlspring.strongbox.testing.storage.repository.RepositoryAttributes;
import org.carlspring.strongbox.testing.storage.repository.RepositoryManagementTestExecutionListener;
import org.carlspring.strongbox.testing.storage.repository.TestRepository.Group;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

/**
 * @author mtodorov
 * @author Pablo Tirado
 */
@SpringBootTest
@ActiveProfiles(profiles = "test")
@ContextConfiguration(classes = Maven2LayoutProviderTestConfig.class)
@Execution(CONCURRENT)
public class ArtifactManagementServiceImplTest
{

    private static final Logger logger = LoggerFactory.getLogger(ArtifactManagementServiceImplTest.class);

    private static final int CONTENT_SIZE = 40000;

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
    private ArtifactRepository artifactEntityRepository;

    @Inject
    private ArtifactResolutionService artifactResolutionService;

    @Inject
    private MavenRepositoryFeatures mavenRepositoryFeatures;

    @Inject
    private ArtifactMetadataService artifactMetadataService;

    @Inject
    private RepositoryPathResolver repositoryPathResolver;

    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @Test
    public void testDeploymentToRepositoryWithForbiddenDeployments(@MavenRepository(repositoryId = AMSI_RELEASES_WITHOUT_DEPLOYMENT)
                                                                   @RepositoryAttributes(allowsDelete = false,
                                                                                         allowsRedeployment = false)
                                                                   Repository repositoryWithoutDeployment,
                                                                   @MavenTestArtifact(repositoryId = AMSI_RELEASES_WITHOUT_DEPLOYMENT,
                                                                                      id = "org.carlspring.strongbox:strongbox-utils",
                                                                                      versions = { "8.0" })
                                                                   Path artifactPath)
            throws Exception
    {
        final String storageId = repositoryWithoutDeployment.getStorage().getId();
        final String repositoryId = repositoryWithoutDeployment.getId();

        RepositoryPath path = (RepositoryPath) artifactPath.normalize();
        String gavtc = "org.carlspring.strongbox:strongbox-utils:8.0:jar";

        //noinspection EmptyCatchBlock
        try (InputStream is = artifactResolutionService.getInputStream(path))
        {
            Artifact artifact = MavenArtifactTestUtils.getArtifactFromGAVTC(gavtc);
            RepositoryPath repositoryPath = repositoryPathResolver.resolve(storageId,
                                                                           repositoryId,
                                                                           MavenArtifactUtils.convertArtifactToPath(artifact));
            mavenArtifactManagementService.validateAndStore(repositoryPath, is);

            fail("Failed to deny artifact operation for repository with disallowed deployments.");
        }

        catch (ArtifactStorageException e)
        {
            // This is the expected correct behavior
        }
    }

    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @Test
    public void testRedeploymentToRepositoryWithForbiddenRedeployments(@MavenRepository(repositoryId = AMSI_RELEASES_WITHOUT_DEPLOYMENT)
                                                                       @RepositoryAttributes(allowsRedeployment =  false)
                                                                       Repository repositoryWithoutDeployment,
                                                                       @MavenTestArtifact(repositoryId = AMSI_RELEASES_WITHOUT_DEPLOYMENT,
                                                                                          id = "org.carlspring.strongbox:strongbox-utils",
                                                                                          versions = { "8.1" })
                                                                       Path artifactPath)
            throws Exception
    {
        final String storageId = repositoryWithoutDeployment.getStorage().getId();
        final String repositoryId = repositoryWithoutDeployment.getId();

        RepositoryPath path = (RepositoryPath) artifactPath.normalize();
        String gavtc = "org.carlspring.strongbox:strongbox-utils:8.1:jar";

        //noinspection EmptyCatchBlock
        try (InputStream is = artifactResolutionService.getInputStream(path))
        {
            Artifact artifact = MavenArtifactTestUtils.getArtifactFromGAVTC(gavtc);
            RepositoryPath repositoryPath = repositoryPathResolver.resolve(storageId,
                                                                           repositoryId,
                                                                           MavenArtifactUtils.convertArtifactToPath(artifact));
            mavenArtifactManagementService.validateAndStore(repositoryPath, is);

            fail("Failed to deny artifact operation for repository with disallowed re-deployments.");
        }
        catch (ArtifactStorageException e)
        {
            // This is the expected correct behavior
        }
    }

    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @Test
    public void testDeletionFromRepositoryWithForbiddenDeletes(@MavenRepository(repositoryId = AMSI_RELEASES_WITHOUT_DELETES)
                                                               @RepositoryAttributes(allowsDelete =  false)
                                                               Repository repositoryWithoutDeployment,
                                                               @MavenTestArtifact(repositoryId = AMSI_RELEASES_WITHOUT_DELETES,
                                                                                  id = "org.carlspring.strongbox:strongbox-utils",
                                                                                  versions = { "8.2", "8.6" })
                                                               List<Path> artifactPaths)
            throws Exception
    {
        final String storageId = repositoryWithoutDeployment.getStorage().getId();
        final String repositoryId = repositoryWithoutDeployment.getId();

        String gavtc = "org.carlspring.strongbox:strongbox-utils:8.2:jar";

        //noinspection EmptyCatchBlock
        try
        {
            Artifact artifact = MavenArtifactTestUtils.getArtifactFromGAVTC(gavtc);
            RepositoryPath repositoryPath = repositoryPathResolver.resolve(storageId,
                                                                           repositoryId,
                                                                           MavenArtifactUtils.convertArtifactToPath(artifact));
            mavenArtifactManagementService.delete(repositoryPath, false);

            fail("Failed to deny artifact operation for repository with disallowed deletions.");
        }
        catch (ArtifactStorageException e)
        {
            // This is the expected correct behavior
        }
    }

    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @Test
    public void testDeploymentRedeploymentAndDeletionAgainstGroupRepository(@MavenRepository(repositoryId = TDRADAGR_RELEASES)
                                                                            @RepositoryAttributes(allowsDelete =  false)
                                                                            Repository repository,
                                                                            @Group({ TDRADAGR_RELEASES })
                                                                            @MavenRepository(repositoryId = TDRADAGR_GROUP)
                                                                            @RepositoryAttributes(allowsDelete =  false,
                                                                                                  allowsRedeployment = false)
                                                                            Repository repositoryGroup,
                                                                            @MavenTestArtifact(repositoryId = TDRADAGR_RELEASES,
                                                                                               id = "org.carlspring.strongbox:strongbox-utils",
                                                                                               versions = { "8.3" })
                                                                            Path artifactPath)
            throws Exception
    {
        final String storageId = repositoryGroup.getStorage().getId();
        final String repositoryId = repositoryGroup.getId();

        RepositoryPath path = (RepositoryPath) artifactPath.normalize();

        String gavtc = "org.carlspring.strongbox:strongbox-utils:8.3:jar";

        Artifact artifact = MavenArtifactTestUtils.getArtifactFromGAVTC(gavtc);

        try (InputStream is = artifactResolutionService.getInputStream(path))
        {
            //noinspection EmptyCatchBlock
            try
            {
                RepositoryPath repositoryPath = repositoryPathResolver.resolve(storageId,
                                                                               repositoryId,
                                                                               MavenArtifactUtils.convertArtifactToPath(artifact));
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
                RepositoryPath repositoryPath = repositoryPathResolver.resolve(storageId,
                                                                               repositoryId,
                                                                               MavenArtifactUtils.convertArtifactToPath(artifact));
                mavenArtifactManagementService.validateAndStore(repositoryPath, is);

                fail("Failed to deny artifact operation for repository with disallowed re-deployments.");
            }
            catch (ArtifactStorageException e)
            {
                // This is the expected correct behavior
            }

            RepositoryPath repositoryPath = repositoryPathResolver.resolve(storageId,
                                                                           repositoryId,
                                                                           MavenArtifactUtils.convertArtifactToPath(artifact));

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

    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @Test
    public void testArtifactResolutionFromGroup(@MavenRepository(repositoryId = TARFG_RELEASES)
                                                @RepositoryAttributes(allowsDelete =  false)
                                                Repository repository,
                                                @Group({ TARFG_RELEASES })
                                                @MavenRepository(repositoryId = TARFG_GROUP)
                                                @RepositoryAttributes(allowsDelete =  false,
                                                                      allowsRedeployment = false)
                                                Repository repositoryGroup,
                                                @MavenTestArtifact(repositoryId = TARFG_RELEASES,
                                                                   id = "org.carlspring.strongbox:strongbox-utils",
                                                                   versions = { "8.0.5" })
                                                Path artifactPath)
            throws Exception
    {
        RepositoryPath path = (RepositoryPath) artifactPath.normalize();

        try (InputStream is = artifactResolutionService.getInputStream(path))
        {
            assertThat(is).as("Failed to resolve artifact from group repository!").isNotNull();
            assertThat(is.available() > 0).as("Failed to resolve artifact from group repository!").isTrue();
        }
    }

    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @Test
    public void testForceDelete(@MavenRepository(repositoryId = TFD_RELEASE_WITHOUT_DELETE)
                                Repository repository1,
                                @MavenTestArtifact(repositoryId = TFD_RELEASE_WITHOUT_DELETE,
                                                   id = "org.carlspring.strongbox:strongbox-utils",
                                                   versions = { "7.0" })
                                Path artifactPath1,
                                @MavenRepository(repositoryId = TFD_RELEASE_WITH_TRASH)
                                @RepositoryAttributes(trashEnabled = true)
                                Repository repository2,
                                @MavenTestArtifact(repositoryId = TFD_RELEASE_WITH_TRASH,
                                                   id = "org.carlspring.strongbox:strongbox-utils",
                                                   versions = { "7.2" })
                                Path artifactPath2)
            throws Exception
    {

        RepositoryPath artifactRepositoryPath = (RepositoryPath) artifactPath1.normalize();

        mavenArtifactManagementService.delete(artifactRepositoryPath, true);

        assertThat(Files.notExists(artifactPath1)).as("Failed to delete artifact during a force delete operation!").isTrue();

        final String artifactPathStr2 = "org/carlspring/strongbox/strongbox-utils/7.2/strongbox-utils-7.2.jar";

        artifactRepositoryPath = (RepositoryPath) artifactPath2.normalize();

        mavenArtifactManagementService.delete(artifactRepositoryPath, true);

        final Path repositoryTrashPath = repositoryPathResolver.resolve(repository2).resolve(".trash");

        assertThat(Files.exists(repositoryTrashPath.resolve(artifactPathStr2)))
                .as("Should have moved the artifact to the trash during a force delete operation, " +
                        "when allowsForceDeletion is not enabled!")
                .isTrue();
    }

    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @Test
    public void testRemoveTimestampedSnapshotsByNumberToKeep(@MavenRepository(repositoryId = TRTS_SNAPSHOTS,
                                                                              policy = RepositoryPolicyEnum.SNAPSHOT)
                                                             Repository repository,
                                                             @MavenTestArtifact(repositoryId = TRTS_SNAPSHOTS,
                                                                                id = "org.carlspring.strongbox:timestamped",
                                                                                versions = { "2.0-20190701.190020-1",
                                                                                             "2.0-20190701.190145-2",
                                                                                             "2.0-20190701.190250-3"})
                                                             List<Path> artifactPaths)
            throws Exception
    {
        final String storageId = repository.getStorage().getId();
        final String repositoryId = repository.getId();

        Path artifactVersionBasePath = artifactPaths.get(0).getParent().normalize();

        try (Stream<Path> pathStream = Files.walk(artifactVersionBasePath))
        {
            long timestampedSnapshots = pathStream.filter(path -> path.toString().endsWith(".jar")).count();
            assertThat(timestampedSnapshots).as("Amount of timestamped snapshots doesn't equal 3.").isEqualTo(3);
        }

        artifactMetadataService.rebuildMetadata(storageId,
                                                repositoryId,
                                                "org/carlspring/strongbox/timestamped");

        //To check removing timestamped snapshot with numberToKeep = 1
        mavenRepositoryFeatures.removeTimestampedSnapshots(storageId,
                                                           repositoryId,
                                                           "org/carlspring/strongbox/timestamped",
                                                           1,
                                                           0);

        try (Stream<Path> pathStream = Files.walk(artifactVersionBasePath))
        {
            long timestampedSnapshots = pathStream.filter(path -> path.toString().endsWith(".jar")).count();
            assertThat(timestampedSnapshots).as("Amount of timestamped snapshots doesn't equal 1.").isEqualTo(1);

            Path snapshotArtifactPath = getSnapshotArtifactPath(artifactVersionBasePath);
            assertThat(snapshotArtifactPath).isNotNull();
            assertThat(snapshotArtifactPath.toString().endsWith("-3.jar")).isTrue();
        }
    }

    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @Test
    public void testRemoveTimestampedSnapshotsWithPreviousDate(@MavenRepository(repositoryId = TRTS_SNAPSHOTS,
                                                                                policy = RepositoryPolicyEnum.SNAPSHOT)
                                                               Repository repository,
                                                               @MavenTestArtifact(repositoryId = TRTS_SNAPSHOTS,
                                                                                  id = "org.carlspring.strongbox:timestamped",
                                                                                  versions = { "2.0-20190626.190020-1",
                                                                                               "2.0-20190626.190145-2"})
                                                               List<Path> artifactPaths)
            throws Exception
    {
        final String storageId = repository.getStorage().getId();
        final String repositoryId = repository.getId();

        RepositoryPath artifactVersionBasePath = (RepositoryPath) artifactPaths.get(1).getParent().normalize();

        artifactMetadataService.rebuildMetadata(storageId,
                                                repositoryId,
                                                "org/carlspring/strongbox/timestamped");

        try (Stream<Path> pathStream = Files.walk(artifactVersionBasePath))
        {
            long timestampedSnapshots = pathStream.filter(path -> path.toString().endsWith(".jar")).count();
            assertThat(timestampedSnapshots).as("Amount of timestamped snapshots doesn't equal 2.").isEqualTo(2);
        }

        SimpleDateFormat formatter = new SimpleDateFormat(MavenSnapshotManager.TIMESTAMP_FORMAT);
        Date keepDate = Date.from(LocalDate.now().minusDays(5).atStartOfDay(ZoneId.systemDefault()).toInstant());

        // To check removing timestamped snapshot with keepDate and numberToKeep = 0
        mavenRepositoryFeatures.removeTimestampedSnapshots(storageId,
                                                           repositoryId,
                                                           "org/carlspring/strongbox/timestamped",
                                                           0,
                                                           keepDate);

        // Check all remaining artifacts from the repository.
        Path repositoryPath = repositoryPathResolver.resolve(repository);
        try (Stream<Path> pathStream = Files.walk(repositoryPath))
        {
            long timestampedSnapshots = pathStream.filter(path -> path.toString().endsWith(".jar")).count();
            assertThat(timestampedSnapshots).as("Amount of timestamped snapshots doesn't equal 0.").isZero();

            Path snapshotArtifactPath = getSnapshotArtifactPath(repositoryPath);
            assertThat(snapshotArtifactPath).isNull();
        }
    }

    private Path getSnapshotArtifactPath(Path artifactPath)
            throws IOException
    {
        Path path = null;

        try (Stream<Path> pathStream = Files.walk(artifactPath))
        {
            Optional<Path> optionalPath = pathStream.filter(p -> p.toString().endsWith(".jar")).findFirst();
            if (optionalPath.isPresent())
            {
                path = optionalPath.get();
            }
        }

        return path;
    }

    @ExtendWith({ RepositoryManagementTestExecutionListener.class })
    @Test
    public void testConcurrentReadWrite(@MavenRepository(repositoryId = TCRW_RELEASES_WITH_LOCK)
                                        Repository repository)
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
        CountDownLatch storedSync = new CountDownLatch(1);
        List<Long> resultList = IntStream.range(0, concurrency * 2)
                                         .parallel()
                                         .mapToObj(i -> getResult(i,
                                                                  storedSync,
                                                                  repositoryPath,
                                                                  loremIpsumContentArray))
                                         .collect(Collectors.toList());

        // then
        for (int i = 0; i < resultList.size(); i++)
        {
            String message = String.format("Operation [%s:%s] content size don't match.",
                                          i % 2 == 0 ? "write" : "read", i);
            assertThat(Long.valueOf(CONTENT_SIZE)).as(message).isEqualTo(resultList.get(i));
        }

        RepositoryPath repositoryPathResult = repositoryPathResolver.resolve(repository, path);
        org.carlspring.strongbox.domain.Artifact artifactEntry = repositoryPathResult.getArtifactEntry();

        assertThat(artifactEntry).isNotNull();
        assertThat(artifactEntry.getDownloadCount()).isEqualTo(Integer.valueOf(concurrency));

        byte[] repositoryPathContent = Files.readAllBytes(repositoryPath);

        assertThat(Arrays.stream(loremIpsumContentArray)
                         .map(c -> Arrays.equals(repositoryPathContent, c))
                         .reduce((r1, r2) -> r1 || r2)
                         .get())
                .isTrue();

    }

    @ExtendWith({ RepositoryManagementTestExecutionListener.class })
    @Test
    public void testLastVersionManagement(@MavenRepository(repositoryId = LAST_VERSION_RELEASES)
                                          Repository repository)
            throws Exception
    {
        final String storageId = repository.getStorage().getId();
        final String repositoryId = repository.getId();

        // store the file without classifier
        String gavtc = "org.carlspring.strongbox:strongbox-lv-artifact:1.0:jar";
        Artifact artifact = MavenArtifactTestUtils.getArtifactFromGAVTC(gavtc);
        String artifactPath = MavenArtifactUtils.convertArtifactToPath(artifact);

        try (InputStream is = new ByteArrayInputStream("strongbox-lv-artifact-content".getBytes(StandardCharsets.UTF_8)))
        {
            RepositoryPath repositoryPath = repositoryPathResolver.resolve(storageId,
                                                                           repositoryId,
                                                                           artifactPath);
            mavenArtifactManagementService.validateAndStore(repositoryPath, is);
        }

        // confirm it has last-version tag
        org.carlspring.strongbox.domain.Artifact artifactEntry = artifactEntityRepository.findOneArtifact(storageId,
                                                                                                          repositoryId,
                                                                                                          artifactPath);
        assertThat(artifactEntry.getTagSet()).isNotNull();
        assertThat(artifactEntry.getTagSet()).hasSize(1);
        assertThat(artifactEntry.getTagSet().iterator().next().getName()).isEqualTo(ArtifactTag.LAST_VERSION);

        // store the file with classifier
        String gavtcWithClassifier = "org.carlspring.strongbox:strongbox-lv-artifact:1.0:jar:sources";
        Artifact artifactWithClassifier = MavenArtifactTestUtils.getArtifactFromGAVTC(gavtcWithClassifier);
        String artifactPathWithClassifier = MavenArtifactUtils.convertArtifactToPath(artifactWithClassifier);

        try (InputStream is = new ByteArrayInputStream("strongbox-lv-artifact-content".getBytes(StandardCharsets.UTF_8)))
        {
            RepositoryPath repositoryPath = repositoryPathResolver.resolve(storageId,
                                                                           repositoryId,
                                                                           artifactPathWithClassifier);
            mavenArtifactManagementService.validateAndStore(repositoryPath, is);
        }

        // confirm it has last-version tag
        org.carlspring.strongbox.domain.Artifact artifactEntryWithClassifier = artifactEntityRepository.findOneArtifact(storageId,
                                                                                                                        repositoryId,
                                                                                                                        artifactPathWithClassifier);

        assertThat(artifactEntryWithClassifier.getTagSet()).isNotNull();
        assertThat(artifactEntryWithClassifier.getTagSet()).hasSize(1);
        assertThat(artifactEntryWithClassifier.getTagSet().iterator().next().getName()).isEqualTo(ArtifactTag.LAST_VERSION);

        // re-fetch the artifact without classifier
        // and confirm it still has the last version tag
        artifactEntry = artifactEntityRepository.findOneArtifact(storageId,
                                                             repositoryId,
                                                             artifactPath);
        assertThat(artifactEntry.getTagSet()).isNotNull();
        assertThat(artifactEntry.getTagSet()).hasSize(1);
        assertThat(artifactEntry.getTagSet().iterator().next().getName()).isEqualTo(ArtifactTag.LAST_VERSION);

        // store the newest version of file without classifier
        String gavtcV2 = "org.carlspring.strongbox:strongbox-lv-artifact:2.0:jar";
        Artifact artifactV2 = MavenArtifactTestUtils.getArtifactFromGAVTC(gavtcV2);
        String artifactPathV2 = MavenArtifactUtils.convertArtifactToPath(artifactV2);

        try (InputStream is = new ByteArrayInputStream("strongbox-lv-artifact-content".getBytes(StandardCharsets.UTF_8)))
        {
            RepositoryPath repositoryPath = repositoryPathResolver.resolve(storageId,
                                                                           repositoryId,
                                                                           artifactPathV2);
            mavenArtifactManagementService.validateAndStore(repositoryPath, is);
        }

        // confirm it has last-version tag
        org.carlspring.strongbox.domain.Artifact artifactEntryV2 = artifactEntityRepository.findOneArtifact(storageId,
                                                                                                            repositoryId,
                                                                                                            artifactPathV2);

        assertThat(artifactEntryV2.getTagSet()).isNotNull();
        assertThat(artifactEntryV2.getTagSet()).hasSize(1);
        assertThat(artifactEntryV2.getTagSet().iterator().next().getName()).isEqualTo(ArtifactTag.LAST_VERSION);

        // re-fetch the artifact without classifier
        // and confirm it no longer has the last version tag
        artifactEntry = artifactEntityRepository.findOneArtifact(storageId,
                                                             repositoryId,
                                                             artifactPath);
        assertThat(artifactEntry.getTagSet()).isNotNull();
        assertThat(artifactEntry.getTagSet()).isEmpty();

        // confirm it no longer has last-version tag
        artifactEntryWithClassifier = artifactEntityRepository.findOneArtifact(storageId,
                                                                           repositoryId,
                                                                           artifactPathWithClassifier);

        assertThat(artifactEntryWithClassifier.getTagSet()).isNotNull();
        assertThat(artifactEntryWithClassifier.getTagSet()).isEmpty();
    }

    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @Test
    public void testChecksumsStorage(@MavenRepository(repositoryId = "checksums-storage")
                                     Repository repository,
                                     @MavenTestArtifact(resource = "org/carlspring/strongbox/strongbox-checksum-test/8.4/strongbox-checksum-test-8.4.jar")
                                     Path artifact)
            throws Exception
    {
        final String storageId = repository.getStorage().getId();
        final String repositoryId = repository.getId();

        String artifactPathStr = "org/carlspring/strongbox/strongbox-checksum-test/8.4/strongbox-checksum-test-8.4.jar";
        RepositoryPath repositoryPath = repositoryPathResolver.resolve(repository).resolve(artifactPathStr);

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
        org.carlspring.strongbox.domain.Artifact artifactEntry = artifactEntityRepository.findOneArtifact(storageId,
                                                                                                          repositoryId,
                                                                                                          path);

        assertThat(artifactEntry).isNotNull();

        Map<String, String> actualChecksums = artifactEntry.getChecksums();

        assertThat(actualChecksums).isNotNull();
        assertThat(actualChecksums).isEqualTo(expectedChecksums);
    }


    private Long getResult(int i,
                           CountDownLatch storedSync, 
                           RepositoryPath repositoryPath,
                           byte[][] loremIpsumContentArray)
    {
        try
        {
            Repository repository = repositoryPath.getRepository();
            String path = RepositoryFiles.relativizePath(repositoryPath);
            return i % 2 == 0 ?
                   new Store(storedSync, new ByteArrayInputStream(loremIpsumContentArray[i / 2]), repository, path).call() :
                   new Fetch(storedSync, repository, path).call();
        }
        catch (Exception e)
        {
            logger.error("Unexpected Exception while getting result:", e);
            return 0L;
        }
    }

    private class Store
            implements Callable<Long>
    {

        private CountDownLatch storedSync = new CountDownLatch(1);
        
        private final Repository repository;

        private final String path;

        private final InputStream is;

        private Store(CountDownLatch storedSync, InputStream is,
                      Repository repository,
                      String path)
        {
            this.storedSync = storedSync;
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
                long result = mavenArtifactManagementService.store(repositoryPath, is);
                storedSync.countDown();
                
                return result;
            }
            catch (Exception ex)
            {
                logger.error("Failed to store artifact [{}]", repositoryPath, ex);

                return 0L;
            }
        }
    }

    private class Fetch
            implements Callable<Long>
    {

        private final CountDownLatch storedSync;
        private final Repository repository;
        private final String path;

        private Fetch(CountDownLatch storedSync, Repository repository,
                      String path)
        {
            this.storedSync = storedSync;
            this.path = path;
            this.repository = repository;
        }

        @Override
        public Long call()
        {
            try
            {
                storedSync.await();
            }
            catch (InterruptedException e)
            {
                logger.error("Failed to read artifact [{}]", path, e);
                
                return 0L;
            }
            
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
            catch (Exception ex)
            {
                logger.error("Failed to read artifact [{}]", repositoryPath, ex);
                
                return 0L;
            }

            return result;
        }
    }

}
