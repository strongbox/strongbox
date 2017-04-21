package org.carlspring.strongbox.testing;

import org.carlspring.strongbox.artifact.generator.NugetPackageGenerator;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import ru.aristar.jnuget.files.NugetFormatException;

/**
 * @author Kate Novik.
 */
public class TestCaseWithNugetPackageGeneration
        extends TestCaseWithRepository
{

    public void generateNugetPackage(String repositoryDir,
                                     String id,
                                     String... versions)
            throws IOException,
                   NoSuchAlgorithmException,
                   NugetFormatException,
                   JAXBException
    {
        NugetPackageGenerator generator = new NugetPackageGenerator(repositoryDir.concat("/")
                                                                                 .concat(id));
        generator.generateNugetPackage(id, versions);
    }

    public void generateAlphaNugetPackage(String repositoryDir,
                                          String id,
                                          String... versions)
            throws IOException, NoSuchAlgorithmException, NugetFormatException, JAXBException
    {
        NugetPackageGenerator generator = new NugetPackageGenerator(repositoryDir.concat("/")
                                                                                 .concat(id));

        for (String version : versions)
        {
            generator.generateNugetPackage(id, getNugetSnapshotVersion(version));
        }
    }

    private String getNugetSnapshotVersion(String version)
    {
        return version.concat("-alpha");
    }
}
