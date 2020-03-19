package org.carlspring.strongbox.providers.layout;

import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.nio.file.Path;
import java.time.ZoneId;
import java.util.Date;
import java.util.function.Function;

import org.carlspring.strongbox.artifact.coordinates.NpmArtifactCoordinates;
import org.carlspring.strongbox.domain.Artifact;
import org.carlspring.strongbox.npm.metadata.PackageEntry;
import org.carlspring.strongbox.npm.metadata.SearchResult;
import org.carlspring.strongbox.providers.io.RepositoryFiles;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author sbespalov
 *
 */
@Component
public class NpmSearchResultSupplier implements Function<Path, SearchResult>
{

    private static final Logger logger = LoggerFactory.getLogger(NpmSearchResultSupplier.class);

    public static final String SEARCH_DATE_FORMAT = "EEE MMM dd yyyy HH:mm:SS ZZZ (zzz)";
    
    @Override
    public SearchResult apply(Path path)
    {
        RepositoryPath repositoryPath = (RepositoryPath) path;

        NpmArtifactCoordinates c;
        Artifact artifactEntry;
        try
        {
            c = (NpmArtifactCoordinates) RepositoryFiles.readCoordinates(repositoryPath);
            artifactEntry = repositoryPath.getArtifactEntry();
        }
        catch (IOException e)
        {
            throw new UndeclaredThrowableException(e);
        }

        SearchResult searchResult = new SearchResult();

        PackageEntry packageEntry = new PackageEntry();
        searchResult.setPackage(packageEntry);

        packageEntry.setDate(Date.from(artifactEntry.getLastUpdated().atZone(ZoneId.systemDefault()).toInstant()));

        packageEntry.setName(c.getName());
        packageEntry.setScope(c.getScope() == null ? "unscoped" : c.getScope());
        packageEntry.setVersion(c.getVersion());

        return searchResult;
    }

}
