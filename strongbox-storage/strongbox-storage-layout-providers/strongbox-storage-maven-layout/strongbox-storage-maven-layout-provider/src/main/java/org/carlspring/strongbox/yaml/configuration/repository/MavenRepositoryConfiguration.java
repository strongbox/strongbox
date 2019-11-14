package org.carlspring.strongbox.yaml.configuration.repository;

import org.carlspring.strongbox.yaml.repository.RepositoryConfiguration;

public interface MavenRepositoryConfiguration extends RepositoryConfiguration
{

    boolean isIndexingEnabled();

    boolean isIndexingClassNamesEnabled();

    String getCronExpression();

    String getMetadataExpirationStrategy();

}