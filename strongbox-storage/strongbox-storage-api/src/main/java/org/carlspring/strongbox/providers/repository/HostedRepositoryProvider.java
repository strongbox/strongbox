package org.carlspring.strongbox.providers.repository;

import org.carlspring.strongbox.client.ArtifactTransportException;
import org.carlspring.strongbox.io.ArtifactInputStream;
import org.carlspring.strongbox.storage.repository.Repository;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;

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

        return getLayoutProviderRegistry().getProvider(repository.getLayout())
                                          .getInputStream(storageId, repositoryId, path);
    }

    @Override
    public OutputStream getOutputStream(String storageId,
                                        String repositoryId,
                                        String path)
            throws IOException
    {
        Repository repository = getConfiguration().getStorage(storageId).getRepository(repositoryId);

        return getLayoutProviderRegistry().getProvider(repository.getLayout())
                                          .getOutputStream(storageId, repositoryId, path);
    }

}
