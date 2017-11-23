package org.carlspring.strongbox.providers.layout;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.commons.codec.digest.MessageDigestAlgorithms;
import org.carlspring.strongbox.artifact.coordinates.NpmArtifactCoordinates;
import org.carlspring.strongbox.repository.NpmRepositoryFeatures;
import org.carlspring.strongbox.repository.NpmRepositoryManagementStrategy;
import org.carlspring.strongbox.repository.RepositoryManagementStrategy;
import org.carlspring.strongbox.services.ArtifactManagementService;
import org.carlspring.strongbox.services.impl.NpmArtifactManagementService;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 
 * @author Sergey Bespalov
 *
 */
@Component
public class NpmLayoutProvider
        extends AbstractLayoutProvider<NpmArtifactCoordinates, NpmRepositoryFeatures, NpmRepositoryManagementStrategy>
{
    private static final Logger logger = LoggerFactory.getLogger(NpmLayoutProvider.class);

    public static final String ALIAS = "npm";

    @Inject
    private NpmRepositoryManagementStrategy npmRepositoryManagementStrategy;

    @Inject
    private NpmArtifactManagementService npmArtifactManagementService;

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
    public NpmArtifactCoordinates getArtifactCoordinates(String path)
    {
        return NpmArtifactCoordinates.parse(path);
    }

    @Override
    public void deleteMetadata(String storageId,
                               String repositoryId,
                               String metadataPath)
        throws IOException
    {

    }

    @Override
    public void rebuildMetadata(String storageId,
                                String repositoryId,
                                String basePath)
        throws IOException,
        NoSuchAlgorithmException,
        XmlPullParserException
    {

    }

    @Override
    public void rebuildIndexes(String storageId,
                               String repositoryId,
                               String basePath,
                               boolean forceRegeneration)
        throws IOException
    {

    }

    @Override
    public boolean isMetadata(String path)
    {
        return path.endsWith("package.json") || path.endsWith("package-lock.json")
                || path.endsWith("npm-shrinkwrap.json");
    }

    @Override
    public RepositoryManagementStrategy getRepositoryManagementStrategy()
    {
        return npmRepositoryManagementStrategy;
    }

    @Override
    public ArtifactManagementService getArtifactManagementService()
    {
        return npmArtifactManagementService;
    }

    @Override
    public Set<String> getDigestAlgorithmSet()
    {
        return Stream.of(MessageDigestAlgorithms.SHA_1)
                     .collect(Collectors.toSet());
    }

}
