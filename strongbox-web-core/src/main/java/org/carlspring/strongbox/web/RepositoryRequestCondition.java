package org.carlspring.strongbox.web;

import org.carlspring.strongbox.storage.repository.RepositoryData;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.Collections;

import static org.carlspring.strongbox.web.Constants.REPOSITORY_REQUEST_ATTRIBUTE;

/**
 * @author Przemyslaw Fusik
 */
public class RepositoryRequestCondition
        extends ExposableRequestCondition
{

    private final RepositoryData repository;

    RepositoryRequestCondition(@Nonnull final RepositoryData repository)
    {
        this.repository = repository;
    }

    @Override
    protected Collection<?> getContent()
    {
        return Collections.singletonList(repository);
    }

    @Override
    protected String getToStringInfix()
    {
        return repository.getId();
    }

    @Nonnull
    protected RepositoryData getRepository()
    {
        return this.repository;
    }

    @Override
    protected void expose(HttpServletRequest request)
    {
        request.setAttribute(REPOSITORY_REQUEST_ATTRIBUTE, repository);
    }
}
