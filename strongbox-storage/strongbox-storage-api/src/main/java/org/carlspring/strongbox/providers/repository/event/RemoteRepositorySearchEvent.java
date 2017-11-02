package org.carlspring.strongbox.providers.repository.event;

import org.carlspring.strongbox.event.Event;
import org.carlspring.strongbox.providers.repository.RepositoryPageRequest;
import org.carlspring.strongbox.providers.repository.RepositorySearchRequest;

public class RemoteRepositorySearchEvent extends Event
{

    private RepositorySearchRequest searchRequest;

    private RepositoryPageRequest pageRequest;

    public RemoteRepositorySearchEvent(RepositorySearchRequest searchRequest,
                                       RepositoryPageRequest pageRequest)
    {
        super(-1);
        this.searchRequest = searchRequest;
        this.pageRequest = pageRequest;
    }

    public RepositorySearchRequest getSearchRequest()
    {
        return searchRequest;
    }

    public void setSearchRequest(RepositorySearchRequest searchRequest)
    {
        this.searchRequest = searchRequest;
    }

    public RepositoryPageRequest getPageRequest()
    {
        return pageRequest;
    }

    public void setPageRequest(RepositoryPageRequest pageRequest)
    {
        this.pageRequest = pageRequest;
    }

}
