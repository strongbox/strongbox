package org.carlspring.strongbox.storage.repository.remote;

import org.carlspring.strongbox.yaml.repository.remote.CustomRemoteRepositoryConfiguration;

import java.io.Serializable;

/**
 * @author Przemyslaw Fusik
 */
public interface RemoteRepository
        extends Serializable
{

    String getUrl();

    boolean isDownloadRemoteIndexes();

    boolean isAutoBlocking();

    boolean isChecksumValidation();

    String getUsername();

    String getPassword();

    String getChecksumPolicy();

    Integer getCheckIntervalSeconds();

    boolean allowsDirectoryBrowsing();

    boolean isAutoImportRemoteSSLCertificate();

    CustomRemoteRepositoryConfiguration getCustomConfiguration();
}
