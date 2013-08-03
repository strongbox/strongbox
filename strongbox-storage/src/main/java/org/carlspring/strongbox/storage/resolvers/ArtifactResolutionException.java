package org.carlspring.strongbox.storage.resolvers;

/**
 * @author mtodorov
 */
public class ArtifactResolutionException extends Exception
{

    public ArtifactResolutionException()
    {
    }

    public ArtifactResolutionException(String message)
    {
        super(message);
    }

    public ArtifactResolutionException(String message,
                                       Throwable cause)
    {
        super(message, cause);
    }

    public ArtifactResolutionException(Throwable cause)
    {
        super(cause);
    }

}