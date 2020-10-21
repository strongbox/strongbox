package org.carlspring.strongbox.converters.configuration;

import org.carlspring.strongbox.forms.configuration.RemoteRepositoryForm;
import org.carlspring.strongbox.storage.repository.remote.RemoteRepositoryDto;

import org.springframework.core.convert.converter.Converter;

/**
 * @author Przemyslaw Fusik
 */
public enum RemoteRepositoryFormConverter
        implements Converter<RemoteRepositoryForm, RemoteRepositoryDto>
{

    INSTANCE;

    @Override
    public RemoteRepositoryDto convert(final RemoteRepositoryForm source)
    {
        RemoteRepositoryDto result = new RemoteRepositoryDto();
        result.setUrl(source.getUrl());
        result.setDownloadRemoteIndexes(source.isDownloadRemoteIndexes());
        result.setAutoBlocking(source.isAutoBlocking());
        result.setChecksumValidation(source.isChecksumValidation());
        result.setUsername(source.getUsername());
        result.setPassword(source.getPassword());
        result.setChecksumPolicy(source.getChecksumPolicy());
        result.setCheckIntervalSeconds(source.getCheckIntervalSeconds());
        result.setAllowsDirectoryBrowsing(source.isAllowsDirectoryBrowsing());
        result.setAutoImportRemoteSSLCertificate(source.isAutoImportRemoteSSLCertificate());
        return result;
    }
}
