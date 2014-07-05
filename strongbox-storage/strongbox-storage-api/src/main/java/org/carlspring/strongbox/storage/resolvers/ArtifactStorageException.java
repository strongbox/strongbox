package org.carlspring.strongbox.storage.resolvers;

/**
 * @author mtodorov
 */
public class ArtifactStorageException extends Exception
{

    public ArtifactStorageException()
    {
    }

    public ArtifactStorageException(String message)
    {
        super(message);
    }

    public ArtifactStorageException(String message,
                                    Throwable cause)
    {
        super(message, cause);
    }

    public ArtifactStorageException(Throwable cause)
    {
        super(cause);
    }

}
