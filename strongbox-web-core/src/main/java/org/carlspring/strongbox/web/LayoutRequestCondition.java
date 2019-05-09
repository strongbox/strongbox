package org.carlspring.strongbox.web;

import org.carlspring.strongbox.configuration.StoragesConfigurationManager;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import org.springframework.web.servlet.mvc.condition.AbstractRequestCondition;
import static org.carlspring.strongbox.web.Constants.ARTIFACT_ROOT_PATH;

/**
 * @author sbespalov
 */
public class LayoutRequestCondition
        extends AbstractRequestCondition<LayoutRequestCondition>
{

    private static final String ARTIFACT_COPY_PATH = ARTIFACT_ROOT_PATH + "/copy";

    private final String layout;
    private final StoragesConfigurationManager configurationManager;

    public LayoutRequestCondition(StoragesConfigurationManager configurationManager,
                                  String layout)
    {
        this.layout = layout;
        this.configurationManager = configurationManager;
    }

    @Override
    public LayoutRequestCondition combine(LayoutRequestCondition other)
    {
        return this;
    }

    @Override
    public LayoutRequestCondition getMatchingCondition(HttpServletRequest request)
    {
        String servletPath = Optional.ofNullable(request.getServletPath()).filter(s -> s != null && s.trim().length() > 0).orElse(request.getPathInfo());
        if (servletPath.startsWith(ARTIFACT_COPY_PATH))
        {
            return getPathCopyCondition(request);
        }
        else if (servletPath.startsWith(ARTIFACT_ROOT_PATH))
        {
            return getStorageAndRepositoryCondition(servletPath);
        }

        return null;
    }

    private LayoutRequestCondition getPathCopyCondition(HttpServletRequest request)
    {
        String storageId = request.getParameter("srcStorageId");
        if (storageId == null)
        {
            return null;
        }
        String repositoryId = request.getParameter("srcRepositoryId");
        if (repositoryId == null)
        {
            return null;
        }

        return getStorageAndRepositoryCondition(storageId, repositoryId);
    }

    private LayoutRequestCondition getStorageAndRepositoryCondition(String servletPath)
    {
        String[] pathParts = servletPath.split("/");
        if (pathParts.length < 4)
        {
            return null;
        }

        String storageId = pathParts[2];
        String repositoryId = pathParts[3];

        return getStorageAndRepositoryCondition(storageId, repositoryId);
    }

    private LayoutRequestCondition getStorageAndRepositoryCondition(String storageId,
                                                                    String repositoryId)
    {
        Storage storage = configurationManager.getStorage(storageId);
        if (storage == null)
        {
            return null;
        }
        Repository repository = configurationManager.getRepository(storageId, repositoryId);
        if (repository == null)
        {
            return null;
        }

        String requestedLayout = repository.getLayout();
        if (!layout.equals(requestedLayout))
        {
            return null;
        }

        return this;
    }

    @Override
    public int compareTo(LayoutRequestCondition other,
                         HttpServletRequest request)
    {
        return 1;
    }

    @Override
    protected Collection<?> getContent()
    {
        return Collections.singleton(layout);
    }

    @Override
    protected String getToStringInfix()
    {
        return layout;
    }

}
