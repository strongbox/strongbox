package org.carlspring.strongbox.services;

import org.carlspring.maven.commons.io.filters.JarFilenameFilter;
import org.carlspring.strongbox.artifact.MavenArtifact;
import org.carlspring.strongbox.artifact.MavenArtifactUtils;
import org.carlspring.strongbox.client.ArtifactTransportException;
import org.carlspring.strongbox.config.Maven2LayoutProviderTestConfig;
import org.carlspring.strongbox.providers.ProviderImplementationException;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.io.RepositoryPathResolver;
import org.carlspring.strongbox.providers.layout.Maven2LayoutProvider;
import org.carlspring.strongbox.repository.MavenRepositoryFeatures;
import org.carlspring.strongbox.resource.ResourceCloser;
import org.carlspring.strongbox.storage.ArtifactStorageException;
import org.carlspring.strongbox.storage.repository.MavenRepositoryFactory;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.RepositoryPolicyEnum;
import org.carlspring.strongbox.storage.repository.RepositoryTypeEnum;
import org.carlspring.strongbox.testing.TestCaseWithMavenArtifactGenerationAndIndexing;

import javax.inject.Inject;
import javax.xml.bind.JAXBException;
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
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author mtodorov
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = Maven2LayoutProviderTestConfig.class)
public class ArtifactManagementServiceImplTest
        extends TestCaseWithMavenArtifactGenerationAndIndexing
{

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

    public static Set<Repository> getRepositoriesToClean()
    {
        Set<Repository> repositories = new LinkedHashSet<>();
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_RELEASES));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_RELEASES_WITHOUT_DEPLOYMENT));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_RELEASES_WITHOUT_REDEPLOYMENT));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_RELEASES_WITH_TRASH));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_RELEASES_WITHOUT_DELETE));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_SNAPSHOTS));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_GROUP));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_WITH_LOCK));

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
        Repository repositoryWithoutDelete = mavenRepositoryFactory.createRepository(STORAGE0,
                                                                                     REPOSITORY_RELEASES_WITHOUT_DELETE);
        repositoryWithoutDelete.setAllowsDelete(false);
        repositoryWithoutDelete.setLayout(Maven2LayoutProvider.ALIAS);

        createRepositoryWithArtifacts(repositoryWithoutDelete,
                                      "org.carlspring.strongbox:strongbox-utils",
                                      "8.0");

        // Used by testRedeploymentToRepositoryWithForbiddenRedeployments()
        Repository repositoryWithoutRedeployments = mavenRepositoryFactory.createRepository(STORAGE0,
                                                                                            REPOSITORY_RELEASES_WITHOUT_REDEPLOYMENT);
        repositoryWithoutRedeployments.setAllowsRedeployment(false);

        createRepositoryWithArtifacts(repositoryWithoutRedeployments,
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

        Repository repositoryGroup = mavenRepositoryFactory.createRepository(STORAGE0, REPOSITORY_GROUP);
        repositoryGroup.setType(RepositoryTypeEnum.GROUP.getType());
        repositoryGroup.setAllowsRedeployment(false);
        repositoryGroup.setAllowsDelete(false);
        repositoryGroup.setAllowsForceDeletion(false);
        repositoryGroup.addRepositoryToGroup(REPOSITORY_RELEASES);

        createRepositoryWithArtifacts(repositoryGroup,
                                      "org.carlspring.strongbox:strongbox-utils",
                                      "8.2" // Used by testDeploymentRedeploymentAndDeletionAgainstGroupRepository()
        );

        // Used by testForceDelete()
        Repository repositoryWithTrash = mavenRepositoryFactory.createRepository(STORAGE0,
                                                                                 REPOSITORY_RELEASES_WITH_TRASH);
        repositoryWithTrash.setTrashEnabled(true);

        createRepositoryWithArtifacts(repositoryWithTrash,
                                      "org.carlspring.strongbox:strongbox-utils",
                                      "7.2");

        // Used by testRemoveTimestampedSnapshots()
        Repository repositorySnapshots = mavenRepositoryFactory.createRepository(STORAGE0, REPOSITORY_SNAPSHOTS);
        repositorySnapshots.setPolicy(RepositoryPolicyEnum.SNAPSHOT.getPolicy());

        createRepository(repositorySnapshots);

        //
        Repository repositoryWithLock = mavenRepositoryFactory.createRepository(STORAGE0, REPOSITORY_WITH_LOCK);
        createRepository(repositoryWithLock);
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

            File repositoryDir = getRepositoryBasedir(STORAGE0, REPOSITORY_RELEASES);
            is = generateArtifactInputStream(repositoryDir.getAbsolutePath(),
                                             REPOSITORY_RELEASES_WITHOUT_DELETE,
                                             gavtc,
                                             true);

            MavenArtifact artifact = MavenArtifactUtils.getArtifactFromGAVTC(gavtc);
            mavenArtifactManagementService.validateAndStore(STORAGE0,
                                                            REPOSITORY_RELEASES_WITHOUT_DELETE,
                                                            MavenArtifactUtils.convertArtifactToPath(artifact),
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

            is = generateArtifactInputStream(repositoryBasedir.getAbsolutePath(),
                                             REPOSITORY_RELEASES_WITHOUT_REDEPLOYMENT,
                                             gavtc,
                                             true);

            MavenArtifact artifact = MavenArtifactUtils.getArtifactFromGAVTC(gavtc);
            mavenArtifactManagementService.validateAndStore(STORAGE0,
                                                            REPOSITORY_RELEASES_WITHOUT_REDEPLOYMENT,
                                                            MavenArtifactUtils.convertArtifactToPath(artifact),
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

            MavenArtifact artifact = MavenArtifactUtils.getArtifactFromGAVTC(gavtc);
            mavenArtifactManagementService.delete(STORAGE0,
                                                  REPOSITORY_RELEASES_WITHOUT_DELETE,
                                                  MavenArtifactUtils.convertArtifactToPath(artifact),
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

        MavenArtifact artifact = MavenArtifactUtils.getArtifactFromGAVTC(gavtc);

        //noinspection EmptyCatchBlock
        try
        {
            File repositoryDir = getRepositoryBasedir(STORAGE0, REPOSITORY_GROUP);
            is = generateArtifactInputStream(repositoryDir.getAbsolutePath(), REPOSITORY_GROUP, gavtc, true);

            mavenArtifactManagementService.validateAndStore(STORAGE0,
                                                            REPOSITORY_GROUP,
                                                            MavenArtifactUtils.convertArtifactToPath(artifact),
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
                                                            MavenArtifactUtils.convertArtifactToPath(artifact),
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

        // Delete: Case 1: No forcing
        //noinspection EmptyCatchBlock
        try
        {
            mavenArtifactManagementService.delete(STORAGE0,
                                                  REPOSITORY_GROUP,
                                                  MavenArtifactUtils.convertArtifactToPath(artifact),
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
            mavenArtifactManagementService.delete(STORAGE0,
                                                  REPOSITORY_GROUP,
                                                  MavenArtifactUtils.convertArtifactToPath(artifact),
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
        InputStream is = mavenArtifactManagementService.resolve(STORAGE0,
                                                                REPOSITORY_GROUP,
                                                                "org/carlspring/strongbox/strongbox-utils/7.3/strongbox-utils-7.3.jar");

        assertFalse("Failed to resolve artifact from group repository!", is == null);
        assertTrue("Failed to resolve artifact from group repository!", is.available() > 0);

        is.close();
    }

    @Test
    public void testForceDelete()
            throws IOException
    {
        final String artifactPath = "org/carlspring/strongbox/strongbox-utils/7.0/strongbox-utils-7.0.jar";

        mavenArtifactManagementService.delete(STORAGE0, REPOSITORY_RELEASES, artifactPath, true);

        assertFalse("Failed to delete artifact during a force delete operation!",
                    new File(getRepositoryBasedir(STORAGE0, REPOSITORY_RELEASES), artifactPath).exists());

        final String artifactPath2 = "org/carlspring/strongbox/strongbox-utils/7.2/strongbox-utils-7.2.jar";

        mavenArtifactManagementService.delete(STORAGE0,
                                              REPOSITORY_RELEASES_WITH_TRASH,
                                              artifactPath2,
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
        MavenArtifact artifact = MavenArtifactUtils.convertPathToArtifact(files[0].getPath());
        String artifactName = artifact.getVersion();

        assertEquals("Amount of timestamped snapshots doesn't equal 1.", 1, files.length);
        assertTrue(artifactName.endsWith("-3"));

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
        artifact = MavenArtifactUtils.convertPathToArtifact(files[0].getPath());
        artifactName = artifact.getVersion();

        assertEquals("Amount of timestamped snapshots doesn't equal 1.", 1, files.length);
        assertTrue(artifactName.endsWith("-3"));
    }

    @Test
    public void storageContentShouldNotBeAffectedByMoreThanOneThreadAtTheSameTime()
            throws Exception
    {
        Random random = new Random();


        byte[] loremIpsum1ContentArray = new byte[40000];
        random.nextBytes(loremIpsum1ContentArray);

        byte[] loremIpsum2ContentArray = new byte[40000];
        random.nextBytes(loremIpsum2ContentArray);

        byte[] loremIpsum3ContentArray = new byte[40000];
        random.nextBytes(loremIpsum3ContentArray);

        byte[] loremIpsum4ContentArray = new byte[40000];
        random.nextBytes(loremIpsum4ContentArray);

        Repository repository = getConfiguration().getStorage(STORAGE0).getRepository(REPOSITORY_WITH_LOCK);
        RepositoryPath repositoryPath = repositoryPathResolver.resolve(repository).resolve(
                "org/carlspring/strongbox/locked-artifact/12.2.0.1/locked-artifact-12.2.0.1.pom");

        List<Callable<Exception>> callables = Arrays.asList(
                new InvokeStoreCallable(repositoryPath, new ByteArrayInputStream(loremIpsum1ContentArray)),
                new InvokeStoreCallable(repositoryPath, new ByteArrayInputStream(loremIpsum2ContentArray)),
                new InvokeStoreCallable(repositoryPath, new ByteArrayInputStream(loremIpsum3ContentArray)),
                new InvokeStoreCallable(repositoryPath, new ByteArrayInputStream(loremIpsum4ContentArray))
        );

        // when
        List<Future<Exception>> exceptions = Executors.newFixedThreadPool(
                Runtime.getRuntime().availableProcessors()).invokeAll(callables);
        byte[] repositoryPathContent = Files.readAllBytes(repositoryPath);

        // then
        assertTrue(Arrays.equals(repositoryPathContent, loremIpsum1ContentArray) ||
                   Arrays.equals(repositoryPathContent, loremIpsum2ContentArray) ||
                   Arrays.equals(repositoryPathContent, loremIpsum3ContentArray) ||
                   Arrays.equals(repositoryPathContent, loremIpsum4ContentArray));

        for (Future<Exception> exceptionFuture : exceptions)
        {
            assertTrue(exceptionFuture.isDone());
            assertThat(exceptionFuture.get(), CoreMatchers.nullValue());
        }
    }

    private class InvokeStoreCallable
            implements Callable<Exception>
    {

        private final RepositoryPath repositoryPath;

        private final InputStream is;

        private InvokeStoreCallable(final RepositoryPath repositoryPath,
                                    final InputStream is)
        {
            this.repositoryPath = repositoryPath;
            this.is = is;
        }

        @Override
        public Exception call()
                throws Exception
        {
            try
            {
                mavenArtifactManagementService.store(repositoryPath, is);
            }
            catch (Exception ex)
            {
                return ex;
            }

            return null;
        }
    }

}
