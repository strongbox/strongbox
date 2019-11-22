package org.carlspring.strongbox.controllers.layout.pypi;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.carlspring.strongbox.artifact.coordinates.PypiArtifactCoordinates;
import org.carlspring.strongbox.controllers.BaseArtifactController;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.web.LayoutRequestMapping;
import org.carlspring.strongbox.web.RepositoryMapping;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.file.Files;

/**
 * Rest Api for Pypi Artifacts
 */

@RestController
@LayoutRequestMapping(PypiArtifactCoordinates.LAYOUT_NAME)
public class PypiArtifactController extends BaseArtifactController{

    @ApiOperation(value = "Used to retrieve a whl or sdist artifact")
    @ApiResponses(value = { @ApiResponse(code = 200, message = ""),
                            @ApiResponse(code = 404, message = "Requested path not found."),
                            @ApiResponse(code = 500, message = "Server error."),
                            @ApiResponse(code = 503, message = "Repository currently not in service.")})
    @PreAuthorize("hasAuthority('ARTIFACTS_RESOLVE')")
    @GetMapping(value = { "/{storageId}/{repositoryId}/{artifactPath:.+(\\.whl|\\.tar.gz)}"})
    public void download(@RepositoryMapping Repository repository,
                         @RequestHeader HttpHeaders httpHeaders,
                         @PathVariable String artifactPath,
                         HttpServletRequest request,
                         HttpServletResponse response)
            throws Exception
    {
        final String storageId = repository.getStorage().getId();
        final String repositoryId = repository.getId();

        logger.debug("Requested /{}/{}/{}.", storageId, repositoryId, artifactPath);

        RepositoryPath repositoryPath = artifactResolutionService.resolvePath(storageId, repositoryId, artifactPath);

        provideArtifactDownloadResponse(request, response, httpHeaders, repositoryPath);
    }

    @ApiOperation(value = "Used to List all packages")
    @GetMapping(value = {"/{storageId}/{repositoryId}/packages"}, produces = MediaType.TEXT_HTML_VALUE)
    public void listPackages(@RepositoryMapping Repository repository,
                     HttpServletRequest request)
    {
        final String storageId = repository.getStorage().getId();
        final String repositoryId = repository.getId();


    }
    @ApiOperation(value = "Used to retrieve simple index")
    @GetMapping(value = {"/{storageId}/{repositoryId}/simple"}, produces = MediaType.TEXT_HTML_VALUE)
    public void listSimple(@RepositoryMapping Repository repository,
                             HttpServletRequest request)
    {
        final String storageId = repository.getStorage().getId();
        final String repositoryId = repository.getId();


    }

    @ApiOperation(value = "Used to retrieve simple project releases")
    @GetMapping(value = { "/{storageId}/{repositoryId}/simple/{projectName}" })
    public void listProject(@RepositoryMapping Repository repository,
                            @PathVariable String projectName,
                            HttpServletRequest request)
    {

    }

    @ApiOperation(value = "Entry point for upload, delete, and documentation upload")
    @PreAuthorize("hasAuthority('ARTIFACTS_DEPLOY')")
    @PostMapping(value = {"/{storageId}/{repositoryId}"})
    public ResponseEntity<String> postFunctions(@RepositoryMapping Repository repository,
                              @RequestParam(value = ":action") String Action,
                              @RequestParam(value = "protocol_version") Integer ProtocolVersion,
                              @RequestParam(value = "md5_digest", required = false) String MD5,
                              @RequestParam(value = "name", required = false) String Name,
                              @RequestParam(value = "content", required = false) MultipartFile content,
                              @RequestParam(value = "gpg_signature", required = false) MultipartFile GPGSig,
                              @RequestParam(value = "version", required = false) String Version,
                              @RequestParam(value = "filetype", required = false) String FileType,
                              HttpServletRequest request)
    {
        if(Action.equals("file_upload"))
            return upload(repository,content,GPGSig,Name,Version,MD5,FileType);
        if(Action.equals("remove_pkg"))
            return delete(repository,Name,Version);


        return ResponseEntity.badRequest().body("Invalid action");
    }

    public boolean packageNameCorrect(String Name,
                                      String Version,
                                      String FileType,
                                      String Filename)
    {
        StringBuilder builder = new StringBuilder(Name);

        builder.append("-").append(Version).append(FileType.equals("bdist_wheel") ? ".whl":".tar.gz");

        return Filename.equals(builder.toString());
    }

    private ResponseEntity<String> upload(Repository repository,
                                         MultipartFile artifact,
                                         MultipartFile GPGSig,
                                         String Name,
                                         String Version,
                                         String MD5,
                                         String FileType){
        final String storageId = repository.getStorage().getId();
        final String repositoryId = repository.getId();

        if( FileType == null || !(FileType.equals("bdist_wheel") || FileType.equals("sdist")) )
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid \"filetype\" parameter");

        if( artifact == null )
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing File \"content\"");

        if(!packageNameCorrect(Name,Version,FileType,artifact.getOriginalFilename()))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("File name does not match metadata");

        try
        {
            RepositoryPath repositoryPath;
            if(GPGSig != null)
            {
                if(!(artifact.getOriginalFilename().concat(".asc").equals(GPGSig.getOriginalFilename())))
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("GPG signature filename mismatch");
                else
                {
                    repositoryPath = repositoryPathResolver.resolve(storageId,repositoryId,String.format("%s/%s/%s",Name,Version,GPGSig.getOriginalFilename()));
                    artifactManagementService.validateAndStore(repositoryPath,GPGSig.getInputStream());
                }
            }

            repositoryPath = repositoryPathResolver.resolve(storageId, repositoryId, String.format("%s/%s/%s",Name,Version,artifact.getOriginalFilename()));
            artifactManagementService.validateAndStore(repositoryPath, artifact.getInputStream());

            File hash = File.createTempFile(artifact.getName(),"md5");
            OutputStream os = new BufferedOutputStream(Files.newOutputStream(hash.toPath()));
            Writer writer = new OutputStreamWriter(os);
            writer.write(MD5);
            writer.flush();
            os.flush();
            writer.close();
            os.close();

            InputStream is = new BufferedInputStream(Files.newInputStream(hash.toPath()));
            repositoryPath = repositoryPathResolver.resolve(storageId, repositoryId, String.format("%s/%s/%s",Name,Version,artifact.getName() + ".md5"));
            artifactManagementService.validateAndStore(repositoryPath,is);

            return ResponseEntity.ok("The artifact was deployed successfully.");
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }

    }


    private ResponseEntity<String> delete(Repository repository,
                                         String name,
                                         String version)
    {
        final String storageId = repository.getStorage().getId();
        final String repositoryId = repository.getId();
        RepositoryPath repositoryPath;
        String baseName = name.concat("/").concat(version).concat("/").concat(name).concat("-").concat(version);

        try {
            repositoryPath = repositoryPathResolver.resolve(storageId,repositoryId,baseName.concat(".whl"));
            artifactManagementService.delete(repositoryPath,true);

            repositoryPath = repositoryPathResolver.resolve(storageId,repositoryId,baseName.concat(".whl.md5"));
            artifactManagementService.delete(repositoryPath,true);

            repositoryPath = repositoryPathResolver.resolve(storageId,repositoryId,baseName.concat(".whl.asc"));
            artifactManagementService.delete(repositoryPath,true);

            repositoryPath = repositoryPathResolver.resolve(storageId,repositoryId,baseName.concat(".tar.gz"));
            artifactManagementService.delete(repositoryPath,true);

            repositoryPath = repositoryPathResolver.resolve(storageId,repositoryId,baseName.concat(".tar.gz.asc"));
            artifactManagementService.delete(repositoryPath,true);

            repositoryPath = repositoryPathResolver.resolve(storageId,repositoryId,baseName.concat(".tar.gz.md5"));
            artifactManagementService.delete(repositoryPath,true);

            return ResponseEntity.ok("The artifact was deleted successfully.");
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}
