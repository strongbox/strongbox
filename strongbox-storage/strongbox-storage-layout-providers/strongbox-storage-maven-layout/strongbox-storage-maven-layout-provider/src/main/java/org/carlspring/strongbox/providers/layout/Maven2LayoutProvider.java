package org.carlspring.strongbox.providers.layout;


import org.carlspring.maven.commons.util.ArtifactUtils;
import org.carlspring.strongbox.artifact.MavenArtifact;
import org.carlspring.strongbox.artifact.MavenArtifactUtils;
import org.carlspring.strongbox.artifact.archive.JarArchiveListingFunction;
import org.carlspring.strongbox.artifact.coordinates.MavenArtifactCoordinates;
import org.carlspring.strongbox.providers.header.HeaderMappingRegistry;
import org.carlspring.strongbox.providers.io.*;
import org.carlspring.strongbox.repository.MavenRepositoryFeatures;
import org.carlspring.strongbox.repository.MavenRepositoryManagementStrategy;
import org.carlspring.strongbox.storage.metadata.MavenMetadataManager;
import org.carlspring.strongbox.storage.metadata.MetadataHelper;
import org.carlspring.strongbox.storage.metadata.MetadataType;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.maven.artifact.repository.metadata.Metadata;
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

    public static final String ALIAS = MavenArtifactCoordinates.LAYOUT_NAME;

    public static final String USER_AGENT_PREFIX = "Maven";

    private static final Logger logger = LoggerFactory.getLogger(Maven2LayoutProvider.class);

    @Inject
    private HeaderMappingRegistry headerMappingRegistry;

    @Inject
    private MavenRepositoryManagementStrategy mavenRepositoryManagementStrategy;

    @Inject
    private MavenRepositoryFeatures mavenRepositoryFeatures;


    @PostConstruct
    public void register()
    {
        headerMappingRegistry.register(ALIAS, USER_AGENT_PREFIX);

        logger.info("Registered layout provider '" + getClass().getCanonicalName() + "' with alias '" + ALIAS + "'.");
    }

    protected MavenArtifactCoordinates getArtifactCoordinates(RepositoryPath repositoryPath)
            throws IOException
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
        return MetadataHelper.MAVEN_METADATA_XML.equals(path.getFileName().toString());
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
                    value = BooleanUtils.isTrue((Boolean) value) && !isMavenMetadata(repositoryPath) &&
                            !isIndex(repositoryPath);

                    result.put(attributeType, value);

                    break;
                case METADATA:
                    value = BooleanUtils.isTrue((Boolean) value) || isMavenMetadata(repositoryPath);

                    result.put(attributeType, value);

                    break;
                case EXPIRED:
                    final Instant oneMinuteAgo = Instant.now().minus(1, ChronoUnit.MINUTES);
                    value = BooleanUtils.isTrue((Boolean) value) || (isMavenMetadata(repositoryPath)
                                                                     &&
                                                                     !RepositoryFiles.wasModifiedAfter(repositoryPath,
                                                                                                       oneMinuteAgo));

                    result.put(attributeType, value);

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
        RepositoryPath indexRoot = path.getFileSystem().getRootDirectory().resolve(LayoutFileSystem.INDEX);
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
    public String getAlias()
    {
        return ALIAS;
    }

    @Override
    public MavenRepositoryManagementStrategy getRepositoryManagementStrategy()
    {
        return mavenRepositoryManagementStrategy;
    }

    @Override
    public Set<String> listArchiveFilenames(final RepositoryPath repositoryPath)
    {
        if (JarArchiveListingFunction.INSTANCE.supports(repositoryPath))
        {
            try
            {
                return JarArchiveListingFunction.INSTANCE.listFilenames(repositoryPath);
            }
            catch (IOException e)
            {
                logger.warn(String.format("Unable to list filenames in archive path %s using %s", repositoryPath,
                                           JarArchiveListingFunction.INSTANCE.getClass()), e);
            }
        }
        return Collections.emptySet();
    }

    public boolean requiresGroupAggregation(final RepositoryPath repositoryPath)
    {
        return isMavenMetadata(repositoryPath) &&
               !ArtifactUtils.isSnapshot(repositoryPath.getParent().getFileName().toString());
    }
}
