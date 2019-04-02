package org.carlspring.strongbox.xml.configuration.repository;

import org.carlspring.strongbox.xml.repository.RepositoryConfiguration;

public interface NugetRepositoryConfiguration extends RepositoryConfiguration
{

    String getFeedVersion();

    Integer getRemoteFeedPageSize();

}