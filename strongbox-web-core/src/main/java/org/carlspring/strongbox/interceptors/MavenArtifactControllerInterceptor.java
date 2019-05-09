package org.carlspring.strongbox.interceptors;

import org.carlspring.strongbox.artifact.MavenArtifact;
import org.carlspring.strongbox.artifact.MavenArtifactUtils;
import org.carlspring.strongbox.controllers.layout.maven.MavenArtifactController;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.io.RepositoryPathResolver;
import org.carlspring.strongbox.services.ConfigurationManagementService;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerMapping;
import static org.carlspring.strongbox.storage.metadata.MetadataHelper.MAVEN_METADATA_XML;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

/**
 * @author Przemyslaw Fusik
 */
public class MavenArtifactControllerInterceptor
        extends BaseArtifactControllerInterceptor
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
        final HandlerMethod handlerMethod = (HandlerMethod) handler;
        if (handlerMethod == null || !MavenArtifactController.class.equals(handlerMethod.getBeanType()))
        {
            return true;
        }

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

        final String path = (String) pathVariables.get("path");

        final Storage storage = getStorage(storageId);
        final Repository repository = storage.getRepository(repositoryId);
        if (StringUtils.isBlank(path))
        {
            response.sendError(BAD_REQUEST.value(), "Path should be provided!");
            return false;
        }
        if (path.endsWith("/"))
        {
            response.sendError(BAD_REQUEST.value(), "The specified path should not ends with `/` character!");
            return false;
        }

        final boolean mavenArtifactRequest = isMavenArtifactRequest(request.getMethod());
        if (mavenArtifactRequest)
        {
            final RepositoryPath repositoryPath = repositoryPathResolver.resolve(repository, path);
            if (Files.exists(repositoryPath) && Files.isDirectory(repositoryPath))
            {
                response.sendError(BAD_REQUEST.value(), "The specified path is a directory!");
                return false;
            }
            final MavenArtifact mavenArtifact = MavenArtifactUtils.convertPathToArtifact(repositoryPath);
            if (StringUtils.isBlank(mavenArtifact.getArtifactId()))
            {
                response.sendError(BAD_REQUEST.value(),
                                   "The specified path is invalid. Maven artifact artifactId not recognized.");
                return false;
            }
            if (StringUtils.isBlank(mavenArtifact.getGroupId()))
            {
                response.sendError(BAD_REQUEST.value(),
                                   "The specified path is invalid. Maven artifact groupId not recognized.");
                return false;
            }
            if (!mavenArtifact.getArtifactId().startsWith(MAVEN_METADATA_XML))
            {
                if (StringUtils.isBlank(mavenArtifact.getVersion()))
                {
                    response.sendError(BAD_REQUEST.value(),
                                       "The specified path is invalid. Maven artifact version not recognized.");
                    return false;
                }
                if (StringUtils.isBlank(mavenArtifact.getType()))
                {
                    response.sendError(BAD_REQUEST.value(),
                                       "The specified path is invalid. Maven artifact type not recognized.");
                    return false;
                }
            }
        }

        return true;
    }

    private static boolean isMavenArtifactRequest(final String method)
    {
        return "get".equalsIgnoreCase(method) ||
               "head".equalsIgnoreCase(method) ||
               "post".equalsIgnoreCase(method) ||
               "put".equalsIgnoreCase(method) ||
               "patch".equalsIgnoreCase(method);
    }

}
