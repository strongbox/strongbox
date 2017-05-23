package org.carlspring.strongbox.providers.repository;

import java.io.IOException;
import java.nio.file.Files;
import java.security.NoSuchAlgorithmException;

import javax.annotation.PostConstruct;

import org.carlspring.strongbox.client.ArtifactTransportException;
import org.carlspring.strongbox.io.ArtifactInputStream;
import org.carlspring.strongbox.io.ArtifactOutputStream;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.layout.LayoutProvider;
import org.carlspring.strongbox.storage.repository.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author carlspring
 */
@Component
public class HostedRepositoryProvider extends AbstractRepositoryProvider
{

    private static final Logger logger = LoggerFactory.getLogger(HostedRepositoryProvider.class);

    private static final String ALIAS = "hosted";


    @PostConstruct
    @Override
    public void register()
    {
        getRepositoryProviderRegistry().addProvider(ALIAS, this);

        logger.info("Registered repository provider '" + getClass().getCanonicalName() + "' with alias '" + ALIAS + "'.");
    }

    @Override
    public String getAlias()
    {
        return ALIAS;
    }

    @Override
    public ArtifactInputStream getInputStream(String storageId,
                                              String repositoryId,
                                              String path)
            throws IOException,
                   NoSuchAlgorithmException,
                   ArtifactTransportException
    {
        Repository repository = getConfiguration().getStorage(storageId).getRepository(repositoryId);

        LayoutProvider layoutPtovider = getLayoutProviderRegistry().getProvider(repository.getLayout());
        RepositoryPath repositoryPath = layoutPtovider.resolve(repository).resolve(path);
        return (ArtifactInputStream) Files.newInputStream(repositoryPath);
    }

    @Override
    public ArtifactOutputStream getOutputStream(String storageId,
                                                String repositoryId,
                                                String path)
            throws IOException, NoSuchAlgorithmException
    {
        Repository repository = getConfiguration().getStorage(storageId).getRepository(repositoryId);

        LayoutProvider layoutPtovider = getLayoutProviderRegistry().getProvider(repository.getLayout());
        RepositoryPath repositoryPath = layoutPtovider.resolve(repository).resolve(path);
        return (ArtifactOutputStream) Files.newOutputStream(repositoryPath);
    }

}
