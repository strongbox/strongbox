package org.carlspring.strongbox.testing;

import org.carlspring.strongbox.artifact.coordinates.NugetArtifactCoordinates;
import org.carlspring.strongbox.artifact.generator.NugetPackageGenerator;
import org.carlspring.strongbox.booters.PropertiesBooter;
import org.carlspring.strongbox.nuget.NugetFormatException;
import org.carlspring.strongbox.providers.ProviderImplementationException;
import org.carlspring.strongbox.services.ArtifactManagementService;
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
 */
public class TestCaseWithNugetPackageGeneration
        extends TestCaseWithRepository
{

    @Inject
    protected ArtifactManagementService artifactManagementService;

    @Inject
    private PropertiesBooter propertiesBooter;

    
    public void generateRepositoryPackages(String storageId,
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
            Path packageFilePath = generatePackageFile(packageId, packageVersion);
            try (InputStream is = new BufferedInputStream(Files.newInputStream(packageFilePath)))
            {
                artifactManagementService.validateAndStore(storageId, repositoryId, coordinates.toPath(), is);
            }
        }
    }

    public Path generatePackageFile(String packageId,
                                    String packageVersion,
                                    String... dependencyList)
        throws NoSuchAlgorithmException,
               NugetFormatException,
               JAXBException,
               IOException
    {
        String basedir = propertiesBooter.getHomeDirectory() + "/tmp";
        return generatePackageFile(basedir, packageId, packageVersion, dependencyList);
    }

    public static Path generatePackageFile(String basedir,
                                    String packageId,
                                    String packageVersion,
                                    String... dependencyList)
        throws NugetFormatException,
               JAXBException,
               IOException,
               NoSuchAlgorithmException
    {
        String packageFileName = packageId + "." + packageVersion + ".nupkg";

        NugetPackageGenerator nugetPackageGenerator = new NugetPackageGenerator(basedir);
        nugetPackageGenerator.generateNugetPackage(packageId, packageVersion, dependencyList);

        Path basePath = Paths.get(basedir).normalize().toAbsolutePath();
        return basePath.resolve(packageId)
                       .resolve(packageVersion)
                       .resolve(packageFileName)
                       .normalize()
                       .toAbsolutePath();
    }

    public void generateNugetPackage(String repositoryDir,
                                     String id,
                                     String version,
                                     String... dependencyList)
        throws IOException,
               NoSuchAlgorithmException,
               NugetFormatException,
               JAXBException
    {
        NugetPackageGenerator generator = new NugetPackageGenerator(repositoryDir);
        generator.generateNugetPackage(id, version, dependencyList);
    }

    public void generateAlphaNugetPackage(String repositoryDir,
                                          String id,
                                          String version,
                                          String... dependencyList)
        throws IOException,
               NoSuchAlgorithmException,
               NugetFormatException,
               JAXBException
    {
        NugetPackageGenerator generator = new NugetPackageGenerator(repositoryDir);

        generator.generateNugetPackage(id, getNugetSnapshotVersion(version), dependencyList);
    }

    private String getNugetSnapshotVersion(String version)
    {
        return version.concat("-alpha");
    }

}
