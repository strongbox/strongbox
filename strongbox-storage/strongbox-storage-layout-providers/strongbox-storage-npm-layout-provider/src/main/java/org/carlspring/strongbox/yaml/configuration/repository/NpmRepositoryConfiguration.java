package org.carlspring.strongbox.yaml.configuration.repository;

import org.carlspring.strongbox.yaml.repository.RepositoryConfiguration;

/**
 * @author ankit.tomar
 */
public interface NpmRepositoryConfiguration extends RepositoryConfiguration
{

    String getCronExpression();

    boolean isCronEnabled();

}
