package org.carlspring.strongbox.testing;

import org.carlspring.strongbox.artifact.generator.NugetPackageGenerator;
import org.carlspring.strongbox.data.PropertyUtils;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;

import ru.aristar.jnuget.files.NugetFormatException;

/**
 * @author Kate Novik.
 */
public class TestCaseWithNugetPackageGeneration
        extends TestCaseWithRepository
{
    
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
                                    String...dependencyList)
        throws NugetFormatException,
               JAXBException,
               IOException,
               NoSuchAlgorithmException
    {        
        String packageFileName = packageId + "." + packageVersion + ".nupkg";

        NugetPackageGenerator nugetPackageGenerator = new NugetPackageGenerator(basedir);
        nugetPackageGenerator.generateNugetPackage(packageId, packageVersion, dependencyList);

        Path basePath = Paths.get(basedir).normalize().toAbsolutePath();
        return basePath.resolve(packageVersion).resolve(packageFileName).normalize().toAbsolutePath();
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
        NugetPackageGenerator generator = new NugetPackageGenerator(repositoryDir.concat("/").concat(id));
        generator.generateNugetPackage(id, version, dependencyList);
    }

    public void generateAlphaNugetPackage(String repositoryDir,
                                          String id,
                                          String version,
                                          String... dependencyList)
            throws IOException, NoSuchAlgorithmException, NugetFormatException, JAXBException
    {
        NugetPackageGenerator generator = new NugetPackageGenerator(repositoryDir.concat("/").concat(id));

        generator.generateNugetPackage(id, getNugetSnapshotVersion(version), dependencyList);
    }

    private String getNugetSnapshotVersion(String version)
    {
        return version.concat("-alpha");
    }
    
}
