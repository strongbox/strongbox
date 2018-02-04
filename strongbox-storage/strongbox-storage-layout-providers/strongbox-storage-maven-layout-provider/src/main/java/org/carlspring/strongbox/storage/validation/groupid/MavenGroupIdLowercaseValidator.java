package org.carlspring.strongbox.storage.validation.groupid;

import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
import org.carlspring.strongbox.artifact.coordinates.MavenArtifactCoordinates;
import org.carlspring.strongbox.providers.io.RepositoryFileAttributes;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.validation.artifact.ArtifactCoordinatesValidationException;
import org.carlspring.strongbox.storage.validation.artifact.ArtifactCoordinatesValidatorRegistry;
import org.carlspring.strongbox.storage.validation.artifact.LowercaseValidationException;
import org.carlspring.strongbox.storage.validation.artifact.LowercaseValidator;
import org.carlspring.strongbox.storage.validation.artifact.version.SemanticVersioningValidator;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Created by dinesh on 12/6/17.
 */
@Component
public class MavenGroupIdLowercaseValidator
        implements LowercaseValidator
{

    private static final Logger logger = LoggerFactory.getLogger(MavenGroupIdLowercaseValidator.class);

    public static final String ALIAS = "Maven groupId lowercase validator";

    @Inject
    private ArtifactCoordinatesValidatorRegistry artifactCoordinatesValidatorRegistry;


    @PostConstruct
    @Override
    public void register()
    {
        artifactCoordinatesValidatorRegistry.addProvider(ALIAS, this);

        logger.info("Registered artifact coordinates validator '" + getClass().getCanonicalName() +"'" +
                    " with alias '" + ALIAS + "'.");
    }

    @Override
    public String getAlias()
    {
        return ALIAS;
    }

    @Override
    public void validate(Repository repository,
                         ArtifactCoordinates coordinates)
            throws ArtifactCoordinatesValidationException
    {
        MavenArtifactCoordinates mac = (MavenArtifactCoordinates) coordinates;
        if (!mac.getGroupId().toLowerCase().equals(mac.getGroupId()))
        {
            throw new LowercaseValidationException("The groupId should be defined in lowercase.");
        }
    }

    public RepositoryFileAttributes getAttributes(RepositoryPath repositoryPath)
            throws IOException
    {
        return Files.readAttributes(repositoryPath, RepositoryFileAttributes.class);
    }

}

