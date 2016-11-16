package org.carlspring.strongbox.providers.layout.p2;

import org.carlspring.strongbox.artifact.coordinates.P2ArtifactCoordinates;
import org.carlspring.strongbox.xml.parsers.GenericParser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;

public class ArtifactReader
{

    private static final Logger logger = LoggerFactory.getLogger(ArtifactReader.class);

    public static P2ArtifactCoordinates getArtifact(String repositoryBaseDir,
                                                    String bundle)
    {
        GenericParser<Repository> repositoryParser = new GenericParser<>(Repository.class);
        try
        {
            Repository repository = repositoryParser.parse(new File(repositoryBaseDir, "artifacts.xml"));
            final P2ArtifactCoordinates artifactToFind = P2ArtifactCoordinates.create(bundle);
            for (Artifact artifact : repository.getArtifacts().getArtifacts())
            {
                P2ArtifactCoordinates foundArtifact = new P2ArtifactCoordinates(artifact.getId(), artifact.getVersion(),
                                                                                artifact.getClassifier());
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
