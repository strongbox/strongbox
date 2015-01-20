package org.carlspring.strongbox.services;

import org.carlspring.strongbox.configuration.Configuration;
import org.carlspring.strongbox.configuration.ProxyConfiguration;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;

import javax.xml.bind.JAXBException;
import java.io.IOException;

/**
 * @author mtodorov
 */
public interface ConfigurationManagementService
{

    void setConfiguration(Configuration configuration)
            throws IOException, JAXBException;

    Configuration getConfiguration()
            throws IOException;

    String getBaseUrl()
            throws IOException;

    void setBaseUrl(String baseUrl)
            throws IOException, JAXBException;

    int getPort()
            throws IOException;

    void setPort(int port)
            throws IOException, JAXBException;

    void setProxyConfiguration(String storageId,
                               String repositoryId,
                               ProxyConfiguration proxyConfiguration)
            throws IOException, JAXBException;

    void addOrUpdateStorage(Storage storage)
            throws IOException, JAXBException;

    Storage getStorage(String storageId)
            throws IOException;

    void removeStorage(String storageId)
            throws IOException, JAXBException;

    void addOrUpdateRepository(String storageId, Repository repository)
            throws IOException, JAXBException;

    Repository getRepository(String storageId, String repositoryId)
            throws IOException;

    void removeRepository(String storageId, String repositoryId)
            throws IOException, JAXBException;

    ProxyConfiguration getProxyConfiguration()
            throws IOException, JAXBException;

}
