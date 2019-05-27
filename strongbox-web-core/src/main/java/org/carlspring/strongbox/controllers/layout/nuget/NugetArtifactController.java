package org.carlspring.strongbox.controllers.layout.nuget;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBException;

import org.apache.commons.fileupload.MultipartStream;
import org.apache.commons.lang.StringUtils;
import org.carlspring.strongbox.artifact.ArtifactTag;
import org.carlspring.strongbox.artifact.coordinates.NugetArtifactCoordinates;
import org.carlspring.strongbox.artifact.coordinates.PathNupkg;
import org.carlspring.strongbox.artifact.coordinates.versioning.SemanticVersion;
import org.carlspring.strongbox.controllers.BaseArtifactController;
import org.carlspring.strongbox.data.criteria.Expression.ExpOperator;
import org.carlspring.strongbox.data.criteria.Paginator;
import org.carlspring.strongbox.data.criteria.Predicate;
import org.carlspring.strongbox.domain.ArtifactEntry;
import org.carlspring.strongbox.domain.ArtifactTagEntry;
import org.carlspring.strongbox.io.ReplacingInputStream;
import org.carlspring.strongbox.nuget.NugetSearchRequest;
import org.carlspring.strongbox.nuget.filter.NugetODataFilterQueryParser;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.repository.RepositoryProvider;
import org.carlspring.strongbox.providers.repository.RepositoryProviderRegistry;
import org.carlspring.strongbox.repository.NugetRepositoryFeatures.RepositorySearchEventListener;
import org.carlspring.strongbox.services.ArtifactTagService;
import org.carlspring.strongbox.storage.metadata.nuget.NugetFormatException;
import org.carlspring.strongbox.storage.metadata.nuget.Nupkg;
import org.carlspring.strongbox.storage.metadata.nuget.Nuspec;
import org.carlspring.strongbox.storage.metadata.nuget.TempNupkgFile;
import org.carlspring.strongbox.storage.metadata.nuget.rss.EntryProperties;
import org.carlspring.strongbox.storage.metadata.nuget.rss.PackageEntry;
import org.carlspring.strongbox.storage.metadata.nuget.rss.PackageFeed;
import org.carlspring.strongbox.storage.repository.Repository;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import org.carlspring.strongbox.web.LayoutRequestMapping;

import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * This Controller used to handle Nuget requests.
 * 
 * @author Sergey Bespalov
 *
 */
@RestController
@LayoutRequestMapping(NugetArtifactCoordinates.LAYOUT_NAME)
public class NugetArtifactController
        extends BaseArtifactController
{

    @Inject
    private ArtifactTagService artifactTagService;

    @Inject
    private RepositoryProviderRegistry repositoryProviderRegistry;

    @Inject
    private RepositorySearchEventListener repositorySearchEventListener;

    @DeleteMapping(path = { "{storageId}/{repositoryId}/{packageId}/{version}" })
    @PreAuthorize("hasAuthority('ARTIFACTS_DEPLOY')")
    public ResponseEntity deletePackage(@RequestHeader(name = "X-NuGet-ApiKey", required = false) String apiKey,
                                        @ApiParam(value = "The storageId", required = true) @PathVariable(name = "storageId") String storageId,
                                        @ApiParam(value = "The repositoryId", required = true) @PathVariable(name = "repositoryId") String repositoryId,
                                        @PathVariable("packageId") String packageId,
                                        @PathVariable("version") String version)
    {
        logger.info(String.format("Nuget delete request: storageId-[%s]; repositoryId-[%s]; packageId-[%s]", storageId, repositoryId, packageId));

        RepositoryPath path = repositoryPathResolver.resolve(storageId, repositoryId, String.format("%s/%s/%s.nuspec", packageId, version, packageId));;
        try
        {
            //TODO: we should move associated files deletion into corresponding layout providers.
            artifactManagementService.delete(path, true);
            path = repositoryPathResolver.resolve(storageId, repositoryId, String.format("%s/%s/%s.%s.nupkg", packageId, version, packageId,version));
            artifactManagementService.delete(path, true);
            path = repositoryPathResolver.resolve(storageId, repositoryId, String.format("%s/%s/%s.%s.nupkg.sha512", packageId, version, packageId,version));
            artifactManagementService.delete(path, true);
        }
        catch (IOException e)
        {
            logger.error(String.format("Failed to process Nuget delete request: path-[%s]", path), e);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
        
        return ResponseEntity.status(HttpStatus.OK).build();
    }
    
    @GetMapping(path = { "{storageId}/{repositoryId}/Search()/$count" }, produces = MediaType.TEXT_PLAIN)
    public ResponseEntity<String> countPackages(@ApiParam(value = "The storageId", required = true) @PathVariable(name = "storageId") String storageId,
                                                @ApiParam(value = "The repositoryId", required = true) @PathVariable(name = "repositoryId") String repositoryId,
                                                @RequestParam(name = "$filter", required = false) String filter,
                                                @RequestParam(name = "searchTerm", required = false) String searchTerm,
                                                @RequestParam(name = "targetFramework", required = false) String targetFramework)
    {
        String normalizedSearchTerm = normaliseSearchTerm(searchTerm);
        
        NugetSearchRequest nugetSearchRequest = new NugetSearchRequest();
        nugetSearchRequest.setFilter(filter);
        nugetSearchRequest.setSearchTerm(searchTerm);
        nugetSearchRequest.setTargetFramework(targetFramework);
        repositorySearchEventListener.setNugetSearchRequest(nugetSearchRequest);
        
        Repository repository = getRepository(storageId, repositoryId);
        RepositoryProvider provider = repositoryProviderRegistry.getProvider(repository.getType());
        
        Predicate predicate = createSearchPredicate(filter, normalizedSearchTerm);
        Long count = provider.count(storageId, repositoryId, predicate);

        return new ResponseEntity<>(String.valueOf(count), HttpStatus.OK);
    }

    @GetMapping(path = { "{storageId}/{repositoryId}/{searchCommandName:(?:Packages(?:\\(\\))?|Search\\(\\))}" },
                produces = MediaType.APPLICATION_XML)
    public ResponseEntity<?> searchPackages(@ApiParam(value = "The storageId", required = true) @PathVariable(name = "storageId") String storageId,
                                            @ApiParam(value = "The repositoryId", required = true) @PathVariable(name = "repositoryId") String repositoryId,
                                            @PathVariable(name = "searchCommandName") String searchCommandName,
                                            @RequestParam(name = "$filter", required = false) String filter,
                                            @RequestParam(name = "$orderby", required = false, defaultValue = "Id") String orderBy,
                                            @RequestParam(name = "$skip", required = false) Integer skip,
                                            @RequestParam(name = "$top", required = false) Integer top,
                                            @RequestParam(name = "searchTerm", required = false) String searchTerm,
                                            @RequestParam(name = "targetFramework", required = false) String targetFramework,
                                            HttpServletResponse response)
            throws JAXBException, IOException
    {
        String normalizedSearchTerm = normaliseSearchTerm(searchTerm);
        
        NugetSearchRequest nugetSearchRequest = new NugetSearchRequest();
        nugetSearchRequest.setFilter(filter);
        nugetSearchRequest.setSearchTerm(searchTerm);
        nugetSearchRequest.setTargetFramework(targetFramework);
        repositorySearchEventListener.setNugetSearchRequest(nugetSearchRequest);
        
        String feedId = getFeedUri(((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest(),
                                   storageId,
                                   repositoryId);

        Collection<? extends Nupkg> files;
        files = getPackages(storageId,
                            repositoryId,
                            filter,
                            orderBy,
                            normalizedSearchTerm,
                            targetFramework,
                            skip,
                            top);

        PackageFeed feed = transform(feedId, files);

        response.setHeader("content-type", MediaType.APPLICATION_XML);
        feed.writeXml(response.getOutputStream());

        return new ResponseEntity<>(HttpStatus.OK);
    }
    
    private PackageFeed transform(String feedId,
                                  Collection<? extends Nupkg> files)
    {
        PackageFeed feed = new PackageFeed();
        // feed.setId(getContext().getRootUri().toString());
        feed.setId(feedId);
        feed.setUpdated(new Date());
        feed.setTitle("Packages");
        List<PackageEntry> packageEntrys = new ArrayList<>();
        for (Nupkg nupkg : files)
        {
            try
            {
                PackageEntry entry = createPackageEntry(feedId, (PathNupkg) nupkg);
                calculateFeedEntryProperties((PathNupkg) nupkg, entry.getProperties());
                packageEntrys.add(entry);
            }
            catch (NoSuchAlgorithmException | IOException | NugetFormatException e)
            {
                logger.error("Failed to parse package " + nupkg, e);
            }
        }
        logger.debug("Got {} packages", new Object[] { packageEntrys.size() });
        feed.setEntries(packageEntrys);
        return feed;
    }

    private void calculateFeedEntryProperties(PathNupkg nupkg,
                                              EntryProperties properties) throws IOException
    {
        RepositoryPath path = nupkg.getPath();
        ArtifactEntry artifactEntry = path.getArtifactEntry();

        properties.setId(nupkg.getId());

        properties.setReportAbuseUrl("");

        properties.setDownloadCount(artifactEntry.getDownloadCount());
        properties.setVersionDownloadCount(artifactEntry.getDownloadCount());

        properties.setRatingsCount(0);
        properties.setVersionRatingsCount(0);

        properties.setRating(Double.valueOf(0));
        properties.setVersionRating(Double.valueOf(0));

        ArtifactTag lastVersionTag = artifactTagService.findOneOrCreate(ArtifactTagEntry.LAST_VERSION);
        if (artifactEntry.getTagSet().contains(lastVersionTag))
        {
            properties.setIsLatestVersion(true);
        }
        else
        {
            properties.setIsLatestVersion(false);
        }

    }

    private PackageEntry createPackageEntry(String feedId,
                                            PathNupkg nupkg)
        throws NoSuchAlgorithmException,
        IOException,
        NugetFormatException
    {
        return new PackageEntry(nupkg){

            @Override
            protected String getRootUri()
            {
                return feedId;
            }
        };
    }

    @GetMapping(path = { "{storageId}/{repositoryId}/FindPackagesById()" }, produces = MediaType.APPLICATION_XML)
    public ResponseEntity<?> searchPackageById(@PathVariable(name = "storageId") String storageId,
                                               @PathVariable(name = "repositoryId") String repositoryId,
                                               @RequestParam(name = "id", required = true) String packageId,
                                               HttpServletResponse response)
            throws JAXBException, IOException
    {
        String normalisedPackageId = normaliseSearchTerm(packageId);

        NugetSearchRequest nugetSearchRequest = new NugetSearchRequest();
        nugetSearchRequest.setFilter(String.format("Id eq '%s'", packageId));
        repositorySearchEventListener.setNugetSearchRequest(nugetSearchRequest);

        Repository repository = getRepository(storageId, repositoryId);
        RepositoryProvider provider = repositoryProviderRegistry.getProvider(repository.getType());

        Paginator paginator = new Paginator();
        paginator.setProperty("artifactCoordinates.coordinates.version");

        Predicate predicate = Predicate.of(ExpOperator.EQ.of("artifactCoordinates.coordinates.id", normalisedPackageId));

        Collection<? extends Nupkg> files = searchNupkg(storageId, repositoryId, provider, paginator, predicate);

        String feedId = getFeedUri(((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest(),
                                   storageId,
                                   repositoryId);

        PackageFeed feed = transform(feedId, files);

        response.setHeader("Content-Type", MediaType.APPLICATION_XML);
        feed.writeXml(response.getOutputStream());

        return new ResponseEntity<>(HttpStatus.OK);
    }

    public Collection<? extends Nupkg> getPackages(String storageId,
                                                   String repositoryId,
                                                   String filter,
                                                   String orderBy,
                                                   String searchTerm,
                                                   String targetFramework,
                                                   Integer skip,
                                                   Integer top)
    {
        Repository repository = getRepository(storageId, repositoryId);
        RepositoryProvider provider = repositoryProviderRegistry.getProvider(repository.getType());

        Paginator paginator = new Paginator();
        paginator.setSkip(skip);
        paginator.setLimit(top);
        paginator.setProperty(orderBy);

        Predicate rootPredicate = createSearchPredicate(filter, searchTerm);

        return searchNupkg(storageId, repositoryId, provider, paginator, rootPredicate);
    }

    private List<PathNupkg> searchNupkg(String storageId,
                                        String repositoryId,
                                        RepositoryProvider provider,
                                        Paginator paginator,
                                        Predicate predicate)
    {
        return provider.search(storageId, repositoryId, predicate, paginator)
                       .stream()
                       .map(p -> {
                           try
                           {
                               return new PathNupkg((RepositoryPath) p);
                           }
                           catch (Exception e)
                           {
                               logger.error(String.format("Failed to resolve Nuget package path [%s]", p), e);
                               return null;
                           }
                       })
                       .collect(Collectors.toList());
    }

    private Predicate createSearchPredicate(String filter,
                                            String searchTerm)
    {
        Predicate rootPredicate = Predicate.empty();

        if (filter != null && !filter.trim().isEmpty())
        {
           NugetODataFilterQueryParser t = new NugetODataFilterQueryParser(filter);
           rootPredicate = t.parseQuery().getPredicate();
        }

        rootPredicate.and(Predicate.of(ExpOperator.EQ.of("artifactCoordinates.coordinates.extension", "nupkg")));

        if (searchTerm != null && !searchTerm.trim().isEmpty())
        {
            rootPredicate.and(Predicate.of(ExpOperator.LIKE.of("artifactCoordinates.coordinates.id",
                                                               "%" + searchTerm + "%")));
        }
        return rootPredicate;
    }

    private String getFeedUri(HttpServletRequest request, String storageId, String repositoryId)
    {
        return String.format("%s://%s:%s%s/storages/%s/%s/",
                             request.getScheme(),
                             request.getServerName(),
                             request.getServerPort(),
                             request.getContextPath(),
                             storageId,
                             repositoryId);
    }

    @ApiOperation(value = "Used to get storage metadata")
    @ApiResponses(value = { @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "The metadata was downloaded successfully."),
                            @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "An error occurred.") })
    @RequestMapping(path = { "{storageId}/{repositoryId}/$metadata" }, method = RequestMethod.GET, produces = MediaType.APPLICATION_XML)
    public ResponseEntity<Resource> getMetadata()
    {
        InputStream inputStream = NugetArtifactController.class.getResourceAsStream("/metadata.xml");
        return new ResponseEntity<>(new InputStreamResource(inputStream), HttpStatus.OK);
    }

    /**
     * This method is used to check storage availability.<br>
     * For example NuGet pings the root without credentials to determine if the repository is healthy. If this receives
     * a 401 response then NuGet will prompt for authentication.
     * 
     * @return
     */
    @ApiOperation(value = "Used to check storage availability")
    @ApiResponses(value = { @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Storage available."),
                            @ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Storage requires authorization.") })
    @GetMapping(path = { "{storageId}/{repositoryId}", "greet" })
    @PreAuthorize("hasAuthority('ARTIFACTS_DEPLOY')")
    public ResponseEntity<String> greet()
    {
        return new ResponseEntity<>("success", HttpStatus.OK);
    }

    @ApiOperation(value = "Used to deploy a package")
    @ApiResponses(value = { @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "The package was deployed successfully."),
                            @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "An error occurred.") })
    @PreAuthorize("hasAuthority('ARTIFACTS_DEPLOY')")
    @RequestMapping(path = "{storageId}/{repositoryId}/", method = RequestMethod.PUT, consumes = MediaType.MULTIPART_FORM_DATA)
    public ResponseEntity putPackage(@RequestHeader(name = "X-NuGet-ApiKey", required = false) String apiKey,
                                     @ApiParam(value = "The storageId", required = true) @PathVariable(name = "storageId") String storageId,
                                     @ApiParam(value = "The repositoryId", required = true) @PathVariable(name = "repositoryId") String repositoryId,
                                     HttpServletRequest request)
    {
        logger.info(String.format("Nuget push request: storageId-[%s]; repositoryId-[%s]", storageId, repositoryId));
        String contentType = request.getHeader("content-type");

        URI resourceUri;
        try
        {
            ServletInputStream is = request.getInputStream();
            InputStream packagePartInputStream = extractPackageMultipartStream(extractBoundary(contentType), is);

            if (packagePartInputStream == null)
            {
                logger.error(String.format("Failed to extract Nuget package from request: [%s]:[%s]",
                                           storageId,
                                           repositoryId));

                return ResponseEntity.badRequest().build();
            }

            resourceUri = storePackage(storageId, repositoryId, packagePartInputStream);
        }
        catch (Exception e)
        {
            logger.error(String.format("Failed to process Nuget push request: %s:%s", storageId, repositoryId), e);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }

        if (resourceUri == null)
        {
            // Return 501 status in case of empty package recieved.
            // For some reason nuget.exe sends empty package first.
            return ResponseEntity.status(HttpURLConnection.HTTP_BAD_REQUEST).build();
        }

        return ResponseEntity.created(resourceUri).build();
    }

    @ApiOperation(value = "Used to download a package")
    @ApiResponses(value = { @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "The request was successfull."),
                            @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Server error occurred."),
                            @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "The requested path was not found."),
                            @ApiResponse(code = HttpURLConnection.HTTP_UNAVAILABLE, message = "Repository not in service currently.")})
    @PreAuthorize("hasAuthority('ARTIFACTS_RESOLVE')")
    @RequestMapping(path = "{storageId}/{repositoryId}/{commandName:(?:download|package)}/{packageId}/{packageVersion}",
                    method = {RequestMethod.GET, RequestMethod.HEAD},
                    produces = MediaType.APPLICATION_OCTET_STREAM)
    public void downloadPackage(@ApiParam(value = "The storageId", required = true) @PathVariable(name = "storageId") String storageId,
                                @ApiParam(value = "The repositoryId", required = true) @PathVariable(name = "repositoryId") String repositoryId,
                                @ApiParam(value = "The packageId", required = true) @PathVariable(name = "packageId") String packageId,
                                @ApiParam(value = "The packageVersion", required = true) @PathVariable(name = "packageVersion") String packageVersion,
                                HttpServletResponse response,
                                HttpServletRequest request,
                                @RequestHeader HttpHeaders httpHeaders)
            throws Exception
    {
        getPackageInternal(storageId, repositoryId, packageId, packageVersion, response, request, httpHeaders);
    }

    @ApiOperation(value = "Used to download a package")
    @ApiResponses(value = { @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "The request was successfull."),
                            @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Server error occurred."),
                            @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "The requested path was not found."),
                            @ApiResponse(code = HttpURLConnection.HTTP_UNAVAILABLE, message = "Repository not in service currently.")})
    @PreAuthorize("hasAuthority('ARTIFACTS_RESOLVE')")
    @RequestMapping(path = "{storageId}/{repositoryId}/{packageId}/{packageVersion}",
                    method = {RequestMethod.GET, RequestMethod.HEAD},
                    produces = MediaType.APPLICATION_OCTET_STREAM)
    public void getPackage(@ApiParam(value = "The storageId", required = true) @PathVariable(name = "storageId") String storageId,
                           @ApiParam(value = "The repositoryId", required = true) @PathVariable(name = "repositoryId") String repositoryId,
                           @ApiParam(value = "The packageId", required = true) @PathVariable(name = "packageId") String packageId,
                           @ApiParam(value = "The packageVersion", required = true) @PathVariable(name = "packageVersion") String packageVersion,
                           HttpServletResponse response,
                           HttpServletRequest request, 
                           @RequestHeader HttpHeaders httpHeaders)
            throws Exception
    {
        getPackageInternal(storageId, repositoryId, packageId, packageVersion, response, request, httpHeaders);
    }

    private void getPackageInternal(String storageId,
                                    String repositoryId,
                                    String packageId,
                                    String packageVersion,
                                    HttpServletResponse response,
                                    HttpServletRequest request,
                                    HttpHeaders httpHeaders)
            throws Exception
    {
        logger.debug("Requested Nuget Package %s, %s, %s, %s.", storageId, repositoryId, packageId, packageVersion);

        String fileName = String.format("%s.%s.nupkg", packageId, packageVersion);
        String path = String.format("%s/%s/%s", packageId, packageVersion, fileName);

        RepositoryPath repositoryPath = artifactResolutionService.resolvePath(storageId, repositoryId, path);
        if (provideArtifactDownloadResponse(request, response, httpHeaders, repositoryPath))
        {
            response.setHeader("Content-Disposition", String.format("attachment; filename=\"%s\"", fileName));
        }
    }

    private String extractBoundary(String contentType)
    {
        String boundaryString = "";
        Pattern pattern = Pattern.compile("multipart/form-data;(\\s*)boundary=([^;]+)(.*)");
        Matcher matcher = pattern.matcher(contentType);
        if (matcher.matches())
        {
            boundaryString = matcher.group(2);
        }
        boundaryString = boundaryString.startsWith("\"") ? boundaryString.substring(1) : boundaryString;
        boundaryString = boundaryString.endsWith("\"") ? boundaryString.substring(0, boundaryString.length() - 1)
                : boundaryString;
        return boundaryString;
    }

    private InputStream extractPackageMultipartStream(String boundaryString,
                                                      ServletInputStream is)
            throws IOException
    {
        if (StringUtils.isEmpty(boundaryString))
        {
            return null;
        }

        final Path packagePartFile = Files.createTempFile("nupkg", "part");
        try (OutputStream packagePartOutputStream = new BufferedOutputStream(Files.newOutputStream(packagePartFile)))
        {

            writePackagePart(boundaryString, is, packagePartOutputStream);
        }

        return new BufferedInputStream(Files.newInputStream(packagePartFile));
    }

    private void writePackagePart(String boundaryString,
                                  InputStream is,
                                  OutputStream packagePartOutputStream)
        throws IOException
    {
        // According to the specification, the final Boundary of MultipartStream should be prefixed with
        // `0x0D0x0A0x2D0x2D` characters, but seems that Nuget command line tool has broken Multipart Boundary format.
        // We need to fix missing starting byte of ending Mulipart boundary (0x0D), which is incorrectly generated by
        // NuGet `push` implementation.
        byte[] boundary = boundaryString.getBytes();
        byte[] boundaryPrefixToFix = {0x00, 0x0A, 0x2D, 0x2D};
        byte[] boundaryPrefixTarget = {0x00, 0x0D, 0x0A, 0x2D, 0x2D};

        byte[] replacementPattern = new byte[boundary.length + 4];
        byte[] replacementTarget = new byte[boundary.length + 5];

        System.arraycopy(boundaryPrefixToFix, 0, replacementPattern, 0, 4);
        System.arraycopy(boundaryPrefixTarget, 0, replacementTarget, 0, 5);

        System.arraycopy(boundary, 0, replacementPattern, 4, boundary.length);
        System.arraycopy(boundary, 0, replacementTarget, 4, boundary.length);

        Path streamContentPath = Files.createTempFile("boundaryString", "nupkg");
        ReplacingInputStream replacingIs = new ReplacingInputStream(is, boundaryPrefixToFix, boundaryPrefixTarget);
        long len = Files.copy(replacingIs, streamContentPath,
                   StandardCopyOption.REPLACE_EXISTING);
        System.out.println(len);

        try (InputStream streamContentIs = new BufferedInputStream(Files.newInputStream(streamContentPath)))
        {
            MultipartStream multipartStream = new MultipartStream(streamContentIs, boundary, 4096, null);
            multipartStream.skipPreamble();
            String header = multipartStream.readHeaders();

            // Package Multipart Header should be like follows:
            // Content-Disposition: form-data; name="package";
            // filename="package"
            // Content-Type: application/octet-stream
            if (!header.contains("package"))
            {
                logger.error("Invalid package multipart format");
                return;
            }

            int contentLength = multipartStream.readBodyData(packagePartOutputStream);
            logger.info(String.format("NuGet package content length [%s]", contentLength));
        }
    }

    private URI storePackage(String storageId,
                             String repositoryId,
                             InputStream is)
        throws Exception
    {
        try (TempNupkgFile nupkgFile = new TempNupkgFile(is))
        {
            Nuspec nuspec = nupkgFile.getNuspec();
            if (nuspec == null)
            {
                return null;
            }

            String nuspecId = nuspec.getId();

            SemanticVersion nuspecVersion = nuspec.getVersion();
            String path = String.format("%s/%s/%s.%s.nupkg",
                                        nuspecId,
                                        nuspecVersion,
                                        nuspecId,
                                        nuspecVersion);

            RepositoryPath repositoryPath = repositoryPathResolver.resolve(storageId, repositoryId, path);
            artifactManagementService.validateAndStore(repositoryPath, nupkgFile.getStream());

            Path nuspecFile = Files.createTempFile(nuspec.getId(), "nuspec");
            try (OutputStream outputStream = new BufferedOutputStream(Files.newOutputStream(nuspecFile)))
            {
                nuspec.saveTo(outputStream);
            }
            path = String.format("%s/%s/%s.nuspec", nuspecId, nuspecVersion, nuspecId);
            repositoryPath = repositoryPathResolver.resolve(storageId, repositoryId, path);
            try (InputStream bis = new BufferedInputStream(Files.newInputStream(nuspecFile)))
            {
                artifactManagementService.validateAndStore(repositoryPath, bis);
            }

            Path hashFile = Files.createTempFile(String.format("%s.%s", nuspecId, nuspecVersion),
                                                "nupkg.sha512");

            try (OutputStream bos = new BufferedOutputStream(Files.newOutputStream(hashFile)))
            {
                Writer writer = new OutputStreamWriter(bos);
                writer.write(nupkgFile.getHash());
                writer.flush();
                bos.flush();
            }

            path = String.format("%s/%s/%s.%s.nupkg.sha512",
                                 nuspecId,
                                 nuspecVersion,
                                 nuspecId,
                                 nuspecVersion);
            repositoryPath = repositoryPathResolver.resolve(storageId, repositoryId, path);
            try (InputStream bis = new BufferedInputStream(Files.newInputStream(hashFile)))
            {
                artifactManagementService.validateAndStore(repositoryPath, bis);
            }
        }

        return new URI("");
    }

    private String normaliseSearchTerm(String sourceValue)
    {
        if (sourceValue == null)
        {
            return null;
        }

        return sourceValue.replaceAll("['\"]", "");
    }

}
