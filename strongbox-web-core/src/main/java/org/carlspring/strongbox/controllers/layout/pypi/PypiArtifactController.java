package org.carlspring.strongbox.controllers.layout.pypi;

import org.carlspring.strongbox.artifact.coordinates.PypiArtifactCoordinates;
import org.carlspring.strongbox.controllers.BaseArtifactController;
import org.carlspring.strongbox.metadata.PypiArtifactMetadata;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;


/**
 * This controller is used to handle PyPi requests.
 *
 * @author carlspring
 */
@RestController
/*
@RequestMapping(path = MavenArtifactController.ROOT_CONTEXT, headers = "user-agent=Maven/*")
*/
@RequestMapping(path = PypiArtifactController.ROOT_CONTEXT,
                headers = { "user-agent=Python-urllib/*",
                            /*
                            ,
                            "user-agent=pip/*",
                            "user-agent=twine/*",
                            "user-agent=requests/*",
                            "user-agent=setuptools/*",
                            "user-agent=requests-toolbelt/*",
                            "user-agent=tqdm/*"
                            */
                })
public class PypiArtifactController
        extends BaseArtifactController
{

    private static final Logger logger = LoggerFactory.getLogger(PypiArtifactController.class);

    public final static String ROOT_CONTEXT = "/storages";


    @ApiOperation(value = "Used to deploy an artifact")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "The artifact was deployed successfully."),
                            @ApiResponse(code = 400, message = "An error occurred.") })
    @PreAuthorize("hasAuthority('ARTIFACTS_DEPLOY')")
    @PostMapping(value = "{storageId}/{repositoryId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity uploadViaPost(@ApiParam(value = "The storageId", required = true)
                                        @PathVariable(name = "storageId") String storageId,
                                        @ApiParam(value = "The storageId", required = true)
                                        @PathVariable(name = "repositoryId") String repositoryId,
                                        @ApiParam(value = "The content", required = true)
                                        @RequestPart("content")
                                        MultipartFile multipartFile,
                                        @ApiParam(value = "The name of the PyPi artifact", required = true)
                                        @RequestParam("name")
                                        String name,
                                        @ApiParam(value = "The platform", required = true)
                                        @RequestParam(name = "platform", required = false)
                                        String platform,
                                        @ApiParam(value = "The version of the artifact", required = true)
                                        @RequestParam("version")
                                        String version,
                                        @ApiParam(value = "A summary for the artifact", required = true)
                                        @RequestParam("summary")
                                        String summary,
                                        @ApiParam(value = "A description of the artifact", required = true)
                                        @RequestParam("description")
                                        String description,
                                        @ApiParam(value = "The MD5 digest of the artifact", required = true)
                                        @RequestParam("md5_digest")
                                        String md5Digest,
                                        @ApiParam(value = "The artifact's license", required = true)
                                        @RequestParam("license")
                                        String license,
                                        @ApiParam(value = "The version of Python required for this artifact",
                                                  required = true)
                                        @RequestParam("pyversion")
                                        String pythonVersion,
                                        @ApiParam(value = "A comment for the artifact", required = true)
                                        @RequestParam("comment")
                                        String comment,
                                        @ApiParam(value = "The artifact's author", required = true)
                                        @RequestParam("author")
                                        String author,
                                        @ApiParam(value = "The artifact author's e-mail", required = true)
                                        @RequestParam("author_email")
                                        String authorEmail,
                                        @ApiParam(value = "The PyPi protocol version. (Please, note that there is" +
                                                          " typo in the PyPi implementation).",
                                                  required = true)
                                        @RequestParam("protcol_version")
                                        String protocolVersion,
                                        @ApiParam(value = "The file type", required = true)
                                        @RequestParam("filetype")
                                        String fileType,
                                        @ApiParam(value = "The version of the metadata", required = true)
                                        @RequestParam("metadata_version")
                                        String metadataVersion,
                                        @ApiParam(value = "The project's home page", required = true)
                                        @RequestParam("home_page")
                                        String homePage,
                                        @ApiParam(value = "The artifact's download URL", required = true)
                                        @RequestParam("download_url")
                                        String downloadUrl,
                                        HttpServletRequest request)
            throws IOException
    {
        String path = multipartFile.getOriginalFilename();

        logger.debug("Received upload request for /" + storageId + "/" + repositoryId + "/" + path + "...");

        logger.debug(" name = {}, platform = {}, version = {}," +
                     " summary = {}, description = {}, md5Digest = {}, license = {}," +
                     " pythonVersion = {}, comment = {}, author = {}, authorEmail = {}," +
                     " protocolVersion = {}, fileType = {}, metadataVersion = {}, homePage = {}, download_url = {}",
                     name, platform, version,
                     summary, description, md5Digest, license,
                     pythonVersion, comment, author, authorEmail,
                     protocolVersion, fileType, metadataVersion, homePage, downloadUrl);

        PypiArtifactMetadata pypiArtifactMetadata = new PypiArtifactMetadata(name,
                                                                             platform,
                                                                             version,
                                                                             summary,
                                                                             description,
                                                                             md5Digest,
                                                                             license,
                                                                             pythonVersion,
                                                                             comment,
                                                                             author,
                                                                             authorEmail,
                                                                             protocolVersion,
                                                                             fileType,
                                                                             metadataVersion,
                                                                             homePage,
                                                                             downloadUrl);

        InputStream is = multipartFile.getInputStream();

        PypiArtifactCoordinates coordinates = PypiArtifactCoordinates.parse(path);

        RepositoryPath repositoryPath = repositoryPathResolver.resolve(storageId, repositoryId, coordinates.toPath());

        File file = new File(configurationManager.getRepository(storageId, repositoryId).getBasedir(),
                             multipartFile.getOriginalFilename());

        logger.debug("Storing " + file.getAbsolutePath() + "...");
        
        try
        {
            if (!repositoryPath.getRepository().isInService())
            {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("Repository is not in service.");
            }

            // TODO: Use artifactManagementService.validateAndStore(repositoryPath, request.getInputStream());
            // TODO: when the actual PypiWheelArtifactCoordinates pull request is ready.
            // TODO: This is currently throwing an (expected) NullPointerException.
            // artifactManagementService.validateAndStore(repositoryPath, is);
            artifactManagementService.validateAndStore(repositoryPath, is);

            return ResponseEntity.ok("The artifact was deployed successfully.");
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

//    @ApiOperation(value = "Used to retrieve an artifact")
//    @ApiResponses(value = { @ApiResponse(code = 200, message = ""),
//                            @ApiResponse(code = 404, message = "Requested path not found."),
//                            @ApiResponse(code = 500, message = "Server error."),
//                            @ApiResponse(code = 503, message = "Repository currently not in service.")})
//    @PreAuthorize("hasAuthority('ARTIFACTS_RESOLVE')")
//    @RequestMapping(value = { "/{storageId}/{repositoryId}/{path:.+}" }, method = {RequestMethod.GET, RequestMethod.HEAD})
//    public void download(@ApiParam(value = "The storageId", required = true)
//                         @PathVariable String storageId,
//                         @ApiParam(value = "The repositoryId", required = true)
//                         @PathVariable String repositoryId,
//                         @RequestHeader HttpHeaders httpHeaders,
//                         @PathVariable String path,
//                         HttpServletRequest request,
//                         HttpServletResponse response)
//            throws Exception
//    {
//        logger.debug("Requested /" + storageId + "/" + repositoryId + "/" + path + ".");
//
//        Storage storage = configurationManager.getConfiguration().getStorage(storageId);
//        if (storage == null)
//        {
//            logger.error("Unable to find storage by ID " + storageId);
//
//            response.sendError(INTERNAL_SERVER_ERROR.value(), "Unable to find storage by ID " + storageId);
//
//            return;
//        }
//
//        Repository repository = storage.getRepository(repositoryId);
//        if (repository == null)
//        {
//            logger.error("Unable to find repository by ID " + repositoryId + " for storage " + storageId);
//
//            response.sendError(INTERNAL_SERVER_ERROR.value(),
//                               "Unable to find repository by ID " + repositoryId + " for storage " + storageId);
//            return;
//        }
//
//        if (!repository.isInService())
//        {
//            logger.error("The /" + storageId + "/" + repositoryId +
//                         " repository is not in service.");
//
//            response.setStatus(HttpStatus.SERVICE_UNAVAILABLE.value());
//
//            return;
//        }
//
//        RepositoryPath repositoryPath = artifactResolutionService.resolvePath(storageId, repositoryId, path);
//
//        provideArtifactDownloadResponse(request, response, httpHeaders, repositoryPath);
//    }

}
