package org.carlspring.strongbox.testing;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;

import javax.inject.Inject;
import javax.xml.bind.JAXBException;

import org.carlspring.strongbox.artifact.coordinates.NugetArtifactCoordinates;
import org.carlspring.strongbox.artifact.generator.NugetPackageGenerator;
import org.carlspring.strongbox.data.PropertyUtils;
import org.carlspring.strongbox.providers.ProviderImplementationException;
import org.carlspring.strongbox.services.ArtifactManagementService;

import ru.aristar.jnuget.files.NugetFormatException;

/**
 * @author Kate Novik.
 */
public class TestCaseWithNugetPackageGeneration
        extends TestCaseWithRepository
{

    @Inject
    protected ArtifactManagementService artifactManagementService;
    
    public void generateRepositoryPackages(String storageId,
                                           String repositoryId,
                                           String packageId,
                                           int count)
        throws NoSuchAlgorithmException,
        NugetFormatException,
        JAXBException,
        IOException,
        ProviderImplementationException
    {
        for (int i = 0; i < count; i++)
        {
            String packageVersion = String.format("1.0.%s", i);
            NugetArtifactCoordinates coordinates = new NugetArtifactCoordinates(packageId, packageVersion, "nupkg");
            Path packageFilePath = generatePackageFile(packageId, packageVersion);
            artifactManagementService.validateAndStore(storageId, repositoryId, coordinates.toPath(),
                                                       Files.newInputStream(packageFilePath));
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
        String basedir = PropertyUtils.getHomeDirectory() + "/tmp";
        return generatePackageFile(basedir, packageId, packageVersion, dependencyList);
    }

    public Path generatePackageFile(String basedir,
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
