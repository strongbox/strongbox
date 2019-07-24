package org.carlspring.strongbox.testing.artifact;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.URI;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.Stream;

import javax.annotation.PreDestroy;

import org.carlspring.strongbox.artifact.generator.ArtifactGenerator;
import org.carlspring.strongbox.booters.PropertiesBooter;
import org.carlspring.strongbox.providers.io.RepositoryFiles;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.io.RepositoryPathResolver;
import org.carlspring.strongbox.services.ArtifactManagementService;
import org.carlspring.strongbox.util.ThrowingComparator;
import org.carlspring.strongbox.util.ThrowingConsumer;
import org.junit.jupiter.api.TestInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author sbespalov
 */
public class TestArtifactContext implements AutoCloseable
{

    private static final Logger logger = LoggerFactory.getLogger(TestArtifactContext.class);

    private final TestArtifact testArtifact;

    private final Map<String, Object> attributesMap;

    private final PropertiesBooter propertiesBooter;

    private final ArtifactManagementService artifactManagementService;

    private final RepositoryPathResolver repositoryPathResolver;

    private final TestInfo testInfo;

    private final List<Path> artifactPaths;

    private ArtifactGenerator artifactGenerator;

    @SuppressWarnings("rawtypes")
    private ArtifactGeneratorStrategy strategy;

    private Path generatorBasePath;
    

    public TestArtifactContext(TestArtifact testArtifact,
                               Map<String, Object> attributesMap,
                               PropertiesBooter propertiesBooter,
                               ArtifactManagementService artifactManagementService,
                               RepositoryPathResolver repositoryPathResolver,
                               TestInfo testInfo)
            throws IOException
    {
        this.testArtifact = testArtifact;
        this.attributesMap = attributesMap;
        this.propertiesBooter = propertiesBooter;
        this.artifactManagementService = artifactManagementService;
        this.repositoryPathResolver = repositoryPathResolver;
        this.testInfo = testInfo;
        
        artifactPaths = createArtifacts();
    }

    private List<Path> createArtifacts()
            throws IOException
    {
        checkArguments();
            
        logger.info(String.format("Create [%s] resource [%s] ", TestArtifact.class.getSimpleName(), id(testArtifact)));
        Class<? extends ArtifactGenerator> generatorClass = testArtifact.generator();

        generatorBasePath = Paths.get(propertiesBooter.getVaultDirectory(), ".temp",
                                      testInfo.getTestClass().get().getSimpleName(),
                                      testInfo.getTestMethod().get().getName());

        try
        {
            artifactGenerator = generatorClass.getConstructor(Path.class).newInstance(generatorBasePath);
        }
        catch (Exception e)
        {
            throw new IOException(e);
        }

        Path directoryWhereGeneratedArtifactsWillBePlaced = generatorBasePath.resolve(testArtifact.resource())
                                                                             .getParent();
        if (Files.exists(directoryWhereGeneratedArtifactsWillBePlaced))
        {
            try (Stream<Path> s = Files.list(directoryWhereGeneratedArtifactsWillBePlaced))
            {
                if (s.anyMatch(p -> !Files.isDirectory(p)))
                {
                    throw new IOException(
                            String.format("Directory [%s] is not empty, consider to clean it up before other artifacts can be generated there.",
                                          directoryWhereGeneratedArtifactsWillBePlaced));
                }
            }
        }
        
        if (!testArtifact.resource().trim().isEmpty())
        {
            return Collections.singletonList(generateArtifact(testArtifact.resource(), testArtifact.size()));
        }

        try
        {
            strategy = testArtifact.strategy().newInstance();
        }
        catch (InstantiationException|IllegalAccessException e)
        {
            throw new IOException(e);
        }
        
        if (testArtifact.versions().length == 0)
        {
            throw new IllegalArgumentException(String.format("Versions should be provided for [%s]", testArtifact.id()));
        }
        
        List<Path> paths = new ArrayList<>(testArtifact.versions().length);
        for (String version : testArtifact.versions())
        {
            paths.add(generateArtifact(testArtifact.id(), version, testArtifact.size()));
        }
        
        return paths;
    }

    private void checkArguments()
    {
        if (testArtifact.resource().trim().isEmpty() && testArtifact.id().trim().isEmpty()
                || !testArtifact.resource().trim().isEmpty() && !testArtifact.id().trim().isEmpty())
        {
            throw new IllegalArgumentException("One of the TestArtifact.resource() or TestArtifact.id() should be provided.");
        }
    }

    private Path generateArtifact(String id,
                                  String version,
                                  int size)
        throws IOException
    {
        @SuppressWarnings("unchecked")
        Path artifactPathLocal = strategy.generateArtifact(artifactGenerator, id, version, size, attributesMap);
        if (testArtifact.repositoryId().isEmpty())
        {
            return artifactPathLocal;
        }

        return deployArtifact(artifactPathLocal);
    }
    
    private Path generateArtifact(String resource, int size)
            throws IOException
    {
        Path artifactPathLocal = artifactGenerator.generateArtifact(URI.create(resource), size);
        if (testArtifact.repositoryId().isEmpty())
        {
            return artifactPathLocal;
        }

        return deployArtifact(artifactPathLocal);
    }

    private Path deployArtifact(Path artifactPathLocal)
            throws IOException
    {
        Objects.requireNonNull(testArtifact.storageId(),
                               String.format("Repository [%s] requires to specify Storage as well.",
                                             testArtifact.repositoryId()));

        String relativePath = generatorBasePath.relativize(artifactPathLocal).toString();
        RepositoryPath repositoryPath = repositoryPathResolver.resolve(testArtifact.storageId(),
                                                                       testArtifact.repositoryId(),
                                                                       relativePath);

        Map<RepositoryPath, Path> repositoryPathMap = new TreeMap<RepositoryPath, Path>(ThrowingComparator.unchecked(this::repositoryPathChecksumComparator));
        try (DirectoryStream<Path> s = Files.newDirectoryStream(artifactPathLocal.getParent()))
        {
            s.forEach(p -> repositoryPathMap.put(repositoryPath.resolveSibling(p.getFileName()), p));
        }

        repositoryPathMap.entrySet().forEach(ThrowingConsumer.unchecked(this::store));

        return repositoryPath;
    }

    private void store(Map.Entry<RepositoryPath, Path> e)
            throws IOException
    {
        try (InputStream is = Files.newInputStream(e.getValue()))
        {
            artifactManagementService.store(e.getKey(), is);
        }
        catch (IOException ioe)
        {
            throw new UndeclaredThrowableException(ioe);
        }

        Files.delete(e.getValue());
    }

    /**
     * This comparator will sort the paths in reverse alphabetical order, with
     * checksum files ordered after other types of files.  This is necessary because
     * Apache Maven Indexer's MinimalArtifactInfoIndexCreator will apply the checksum
     * from a JAR file to the POM's entry in the index if the JAR file is added to the
     * repository before the POM.
     */
    private int repositoryPathChecksumComparator(RepositoryPath p1,
                                                 RepositoryPath p2)
            throws IOException
    {
        Boolean isChecksumP1;
        Boolean isChecksumP2;

        isChecksumP1 = RepositoryFiles.isChecksum(p1);
        isChecksumP2 = RepositoryFiles.isChecksum(p2);
        
        if (isChecksumP1 && isChecksumP2 || !isChecksumP1 && !isChecksumP2)
        {
            return -1 * p1.compareTo(p2);
        }
        
        return isChecksumP1 ? 1 : -1;
    }
    
    public List<Path> getArtifacts()
    {
        return artifactPaths;
    }

    @PreDestroy
    @Override
    public void close()
        throws IOException
    {
        if (artifactPaths == null)
        {
            return;
        }
        
        for (Path artifactPath : artifactPaths)
        {
            if (artifactPath == null || artifactPath instanceof RepositoryPath)
            {
                continue;
            }

            close(artifactPath);
        }
    }

    private void close(Path path)
            throws IOException
    {
        Path directoryWhereGeneratedArtifactsWasPlaced = path.getParent();
        try (Stream<Path> s = Files.list(directoryWhereGeneratedArtifactsWasPlaced))
        {
            s.filter(p -> !Files.isDirectory(p)).forEach(ThrowingConsumer.unchecked(Files::delete));
        }
    }

    public static String id(TestArtifact testArtifact)
    {
        String format = "%s/%s/%s";
        if (!testArtifact.resource().trim().isEmpty())
        {
            return String.format(format, testArtifact.storageId(), testArtifact.repositoryId(), testArtifact.resource());
        }
        
        return String.format(format, testArtifact.storageId(), testArtifact.repositoryId(), testArtifact.id());
    }

}
