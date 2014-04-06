package org.carlspring.strongbox.storage.resolvers;

import org.apache.maven.artifact.Artifact;

import org.carlspring.maven.commons.util.ArtifactUtils;
import org.carlspring.strongbox.annotations.ArtifactExistenceState;
import org.carlspring.strongbox.annotations.ArtifactResource;
import org.carlspring.strongbox.annotations.ArtifactResourceMapper;
import org.carlspring.strongbox.io.RandomInputStream;
import org.carlspring.strongbox.storage.DataCenter;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.Map;

/**
 * @author mtodorov
 */
@Component
public class InMemoryLocationResolver implements LocationResolver
{

    private static final Logger logger = LoggerFactory.getLogger(InMemoryLocationResolver.class);

    private String alias = "in-memory";

    @Autowired
    private DataCenter dataCenter;


    public InMemoryLocationResolver()
    {
    }

    @Override
    public InputStream getInputStream(String repository,
                                      String artifactPath)
            throws IOException
    {
        for (Map.Entry entry : dataCenter.getStorages().entrySet())
        {
            Storage storage = (Storage) entry.getValue();

            if (storage.containsRepository(repository))
            {
                logger.debug("Checking in storage " + storage.getBasedir() + "...");

                final Map<String, Repository> repositories = storage.getRepositories();

                for (Map.Entry<String, Repository> e : repositories.entrySet())
                {
                    Repository r = e.getValue();

                    logger.debug("Checking in repository " + r.getName() + "...");

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
            }
        }

        return null;
    }

    @Override
    public OutputStream getOutputStream(String repository,
                                        String artifactPath)
            throws IOException
    {
        for (Map.Entry entry : dataCenter.getStorages().entrySet())
        {
            Storage storage = (Storage) entry.getValue();

            if (storage.containsRepository(repository))
            {
                if (!artifactPath.contains("/maven-metadata."))
                {
                    Artifact artifact = ArtifactUtils.convertPathToArtifact(artifactPath);
                    ArtifactResourceMapper.addResource(ArtifactResourceMapper.getArtifactResourceInstance(repository,
                                                                                                          artifact,
                                                                                                          10000L, // Hard-coding to 10 KB as we can't guess
                                                                                                                  // the size at this point and we shouldn't be
                                                                                                                  // caring about this too much as it's in memory
                                                                                                          ArtifactExistenceState.EXISTS));
                }

                return new ByteArrayOutputStream();
            }
        }

        return null;
    }

    @Override
    public void delete(String repository,
                       String path)
            throws IOException
    {
        for (Map.Entry entry : dataCenter.getStorages().entrySet())
        {
            Storage storage = (Storage) entry.getValue();

            if (storage.containsRepository(repository))
            {
                if (!path.contains("/maven-metadata."))
                {
                    Artifact artifact = ArtifactUtils.convertPathToArtifact(path);
                    ArtifactResourceMapper.removeResources(artifact.getGroupId(), artifact.getArtifactId(),
                                       artifact.getVersion());

                    logger.debug("Removed /" + repository + path);
                }
            }
        }
    }

    @Override
    public void initialize()
    {
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

    public DataCenter getDataCenter()
    {
        return dataCenter;
    }

    public void setDataCenter(DataCenter dataCenter)
    {
        this.dataCenter = dataCenter;
    }

}
