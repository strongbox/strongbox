package org.carlspring.strongbox.providers.storage;

import org.carlspring.commons.http.range.ByteRange;
import org.carlspring.commons.io.reloading.ReloadableInputStreamHandler;
import org.carlspring.strongbox.io.ArtifactInputStream;

import javax.annotation.PostConstruct;
import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author carlspring
 */
@Component("filesystemStorageProvider")
public class FileSystemStorageProvider
        extends AbstractStorageProvider
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
            throws IOException, NoSuchAlgorithmException
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
    public ArtifactInputStream getInputStreamImplementation(String path)
            throws NoSuchAlgorithmException, FileNotFoundException
    {
        return new ArtifactInputStream(new FileInputStream(path));
    }

    @Override
    public ArtifactInputStream getInputStreamImplementation(File file)
            throws NoSuchAlgorithmException, FileNotFoundException
    {
        return new ArtifactInputStream(new FileInputStream(file));
    }

    @Override
    public ArtifactInputStream getInputStreamImplementation(InputStream is,
                                                            String[] algorithms)
            throws NoSuchAlgorithmException
    {
        return new ArtifactInputStream(is, algorithms);
    }

    @Override
    public ArtifactInputStream getInputStreamImplementation(Artifact artifact,
                                                            InputStream is)
            throws NoSuchAlgorithmException
    {
        ArtifactInputStream ais = new ArtifactInputStream(is);
        ais.setArtifact(artifact);

        return ais;
    }

    @Override
    public File getFileImplementation(String path)
            throws IOException
    {
        return new File(path);
    }

    @Override
    public File getFileImplementation(String parentPath,
                                      String path)
            throws IOException
    {
        return new File(parentPath, path);
    }

}
