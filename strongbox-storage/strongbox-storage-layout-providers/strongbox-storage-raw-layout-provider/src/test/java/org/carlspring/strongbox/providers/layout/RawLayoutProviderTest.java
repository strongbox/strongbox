package org.carlspring.strongbox.providers.layout;

import org.carlspring.strongbox.artifact.coordinates.NullArtifactCoordinates;
import org.carlspring.strongbox.config.RawLayoutProviderTestConfig;
import org.carlspring.strongbox.configuration.Configuration;
import org.carlspring.strongbox.data.PropertyUtils;
import org.carlspring.strongbox.repository.RepositoryManagementStrategyException;
import org.carlspring.strongbox.services.ConfigurationManagementService;
import org.carlspring.strongbox.services.RepositoryManagementService;
import org.carlspring.strongbox.services.StorageManagementService;
import org.carlspring.strongbox.services.impl.RawArtifactManagementService;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;

import javax.inject.Inject;
import javax.xml.bind.JAXBException;
import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static org.junit.Assert.assertTrue;

/**
 * @author carlspring
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = RawLayoutProviderTestConfig.class)
public class RawLayoutProviderTest
{

    public static final String STORAGE = "storage-raw";

    public static final String REPOSITORY = "raw-releases";

    @Inject
    ConfigurationManagementService configurationManagementService;

    @Inject
    StorageManagementService storageManagementService;

    @Inject
    RepositoryManagementService repositoryManagementService;

    @Inject
    RawArtifactManagementService rawArtifactManagementService;


    @Before
    public void setUp()
            throws Exception
    {
        Configuration configuration = configurationManagementService.getConfiguration();

        if (configuration.getStorage(STORAGE) == null)
        {
            createStorage(STORAGE);
        }

        if (configuration.getStorage(STORAGE).getRepository(REPOSITORY) == null)
        {
            createRepository(STORAGE, REPOSITORY);
        }
    }

    @Test
    public void testNullArtifactCoordinates()
    {
        NullArtifactCoordinates coordinates = new NullArtifactCoordinates("foo/bar/blah.bz2");

        System.out.println("coordinates.toPath(): " + coordinates.toPath());
    }


    @Test
    public void testDeployAndResolveArtifact()
            throws Exception
    {
        String path = "foo/bar.zip";
        File artifactFile = new File("target/strongbox-vault/storages/" + STORAGE + "/" + REPOSITORY + "/" + path);
        File artifactTempFile = new File(PropertyUtils.getTempDirectory() +
                                         "/storages/" + STORAGE + "/" + REPOSITORY + "/" + path);

        if (!artifactTempFile.getParentFile().exists())
        {
            //noinspection ResultOfMethodCallIgnored
            artifactTempFile.getParentFile().mkdirs();
        }

        // Deploy the artifact
        rawArtifactManagementService.validateAndStore(STORAGE,
                                                      REPOSITORY,
                                                      path,
                                                      createZipFile(artifactTempFile.getPath()));

        assertTrue("Failed to deploy artifact!", artifactFile.exists());
        assertTrue("Failed to deploy artifact!", artifactFile.length() > 0);

        // Attempt to re-deploy the artifact
        try
        {
            rawArtifactManagementService.validateAndStore(STORAGE,
                                                          REPOSITORY,
                                                          path,
                                                          createZipFile(artifactTempFile.getPath()));
        }
        catch (Exception e)
        {
            if (e.getMessage().contains("repository does not allow artifact re-deployment"))
            {
                // This is expected
                System.out.println("Successfully declined to re-deploy " + artifactFile.getPath() + "!");
            }
            else
            {
                throw e;
            }
        }

        // Attempt to resolve the artifact
        InputStream is = rawArtifactManagementService.resolve(STORAGE, REPOSITORY, path);
        int total = 0;
        int len;
        final int size = 4096;
        byte[] bytes = new byte[size];

        while ((len = is.read(bytes, 0, size)) != -1)
        {
            total += len;
        }

        assertTrue("Failed to resolve artifact!", total > 0);
    }

    private void createRepository(String storageId, String repositoryId)
            throws IOException, JAXBException, RepositoryManagementStrategyException
    {
        Repository repository = new Repository(repositoryId);
        repository.setStorage(configurationManagementService.getConfiguration().getStorage(storageId));
        repository.setLayout(RawLayoutProvider.ALIAS);

        configurationManagementService.saveRepository(storageId, repository);

        // Create the repository
        repositoryManagementService.createRepository(storageId, repositoryId);
    }

    private void createStorage(String storageId)
            throws IOException, JAXBException
    {
        Storage storage = new Storage(storageId);

        configurationManagementService.saveStorage(storage);
        storageManagementService.createStorage(storage);
    }

    private InputStream createZipFile(String path)
            throws IOException
    {
        FileOutputStream fos = new FileOutputStream(path);
        try (ZipOutputStream zos = new ZipOutputStream(fos))
        {
            ZipEntry entry = new ZipEntry("dummy-file.txt");

            zos.putNextEntry(entry);
            zos.write("this is a test file".getBytes());
            zos.closeEntry();
        }

        return new FileInputStream(path);
    }

}
