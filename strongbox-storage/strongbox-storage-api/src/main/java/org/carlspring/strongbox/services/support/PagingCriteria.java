package org.carlspring.strongbox.services.support;

/**
 * @author Przemyslaw Fusik
 */
public class PagingCriteria
{

    public static final PagingCriteria ALL = new PagingCriteria(0, -1);

    private final int skip;

    private final int limit;

    public PagingCriteria(int skip,
                          int limit)
    {
        this.skip = skip;
        this.limit = limit;
    }
}
