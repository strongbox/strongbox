package org.carlspring.strongbox.controllers.layout.pypi;

import org.carlspring.strongbox.controllers.BaseArtifactController;
import org.carlspring.strongbox.providers.io.RepositoryPath;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
                headers = { /*"user-agent=pip/*",*/ "user-agent=Python-urllib/*" })
public class PypiArtifactController
        extends BaseArtifactController
{

    private static final Logger logger = LoggerFactory.getLogger(PypiArtifactController.class);

    public final static String ROOT_CONTEXT = "/storages";


    @PreAuthorize("hasAuthority('ARTIFACTS_DEPLOY')")
    @PostMapping("{storageId}/{repositoryId}")
    public ResponseEntity uploadViaPost(@PathVariable(name = "storageId") String storageId,
                                        @PathVariable(name = "repositoryId") String repositoryId,
                                        @RequestParam("content") MultipartFile multipartFile,
                                        HttpServletRequest request)
            throws IOException
    {
        String path = multipartFile.getOriginalFilename();

        logger.debug("Received upload request for " + path + "...");

        InputStream is = request.getInputStream();

        try
        {
            RepositoryPath repositoryPath = repositoryPathResolver.resolve(storageId, repositoryId, path);

            System.out.println("is != null : " + (is != null));
            System.out.println("repositoryPath != null : " + (repositoryPath != null) + " " + repositoryPath.toString());
            System.out.println("artifactManagementService != null : " + (artifactManagementService != null));

            if (!repositoryPath.getRepository().isInService())
            {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("Repository is not in service.");
            }

            // TODO: Use artifactManagementService.validateAndStore(repositoryPath, request.getInputStream());
            // TODO: when the actual PypiWheelArtifactCoordinates pull request is ready.
            // TODO: This is currently throwing an (expected) NullPointerException.
            artifactManagementService.store(repositoryPath, request.getInputStream());

            return ResponseEntity.ok("The artifact was deployed successfully.");
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

}
