package org.carlspring.strongbox.rest;

import org.carlspring.maven.commons.util.ArtifactUtils;
import org.carlspring.strongbox.storage.DataCenter;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexManager;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexer;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.util.ArtifactInfoUtils;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Set;

import javanet.staxutils.IndentingXMLStreamWriter;
import org.apache.lucene.queryParser.ParseException;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.index.ArtifactInfo;
import org.codehaus.jettison.mapped.MappedNamespaceConvention;
import org.codehaus.jettison.mapped.MappedXMLStreamWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Path("/search")
public class SearchRestlet
        extends BaseRestlet
{

    private static final Logger logger = LoggerFactory.getLogger(SearchRestlet.class);

    public static final String OUTPUT_FORMAT_PLAIN_TEXT = "text";

    public static final String OUTPUT_FORMAT_XML = "xml";

    public static final String OUTPUT_FORMAT_JSON = "json";

    @Autowired
    private RepositoryIndexManager repositoryIndexManager;

    @Autowired
    private DataCenter dataCenter;


    /**
     * Performs a search against the Lucene index of a specified repository.
     *
     * @param repository
     * @param queryText
     * @param format
     * @return
     * @throws IOException
     * @throws ParseException
     */
    @GET
    @Path("lucene/{repository}")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN })
    public Response search(@PathParam("repository") final String repository,
                           @QueryParam("q") final String queryText,
                           @DefaultValue(OUTPUT_FORMAT_PLAIN_TEXT) @QueryParam("format") final String format)
            throws IOException, ParseException
    {
        final Set<ArtifactInfo> results = repositoryIndexManager.getRepositoryIndex(repository).search(queryText);
        return Response.ok(new StreamingOutput()
        {
            @Override
            public void write(final OutputStream os) throws IOException, WebApplicationException
            {
                if (OUTPUT_FORMAT_XML.equals(format) || OUTPUT_FORMAT_JSON.equals(format))
                {
                    XMLStreamWriter xsw = null;

                    try
                    {
                        xsw = "xml".equals(format) ?
                              new IndentingXMLStreamWriter(XMLOutputFactory.newFactory().createXMLStreamWriter(os)) :
                              new MappedXMLStreamWriter(new MappedNamespaceConvention(), new OutputStreamWriter(os));

                        xsw.writeStartDocument();
                        xsw.writeStartElement("artifacts");

                        for (final ArtifactInfo artifactInfo : results)
                        {
                            xsw.writeStartElement("artifact");
                            xsw.writeStartElement("groupId");
                            xsw.writeCharacters(artifactInfo.groupId);
                            xsw.writeEndElement();
                            xsw.writeStartElement("artifactId");
                            xsw.writeCharacters(artifactInfo.artifactId);
                            xsw.writeEndElement();
                            xsw.writeStartElement("version");
                            xsw.writeCharacters(artifactInfo.version);
                            xsw.writeEndElement();
                            xsw.writeEndElement();
                        }

                        xsw.writeEndElement();
                        xsw.writeEndDocument();
                        xsw.flush();
                    }
                    catch (XMLStreamException ex)
                    {
                        throw new IOException(ex);
                    }
                    finally
                    {
                        if (xsw != null)
                        {
                            try
                            {
                                xsw.close();
                            }
                            catch (XMLStreamException e)
                            {
                                logger.trace(e.getMessage(), e);
                            }
                        }
                    }
                }
                else
                {
                    try (final Writer w = new OutputStreamWriter(os))
                    {
                        if (!results.isEmpty())
                        {
                            w.append(repository).append("/");
                            w.append(System.lineSeparator());

                            for (final ArtifactInfo artifactInfo : results)
                            {
                                final String gavtc = ArtifactInfoUtils.convertToGAVTC(artifactInfo);
                                final Artifact artifactFromGAVTC = ArtifactUtils.getArtifactFromGAVTC(gavtc);
                                final String pathToArtifactFile = ArtifactUtils.convertArtifactToPath(artifactFromGAVTC);

                                w.append("   ").append(gavtc).append(", ");
                                w.append(pathToArtifactFile).append(System.lineSeparator());
                                w.flush();
                            }
                        }
                    }
                }
            }
        }).type(OUTPUT_FORMAT_XML.equals(format) ? MediaType.APPLICATION_XML  :
                OUTPUT_FORMAT_JSON.equals(format) ? MediaType.APPLICATION_JSON : MediaType.TEXT_PLAIN).build();
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