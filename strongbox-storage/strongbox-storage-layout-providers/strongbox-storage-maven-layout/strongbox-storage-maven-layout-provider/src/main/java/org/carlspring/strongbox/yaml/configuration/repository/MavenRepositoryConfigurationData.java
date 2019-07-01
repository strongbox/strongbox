package org.carlspring.strongbox.yaml.configuration.repository;

import org.carlspring.strongbox.yaml.repository.RepositoryConfiguration;

public interface MavenRepositoryConfigurationData extends RepositoryConfiguration
{

    boolean isIndexingEnabled();

    boolean isIndexingClassNamesEnabled();

}