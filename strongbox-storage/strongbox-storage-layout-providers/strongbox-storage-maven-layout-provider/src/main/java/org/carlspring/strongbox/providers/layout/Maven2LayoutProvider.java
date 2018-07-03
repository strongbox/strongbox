package org.carlspring.strongbox.providers.layout;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.commons.io.FilenameUtils;
import org.apache.maven.artifact.repository.metadata.Metadata;
import org.carlspring.maven.commons.util.ArtifactUtils;
import org.carlspring.strongbox.artifact.MavenArtifact;
import org.carlspring.strongbox.artifact.MavenArtifactUtils;
import org.carlspring.strongbox.artifact.coordinates.MavenArtifactCoordinates;
import org.carlspring.strongbox.providers.io.RepositoryFileAttributeType;
import org.carlspring.strongbox.providers.io.RepositoryFileAttributes;
import org.carlspring.strongbox.providers.io.RepositoryFileSystem;
import org.carlspring.strongbox.providers.io.RepositoryFiles;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.io.RepositoryRelativePathConstructionException;
import org.carlspring.strongbox.repository.MavenRepositoryFeatures;
import org.carlspring.strongbox.repository.MavenRepositoryManagementStrategy;
import org.carlspring.strongbox.storage.metadata.MavenMetadataManager;
import org.carlspring.strongbox.storage.metadata.MetadataHelper;
import org.carlspring.strongbox.storage.metadata.MetadataType;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author carlspring
 */
@Component("maven2LayoutProvider")
public class Maven2LayoutProvider
        extends AbstractLayoutProvider<MavenArtifactCoordinates>
{

    public static final String ALIAS = "Maven 2";

    private static final Logger logger = LoggerFactory.getLogger(Maven2LayoutProvider.class);

    @Inject
    private MavenMetadataManager mavenMetadataManager;

    @Inject
    private MavenRepositoryManagementStrategy mavenRepositoryManagementStrategy;

    @Inject
    private MavenRepositoryFeatures mavenRepositoryFeatures;

    
    @PostConstruct
    public void register()
    {
        layoutProviderRegistry.addProvider(ALIAS, this);

        logger.info("Registered layout provider '" + getClass().getCanonicalName() + "' with alias '" + ALIAS + "'.");
    }

    protected MavenArtifactCoordinates getArtifactCoordinates(RepositoryPath repositoryPath) throws IOException
    {
        MavenArtifact artifact = MavenArtifactUtils.convertPathToArtifact(repositoryPath);

        return new MavenArtifactCoordinates(artifact);
    }

    public boolean isArtifactMetadata(RepositoryPath path)
    {
        return path.getFileName().toString().endsWith(".pom");
    }

    public boolean isMavenMetadata(RepositoryPath path)
    {
        return path.getFileName().toString().equals("maven-metadata.xml");
    }
    
    @Override
    protected Map<RepositoryFileAttributeType, Object> getRepositoryFileAttributes(RepositoryPath repositoryPath,
                                                                                   RepositoryFileAttributeType... attributeTypes)
        throws IOException
    {
        Map<RepositoryFileAttributeType, Object> result = super.getRepositoryFileAttributes(repositoryPath,
                                                                                            attributeTypes);

        for (RepositoryFileAttributeType attributeType : attributeTypes)
        {
            Object value = result.get(attributeType);
            switch (attributeType)
            {
                case ARTIFACT:
                    value = (Boolean) value && !isMavenMetadata(repositoryPath) && !isIndex(repositoryPath);
    
                    if (value != null)
                    {
                        result.put(attributeType, value);
                    }
    
                    break;
                case METADATA:
                    value = (Boolean) value || isMavenMetadata(repositoryPath);
    
                    if (value != null)
                    {
                        result.put(attributeType, value);
                    }
    
                    break;
                default:
    
                    break;
            }
        }

        return result;
    }

    private boolean isIndex(RepositoryPath path)
    {
        if (!path.isAbsolute())
        {
            return false;
        }
        RepositoryPath indexRoot = path.getFileSystem().getRootDirectory().resolve(RepositoryFileSystem.INDEX);
        if (path.startsWith(indexRoot))
        {
            return true;
        }
        
        return false;
    }

    @Override
    public Set<String> getDefaultArtifactCoordinateValidators()
    {
        return mavenRepositoryFeatures.getDefaultArtifactCoordinateValidators();
    }

    @Override
    public void deleteMetadata(RepositoryPath artifactPath)
    {
        try
        {
            RepositoryPath artifactBasePath = artifactPath;
            RepositoryPath artifactIdLevelPath;
            try
            {
                artifactIdLevelPath = artifactBasePath.getParent();
            }
            catch (RepositoryRelativePathConstructionException e)
            {
                //it's repository root directory, so we have nothing to clean here
                return;
            }

            if (Files.exists(artifactPath))
            {

                RepositoryFileAttributes artifactFileAttributes = Files.readAttributes(artifactPath,
                                                                                       RepositoryFileAttributes.class);

                if (!artifactFileAttributes.isDirectory())
                {
                    artifactBasePath = artifactBasePath.getParent();
                    artifactIdLevelPath = artifactIdLevelPath.getParent();

                    // This is at the version level
                    try (Stream<Path> pathStream = Files.list(artifactBasePath))
                    {
                        Path pomPath = pathStream.filter(
                                p -> p.getFileName().toString().endsWith(".pom")).findFirst().orElse(null);

                        if (pomPath != null)
                        {
                            String version = ArtifactUtils.convertPathToArtifact(RepositoryFiles.relativizePath(artifactPath))
                                                          .getVersion();
                            version = version == null ? pomPath.getParent().getFileName().toString() : version;

                            deleteMetadataAtVersionLevel(artifactBasePath, version);
                        }
                    }

                }
            }
            else
            {
                artifactBasePath = artifactBasePath.getParent();
                artifactIdLevelPath = artifactIdLevelPath.getParent();
            }

            if (Files.exists(artifactIdLevelPath))
            {
                // This is at the artifact level
                try (Stream<Path> pathStream = Files.list(artifactIdLevelPath))
                {
                    Path mavenMetadataPath = pathStream.filter(p -> p.getFileName()
                                                                     .toString()
                                                                     .endsWith("maven-metadata.xml"))
                                                       .findFirst()
                                                       .orElse(null);

                    if (mavenMetadataPath != null)
                    {
                        String version = FilenameUtils.getName(artifactBasePath.toString());

                        deleteMetadataAtArtifactLevel((RepositoryPath) mavenMetadataPath.getParent(), version);
                    }
                }
            }
        }
        catch (IOException | XmlPullParserException e)
        {
            // We won't do anything in this case because it doesn't have an impact to the deletion
            logger.error(e.getMessage(), e);
        }
    }

    public void deleteMetadataAtVersionLevel(RepositoryPath metadataBasePath,
                                             String version)
            throws IOException,
                   XmlPullParserException
    {
        if (ArtifactUtils.isSnapshot(version) && Files.exists(metadataBasePath))
        {
            Metadata metadataVersionLevel = mavenMetadataManager.readMetadata(metadataBasePath);
            if (metadataVersionLevel != null && metadataVersionLevel.getVersioning() != null &&
                metadataVersionLevel.getVersioning().getVersions().contains(version))
            {
                metadataVersionLevel.getVersioning().getVersions().remove(version);

                MetadataHelper.setLastUpdated(metadataVersionLevel.getVersioning());

                mavenMetadataManager.storeMetadata(metadataBasePath,
                                                   null,
                                                   metadataVersionLevel,
                                                   MetadataType.SNAPSHOT_VERSION_LEVEL);
            }
        }
    }

    public void deleteMetadataAtArtifactLevel(RepositoryPath artifactPath,
                                              String version)
            throws IOException,
                   XmlPullParserException
    {
        Metadata metadataVersionLevel = mavenMetadataManager.readMetadata(artifactPath);
        if (metadataVersionLevel != null && metadataVersionLevel.getVersioning() != null)
        {
            metadataVersionLevel.getVersioning().getVersions().remove(version);

            if (version.equals(metadataVersionLevel.getVersioning().getLatest()))
            {
                MetadataHelper.setLatest(metadataVersionLevel);
            }

            if (version.equals(metadataVersionLevel.getVersioning().getRelease()))
            {
                MetadataHelper.setRelease(metadataVersionLevel);
            }

            MetadataHelper.setLastUpdated(metadataVersionLevel.getVersioning());

            mavenMetadataManager.storeMetadata(artifactPath,
                                               null,
                                               metadataVersionLevel,
                                               MetadataType.ARTIFACT_ROOT_LEVEL);
        }
    }

    @Override
    public MavenRepositoryManagementStrategy getRepositoryManagementStrategy()
    {
        return mavenRepositoryManagementStrategy;
    }

}
