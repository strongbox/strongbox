package org.carlspring.strongbox.controllers.layout.pypi;

import org.carlspring.strongbox.controllers.BaseArtifactController;
import org.carlspring.strongbox.providers.ProviderImplementationException;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.services.ArtifactManagementService;
import org.carlspring.strongbox.storage.validation.artifact.ArtifactCoordinatesValidationException;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * This controller is used to handle PyPi requests.
 * 
 * @author carlspring
 */
@RestController
@RequestMapping(path = PypiArtifactController.ROOT_CONTEXT,
                headers = "user-agent=pip/*")
public class PypiArtifactController
        extends BaseArtifactController
{

    private static final Logger logger = LoggerFactory.getLogger(PypiArtifactController.class);

    public final static String ROOT_CONTEXT = "/storages";

    @Inject
    private ArtifactManagementService pypiArtifactManagementService;


    @PreAuthorize("hasAuthority('ARTIFACTS_DEPLOY')")
    @PostMapping("{storageId}/{repositoryId}/{path:.+}")
    public void uploadViaPost(@PathVariable(name = "storageId") String storageId,
                              @PathVariable(name = "repositoryId") String repositoryId,
                              @PathVariable(name = "path") String path,
                              @RequestParam("file") MultipartFile file,
                              HttpServletRequest request)
            throws IOException,
                   ProviderImplementationException,
                   NoSuchAlgorithmException,
                   ArtifactCoordinatesValidationException
    {
        InputStream is = file.getInputStream();
        RepositoryPath repositoryPath = artifactResolutionService.resolvePath(storageId,
                                                                              repositoryId,
                                                                              path);

        logger.debug("Received upload request for " + path + "...");

        Enumeration headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements())
        {
            String key = (String) headerNames.nextElement();
            String value = request.getHeader(key);

            System.out.println("   " + key + ": " + value);
        }

        pypiArtifactManagementService.validateAndStore(repositoryPath, is);
    }

}
