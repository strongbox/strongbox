package org.carlspring.strongbox.providers.storage;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import javax.annotation.PostConstruct;

import org.carlspring.commons.http.range.ByteRange;
import org.carlspring.commons.io.reloading.ReloadableInputStreamHandler;
import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
import org.carlspring.strongbox.io.RepositoryFileSystem;
import org.carlspring.strongbox.io.RepositoryPath;
import org.carlspring.strongbox.io.ArtifactInputStream;
import org.carlspring.strongbox.io.ArtifactOutputStream;
import org.carlspring.strongbox.io.ArtifactPath;
import org.carlspring.strongbox.io.FileSystemWrapper;
import org.carlspring.strongbox.storage.repository.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author carlspring
 */
@Component("filesystemStorageProvider")
public class FileSystemStorageProvider extends AbstractStorageProvider
{

    private static final Logger logger = LoggerFactory.getLogger(FileSystemStorageProvider.class);

    private static final String ALIAS = "file-system";

    @Autowired
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
    public ArtifactInputStream getInputStreamImplementation(ReloadableInputStreamHandler handler,
                                                            List<ByteRange> byteRanges)
        throws IOException,
        NoSuchAlgorithmException
    {
        return new ArtifactInputStream(handler, byteRanges);
    }

    @Override
    public ArtifactInputStream getInputStreamImplementation(ReloadableInputStreamHandler handler,
                                                            ByteRange byteRange)
        throws IOException,
        NoSuchAlgorithmException
    {
        return new ArtifactInputStream(handler, byteRange);
    }

    @Override
    public ArtifactInputStream getInputStreamImplementation(InputStream is)
        throws NoSuchAlgorithmException
    {
        return new ArtifactInputStream(is);
    }

    @Override
    public ArtifactOutputStream getOutputStreamImplementation(ArtifactPath artifactPath)
        throws IOException
    {
        OutputStream os = Files.newOutputStream(artifactPath);
        return new ArtifactOutputStream(os, artifactPath.getCoordinates());
    }

    @Override
    public OutputStream getOutputStreamImplementation(RepositoryPath repositoryPath,
                                                      String path)
        throws IOException
    {
        return Files.newOutputStream(repositoryPath.resolve(path));
    }

    @Override
    public ArtifactInputStream getInputStreamImplementation(ArtifactPath artifactPath)
        throws IOException,
        NoSuchAlgorithmException
    {
        return new ArtifactInputStream(Files.newInputStream(artifactPath));
    }

    @Override
    public ArtifactInputStream getInputStreamImplementation(InputStream is,
                                                            String[] algorithms)
        throws NoSuchAlgorithmException
    {
        return new ArtifactInputStream(is, algorithms);
    }

    @Override
    public ArtifactInputStream getInputStreamImplementation(ArtifactCoordinates coordinates,
                                                            InputStream is)
        throws NoSuchAlgorithmException
    {
        ArtifactInputStream ais = new ArtifactInputStream(is);
        ais.setArtifactCoordinates(coordinates);

        return ais;
    }

    @Override
    public ArtifactPath resolve(Repository repository,
                                ArtifactCoordinates coordinates)
        throws IOException
    {
        Path targetPath = getArtifactPath(repository.getBasedir(), coordinates.toPath());
        // Override FileSystem root to Repository base directory;
        return new ArtifactPath(coordinates, targetPath,
                                getRepositoryFileSystem(repository));
    }

    @Override
    public RepositoryPath resolve(Repository repository)
        throws IOException
    {
        Path path = Paths.get(repository.getBasedir());
        return new RepositoryPath(path, getRepositoryFileSystem(repository));
    }

    private RepositoryFileSystem getRepositoryFileSystem(Repository repository)
    {
        FileSystem storageFileSystem = new FileSystemWrapper(Paths.get(repository.getBasedir()).getFileSystem())
        {

            @Override
            public Path getRootDirectory()
            {
                return Paths.get(repository.getBasedir());
            }

        };
        RepositoryFileSystem repoFileSystem = new RepositoryFileSystem(repository, storageFileSystem);
        return repoFileSystem;
    }

    private Path getArtifactPath(String basePath,
                                 String artifactPath)
        throws IOException
    {
        Path path = Paths.get(basePath).resolve(artifactPath);
        if (!Files.exists(path)){
            Files.createDirectories(path);
        }
        return path;
    }
}
