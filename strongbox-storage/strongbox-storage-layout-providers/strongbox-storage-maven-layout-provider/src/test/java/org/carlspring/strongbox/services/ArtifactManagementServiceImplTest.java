package org.carlspring.strongbox.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.inject.Inject;
import javax.xml.bind.JAXBException;

import org.apache.maven.artifact.Artifact;
import org.carlspring.maven.commons.io.filters.JarFilenameFilter;
import org.carlspring.maven.commons.util.ArtifactUtils;
import org.carlspring.strongbox.artifact.ArtifactNotFoundException;
import org.carlspring.strongbox.client.ArtifactTransportException;
import org.carlspring.strongbox.config.Maven2LayoutProviderTestConfig;
import org.carlspring.strongbox.domain.ArtifactEntry;
import org.carlspring.strongbox.providers.ProviderImplementationException;
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
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author mtodorov
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = Maven2LayoutProviderTestConfig.class)
public class ArtifactManagementServiceImplTest
        extends TestCaseWithMavenArtifactGenerationAndIndexing
{

    private static final Logger logger = LoggerFactory.getLogger(ArtifactManagementServiceImplTest.class);
    
    private static final int CONTENT_SIZE = 40000;

    private static final String REPOSITORY_RELEASES = "amsi-releases";

    private static final String REPOSITORY_RELEASES_WITH_TRASH = "amsi-releases-with-trash";

    private static final String REPOSITORY_RELEASES_WITHOUT_DEPLOYMENT = "amsi-releases-without-deployment";

    private static final String REPOSITORY_RELEASES_WITHOUT_REDEPLOYMENT = "amsi-releases-without-redeployment";

    private static final String REPOSITORY_RELEASES_WITHOUT_DELETE = "amsi-releases-without-delete";

    private static final String REPOSITORY_SNAPSHOTS = "amsi-snapshots";

    private static final String REPOSITORY_WITH_LOCK = "repository-with-lock";

    private static final String REPOSITORY_GROUP = "amsi-group";

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

    @BeforeClass
    public static void cleanUp()
            throws Exception
    {
        cleanUp(getRepositoriesToClean());
    }

    public static Set<MutableRepository> getRepositoriesToClean()
    {
        Set<MutableRepository> repositories = new LinkedHashSet<>();
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_RELEASES, Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_RELEASES_WITHOUT_DEPLOYMENT, Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_RELEASES_WITHOUT_REDEPLOYMENT, Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_RELEASES_WITH_TRASH, Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_RELEASES_WITHOUT_DELETE, Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_SNAPSHOTS, Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_GROUP, Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_WITH_LOCK, Maven2LayoutProvider.ALIAS));

        return repositories;
    }

    @After
    public void removeRepositories()
            throws IOException, JAXBException
    {
        removeRepositories(getRepositoriesToClean());
    }

    @Before
    public void initialize()
            throws Exception
    {
        // Used by testDeploymentToRepositoryWithForbiddenDeployments()
        MutableRepository repositoryWithoutDelete = mavenRepositoryFactory.createRepository(REPOSITORY_RELEASES_WITHOUT_DELETE);
        repositoryWithoutDelete.setAllowsDelete(false);
        repositoryWithoutDelete.setLayout(Maven2LayoutProvider.ALIAS);

        createRepositoryWithArtifacts(STORAGE0,
                                      repositoryWithoutDelete,
                                      "org.carlspring.strongbox:strongbox-utils",
                                      "8.0");

        // Used by testRedeploymentToRepositoryWithForbiddenRedeployments()
        MutableRepository repositoryWithoutRedeployments = mavenRepositoryFactory.createRepository(REPOSITORY_RELEASES_WITHOUT_REDEPLOYMENT);
        repositoryWithoutRedeployments.setAllowsRedeployment(false);

        createRepositoryWithArtifacts(STORAGE0,
                                      repositoryWithoutRedeployments,
                                      "org.carlspring.strongbox:strongbox-utils",
                                      "8.1");

        // Used by testDeletionFromRepositoryWithForbiddenDeletes()
        generateArtifact(getRepositoryBasedir(STORAGE0, REPOSITORY_RELEASES_WITHOUT_DELETE).getAbsolutePath(),
                         "org.carlspring.strongbox:strongbox-utils",
                         new String[]{ "8.2" });

        // Used by:
        // - testForceDelete()
        // - testArtifactResolutionFromGroup()
        // - testDeploymentRedeploymentAndDeletionAgainstGroupRepository()
        createRepositoryWithArtifacts(STORAGE0,
                                      REPOSITORY_RELEASES,
                                      false,
                                      "org.carlspring.strongbox:strongbox-utils",
                                      "7.0", // Used by testForceDelete()
                                      "7.3"  // Used by testArtifactResolutionFromGroup()
        );

        MutableRepository repositoryGroup = mavenRepositoryFactory.createRepository(REPOSITORY_GROUP);
        repositoryGroup.setType(RepositoryTypeEnum.GROUP.getType());
        repositoryGroup.setAllowsRedeployment(false);
        repositoryGroup.setAllowsDelete(false);
        repositoryGroup.setAllowsForceDeletion(false);
        repositoryGroup.addRepositoryToGroup(REPOSITORY_RELEASES);

        createRepositoryWithArtifacts(STORAGE0,
                                      repositoryGroup,
                                      "org.carlspring.strongbox:strongbox-utils",
                                      "8.2" // Used by testDeploymentRedeploymentAndDeletionAgainstGroupRepository()
        );

        // Used by testForceDelete()
        MutableRepository repositoryWithTrash = mavenRepositoryFactory.createRepository(REPOSITORY_RELEASES_WITH_TRASH);
        repositoryWithTrash.setTrashEnabled(true);

        createRepositoryWithArtifacts(STORAGE0,
                                      repositoryWithTrash,
                                      "org.carlspring.strongbox:strongbox-utils",
                                      "7.2");

        // Used by testRemoveTimestampedSnapshots()
        MutableRepository repositorySnapshots = mavenRepositoryFactory.createRepository(REPOSITORY_SNAPSHOTS);
        repositorySnapshots.setPolicy(RepositoryPolicyEnum.SNAPSHOT.getPolicy());

        createRepository(STORAGE0, repositorySnapshots);

        //
        MutableRepository repositoryWithLock = mavenRepositoryFactory.createRepository(REPOSITORY_WITH_LOCK);
        createRepository(STORAGE0, repositoryWithLock);


        //
        MutableRepository releasesWithoutDeployment = mavenRepositoryFactory.createRepository(
                REPOSITORY_RELEASES_WITHOUT_DEPLOYMENT);
        releasesWithoutDeployment.setAllowsDeployment(false);
        releasesWithoutDeployment.setLayout(Maven2LayoutProvider.ALIAS);

        createRepository(STORAGE0, releasesWithoutDeployment);
    }

    @Test
    public void testDeploymentToRepositoryWithForbiddenDeployments()
            throws Exception
    {
        InputStream is = null;

        //noinspection EmptyCatchBlock
        try
        {
            String gavtc = "org.carlspring.strongbox:strongbox-utils:8.0:jar";

            File repositoryDir = getRepositoryBasedir(STORAGE0, REPOSITORY_RELEASES_WITHOUT_DEPLOYMENT);
            is = generateArtifactInputStream(repositoryDir.toPath().getParent().toAbsolutePath().toString(),
                                             REPOSITORY_RELEASES_WITHOUT_DEPLOYMENT,
                                             gavtc,
                                             true);

            Artifact artifact = ArtifactUtils.getArtifactFromGAVTC(gavtc);
            mavenArtifactManagementService.validateAndStore(STORAGE0,
                                                            REPOSITORY_RELEASES_WITHOUT_DEPLOYMENT,
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
        InputStream is = null;

        //noinspection EmptyCatchBlock
        try
        {
            String gavtc = "org.carlspring.strongbox:strongbox-utils:8.1:jar";

            File repositoryBasedir = getRepositoryBasedir(STORAGE0, REPOSITORY_RELEASES_WITHOUT_REDEPLOYMENT);
            generateArtifact(repositoryBasedir.getAbsolutePath(), gavtc);

            is = generateArtifactInputStream(repositoryBasedir.toPath().getParent().toAbsolutePath().toString(),
                                             REPOSITORY_RELEASES_WITHOUT_REDEPLOYMENT,
                                             gavtc,
                                             true);

            Artifact artifact = ArtifactUtils.getArtifactFromGAVTC(gavtc);
            mavenArtifactManagementService.validateAndStore(STORAGE0,
                                                            REPOSITORY_RELEASES_WITHOUT_REDEPLOYMENT,
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
            throws IOException
    {
        //noinspection EmptyCatchBlock
        try
        {
            String gavtc = "org.carlspring.strongbox:strongbox-utils:8.2:jar";

            Artifact artifact = ArtifactUtils.getArtifactFromGAVTC(gavtc);
            RepositoryPath repositoryPath = repositoryPathResolver.resolve(STORAGE0, REPOSITORY_RELEASES_WITHOUT_DELETE, ArtifactUtils.convertArtifactToPath(artifact));
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
        InputStream is = null;

        String gavtc = "org.carlspring.strongbox:strongbox-utils:8.3:jar";

        Artifact artifact = ArtifactUtils.getArtifactFromGAVTC(gavtc);

        //noinspection EmptyCatchBlock
        try
        {
            File repositoryDir = getRepositoryBasedir(STORAGE0, REPOSITORY_GROUP);
            is = generateArtifactInputStream(repositoryDir.toPath().getParent().toAbsolutePath().toString(),
                                             REPOSITORY_GROUP, gavtc, true);

            mavenArtifactManagementService.validateAndStore(STORAGE0,
                                                            REPOSITORY_GROUP,
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
            generateArtifact(getRepositoryBasedir(STORAGE0, REPOSITORY_RELEASES).getAbsolutePath(), gavtc);
            mavenArtifactManagementService.validateAndStore(STORAGE0,
                                                            REPOSITORY_GROUP,
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

        RepositoryPath repositoryPath = repositoryPathResolver.resolve(STORAGE0, REPOSITORY_GROUP,
                                                                       ArtifactUtils.convertArtifactToPath(artifact));

        // Delete: Case 1: No forcing
        //noinspection EmptyCatchBlock
        try
        {
            mavenArtifactManagementService.delete(repositoryPath,
                                                  false);

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
            mavenArtifactManagementService.delete(repositoryPath,
                                                  true);

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
            throws IOException,
                   ArtifactTransportException,
                   ProviderImplementationException
    {
        RepositoryPath path = artifactResolutionService.resolvePath(STORAGE0,
                                                                     REPOSITORY_GROUP,
                                                                     "org/carlspring/strongbox/strongbox-utils/7.3/strongbox-utils-7.3.jar");
        
        try (InputStream is = artifactResolutionService.getInputStream(path))
        {
            assertFalse("Failed to resolve artifact from group repository!", is == null);
            assertTrue("Failed to resolve artifact from group repository!", is.available() > 0);
        }
    }

    @Test
    public void testForceDelete()
            throws IOException
    {
        final String artifactPath = "org/carlspring/strongbox/strongbox-utils/7.0/strongbox-utils-7.0.jar";
        RepositoryPath repositoryPath = repositoryPathResolver.resolve(STORAGE0, REPOSITORY_RELEASES,
                                                                       artifactPath);

        mavenArtifactManagementService.delete(repositoryPath, true);

        assertFalse("Failed to delete artifact during a force delete operation!",
                    new File(getRepositoryBasedir(STORAGE0, REPOSITORY_RELEASES), artifactPath).exists());

        final String artifactPath2 = "org/carlspring/strongbox/strongbox-utils/7.2/strongbox-utils-7.2.jar";
        repositoryPath = repositoryPathResolver.resolve(STORAGE0, REPOSITORY_RELEASES_WITH_TRASH,
                                                        artifactPath2);
        
        mavenArtifactManagementService.delete(repositoryPath,
                                              true);

        final File repositoryDir = new File(getStorageBasedir(STORAGE0), REPOSITORY_RELEASES_WITH_TRASH + "/.trash");

        assertTrue("Should have moved the artifact to the trash during a force delete operation, " +
                   "when allowsForceDeletion is not enabled!",
                   new File(repositoryDir, artifactPath2).exists());
    }

    @Test
    public void testRemoveTimestampedSnapshots()
            throws NoSuchAlgorithmException,
                   XmlPullParserException,
                   IOException
    {
        String repositoryBasedir = getRepositoryBasedir(STORAGE0, REPOSITORY_SNAPSHOTS).getAbsolutePath();
        String artifactPath = repositoryBasedir + "/org/carlspring/strongbox/timestamped";

        File artifactVersionBaseDir = new File(artifactPath, "2.0-SNAPSHOT");

        assertFalse(artifactVersionBaseDir.exists());

        createTimestampedSnapshotArtifact(getRepositoryBasedir(STORAGE0, REPOSITORY_SNAPSHOTS).getAbsolutePath(),
                                          "org.carlspring.strongbox",
                                          "timestamped",
                                          "2.0",
                                          "jar",
                                          null,
                                          3);

        assertEquals("Amount of timestamped snapshots doesn't equal 3.",
                     3,
                     artifactVersionBaseDir.listFiles(new JarFilenameFilter()).length);

        artifactMetadataService.rebuildMetadata(STORAGE0, REPOSITORY_SNAPSHOTS, "org/carlspring/strongbox/timestamped");

        //To check removing timestamped snapshot with numberToKeep = 1
        mavenRepositoryFeatures.removeTimestampedSnapshots(STORAGE0,
                                                           REPOSITORY_SNAPSHOTS,
                                                           "org/carlspring/strongbox/timestamped",
                                                           1,
                                                           0);

        File[] files = artifactVersionBaseDir.listFiles(new JarFilenameFilter());
        assertEquals("Amount of timestamped snapshots doesn't equal 1.", 1, files.length);
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

        artifactMetadataService.rebuildMetadata(STORAGE0, REPOSITORY_SNAPSHOTS, "org/carlspring/strongbox/timestamped");

        assertEquals("Amount of timestamped snapshots doesn't equal 2.", 2,
                     artifactVersionBaseDir.listFiles(new JarFilenameFilter()).length);

        // To check removing timestamped snapshot with keepPeriod = 3 and numberToKeep = 0
        mavenRepositoryFeatures.removeTimestampedSnapshots(STORAGE0,
                                                           REPOSITORY_SNAPSHOTS,
                                                           "org/carlspring/strongbox/timestamped",
                                                           0,
                                                           3);

        files = artifactVersionBaseDir.listFiles(new JarFilenameFilter());
        assertEquals("Amount of timestamped snapshots doesn't equal 1.", 1, files.length);
        assertTrue(files[0].toString().endsWith("-3.jar"));
    }

    @Test
    public void testConcurrentReadWrite()
            throws Exception
    {
        int concurrency = 8;
        
        Random random = new Random();

        byte[][] loremIpsumContentArray = new byte[concurrency][];
        for (int i = 0; i < loremIpsumContentArray.length; i++)
        {
            random.nextBytes(loremIpsumContentArray[i] = new byte[CONTENT_SIZE]);
        }
        
        Repository repository = getConfiguration().getStorage(STORAGE0).getRepository(REPOSITORY_WITH_LOCK);
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
            assertEquals(String.format("Operation [%s:%s] content size don't match.", i % 2 == 0 ? "write" : "read", i),
                         Long.valueOf(CONTENT_SIZE), resultList.get(i));
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
