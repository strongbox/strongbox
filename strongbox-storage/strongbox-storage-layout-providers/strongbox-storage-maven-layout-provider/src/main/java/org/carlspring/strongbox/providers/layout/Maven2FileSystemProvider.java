package org.carlspring.strongbox.providers.layout;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.spi.FileSystemProvider;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.inject.Inject;

import org.carlspring.strongbox.artifact.coordinates.MavenArtifactCoordinates;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.io.RepositoryPathResolver;
import org.carlspring.strongbox.providers.io.RootRepositoryPath;
import org.carlspring.strongbox.providers.search.MavenIndexerSearchProvider;
import org.carlspring.strongbox.providers.search.SearchException;
import org.carlspring.strongbox.services.ArtifactSearchService;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.search.SearchRequest;
import org.carlspring.strongbox.storage.search.SearchResult;
import org.carlspring.strongbox.storage.search.SearchResults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author sbespalov
 *
 */
public class Maven2FileSystemProvider extends RepositoryLayoutFileSystemProvider
{

    private static final Logger logger = LoggerFactory.getLogger(Maven2FileSystemProvider.class);
    
    @Inject
    private Maven2LayoutProvider layoutProvider;

    @Inject
    private ArtifactSearchService artifactSearchService;

    @Inject
    private RepositoryPathResolver repositoryPathResolver;
    
    public Maven2FileSystemProvider(FileSystemProvider storageFileSystemProvider)
    {
        super(storageFileSystemProvider);
    }

    @Override
    protected AbstractLayoutProvider getLayoutProvider()
    {
        return layoutProvider;
    }

    @Override
    public void delete(Path path,
                       boolean force)
        throws IOException
    {
        RepositoryPath repositoryPath = (RepositoryPath) path;
        
        logger.debug("Removing " + repositoryPath + "...");

        if (Files.isDirectory(repositoryPath))
        {
            cleanupDirectory(repositoryPath.relativize(), force);
        }

        layoutProvider.deleteMetadata(repositoryPath);

        super.delete(repositoryPath, force);
    }

    private void cleanupDirectory(RepositoryPath repositoryPathRelative,
                                  boolean force)
        throws IOException
    {
        Repository repository = repositoryPathRelative.getRepository();
        Storage storage = repository.getStorage();
        
        List<String> artifactCoordinateElements = StreamSupport.stream(repositoryPathRelative.spliterator(), false)
                                                               .map(p -> p.toString())
                                                               .collect(Collectors.toList());
        if (artifactCoordinateElements.size() < 2)
        {
            return;
        }
        
        StringBuffer groupId = new StringBuffer();
        for (int i = 0; i < artifactCoordinateElements.size() - 2; i++)
        {
            String element = artifactCoordinateElements.get(i);
            groupId.append((groupId.length() == 0) ? element : "." + element);
        }

        String artifactId = artifactCoordinateElements.get(artifactCoordinateElements.size() - 2);
        String version = artifactCoordinateElements.get(artifactCoordinateElements.size() - 1);

        RepositoryPath pomFilePath = repositoryPathRelative.resolve(artifactId + "-" + version + ".pom");

        // If there is a pom file, read it.
        if (Files.exists(pomFilePath))
        {
            // Run a search against the index and get a list of all the artifacts matching this exact GAV
            SearchRequest request = new SearchRequest(storage.getId(),
                                                      repository.getId(),
                                                      "+g:" + groupId + " " +
                                                      "+a:" + artifactId + " " +
                                                      "+v:" + version,
                                                      MavenIndexerSearchProvider.ALIAS);

            try
            {
                SearchResults results = artifactSearchService.search(request);

                for (SearchResult result : results.getResults())
                {
                    delete(repositoryPathResolver.resolve(repository, (MavenArtifactCoordinates) result.getArtifactCoordinates()), force);
                }
            }
            catch (SearchException e)
            {
                logger.error(e.getMessage(), e);
            }
        }
        // Otherwise, this is either not an artifact directory, or not a valid Maven artifact
    }

    
}
