package org.carlspring.strongbox.providers.layout;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.spi.FileSystemProvider;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.commons.codec.digest.MessageDigestAlgorithms;
import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
import org.carlspring.strongbox.artifact.coordinates.NugetHierarchicalArtifactCoordinates;
import org.carlspring.strongbox.io.ArtifactOutputStream;
import org.carlspring.strongbox.providers.io.RepositoryFileSystemProvider;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.repository.NugetRepositoryFeatures;
import org.carlspring.strongbox.repository.NugetRepositoryManagementStrategy;
import org.carlspring.strongbox.services.ArtifactManagementService;
import org.carlspring.strongbox.services.impl.NugetArtifactManagementService;
import org.carlspring.strongbox.storage.repository.Repository;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
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
public class NugetHierarchicalLayoutProvider
        extends AbstractLayoutProvider<NugetHierarchicalArtifactCoordinates,
                                              NugetRepositoryFeatures,
                                              NugetRepositoryManagementStrategy>
{
    private static final Logger logger = LoggerFactory.getLogger(NugetHierarchicalLayoutProvider.class);

    public static final String ALIAS = "Nuget Hierarchical";

    @Inject
    private NugetRepositoryFeatures nugetRepositoryFeatures;

    @Inject
    private NugetRepositoryManagementStrategy nugetRepositoryManagementStrategy;

    @Inject
    private NugetArtifactManagementService nugetArtifactManagementService;


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
    public boolean isMetadata(String path)
    {
        return path.endsWith(".nuspec");
    }

    @Override
    public void deleteMetadata(String storageId,
                               String repositoryId,
                               String metadataPath)
        throws IOException
    {

    }

    private String toBase64(byte[] digest)
    {
        byte[] encoded = Base64.getEncoder()
                               .encode(digest);
        return new String(encoded);
    }

    @Override
    public Set<String> getDigestAlgorithmSet()
    {
        return Stream.of(MessageDigestAlgorithms.SHA_512)
                     .collect(Collectors.toSet());
    }

    @Override
    public void rebuildMetadata(String storageId,
                                String repositoryId,
                                String basePath)
            throws IOException,
                   NoSuchAlgorithmException,
                   XmlPullParserException
    {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public void rebuildIndexes(String storageId,
                               String repositoryId,
                               String basePath,
                               boolean forceRegeneration)
            throws IOException
    {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public NugetRepositoryFeatures getRepositoryFeatures()
    {
        return nugetRepositoryFeatures;
    }

    @Override
    public NugetRepositoryManagementStrategy getRepositoryManagementStrategy()
    {
        return nugetRepositoryManagementStrategy;
    }

    @Override
    public RepositoryFileSystemProvider getProvider(Repository repository)
    {
        FileSystemProvider storageFileSystemProvider = getStorageProvider(repository).getFileSystemProvider();
        RepositoryLayoutFileSystemProvider repositoryFileSystemProvider = new NugetRepositoryLayoutFileSystemProvider(
                storageFileSystemProvider);
        return repositoryFileSystemProvider;
    }

    public class NugetRepositoryLayoutFileSystemProvider extends RepositoryLayoutFileSystemProvider {

        public NugetRepositoryLayoutFileSystemProvider(FileSystemProvider storageFileSystemProvider)
        {
            super(storageFileSystemProvider, null, NugetHierarchicalLayoutProvider.this);
        }

        @Override
        protected ArtifactOutputStream decorateStream(RepositoryPath path,
                                                      OutputStream os,
                                                      ArtifactCoordinates artifactCoordinates)
            throws NoSuchAlgorithmException,
            IOException
        {
            ArtifactOutputStream result = super.decorateStream(path, os, artifactCoordinates);
            result.setDigestStringifier(NugetHierarchicalLayoutProvider.this::toBase64);
            return result;
        }
        
    }
    
    @Override
    public ArtifactManagementService getArtifactManagementService()
    {
        return nugetArtifactManagementService;
    }

    @Override
    public String resolveResourcePath(Repository repository,
                                      String path)
        throws IOException
    {
        NugetHierarchicalArtifactCoordinates c = getArtifactCoordinates(path);
        return "package/" + c.getId() + "/" + c.getVersion();
    }

}