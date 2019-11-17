package org.carlspring.strongbox.controllers.layout.rpm;

import org.carlspring.strongbox.artifact.coordinates.RpmArtifactCoordinates;
import org.carlspring.strongbox.controllers.BaseArtifactController;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.web.LayoutRequestMapping;
import org.carlspring.strongbox.web.RepositoryMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


/**
 * This Controller used to handle rpm requests.
 *
 * @author Sergey Bespalov
 */
@RestController
@LayoutRequestMapping(RpmArtifactCoordinates.LAYOUT_NAME)
public class RpmArtifactController
        extends BaseArtifactController
{


    /**
     * curl -u username:password -F file=@"./test-package-1.2-34.x86_64.rpm" http://localhost:48080/storages/storage-rpm/test-packages/test/test-package-1.2-34.x86_64.rpm --progress-bar
     */

    @PreAuthorize("hasAuthority('ARTIFACTS_RESOLVE')")
    @RequestMapping(path = "{storageId}/{repositoryId}/{category}/{artifactPath:.+}",
                    method = { RequestMethod.GET,
                               RequestMethod.HEAD })
    public void download(@RepositoryMapping Repository repository,
                         @PathVariable(name = "artifactPath") String artifactPath,
                         @RequestHeader HttpHeaders httpHeaders,
                         HttpServletRequest request,
                         HttpServletResponse response)
            throws Exception
    {
        final String storageId = repository.getStorage().getId();
        final String repositoryId = repository.getId();

        RpmArtifactCoordinates coordinates;
        try
        {
            coordinates = RpmArtifactCoordinates.of(artifactPath);
        }
        catch (IllegalArgumentException e)
        {
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            response.getWriter().write(e.getMessage());
            return;
        }

        RepositoryPath path = artifactResolutionService.resolvePath(storageId, repositoryId, coordinates.toPath());
        provideArtifactDownloadResponse(request, response, httpHeaders, path);
    }

//    @PreAuthorize("hasAuthority('ARTIFACTS_DEPLOY')")
//    @PutMapping(path = "{storageId}/{repositoryId}/{category}/{artifactPath:.+}",
//                consumes = MediaType.APPLICATION_JSON_VALUE)
//    public ResponseEntity upload(@RepositoryMapping Repository repository,
//                                 @PathVariable(name = "name") String name,
//                                 HttpServletRequest request)
//            throws Exception
//    {
//        final String storageId = repository.getStorage().getId();
//        final String repositoryId = repository.getId();
//
//        RpmArtifactCoordinates coordinates;
//        try
//        {
//            coordinates = RpmArtifactCoordinates.of(artifactPath);
//        }
//        catch (IllegalArgumentException e)
//        {
//            response.setStatus(HttpStatus.BAD_REQUEST.value());
//            response.getWriter().write(e.getMessage());
//            return;
//        }
//
//        storeRpmPackage(repository, coordinates, packageJson, packageTgz);
//
//        return ResponseEntity.ok("");
//    }

    @ApiOperation(value = "Used to deploy an artifact")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "The artifact was deployed successfully."),
                            @ApiResponse(code = 400, message = "An error occurred.") })
    @PreAuthorize("hasAuthority('ARTIFACTS_DEPLOY')")
    @PutMapping(value = "{storageId}/{repositoryId}/{artifactPath:.+}")
    public ResponseEntity upload(@RepositoryMapping Repository repository,
                                 @PathVariable String artifactPath,
                                 HttpServletRequest request)
    {
        final String storageId = repository.getStorage().getId();
        final String repositoryId = repository.getId();

        try
        {
            RepositoryPath repositoryPath = repositoryPathResolver.resolve(storageId, repositoryId, artifactPath);
            artifactManagementService.validateAndStore(repositoryPath, request.getInputStream());

            return ResponseEntity.ok("The artifact was deployed successfully.");
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

//    private void storeRpmPackage(Repository repository,
//                                 RpmArtifactCoordinates coordinates,
//                                 PackageVersion packageDef,
//                                 Path packageTgzTmp)
//            throws IOException,
//                   ProviderImplementationException,
//                   NoSuchAlgorithmException,
//                   ArtifactCoordinatesValidationException
//    {
//        RepositoryPath repositoryPath = repositoryPathResolver.resolve(repository, coordinates);
//        try (InputStream is = new BufferedInputStream(Files.newInputStream(packageTgzTmp)))
//        {
//            artifactManagementService.validateAndStore(repositoryPath, is);
//        }
//
//        Path packageJsonTmp = extractPackageJson(packageTgzTmp);
//        RepositoryPath packageJsonPath = repositoryPathResolver.resolve(repository,
//                                                                        repositoryPath.resolveSibling("package.json"));
//        try (InputStream is = new BufferedInputStream(Files.newInputStream(packageJsonTmp)))
//        {
//            artifactManagementService.validateAndStore(packageJsonPath, is);
//        }
//
//        String shasum = Optional.ofNullable(packageDef.getDist()).map(p -> p.getShasum()).orElse(null);
//        if (shasum == null)
//        {
//            logger.warn("No checksum provided for package [{}]", packageDef.getName());
//            return;
//        }
//
//        String packageFileName = repositoryPath.getFileName().toString();
//        RepositoryPath checksumPath = repositoryPath.resolveSibling(packageFileName + ".sha1");
//        artifactManagementService.validateAndStore(checksumPath,
//                                                   new ByteArrayInputStream(shasum.getBytes(StandardCharsets.UTF_8)));
//
//        Files.delete(packageTgzTmp);
//        Files.delete(packageJsonTmp);
//    }

}
