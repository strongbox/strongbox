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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.LinkedHashSet;
import java.util.List;
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
import org.carlspring.strongbox.config.Maven2LayoutProviderTestConfig;
import org.carlspring.strongbox.domain.ArtifactEntry;
import org.carlspring.strongbox.providers.io.RepositoryFiles;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.io.RepositoryPathResolver;
import org.carlspring.strongbox.providers.io.RepositoryStreamSupport.RepositoryInputStream;
import org.carlspring.strongbox.providers.layout.Maven2LayoutProvider;
import org.carlspring.strongbox.repository.MavenRepositoryFeatures;
import org.carlspring.strongbox.resource.ResourceCloser;
import org.carlspring.strongbox.storage.ArtifactResolutionException;
import org.carlspring.strongbox.storage.ArtifactStorageException;
import org.carlspring.strongbox.storage.repository.MavenRepositoryFactory;
import org.carlspring.strongbox.storage.repository.MutableRepository;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.RepositoryPolicyEnum;
import org.carlspring.strongbox.storage.repository.RepositoryTypeEnum;
import org.carlspring.strongbox.testing.TestCaseWithMavenArtifactGenerationAndIndexing;

import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
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

    private Set<MutableRepository> getRepositories(TestInfo testInfo)
    {
        Set<MutableRepository> repositories = new LinkedHashSet<>();
        repositories.add(createRepositoryMock(STORAGE0,
                                              getRepositoryName("amsi-releases-without-deployment", testInfo),
                                              Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0,
                                              getRepositoryName("amsi-releases-without-redeployment", testInfo),
                                              Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0,
                                              getRepositoryName("amsi-releases-without-deletes", testInfo),
                                              Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0,
                                              getRepositoryName("tdradagr-releases", testInfo),
                                              Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0,
                                              getRepositoryName("tdradagr-group", testInfo),
                                              Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0,
                                              getRepositoryName("tarfg-releases", testInfo),
                                              Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0,
                                              getRepositoryName("tarfg-group", testInfo),
                                              Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0,
                                              getRepositoryName("tfd-release-with-trash", testInfo),
                                              Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0,
                                              getRepositoryName("tfd-release-without-delete", testInfo),
                                              Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0,
                                              getRepositoryName("trts-snapshots", testInfo),
                                              Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0,
                                              getRepositoryName("tcrw-releases-with-lock", testInfo),
                                              Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0,
                                              getRepositoryName("last-version-releases", testInfo),
                                              Maven2LayoutProvider.ALIAS));

        return repositories;
    }

    @AfterEach
    public void removeRepositories(TestInfo testInfo)
            throws IOException, JAXBException
    {
        removeRepositories(getRepositories(testInfo));
    }

    @Test
    public void testDeploymentToRepositoryWithForbiddenDeployments(TestInfo testInfo)
            throws Exception
    {
        String repositoryId = getRepositoryName("amsi-releases-without-deployment", testInfo);

        MutableRepository repositoryWithoutDeployment = mavenRepositoryFactory.createRepository(repositoryId);
        repositoryWithoutDeployment.setAllowsDelete(false);
        repositoryWithoutDeployment.setLayout(Maven2LayoutProvider.ALIAS);
        repositoryWithoutDeployment.setAllowsDeployment(false);

        createRepositoryWithArtifacts(STORAGE0,
                                      repositoryWithoutDeployment,
                                      "org.carlspring.strongbox:strongbox-utils",
                                      "8.0");

        InputStream is = null;

        //noinspection EmptyCatchBlock
        try
        {
            String gavtc = "org.carlspring.strongbox:strongbox-utils:8.0:jar";

            File repositoryDir = getRepositoryBasedir(STORAGE0, repositoryId);
            is = generateArtifactInputStream(repositoryDir.toPath().getParent().toAbsolutePath().toString(),
                                             repositoryId,
                                             gavtc,
                                             true);

            Artifact artifact = ArtifactUtils.getArtifactFromGAVTC(gavtc);
            mavenArtifactManagementService.validateAndStore(STORAGE0,
                                                            repositoryId,
                                                            ArtifactUtils.convertArtifactToPath(artifact),
                                                            is);

            fail("Failed to deny artifact operation for repository with disallowed deployments.");
        }
        catch (ArtifactStorageException e)
        {
            // This is the expected correct behavior
        }
        finally
        {
            ResourceCloser.close(is, null);
        }
    }

    @Test
    public void testRedeploymentToRepositoryWithForbiddenRedeployments(TestInfo testInfo)
            throws Exception
    {
        String repositoryId = getRepositoryName("amsi-releases-without-redeployment", testInfo);

        MutableRepository repositoryWithoutRedeployments = mavenRepositoryFactory.createRepository(repositoryId);
        repositoryWithoutRedeployments.setAllowsRedeployment(false);

        createRepositoryWithArtifacts(STORAGE0,
                                      repositoryWithoutRedeployments,
                                      "org.carlspring.strongbox:strongbox-utils",
                                      "8.1");

        InputStream is = null;

        //noinspection EmptyCatchBlock
        try
        {
            String gavtc = "org.carlspring.strongbox:strongbox-utils:8.1:jar";

            File repositoryBasedir = getRepositoryBasedir(STORAGE0, repositoryId);
            generateArtifact(repositoryBasedir.getAbsolutePath(), gavtc);

            is = generateArtifactInputStream(repositoryBasedir.toPath().getParent().toAbsolutePath().toString(),
                                             repositoryId,
                                             gavtc,
                                             true);

            Artifact artifact = ArtifactUtils.getArtifactFromGAVTC(gavtc);
            mavenArtifactManagementService.validateAndStore(STORAGE0,
                                                            repositoryId,
                                                            ArtifactUtils.convertArtifactToPath(artifact),
                                                            is);

            fail("Failed to deny artifact operation for repository with disallowed re-deployments.");
        }
        catch (ArtifactStorageException e)
        {
            // This is the expected correct behavior
        }
        finally
        {
            ResourceCloser.close(is, null);
        }
    }

    @Test
    public void testDeletionFromRepositoryWithForbiddenDeletes(TestInfo testInfo)
            throws Exception
    {
        String repositoryId = getRepositoryName("amsi-releases-without-deletes", testInfo);

        MutableRepository repositoryWithoutDelete = mavenRepositoryFactory.createRepository(repositoryId);
        repositoryWithoutDelete.setAllowsDelete(false);
        repositoryWithoutDelete.setLayout(Maven2LayoutProvider.ALIAS);

        createRepositoryWithArtifacts(STORAGE0,
                                      repositoryWithoutDelete,
                                      "org.carlspring.strongbox:strongbox-utils",
                                      "8.6");

        generateArtifact(getRepositoryBasedir(STORAGE0, repositoryId).getAbsolutePath(),
                         "org.carlspring.strongbox:strongbox-utils",
                         new String[]{ "8.2" });

        //noinspection EmptyCatchBlock
        try
        {
            String gavtc = "org.carlspring.strongbox:strongbox-utils:8.2:jar";

            Artifact artifact = ArtifactUtils.getArtifactFromGAVTC(gavtc);
            RepositoryPath repositoryPath = repositoryPathResolver.resolve(STORAGE0,
                                                                           repositoryId,
                                                                           ArtifactUtils.convertArtifactToPath(artifact));
            mavenArtifactManagementService.delete(repositoryPath,
                                                  false);

            fail("Failed to deny artifact operation for repository with disallowed deletions.");
        }
        catch (ArtifactStorageException e)
        {
            // This is the expected correct behavior
        }
    }

    @Test
    public void testDeploymentRedeploymentAndDeletionAgainstGroupRepository(TestInfo testInfo)
            throws Exception
    {
        // Test resource initialization start:
        String repositoryId = getRepositoryName("tdradagr-releases", testInfo);
        String repositoryGroupId = getRepositoryName("tdradagr-group", testInfo);

        MutableRepository repository = mavenRepositoryFactory.createRepository(repositoryId);
        repository.setAllowsDelete(false);
        repository.setLayout(Maven2LayoutProvider.ALIAS);

        MutableRepository repositoryGroup = mavenRepositoryFactory.createRepository(repositoryGroupId);
        repositoryGroup.setType(RepositoryTypeEnum.GROUP.getType());
        repositoryGroup.setAllowsRedeployment(false);
        repositoryGroup.setAllowsDelete(false);
        repositoryGroup.setAllowsForceDeletion(false);
        repositoryGroup.addRepositoryToGroup(repositoryId);

        createRepositoryWithArtifacts(STORAGE0,
                                      repository,
                                      "org.carlspring.strongbox:strongbox-utils",
                                      "8.3");

        createRepository(STORAGE0, repositoryGroup);
        // Test resource initialization end.

        InputStream is = null;

        String gavtc = "org.carlspring.strongbox:strongbox-utils:8.3:jar";

        Artifact artifact = ArtifactUtils.getArtifactFromGAVTC(gavtc);

        //noinspection EmptyCatchBlock
        try
        {
            File repositoryDir = getRepositoryBasedir(STORAGE0, repositoryGroupId);
            is = generateArtifactInputStream(repositoryDir.toPath().getParent().toAbsolutePath().toString(),
                                             repositoryGroupId,
                                             gavtc,
                                             true);

            mavenArtifactManagementService.validateAndStore(STORAGE0,
                                                            repositoryGroupId,
                                                            ArtifactUtils.convertArtifactToPath(artifact),
                                                            is);

            fail("Failed to deny artifact operation for repository with disallowed deployments.");
        }
        catch (ArtifactStorageException e)
        {
            // This is the expected correct behavior
        }
        finally
        {
            ResourceCloser.close(is, null);
        }

        //noinspection EmptyCatchBlock
        try
        {
            // Generate the artifact on the file-system anyway so that we could achieve
            // the state of having it there before attempting a re-deployment
            generateArtifact(getRepositoryBasedir(STORAGE0, repositoryId).getAbsolutePath(), gavtc);
            mavenArtifactManagementService.validateAndStore(STORAGE0,
                                                            repositoryGroupId,
                                                            ArtifactUtils.convertArtifactToPath(artifact),
                                                            is);

            fail("Failed to deny artifact operation for repository with disallowed re-deployments.");
        }
        catch (ArtifactStorageException e)
        {
            // This is the expected correct behavior
        }
        finally
        {
            ResourceCloser.close(is, null);
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
        finally
        {
            ResourceCloser.close(is, null);
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
        finally
        {
            ResourceCloser.close(is, null);
        }
    }

    @Test
    public void testArtifactResolutionFromGroup(TestInfo testInfo)
            throws Exception
    {
        String repositoryId = getRepositoryName("tarfg-releases", testInfo);
        String repositoryGroupId = getRepositoryName("tarfg-group", testInfo);

        MutableRepository repository = mavenRepositoryFactory.createRepository(repositoryId);
        repository.setAllowsDelete(false);
        repository.setLayout(Maven2LayoutProvider.ALIAS);

        MutableRepository repositoryGroup = mavenRepositoryFactory.createRepository(repositoryGroupId);
        repositoryGroup.setType(RepositoryTypeEnum.GROUP.getType());
        repositoryGroup.setAllowsRedeployment(false);
        repositoryGroup.setAllowsDelete(false);
        repositoryGroup.setAllowsForceDeletion(false);
        repositoryGroup.addRepositoryToGroup(repositoryId);

        createRepositoryWithArtifacts(STORAGE0,
                                      repository,
                                      "org.carlspring.strongbox:strongbox-utils",
                                      "8.0.5");

        createRepository(STORAGE0, repositoryGroup);

        RepositoryPath path = artifactResolutionService.resolvePath(STORAGE0,
                                                                    repositoryGroupId,
                                                                    "org/carlspring/strongbox/strongbox-utils/8.0.5/strongbox-utils-8.0.5.jar");

        try (InputStream is = artifactResolutionService.getInputStream(path))
        {
            assertNotNull(is, "Failed to resolve artifact from group repository!");
            assertTrue(is.available() > 0, "Failed to resolve artifact from group repository!");
        }
    }

    @Test
    public void testForceDelete(TestInfo testInfo)
            throws Exception
    {
        String repositoryId = getRepositoryName("tfd-release-without-delete", testInfo);
        String repositoryWithTrashId = getRepositoryName("tfd-release-with-trash", testInfo);

        createRepositoryWithArtifacts(STORAGE0, repositoryId, false, "org.carlspring.strongbox:strongbox-utils", "7.0");

        final String artifactPath = "org/carlspring/strongbox/strongbox-utils/7.0/strongbox-utils-7.0.jar";

        RepositoryPath repositoryPath = repositoryPathResolver.resolve(STORAGE0, repositoryId, artifactPath);

        mavenArtifactManagementService.delete(repositoryPath, true);

        assertFalse(new File(getRepositoryBasedir(STORAGE0, repositoryId), artifactPath).exists(),
                    "Failed to delete artifact during a force delete operation!");

        MutableRepository repositoryWithTrash = mavenRepositoryFactory.createRepository(repositoryWithTrashId);
        repositoryWithTrash.setTrashEnabled(true);
        repositoryWithTrash.setLayout(Maven2LayoutProvider.ALIAS);

        createRepository(STORAGE0, repositoryWithTrash);

        generateArtifact(getRepositoryBasedir(STORAGE0, repositoryWithTrashId).getAbsolutePath(),
                         "org.carlspring.strongbox:strongbox-utils",
                         new String[]{ "7.2" });

        final String artifactPath2 = "org/carlspring/strongbox/strongbox-utils/7.2/strongbox-utils-7.2.jar";
        repositoryPath = repositoryPathResolver.resolve(STORAGE0, repositoryWithTrashId, artifactPath2);

        mavenArtifactManagementService.delete(repositoryPath, true);

        final File repositoryDir = new File(getStorageBasedir(STORAGE0), repositoryWithTrashId + "/.trash");

        assertTrue(new File(repositoryDir, artifactPath2).exists(),
                   "Should have moved the artifact to the trash during a force delete operation, " +
                   "when allowsForceDeletion is not enabled!");
    }

    @Test
    public void testRemoveTimestampedSnapshots(TestInfo testInfo)
            throws Exception
    {
        String repositoryid = getRepositoryName("trts-snapshots", testInfo);

        MutableRepository repositoryWithSnapshots = mavenRepositoryFactory.createRepository(repositoryid);
        repositoryWithSnapshots.setPolicy(RepositoryPolicyEnum.SNAPSHOT.getPolicy());
        repositoryWithSnapshots.setLayout(Maven2LayoutProvider.ALIAS);

        createRepository(STORAGE0, repositoryWithSnapshots);

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

        assertEquals(2, artifactVersionBaseDir.listFiles(new JarFilenameFilter()).length,
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

    @Test
    public void testConcurrentReadWrite(TestInfo testInfo)
            throws Exception
    {
        String repositoryId = getRepositoryName("tcrw-releases-with-lock", testInfo);

        MutableRepository repositoryWithLock = mavenRepositoryFactory.createRepository(repositoryId);

        createRepository(STORAGE0, repositoryWithLock);

        Repository repository = getConfiguration().getStorage(STORAGE0).getRepository(repositoryId);

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
                         String.format("Operation [%s:%s] content size don't match.", i % 2 == 0 ? "write" : "read", i));
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

    @Test
    public void testLastVersionManagement(TestInfo testInfo)
            throws Exception
    {
        String repositoryId = getRepositoryName("last-version-releases", testInfo);

        MutableRepository repository = mavenRepositoryFactory.createRepository(repositoryId);
        repository.setLayout(Maven2LayoutProvider.ALIAS);
        repository.setType(RepositoryTypeEnum.HOSTED.getType());

        createRepository(STORAGE0, repository);

        // store the file without classifier
        String gavtc = "org.carlspring.strongbox:strongbox-lv-artifact:1.0:jar";
        Artifact artifact = ArtifactUtils.getArtifactFromGAVTC(gavtc);
        String artifactPath = ArtifactUtils.convertArtifactToPath(artifact);

        try (InputStream is = new ByteArrayInputStream("strongbox-lv-artifact-content".getBytes(StandardCharsets.UTF_8)))
        {
            mavenArtifactManagementService.validateAndStore(STORAGE0,
                                                            repositoryId,
                                                            artifactPath,
                                                            is);
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
            mavenArtifactManagementService.validateAndStore(STORAGE0,
                                                            repositoryId,
                                                            artifactPathWithClassifier,
                                                            is);
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
            mavenArtifactManagementService.validateAndStore(STORAGE0,
                                                            repositoryId,
                                                            artifactPathV2,
                                                            is);
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

    private class Store implements Callable<Long>
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

    private class Fetch implements Callable<Long>
    {

        private final Repository repository;
        private final String path;
        private int attempts = 0;

        private Fetch(Repository repository, String path)
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
