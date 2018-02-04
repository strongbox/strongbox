package org.carlspring.strongbox.storage.validation.artifact.version;

import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.validation.artifact.ArtifactCoordinatesValidatorRegistry;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author carlspring
 * @author Przemyslaw Fusik
 */
@Component
public class GenericSnapshotVersionValidator
        implements VersionValidator
{

    private static final Logger logger = LoggerFactory.getLogger(GenericSnapshotVersionValidator.class);

    public static final String ALIAS = "Generic snapshot version validator";

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
    public boolean supports(Repository repository)
    {
        return repository.getArtifactCoordinateValidators().contains(ALIAS);
    }

    @Override
    public void validate(Repository repository,
                         ArtifactCoordinates coordinates)
            throws VersionValidationException
    {
        String version = coordinates.getVersion();
        if (isSnapshot(version) && !repository.acceptsSnapshots())
        {
            throw new VersionValidationException("Cannot deploy a SNAPSHOT artifact to a repository which " +
                                                 "doesn't accept SNAPSHOT policy!");
        }
        if (!isSnapshot(version) && repository.acceptsSnapshots() && !repository.acceptsReleases())
        {
            throw new VersionValidationException("Cannot deploy a release artifact to a repository with " +
                                                 "a SNAPSHOT policy!");
        }
    }

    public boolean isSnapshot(String version)
    {
        return StringUtils.isNotBlank(version) && StringUtils.endsWithIgnoreCase(version, "-SNAPSHOT");
    }

}

