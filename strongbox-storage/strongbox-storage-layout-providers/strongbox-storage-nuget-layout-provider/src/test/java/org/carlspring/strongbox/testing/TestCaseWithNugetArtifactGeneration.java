package org.carlspring.strongbox.testing;

import org.carlspring.strongbox.artifact.coordinates.NugetArtifactCoordinates;
import org.carlspring.strongbox.artifact.generator.ArtifactGenerator;
import org.carlspring.strongbox.artifact.generator.NugetArtifactGenerator;
import org.carlspring.strongbox.booters.PropertiesBooter;
import org.carlspring.strongbox.providers.ProviderImplementationException;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.io.RepositoryPathResolver;
import org.carlspring.strongbox.services.ArtifactManagementService;
import org.carlspring.strongbox.storage.metadata.nuget.NugetFormatException;
import org.carlspring.strongbox.storage.validation.artifact.ArtifactCoordinatesValidationException;

import javax.inject.Inject;
import javax.xml.bind.JAXBException;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;

/**
 * @author Kate Novik.
 * @author Pablo Tirado
 */
public class TestCaseWithNugetArtifactGeneration
        extends TestCaseWithRepository
{

    @Inject
    protected ArtifactManagementService artifactManagementService;

    @Inject
    private PropertiesBooter propertiesBooter;

    @Inject
    protected RepositoryPathResolver repositoryPathResolver;

    public void generateRepositoryArtifacts(String storageId,
                                            String repositoryId,
                                            String packageId,
                                            int count)
            throws NoSuchAlgorithmException,
                   NugetFormatException,
                   JAXBException,
                   IOException,
                   ProviderImplementationException,
                   ArtifactCoordinatesValidationException
    {
        for (int i = 0; i < count; i++)
        {
            String packageVersion = String.format("1.0.%s", i);
            NugetArtifactCoordinates coordinates = new NugetArtifactCoordinates(packageId, packageVersion, "nupkg");
            Path artifactFilePath = generateArtifactFile(packageId, packageVersion);
            try (InputStream is = new BufferedInputStream(Files.newInputStream(artifactFilePath)))
            {
                RepositoryPath repositoryPath = repositoryPathResolver.resolve(storageId,
                                                                               repositoryId,
                                                                               coordinates.toPath());
                artifactManagementService.validateAndStore(repositoryPath, is);
            }
        }
    }

    public Path generateArtifactFile(String packageId,
                                     String packageVersion)
            throws NoSuchAlgorithmException,
                   NugetFormatException,
                   JAXBException,
                   IOException
    {
        String basedir = propertiesBooter.getHomeDirectory() + "/tmp";
        return generateArtifactFile(basedir, packageId, packageVersion);
    }

    public static Path generateArtifactFile(String basedir,
                                            String packageId,
                                            String packageVersion)
            throws NugetFormatException,
                   JAXBException,
                   IOException,
                   NoSuchAlgorithmException
    {
        String packageFileName = packageId + "." + packageVersion + ".nupkg";

        ArtifactGenerator nugetArtifactGenerator = new NugetArtifactGenerator(basedir);
        nugetArtifactGenerator.generateArtifact(packageId, packageVersion, 0);

        Path basePath = Paths.get(basedir).normalize().toAbsolutePath();
        return basePath.resolve(packageId)
                       .resolve(packageVersion)
                       .resolve(packageFileName)
                       .normalize()
                       .toAbsolutePath();
    }

    public void generateNugetArtifact(String repositoryDir,
                                      String id,
                                      String version)
            throws IOException,
                   NoSuchAlgorithmException,
                   NugetFormatException,
                   JAXBException
    {
        ArtifactGenerator generator = new NugetArtifactGenerator(repositoryDir);
        generator.generateArtifact(id, version, 0);
    }

    public void generateAlphaNugetArtifact(String repositoryDir,
                                           String id,
                                           String version)
            throws IOException,
                   NoSuchAlgorithmException,
                   NugetFormatException,
                   JAXBException
    {
        ArtifactGenerator generator = new NugetArtifactGenerator(repositoryDir);

        generator.generateArtifact(id, getNugetSnapshotVersion(version), 0);
    }

    private String getNugetSnapshotVersion(String version)
    {
        return version.concat("-alpha");
    }

}
