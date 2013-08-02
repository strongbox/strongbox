package org.carlspring.repositoryunit.storage.resolvers;

import org.apache.maven.artifact.Artifact;
import org.carlspring.repositoryunit.annotations.ArtifactResource;
import org.carlspring.repositoryunit.annotations.ArtifactResourceMapper;
import org.carlspring.repositoryunit.io.RandomInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;

/**
 * @author mtodorov
 */
public class InMemoryLocationResolver implements LocationResolver
{

    private static final Logger logger = LoggerFactory.getLogger(InMemoryLocationResolver.class);


    public InMemoryLocationResolver()
    {
    }

    @Override
    public InputStream getInputStreamForArtifact(String repository,
                                                 Artifact artifact)
    {
        final ArtifactResource resource = ArtifactResourceMapper.getResource(artifact.getGroupId(),
                                                                             artifact.getArtifactId(),
                                                                             artifact.getVersion());

        if (resource == null)
        {
            logger.debug("Artifact " + artifact.toString() + " not found.");

            return null;
        }
        else
        {
            // Create random data.
            System.out.println("Generating stream with " + resource.length() + " bytes.");

            return new RandomInputStream(resource.length());
        }
    }

    @Override
    public void initialize()
    {
        System.out.println("");
        System.out.println("Initialized InMemoryLocationResolver.");
        System.out.println("");

        logger.debug("Initialized InMemoryLocationResolver.");
    }

}
