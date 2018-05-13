package org.carlspring.strongbox.storage.indexing;

import org.carlspring.strongbox.storage.repository.Repository;

import javax.inject.Qualifier;
import java.io.IOException;
import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author Przemyslaw Fusik
 */
public interface RepositoryIndexingContextFactory
{

    RepositoryCloseableIndexingContext create(Repository repository)
            throws IOException;

    @Qualifier
    @Retention(RUNTIME)
    @interface RepositoryIndexingContextFactoryQualifier
    {

        IndexTypeEnum value();
    }
}
