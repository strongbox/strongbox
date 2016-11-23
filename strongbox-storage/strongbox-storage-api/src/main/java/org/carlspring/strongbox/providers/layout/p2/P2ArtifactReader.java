package org.carlspring.strongbox.providers.layout.p2;

import org.carlspring.strongbox.artifact.coordinates.P2ArtifactCoordinates;
import org.carlspring.strongbox.xml.parsers.GenericParser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;

public class P2ArtifactReader
{

    private static final Logger logger = LoggerFactory.getLogger(P2ArtifactReader.class);

    public static P2ArtifactCoordinates getArtifact(String repositoryBaseDir,
                                                    String bundle)
    {
        GenericParser<P2Repository> repositoryParser = new GenericParser<>(P2Repository.class);
        try
        {
            P2Repository p2Repository = repositoryParser.parse(new File(repositoryBaseDir, "artifacts.xml"));
            final P2ArtifactCoordinates artifactToFind = P2ArtifactCoordinates.create(bundle);
            for (P2Artifact p2Artifact : p2Repository.getArtifacts().getArtifacts())
            {
                P2ArtifactCoordinates foundArtifact = new P2ArtifactCoordinates(p2Artifact.getId(), p2Artifact.getVersion(),
                                                                                p2Artifact.getClassifier());
                if (foundArtifact.equals(artifactToFind))
                {
                    return foundArtifact;
                }
            }
        }
        catch (JAXBException | IOException e)
        {
            logger.error(e.getMessage(), e);
        }

        return null;
    }
}
