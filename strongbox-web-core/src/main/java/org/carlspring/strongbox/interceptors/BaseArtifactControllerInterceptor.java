package org.carlspring.strongbox.interceptors;

import org.carlspring.strongbox.providers.io.RepositoryPathResolver;
import org.carlspring.strongbox.services.ConfigurationManagementService;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import static org.springframework.http.HttpStatus.NOT_FOUND;

/**
 * @author Przemyslaw Fusik
 */
public class BaseArtifactControllerInterceptor
        extends HandlerInterceptorAdapter
{

    @Autowired
    protected RepositoryPathResolver repositoryPathResolver;

    @Autowired
    protected ConfigurationManagementService configurationManagementService;

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

        final String storageId = (String) pathVariables.get("storageId");
        final String repositoryId = (String) pathVariables.get("repositoryId");

        if (StringUtils.isBlank(storageId) || StringUtils.isBlank(repositoryId))
        {
            return true;
        }

        final Storage storage = getStorage(storageId);
        if (storage == null)
        {
            response.sendError(NOT_FOUND.value(), "The specified storageId does not exist!");
            return false;
        }
        final Repository repository = storage.getRepository(repositoryId);
        if (repository == null)
        {
            response.sendError(NOT_FOUND.value(), "The specified repositoryId does not exist!");
            return false;
        }
        if (!repository.isInService())
        {
            response.sendError(HttpStatus.SERVICE_UNAVAILABLE.value(), "Repository is not in service...");
            return false;
        }

        return true;
    }

    protected Storage getStorage(String storageId)
    {
        return configurationManagementService.getConfiguration().getStorage(storageId);
    }
}
