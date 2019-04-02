package org.carlspring.strongbox.xml.configuration.repository;

import org.carlspring.strongbox.xml.repository.RepositoryConfiguration;

public interface MavenRepositoryConfiguration extends RepositoryConfiguration
{

    boolean isIndexingEnabled();

    boolean isIndexingClassNamesEnabled();

}