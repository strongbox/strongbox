package org.carlspring.strongbox.rest;

import org.carlspring.maven.commons.util.ArtifactUtils;
import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.resource.ResourceCloser;
import org.carlspring.strongbox.storage.indexing.SearchRequest;
import org.carlspring.strongbox.storage.indexing.SearchResults;
import org.carlspring.strongbox.storage.services.ArtifactSearchService;
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
import java.util.Collection;

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

    public static final String OUTPUT_INDENT_PRETTY = "pretty";

    @Autowired
    private ArtifactSearchService artifactSearchService;

    @Autowired
    private ConfigurationManager configurationManager;


    /**
     * Performs a search against the Lucene index of a specified repository,
     * or the Lucene indexes of all repositories.
     *
     * @param repository
     * @param query
     * @param format
     * @param indent
     * @return
     * @throws IOException
     * @throws ParseException
     */
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN })
    public Response search(@QueryParam("storage") String storage,
                           @QueryParam("repository") String repository,
                           @QueryParam("q") final String query,
                           @DefaultValue(OUTPUT_FORMAT_PLAIN_TEXT) @QueryParam("format") final String format,
                           @DefaultValue(OUTPUT_INDENT_PRETTY) @QueryParam("indent") final String indent)
            throws IOException, ParseException
    {
        final SearchRequest searchRequest = new SearchRequest(storage, repository, query);
        final SearchResults searchResults = artifactSearchService.search(searchRequest);

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
                        xsw = OUTPUT_FORMAT_XML.equals(format) ?
                              OUTPUT_INDENT_PRETTY.equals(indent) ?
                                  new IndentingXMLStreamWriter(XMLOutputFactory.newFactory().createXMLStreamWriter(os)) :
                                  XMLOutputFactory.newFactory().createXMLStreamWriter(os) :
                              new MappedXMLStreamWriter(new MappedNamespaceConvention(), new OutputStreamWriter(os));

                        xsw.writeStartDocument();
                        xsw.writeStartElement("artifacts");

                        for (String storageAndRepository : searchResults.getResults().keySet())
                        {
                            String[] s = storageAndRepository.split(":");
                            String storage = s[0];
                            String repository = s[1];

                            final Collection<ArtifactInfo> artifactInfos = searchResults.getResults().get(storageAndRepository);

                            for (ArtifactInfo artifactInfo : artifactInfos)
                            {
                                final String gavtc = ArtifactInfoUtils.convertToGAVTC(artifactInfo);
                                final Artifact artifactFromGAVTC = ArtifactUtils.getArtifactFromGAVTC(gavtc);
                                final String pathToArtifactFile = ArtifactUtils.convertArtifactToPath(artifactFromGAVTC);

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
                                xsw.writeStartElement("repository");
                                xsw.writeCharacters(artifactInfo.repository);
                                xsw.writeEndElement();
                                xsw.writeStartElement("path");
                                xsw.writeCharacters(pathToArtifactFile);
                                xsw.writeEndElement();
                                xsw.writeStartElement("url");
                                xsw.writeCharacters(getURLFor(storage, repository, pathToArtifactFile));
                                xsw.writeEndElement();
                                xsw.writeEndElement();
                            }
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
                        ResourceCloser.closeWithReflection(xsw, logger);
                    }
                }
                else
                {
                    try (final Writer w = new OutputStreamWriter(os))
                    {
                        for (String storageAndRepository : searchResults.getResults().keySet())
                        {
                            String[] s = storageAndRepository.split(":");
                            String storage = s[0];
                            String repository = s[1];

                            w.append(storageAndRepository).append("/");
                            w.append(System.lineSeparator());

                            final Collection<ArtifactInfo> artifactInfos = searchResults.getResults().get(storageAndRepository);
                            for (ArtifactInfo artifactInfo : artifactInfos)
                            {
                                final String gavtc = ArtifactInfoUtils.convertToGAVTC(artifactInfo);
                                final Artifact artifactFromGAVTC = ArtifactUtils.getArtifactFromGAVTC(gavtc);
                                final String pathToArtifactFile = ArtifactUtils.convertArtifactToPath(artifactFromGAVTC);

                                w.append("   ").append(gavtc).append(", ");
                                w.append(pathToArtifactFile).append(", ");
                                w.append(getURLFor(storage, repository, pathToArtifactFile));
                                w.append(System.lineSeparator());
                                w.flush();
                            }
                        }
                    }
                }
            }

            private String getURLFor(String storage, String repository, String pathToArtifactFile)
            {
                String baseUrl = configurationManager.getConfiguration().getBaseUrl();
                baseUrl = (baseUrl.endsWith("/") ? baseUrl : baseUrl + "/");

                return baseUrl + "storages/" + storage + "/" + repository + "/" + pathToArtifactFile;
            }

        }).type(OUTPUT_FORMAT_XML.equals(format) ? MediaType.APPLICATION_XML  :
                OUTPUT_FORMAT_JSON.equals(format) ? MediaType.APPLICATION_JSON : MediaType.TEXT_PLAIN).build();
    }

}
