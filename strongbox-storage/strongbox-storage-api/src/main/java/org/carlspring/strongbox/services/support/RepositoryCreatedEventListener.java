package org.carlspring.strongbox.services.support;

import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.event.repository.RepositoryEvent;
import org.carlspring.strongbox.event.repository.RepositoryEventTypeEnum;
import org.carlspring.strongbox.services.TrustStoreService;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.RepositoryData;
import org.carlspring.strongbox.storage.repository.remote.RemoteRepository;

import javax.inject.Inject;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * @author Przemyslaw Fusik
 */
@Component
public class RepositoryCreatedEventListener
{

    private static final Logger logger = LoggerFactory.getLogger(RepositoryCreatedEventListener.class);


    @Inject
    private ConfigurationManager configurationManager;

    @Inject
    private TrustStoreService trustStoreService;

    @EventListener
    public void handle(RepositoryEvent event)
    {
        if (event.getType() != RepositoryEventTypeEnum.EVENT_REPOSITORY_CREATED.getType())
        {
            return;
        }

        Repository repository = configurationManager.getConfiguration().getStorage(event.getStorageId()).getRepository(
                event.getRepositoryId());

        if (((RepositoryData)repository).getRemoteRepository() != null)
        {
            initializeRemoteRepository(repository.getRemoteRepository());
        }
    }

    private void initializeRemoteRepository(RemoteRepository remoteRepository)
    {
        if (remoteRepository.isAutoImportRemoteSSLCertificate())
        {
            try
            {
                trustStoreService.addSslCertificatesToTrustStore(remoteRepository.getUrl());
            }
            catch (IOException | TrustStoreCertificateOperationException e)
            {
                logger.error("Could not import remote SSL certificate to trust store", e);
            }
        }
    }


}
