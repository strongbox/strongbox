package org.carlspring.strongbox.providers.layout;

import java.io.IOException;

import javax.annotation.PostConstruct;

import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
import org.carlspring.strongbox.artifact.coordinates.NugetHierarchicalArtifactCoordinates;
import org.carlspring.strongbox.storage.repository.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Layout provider for Nuget package repository.<br>
 * It provides hierarchical directory layout like follows: <br>
 * &lt;packageID&gt;<br>
 * └─&lt;version&gt;<br>
 * &emsp;├─&lt;packageID&gt;.&lt;version&gt;.nupkg<br>
 * &emsp;├─&lt;packageID&gt;.&lt;version&gt;.nupkg.sha512<br>
 * &emsp;└─&lt;packageID&gt;.nuspec<br>
 * 
 * 
 * @author Sergey Bespalov
 *
 */
@Component
public class NugetHierarchicalLayoutProvider extends AbstractLayoutProvider<NugetHierarchicalArtifactCoordinates>
{
    private static final Logger logger = LoggerFactory.getLogger(NugetHierarchicalLayoutProvider.class);

    public static final String ALIAS = "Nuget Hierarchical";

    @Override
    @PostConstruct
    public void register()
    {
        layoutProviderRegistry.addProvider(ALIAS, this);
        logger.info("Registered layout provider '" + getClass().getCanonicalName() + "' with alias '" + ALIAS + "'.");
    }

    @Override
    public String getAlias()
    {
        return ALIAS;
    }

    @Override
    public NugetHierarchicalArtifactCoordinates getArtifactCoordinates(String path)
    {
        return new NugetHierarchicalArtifactCoordinates(path);
    }

    @Override
    protected boolean isMetadata(String path)
    {
        return path.endsWith("nuspec");
    }
    
    @Override
    protected boolean isChecksum(String path)
    {
        return path.endsWith("nupkg.sha512");
    }

    @Override
    public boolean containsArtifact(Repository repository,
                                    ArtifactCoordinates coordinates)
        throws IOException
    {
        return false;
    }

    @Override
    public boolean contains(String storageId,
                            String repositoryId,
                            String path)
        throws IOException
    {
        return false;
    }

    @Override
    public boolean containsPath(Repository repository,
                                String path)
        throws IOException
    {
        return false;
    }

    @Override
    public void copy(String srcStorageId,
                     String srcRepositoryId,
                     String destStorageId,
                     String destRepositoryId,
                     String path)
        throws IOException
    {

    }

    @Override
    public void move(String srcStorageId,
                     String srcRepositoryId,
                     String destStorageId,
                     String destRepositoryId,
                     String path)
        throws IOException
    {

    }

    @Override
    public void delete(String storageId,
                       String repositoryId,
                       String path,
                       boolean force)
        throws IOException
    {

    }

    @Override
    public void deleteMetadata(String storageId,
                               String repositoryId,
                               String metadataPath)
        throws IOException
    {

    }

    @Override
    public void deleteTrash(String storageId,
                            String repositoryId)
        throws IOException
    {

    }

    @Override
    public void deleteTrash()
        throws IOException
    {

    }

    @Override
    public void undelete(String storageId,
                         String repositoryId,
                         String path)
        throws IOException
    {

    }

    @Override
    public void undeleteTrash(String storageId,
                              String repositoryId)
        throws IOException
    {

    }

    @Override
    public void undeleteTrash()
        throws IOException
    {

    }

}