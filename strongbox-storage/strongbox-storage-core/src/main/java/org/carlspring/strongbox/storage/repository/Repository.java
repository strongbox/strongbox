package org.carlspring.strongbox.storage.repository;

import java.util.Set;

import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.remote.RemoteRepository;
import org.carlspring.strongbox.yaml.repository.RepositoryConfiguration;

public interface Repository
{

    String getId();

    String getBasedir();

    String getPolicy();

    String getStorageProvider();

    String getLayout();

    String getType();

    boolean isSecured();

    String getStatus();

    long getArtifactMaxSize();

    boolean isTrashEnabled();

    boolean allowsForceDeletion();

    boolean allowsDeployment();

    boolean allowsRedeployment();

    boolean allowsDeletion();

    boolean allowsDirectoryBrowsing();

    boolean isChecksumHeadersEnabled();

    Set<String> getGroupRepositories();

    Set<String> getArtifactCoordinateValidators();

    Storage getStorage();

    boolean isHostedRepository();

    boolean isProxyRepository();

    boolean isGroupRepository();

    boolean isInService();

    boolean acceptsSnapshots();

    boolean acceptsReleases();
    
    RepositoryConfiguration getRepositoryConfiguration();

    String getStorageIdAndRepositoryId();

    boolean isType(String type);

    RemoteRepository getRemoteRepository();
    
}
