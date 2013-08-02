package org.carlspring.repositoryunit.storage.resolvers;

import org.apache.maven.artifact.Artifact;
import org.carlspring.repositoryunit.storage.DataCenter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;

/**
 * @author mtodorov
 */
public class FSLocationResolver implements LocationResolver
{

    private static final Logger logger = LoggerFactory.getLogger(FSLocationResolver.class);

    private DataCenter dataCenter = new DataCenter();


    public FSLocationResolver()
    {
    }

    @Override
    public InputStream getInputStreamForArtifact(String repository,
                                                 Artifact artifact)
    {
        // TODO: 1) Loop over the storages in the dataCenter

        // TODO: 2) Check if the repositories contain the artifact

        // TODO: 3) Return the InputStream for the artifact

        return null;
    }

    @Override
    public void initialize()
    {
        System.out.println("");
        System.out.println("Initialized FSLocationResolver.");
        System.out.println("");

        logger.debug("Initialized FSLocationResolver.");
    }

    public DataCenter getDataCenter()
    {
        return dataCenter;
    }

    public void setDataCenter(DataCenter dataCenter)
    {
        this.dataCenter = dataCenter;
    }

}
