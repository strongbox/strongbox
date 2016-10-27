package org.carlspring.strongbox.rest;

import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.services.ArtifactManagementService;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.metadata.MavenMetadataManager;
import org.carlspring.strongbox.storage.repository.Repository;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public abstract class BaseArtifactController
{

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    @Autowired
    protected ArtifactManagementService artifactManagementService;

    @Autowired
    protected MavenMetadataManager mavenMetadataManager;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected ConfigurationManager configurationManager;


    // ----------------------------------------------------------------------------------------------------------------
    // Common-purpose methods

    protected synchronized <T> T read(String json,
                                      Class<T> type)
    {
        try
        {
            return objectMapper.readValue(json, type);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    protected synchronized ResponseEntity toResponse(Object arg)
    {
        try
        {
            return ResponseEntity.ok(objectMapper.writeValueAsString(arg));
        }
        catch (Exception e)
        {
            return toError(e);
        }
    }

    protected synchronized ResponseEntity toError(String message)
    {
        return toError(new RuntimeException(message));
    }

    protected synchronized ResponseEntity toError(Throwable cause)
    {
        logger.error(cause.getMessage(), cause);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(cause.getMessage());
    }

    public Storage getStorage(String storageId)
    {
        return configurationManager.getConfiguration().getStorage(storageId);
    }

    public Repository getRepository(String storageId,
                                    String repositoryId)
    {
        return getStorage(storageId).getRepository(repositoryId);
    }

    public ArtifactManagementService getArtifactManagementService()
    {
        return artifactManagementService;
    }

    public void setArtifactManagementService(ArtifactManagementService artifactManagementService)
    {
        this.artifactManagementService = artifactManagementService;
    }

    public MavenMetadataManager getMavenMetadataManager()
    {
        return mavenMetadataManager;
    }

    public void setMavenMetadataManager(MavenMetadataManager mavenMetadataManager)
    {
        this.mavenMetadataManager = mavenMetadataManager;
    }

    public String convertRequestToPath(String rootMapping,
                                       HttpServletRequest request,
                                       String... pathVariables)
    {
        int pathVarsLength = 0;
        int pathVarsCount = 0;
        for (String pathVariable : pathVariables)
        {
            if (pathVariable != null)
            {
                pathVarsLength += pathVariable.length();
                pathVarsCount++;
            }
        }

        int totalPrefixLength = rootMapping.length() + pathVarsLength + pathVarsCount + 1;
        int requestUriLength = request.getRequestURI().length();

        // process "/" and "" paths
        if (totalPrefixLength == requestUriLength || totalPrefixLength == requestUriLength + 1)
        {
            return "/";
        }

        if (requestUriLength > totalPrefixLength)
        {
            return request.getRequestURI().substring(totalPrefixLength);
        }
        else
        {
            logger.warn("Unable to calculate path for request uri " + request.getRequestURI());
            return null;
        }
    }
}
