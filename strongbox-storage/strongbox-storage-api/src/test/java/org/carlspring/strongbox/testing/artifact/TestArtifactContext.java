package org.carlspring.strongbox.testing.artifact;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.carlspring.strongbox.artifact.generator.ArtifactGenerator;
import org.carlspring.strongbox.booters.PropertiesBooter;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.io.RepositoryPathResolver;
import org.carlspring.strongbox.services.ArtifactManagementService;
import org.junit.jupiter.api.TestInfo;
import org.springframework.cglib.proxy.UndeclaredThrowableException;

/**
 * @author sbespalov
 *
 */
public class TestArtifactContext
{

    private final TestArtifact testArtifact;
    private final PropertiesBooter propertiesBooter;
    private final ArtifactManagementService artifactManagementService;
    private final RepositoryPathResolver repositoryPathResolver;
    private final TestInfo testInfo;
    private final Path artifactPath;

    public TestArtifactContext(TestArtifact testArtifact,
                               PropertiesBooter propertiesBooter,
                               ArtifactManagementService artifactManagementService,
                               RepositoryPathResolver repositoryPathResolver,
                               TestInfo testInfo)
        throws IOException
    {
        this.testArtifact = testArtifact;
        this.propertiesBooter = propertiesBooter;
        this.artifactManagementService = artifactManagementService;
        this.repositoryPathResolver = repositoryPathResolver;
        this.testInfo = testInfo;

        artifactPath = createArtifact();
    }

    private Path createArtifact()
        throws IOException
    {
        Class<? extends ArtifactGenerator> generatorClass = testArtifact.generator();

        Path vaultDirectoryPath = Paths.get(propertiesBooter.getVaultDirectory(), ".temp",
                                            testInfo.getTestClass().get().getSimpleName(),
                                            testInfo.getTestMethod().get().getName());

        ArtifactGenerator artifactGenerator;
        try
        {
            artifactGenerator = generatorClass.getConstructor(Path.class).newInstance(vaultDirectoryPath);
        }
        catch (Exception e)
        {
            throw new IOException(e);
        }

        Path artifactPathLocal = artifactGenerator.generateArtifact(URI.create(testArtifact.resource()),
                                                                    testArtifact.size());
        if (testArtifact.repository().isEmpty())
        {
            return artifactPathLocal;
        }

        RepositoryPath repositoryPath = repositoryPathResolver.resolve(testArtifact.storage(),
                                                                       testArtifact.repository(),
                                                                       testArtifact.resource());
        artifactManagementService.store(repositoryPath, Files.newInputStream(artifactPathLocal));
        repositoryPath.getFileSystem()
                      .provider()
                      .resolveChecksumPathMap(repositoryPath)
                      .values()
                      .stream()
                      .forEach(p -> {
                          try
                          {
                              artifactManagementService.store(p,
                                                              Files.newInputStream(artifactPathLocal.resolveSibling(p.getFileName())));
                          }
                          catch (IOException e)
                          {
                              throw new UndeclaredThrowableException(e);
                          }
                      });

        Files.delete(artifactPathLocal);
        return repositoryPath;
    }

    public static String id(TestArtifact testArtifact)
    {
        return String.format("%s", testArtifact.resource());
    }

    public Path getArtifact()
    {
        return artifactPath;
    }

}
