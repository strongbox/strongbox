package org.carlspring.strongbox.yaml.configuration.repository;

import org.carlspring.strongbox.yaml.repository.RepositoryConfiguration;

public interface NugetRepositoryConfiguration extends RepositoryConfiguration
{

    String getFeedVersion();

    Integer getRemoteFeedPageSize();

}