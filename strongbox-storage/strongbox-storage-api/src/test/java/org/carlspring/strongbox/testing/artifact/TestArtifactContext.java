package org.carlspring.strongbox.testing.artifact;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import javax.annotation.PreDestroy;

import org.carlspring.strongbox.artifact.generator.ArtifactGenerator;
import org.carlspring.strongbox.booters.PropertiesBooter;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.io.RepositoryPathResolver;
import org.carlspring.strongbox.services.ArtifactManagementService;
import org.junit.jupiter.api.TestInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cglib.proxy.UndeclaredThrowableException;

/**
 * @author sbespalov
 *
 */
public class TestArtifactContext implements AutoCloseable
{
    
    private static final Logger logger = LoggerFactory.getLogger(TestArtifactContext.class);

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
        logger.info(String.format("Create [%s] resource [%s] ", TestArtifact.class.getSimpleName(), id(testArtifact)));
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

        Objects.requireNonNull(testArtifact.storage(), String.format("Repository [%s] requires to specify Storage as well.", testArtifact.repository()));
        
        RepositoryPath repositoryPath = repositoryPathResolver.resolve(testArtifact.storage(),
                                                                       testArtifact.repository(),
                                                                       testArtifact.resource());
        try (DirectoryStream<Path> s = Files.newDirectoryStream(artifactPathLocal.getParent()))
        {
            s.forEach(p -> {
                try (InputStream is = Files.newInputStream(p))
                {
                    artifactManagementService.store(repositoryPath.resolveSibling(p.getFileName()), is);
                    Files.delete(p);
                }
                catch (IOException e)
                {
                    throw new UndeclaredThrowableException(e);
                }
            });
        }        

        return repositoryPath;
    }

    public Path getArtifact()
    {
        return artifactPath;
    }

    @PreDestroy
    @Override
    public void close()
        throws Exception
    {
        Files.deleteIfExists(artifactPath);
    }

    public static String id(TestArtifact testArtifact)
    {
        return String.format("%s", testArtifact.resource());
    }

}
