package org.carlspring.strongbox.providers.storage;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.carlspring.commons.io.reloading.FSReloadableInputStreamHandler;
import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
import org.carlspring.strongbox.io.ByteRangeInputStream;
import org.carlspring.strongbox.storage.repository.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author carlspring
 */
@Component("filesystemStorageProvider")
public class FileSystemStorageProvider extends AbstractStorageProvider
{

    private static final Logger logger = LoggerFactory.getLogger(FileSystemStorageProvider.class);

    private static final String ALIAS = "file-system";

    @Inject
    private StorageProviderRegistry storageProviderRegistry;

    @Override
    public String getAlias()
    {
        return ALIAS;
    }

    @PostConstruct
    @Override
    public void register()
    {
        storageProviderRegistry.addProvider(getAlias(), this);

        logger.info("Registered storage provider '" + getClass().getCanonicalName() + "' with alias '" + ALIAS + "'.");
    }

    @Override
    public OutputStream getOutputStreamImplementation(Path artifactPath)
        throws IOException,
        NoSuchAlgorithmException
    {
        return Files.newOutputStream(artifactPath);
    }

    @Override
    public OutputStream getOutputStreamImplementation(Path repositoryPath,
                                                      String path)
        throws IOException
    {
        return Files.newOutputStream(repositoryPath.resolve(path));
    }

    @Override
    public InputStream getInputStreamImplementation(Path repositoryPath,
                                                    String path)
        throws IOException,
        NoSuchAlgorithmException
    {
        Path artifactPath = repositoryPath.resolve(path);
        if (!Files.exists(artifactPath) || Files.isDirectory(artifactPath))
        {
            throw new FileNotFoundException(artifactPath.toString());
        }

        return getInputStream(artifactPath);
    }

    @Override
    public InputStream getInputStreamImplementation(Path artifactPath)
        throws IOException,
        NoSuchAlgorithmException
    {
        if (!Files.exists(artifactPath))
        {
            throw new FileNotFoundException(artifactPath.toString());
        }

        return getInputStream(artifactPath);
    }

    private InputStream getInputStream(Path artifactPath)
        throws IOException,
        NoSuchAlgorithmException
    {
        ByteRangeInputStream bris = new ByteRangeInputStream(Files.newInputStream(artifactPath));
        bris.setReloadableInputStreamHandler(new FSReloadableInputStreamHandler(artifactPath.toFile()));
        bris.setLength(Files.size(artifactPath));

        return bris;
    }

    @Override
    public Path resolve(Repository repository,
                        ArtifactCoordinates coordinates)
        throws IOException
    {
        Path targetPath = getArtifactPath(repository.getBasedir(), coordinates.toPath());

        return getArtifactPath(repository.getBasedir(), coordinates.toPath());
    }

    @Override
    public Path resolve(Repository repository)
        throws IOException
    {
        return Paths.get(repository.getBasedir());
    }

    @Override
    public Path resolve(Repository repository,
                                  String path)
        throws IOException
    {
        return resolve(repository).resolve(path);
    }

    public static Path getArtifactPath(String basePath,
                                       String artifactPath)
        throws IOException
    {
        Path base = Paths.get(basePath);
        Path path = base.resolve(artifactPath);
        if (!Files.exists(path.getParent()))
        {
            Files.createDirectories(path.getParent());
        }

        return path;
    }

}
