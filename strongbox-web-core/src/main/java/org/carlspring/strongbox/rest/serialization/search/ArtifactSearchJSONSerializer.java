package org.carlspring.strongbox.rest.serialization.search;

import org.carlspring.maven.commons.util.ArtifactUtils;
import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.resource.ResourceCloser;
import org.carlspring.strongbox.storage.indexing.SearchResults;
import org.carlspring.strongbox.util.ArtifactInfoUtils;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Collection;

import de.odysseus.staxon.json.JsonXMLConfig;
import de.odysseus.staxon.json.JsonXMLConfigBuilder;
import de.odysseus.staxon.json.JsonXMLOutputFactory;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.index.ArtifactInfo;
import org.codehaus.jettison.mapped.MappedNamespaceConvention;
import org.codehaus.jettison.mapped.MappedXMLStreamWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mtodorov
 */
public class ArtifactSearchJSONSerializer extends AbstractArtifactSearchSerializer
{

    private static final Logger logger = LoggerFactory.getLogger(ArtifactSearchJSONSerializer.class);


    public ArtifactSearchJSONSerializer(ConfigurationManager configurationManager)
    {
        super(configurationManager);
    }

    @Override
    public void write(SearchResults searchResults,
                      OutputStream os,
                      boolean indent)
            throws XMLStreamException, IOException
    {
        XMLStreamWriter xsw = null;

        try
        {
            if (indent)
            {
                JsonXMLConfig config = new JsonXMLConfigBuilder().prettyPrint(true).build();
                XMLOutputFactory factory = new JsonXMLOutputFactory(config);
                xsw = factory.createXMLStreamWriter(os);
            }
            else
            {
                xsw = new MappedXMLStreamWriter(new MappedNamespaceConvention(), new OutputStreamWriter(os));
            }

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
                    xsw.writeCharacters(artifactInfo.getGroupId());
                    xsw.writeEndElement();
                    xsw.writeStartElement("artifactId");
                    xsw.writeCharacters(artifactInfo.getArtifactId());
                    xsw.writeEndElement();
                    xsw.writeStartElement("version");
                    xsw.writeCharacters(artifactInfo.getVersion());
                    xsw.writeEndElement();
                    xsw.writeStartElement("repository");
                    xsw.writeCharacters(artifactInfo.getRepository());
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
        finally
        {
            ResourceCloser.closeWithReflection(xsw, logger);
        }
    }

}
