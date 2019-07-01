package org.carlspring.strongbox.storage.repository;

import java.util.Set;

import org.carlspring.strongbox.storage.StorageData;
import org.carlspring.strongbox.yaml.repository.RepositoryConfiguration;

public interface RepositoryData
{

    String getId();

    String getBasedir();

    String getPolicy();

    String getImplementation();

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

    StorageData getStorage();

    boolean isHostedRepository();

    boolean isProxyRepository();

    boolean isGroupRepository();

    boolean isInService();

    boolean acceptsSnapshots();

    boolean acceptsReleases();
    
    RepositoryConfiguration getRepositoryConfiguration();
    
}