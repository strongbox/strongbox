package org.carlspring.strongbox.providers.repository.event;

import org.carlspring.strongbox.data.criteria.Paginator;
import org.carlspring.strongbox.data.criteria.Predicate;
import org.carlspring.strongbox.event.Event;

public class RemoteRepositorySearchEvent extends Event
{

    private String sorageId;
    private String repositoryId;
    private Predicate predicate;
    private Paginator paginator = new Paginator();

    public RemoteRepositorySearchEvent(String sorageId,
                                       String repositoryId,
                                       Predicate predicate,
                                       Paginator paginator)
    {
        super(-1);
        this.sorageId = sorageId;
        this.repositoryId = repositoryId;
        this.predicate = predicate;
        this.paginator = paginator;
    }

    public String getSorageId()
    {
        return sorageId;
    }

    public void setSorageId(String sorageId)
    {
        this.sorageId = sorageId;
    }

    public String getRepositoryId()
    {
        return repositoryId;
    }

    public void setRepositoryId(String repositoryId)
    {
        this.repositoryId = repositoryId;
    }

    public Predicate getPredicate()
    {
        return predicate;
    }

    public void setPredicate(Predicate predicate)
    {
        this.predicate = predicate;
    }

    public Paginator getPaginator()
    {
        return paginator;
    }

    public void setPaginator(Paginator paginator)
    {
        this.paginator = paginator;
    }

}
