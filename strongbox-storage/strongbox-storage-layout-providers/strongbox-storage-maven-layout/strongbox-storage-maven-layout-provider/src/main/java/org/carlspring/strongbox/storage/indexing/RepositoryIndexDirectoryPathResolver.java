package org.carlspring.strongbox.storage.indexing;

import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.storage.repository.Repository;

import javax.inject.Qualifier;
import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author Przemyslaw Fusik
 */
public interface RepositoryIndexDirectoryPathResolver
{

    RepositoryPath resolve(Repository repository);

    @Qualifier
    @Retention(RUNTIME)
    @interface RepositoryIndexDirectoryPathResolverQualifier
    {

        IndexTypeEnum value();
    }
}
