package org.carlspring.strongbox.testing;

import org.carlspring.strongbox.artifact.generator.ArtifactGenerator;
import org.carlspring.strongbox.artifact.generator.NugetArtifactGenerator;
import org.carlspring.strongbox.booters.PropertiesBooter;
import org.carlspring.strongbox.providers.io.RepositoryPathResolver;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Kate Novik.
 * @author Pablo Tirado
 */
public class TestCaseWithNugetArtifactGeneration
        extends TestCaseWithRepository
{

    @Inject
    private PropertiesBooter propertiesBooter;

    @Inject
    protected RepositoryPathResolver repositoryPathResolver;

    public Path generateArtifactFile(String packageId,
                                     String packageVersion)
            throws IOException
    {
        Path basePath = Paths.get(propertiesBooter.getHomeDirectory(), "tmp");
        return generateArtifactFile(basePath, packageId, packageVersion);
    }

    public static Path generateArtifactFile(Path basePath,
                                            String packageId,
                                            String packageVersion)
            throws IOException
    {
        String packageFileName = String.format("%s.%s.nupkg", packageId, packageVersion);

        ArtifactGenerator nugetArtifactGenerator = new NugetArtifactGenerator(basePath);
        nugetArtifactGenerator.generateArtifact(packageId, packageVersion, 0);

        return basePath.resolve(packageId)
                       .resolve(packageVersion)
                       .resolve(packageFileName)
                       .normalize()
                       .toAbsolutePath();
    }

}
