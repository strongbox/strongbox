package org.carlspring.strongbox.repository;

/**
 * @author Przemyslaw Fusik
 */
public class ArchiveListinger
{
    private final ArchiveListingFunction archiveListingFunction;


    public ArchiveListinger(final ArchiveListingFunction archiveListingFunction)
    {
        this.archiveListingFunction = archiveListingFunction;
    }
}
