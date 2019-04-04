package org.carlspring.strongbox.providers.layout;

import org.carlspring.strongbox.artifact.coordinates.PypiArtifactCoordinates;
import org.carlspring.strongbox.providers.header.HeaderMappingRegistry;
import org.carlspring.strongbox.providers.io.RepositoryFileAttributeType;
import org.carlspring.strongbox.providers.io.RepositoryFiles;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.repository.PypiRepositoryFeatures;
import org.carlspring.strongbox.repository.PypiRepositoryManagementStrategy;
import org.carlspring.strongbox.repository.RepositoryManagementStrategy;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.codec.digest.MessageDigestAlgorithms;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author carlspring
 */
@Component
public class PypiLayoutProvider
        extends AbstractLayoutProvider<PypiArtifactCoordinates>
{
    private static final Logger logger = LoggerFactory.getLogger(PypiLayoutProvider.class);

    public static final String ALIAS = PypiArtifactCoordinates.LAYOUT_NAME;

    public static final String USER_AGENT_PREFIX = ALIAS;

    @Inject
    private HeaderMappingRegistry headerMappingRegistry;

    @Inject
    private PypiRepositoryManagementStrategy pypiRepositoryManagementStrategy;

    @Inject
    private PypiRepositoryFeatures pypiRepositoryFeatures;


    @PostConstruct
    public void register()
    {
        headerMappingRegistry.register(ALIAS, USER_AGENT_PREFIX);

        logger.info("Registered layout provider '" + getClass().getCanonicalName() + "' with alias '" + ALIAS + "'.");
    }

    protected PypiArtifactCoordinates getArtifactCoordinates(RepositoryPath path) throws IOException
    {
        return PypiArtifactCoordinates.parse(RepositoryFiles.relativizePath(path));
    }

    public boolean isArtifactMetadata(RepositoryPath path)
    {
        // TODO: Fix
        return false;
    }

    public boolean isMetadata(RepositoryPath path)
    {
        // TODO: Fix
        return false;
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
                    value = (Boolean) value && !isMetadata(repositoryPath);
    
                    if (value != null)
                    {
                        result.put(attributeType, value);
                    }
    
                    break;
                case METADATA:
                    value = (Boolean) value || isMetadata(repositoryPath);
    
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
    
    @Override
    public RepositoryManagementStrategy getRepositoryManagementStrategy()
    {
        return pypiRepositoryManagementStrategy;
    }

    @Override
    public Set<String> getDefaultArtifactCoordinateValidators()
    {
        return pypiRepositoryFeatures.getDefaultArtifactCoordinateValidators();
    }

    @Override
    public String getAlias()
    {
        return ALIAS;
    }

    @Override
    public Set<String> getDigestAlgorithmSet()
    {
        return Stream.of(MessageDigestAlgorithms.SHA_256).collect(Collectors.toSet());
    }

}
