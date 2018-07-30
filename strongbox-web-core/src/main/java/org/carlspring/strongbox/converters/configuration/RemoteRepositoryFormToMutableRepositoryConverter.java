package org.carlspring.strongbox.converters.configuration;

import org.carlspring.strongbox.forms.configuration.RemoteRepositoryForm;
import org.carlspring.strongbox.storage.repository.remote.MutableRemoteRepository;

import org.springframework.core.convert.converter.Converter;

/**
 * @author Przemyslaw Fusik
 */
public enum RemoteRepositoryFormToMutableRepositoryConverter
        implements Converter<RemoteRepositoryForm, MutableRemoteRepository>
{

    INSTANCE;

    @Override
    public MutableRemoteRepository convert(final RemoteRepositoryForm source)
    {
        MutableRemoteRepository result = new MutableRemoteRepository();
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
