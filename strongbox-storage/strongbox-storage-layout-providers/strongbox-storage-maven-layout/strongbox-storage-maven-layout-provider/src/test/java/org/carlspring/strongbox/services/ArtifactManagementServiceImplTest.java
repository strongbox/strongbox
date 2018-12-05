package org.carlspring.strongbox.services;

import org.carlspring.maven.commons.io.filters.JarFilenameFilter;
import org.carlspring.maven.commons.util.ArtifactUtils;
import org.carlspring.strongbox.artifact.ArtifactNotFoundException;
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
import org.carlspring.strongbox.storage.repository.*;
import org.carlspring.strongbox.testing.TestCaseWithMavenArtifactGenerationAndIndexing;

import javax.inject.Inject;
import javax.xml.bind.JAXBException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.maven.artifact.Artifact;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

/**
 * @author mtodorov
 */
@ExtendWith(SpringExtension.class)
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
    private ArtifactResolutionService artifactResolutionService;
    
    @Inject
    private MavenRepositoryFeatures mavenRepositoryFeatures;

    @Inject
    private ArtifactMetadataService artifactMetadataService;

    @Inject
    private MavenRepositoryFactory mavenRepositoryFactory;
    
    @Inject
    private RepositoryPathResolver repositoryPathResolver;

    @BeforeAll
    public static void cleanUp()
            throws Exception
    {
        cleanUp(getRepositoriesToClean());
    }

    public static Set<MutableRepository> getRepositoriesToClean()
    {
        Set<MutableRepository> repositories = new LinkedHashSet<>();
        repositories.add(createRepositoryMock(STORAGE0, "amsi-releases-without-deployment", Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0, "amsi-releases-without-redeployment", Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0, "amsi-releases-without-deletes", Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0, "tdradagr-releases", Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0, "tdradagr-group", Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0, "tarfg-releases", Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0, "tarfg-group", Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0, "tfd-release-with-trash", Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0, "tfd-release-without-delete", Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0, "trts-snapshots", Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0, "tcrw-releases-with-lock", Maven2LayoutProvider.ALIAS));

        return repositories;
    }

    @AfterEach
    public void removeRepositories()
            throws IOException, JAXBException
    {
        removeRepositories(getRepositoriesToClean());
    }

    @Test
    public void testDeploymentToRepositoryWithForbiddenDeployments()
            throws Exception
    {
        String repositoryId = "amsi-releases-without-deployment";

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
    public void testRedeploymentToRepositoryWithForbiddenRedeployments()
            throws Exception
    {
        String repositoryId = "amsi-releases-without-redeployment";

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
    public void testDeletionFromRepositoryWithForbiddenDeletes()
            throws Exception
    {
        String repositoryId = "amsi-releases-without-deletes";

        MutableRepository repositoryWithoutDelete = mavenRepositoryFactory.createRepository(repositoryId);
        repositoryWithoutDelete.setAllowsDelete(false);
        repositoryWithoutDelete.setLayout(Maven2LayoutProvider.ALIAS);

        createRepositoryWithArtifacts(STORAGE0,
                                      repositoryWithoutDelete,
                                      "org.carlspring.strongbox:strongbox-utils",
                                      "8.0");

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
    public void testDeploymentRedeploymentAndDeletionAgainstGroupRepository()
            throws Exception
    {
        // Test resource initialization start:
        String repositoryId = "tdradagr-releases";
        String repositoryGroupId = "tdradagr-group";

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
    public void testArtifactResolutionFromGroup()
            throws Exception
    {
        String repositoryId = "tarfg-releases";
        String repositoryGroupId = "tarfg-group";

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
    public void testForceDelete()
            throws Exception
    {
        String repositoryId = "tfd-release-without-delete";
        String repositoryWithTrashId = "tfd-release-with-trash";

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
    public void testRemoveTimestampedSnapshots()
            throws Exception
    {
        String repositoryid = "trts-snapshots";

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
    public void testConcurrentReadWrite()
            throws Exception
    {
        String repositoryId = "tcrw-releases-with-lock";

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
        List<Long> resultList = IntStream.range(0, concurrency * 2)
                                         .parallel()
                                         .mapToObj(i -> getResult(i,
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

    private Long getResult(int i,
                           RepositoryPath repositoryPath,
                           byte[][] loremIpsumContentArray)
    {
        try
        {
            Repository repository = repositoryPath.getRepository();
            String path = RepositoryFiles.relativizePath(repositoryPath);

            return i % 2 == 0
                    ? new Store(new ByteArrayInputStream(loremIpsumContentArray[i / 2]), repository, path).call()
                    : new Fetch(repository, path).call();
        }
        catch (IOException e)
        {
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
                
                if (attempts++ > 3) {
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
