package org.carlspring.strongbox.interceptors;

import org.carlspring.strongbox.services.ConfigurationManagementService;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import static org.springframework.http.HttpStatus.NOT_FOUND;

/**
 * @author Przemyslaw Fusik
 */
public class RepositoryRequestInterceptor
        extends HandlerInterceptorAdapter
{

    private final String storageIdPathVariableName;

    private final String repositoryIdPathVariableName;

    private final String exposedRepositoryRequestAttributeName;

    private final ConfigurationManagementService configurationManagementService;

    public RepositoryRequestInterceptor(String storageIdPathVariableName,
                                        String repositoryIdPathVariableName,
                                        String exposedRepositoryRequestAttributeName,
                                        ConfigurationManagementService configurationManagementService)
    {
        this.storageIdPathVariableName = storageIdPathVariableName;
        this.repositoryIdPathVariableName = repositoryIdPathVariableName;
        this.exposedRepositoryRequestAttributeName = exposedRepositoryRequestAttributeName;
        this.configurationManagementService = configurationManagementService;
    }

    @Override
    public boolean preHandle(final HttpServletRequest request,
                             final HttpServletResponse response,
                             final Object handler)
            throws IOException
    {
        final Map pathVariables = (Map) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        if (pathVariables == null)
        {
            return true;
        }

        final String storageId = (String) pathVariables.get(storageIdPathVariableName);
        final String repositoryId = (String) pathVariables.get(repositoryIdPathVariableName);

        if (StringUtils.isBlank(storageId) || StringUtils.isBlank(repositoryId))
        {
            return true;
        }

        final Storage storage = getStorage(storageId);
        if (storage == null)
        {
            response.sendError(NOT_FOUND.value(), "The specified storage does not exist!");
            return false;
        }
        final Repository repository = storage.getRepository(repositoryId);
        if (repository == null)
        {
            response.sendError(NOT_FOUND.value(), "The specified repository does not exist!");
            return false;
        }
        if (!repository.isInService())
        {
            response.sendError(HttpStatus.SERVICE_UNAVAILABLE.value(), "Repository is not in service...");
            return false;
        }

        exposeRepository(repository, request);

        return true;
    }

    protected Storage getStorage(String storageId)
    {
        return configurationManagementService.getConfiguration().getStorage(storageId);
    }

    /**
     * Exposes the {@link Repository} as request attribute.
     */
    protected void exposeRepository(final Repository repository,
                                    final HttpServletRequest request)
    {
        request.setAttribute(exposedRepositoryRequestAttributeName, repository);
    }
}
