package org.carlspring.strongbox.converters.configuration;

import org.carlspring.strongbox.forms.configuration.RepositoryForm;
import org.carlspring.strongbox.storage.repository.MutableHttpConnectionPool;
import org.carlspring.strongbox.storage.repository.MutableRepository;

import org.springframework.core.convert.converter.Converter;

/**
 * @author Przemyslaw Fusik
 */
public enum RepositoryFormToMutableRepositoryConverter
        implements Converter<RepositoryForm, MutableRepository>
{
    INSTANCE;

    @Override
    public MutableRepository convert(final RepositoryForm source)
    {
        MutableRepository result = new MutableRepository();
        result.setId(source.getId());
        result.setPolicy(source.getPolicy());
        result.setImplementation(source.getImplementation());
        result.setLayout(source.getLayout());
        result.setType(source.getType());
        result.setSecured(source.isSecured());
        result.setStatus(source.getStatus());
        result.setArtifactMaxSize(source.getArtifactMaxSize());
        result.setTrashEnabled(source.isTrashEnabled());
        result.setAllowsForceDeletion(source.isAllowsForceDeletion());
        result.setAllowsDeployment(source.isAllowsDeployment());
        result.setAllowsRedeployment(source.isAllowsRedeployment());
        result.setAllowsDelete(source.isAllowsDelete());
        result.setAllowsDirectoryBrowsing(source.isAllowsDirectoryBrowsing());
        result.setChecksumHeadersEnabled(source.isChecksumHeadersEnabled());
        result.setProxyConfiguration(
                ProxyConfigurationFormToProxyConfigurationConverter.INSTANCE.convert(source.getProxyConfiguration()));
        result.setRemoteRepository(
                RemoteRepositoryFormToMutableRepositoryConverter.INSTANCE.convert(source.getRemoteRepository()));
        MutableHttpConnectionPool httpConnectionPool = new MutableHttpConnectionPool();
        httpConnectionPool.setAllocatedConnections(source.getHttpConnectionPool());
        result.setHttpConnectionPool(httpConnectionPool);
        if (source.getGroupRepositories() != null)
        {
            result.setGroupRepositories(source.getGroupRepositories());
        }
        if (source.getArtifactCoordinateValidators() != null)
        {
            result.setArtifactCoordinateValidators(source.getArtifactCoordinateValidators());
        }
        result.setBasedir(source.getBasedir());
        return result;
    }
}
