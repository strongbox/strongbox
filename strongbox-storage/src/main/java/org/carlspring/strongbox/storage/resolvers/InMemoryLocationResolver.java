package org.carlspring.strongbox.storage.resolvers;

import org.apache.maven.artifact.Artifact;

import org.carlspring.maven.commons.util.ArtifactUtils;
import org.carlspring.strongbox.annotations.ArtifactResource;
import org.carlspring.strongbox.annotations.ArtifactResourceMapper;
import org.carlspring.strongbox.io.RandomInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author mtodorov
 */
public class InMemoryLocationResolver implements LocationResolver
{

    private static final Logger logger = LoggerFactory.getLogger(InMemoryLocationResolver.class);

    private String alias = "in-memory";


    public InMemoryLocationResolver()
    {
    }

    @Override
    public InputStream getInputStream(String repository,
                                      String artifactPath)
            throws IOException
    {
        Artifact artifact = ArtifactUtils.convertPathToArtifact(artifactPath);

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

    @Override
    public String getAlias()
    {
        return alias;
    }

    @Override
    public void setAlias(String alias)
    {
        this.alias = alias;
    }

}
