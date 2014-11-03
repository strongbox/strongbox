package org.carlspring.strongbox.rest.serialization.search;

import org.carlspring.maven.commons.util.ArtifactUtils;
import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.storage.indexing.SearchResults;
import org.carlspring.strongbox.util.ArtifactInfoUtils;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Collection;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.index.ArtifactInfo;

/**
 * @author mtodorov
 */
public class ArtifactSearchPlainTextSerializer extends AbstractArtifactSearchSerializer
{


    public ArtifactSearchPlainTextSerializer(ConfigurationManager configurationManager)
    {
        super(configurationManager);
    }

    @Override
    public void write(SearchResults searchResults,
                      OutputStream os,
                      boolean indent)
            throws XMLStreamException, IOException
    {
        /*
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
        */
    }


}
