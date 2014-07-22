package org.carlspring.strongbox.rest;

import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.index.ArtifactInfo;

import org.carlspring.maven.commons.util.ArtifactUtils;
import org.carlspring.strongbox.storage.DataCenter;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexManager;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexer;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.util.ArtifactInfoUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.Set;

@Component
@Path("/search")
public class SearchRestlet
        extends BaseRestlet
{

    private static final Logger logger = LoggerFactory.getLogger(SearchRestlet.class);

    @Autowired
    private RepositoryIndexManager repositoryIndexManager;

    @Autowired
    private DataCenter dataCenter;


    /**
     * Performs a search against the Lucene index of a specified repository.
     *
     * @param repository
     * @param queryText
     * @return
     * @throws IOException
     * @throws ParseException
     */
    @GET
    @Path("lucene/{repository}")
    @Produces(MediaType.TEXT_PLAIN)
    public String search(@PathParam("repository") final String repository,
                         @QueryParam("q") final String queryText)
            throws IOException, ParseException
    {
        final Set<ArtifactInfo> results = repositoryIndexManager.getRepositoryIndex(repository).search(queryText);
        final StringBuilder response = new StringBuilder();

        for (final ArtifactInfo artifactInfo : results)
        {
            final String gavtc = ArtifactInfoUtils.convertToGAVTC(artifactInfo);
            final Artifact artifactFromGAVTC = ArtifactUtils.getArtifactFromGAVTC(gavtc);
            final String pathToArtifactFile = ArtifactUtils.convertArtifactToPath(artifactFromGAVTC);

            response.append(gavtc).append(", ");
            response.append(pathToArtifactFile).append(System.lineSeparator());
        }

        final String responseText = response.toString();

        logger.debug("Response:\n{}", responseText);

        return responseText;
    }

    /**
     * Performs a Lucene search across repositories.
     *
     * @param queryText
     * @return
     * @throws IOException
     * @throws ParseException
     */
    @GET
    @Path("lucene")
    @Produces(MediaType.TEXT_PLAIN)
    public String search(@QueryParam("q") final String queryText)
            throws IOException, ParseException
    {
        final StringBuilder response = new StringBuilder();
        for (Storage storage : dataCenter.getStorages().values())
        {
            for (Repository repository : storage.getRepositories().values())
            {
                final RepositoryIndexer repositoryIndex = repositoryIndexManager.getRepositoryIndex(repository.getName());
                if (repositoryIndex != null)
                {
                    final Set<ArtifactInfo> results = repositoryIndex.search(queryText);

                    if (!results.isEmpty())
                    {
                        response.append(repository.getName()).append("/");
                        response.append(System.lineSeparator());

                        for (final ArtifactInfo artifactInfo : results)
                        {
                            final String gavtc = ArtifactInfoUtils.convertToGAVTC(artifactInfo);
                            final Artifact artifactFromGAVTC = ArtifactUtils.getArtifactFromGAVTC(gavtc);
                            final String pathToArtifactFile = ArtifactUtils.convertArtifactToPath(artifactFromGAVTC);

                            response.append("   ").append(gavtc).append(", ");
                            response.append(pathToArtifactFile).append(System.lineSeparator());
                        }
                    }
                }
            }
        }

        final String responseText = response.toString();

        logger.debug("Response:\n{}", responseText);

        return responseText;
    }

}