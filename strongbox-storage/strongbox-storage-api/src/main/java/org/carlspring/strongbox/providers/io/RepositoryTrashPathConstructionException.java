package org.carlspring.strongbox.providers.io;

import java.io.IOException;

/**
 * @author Przemyslaw Fusik
 */
public class RepositoryTrashPathConstructionException
        extends RepositoryPathConstructionException
{

    public RepositoryTrashPathConstructionException(final Exception e)
    {
        super(e);
    }
}
