package org.carlspring.strongbox.storage.validation.version;

import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.validation.artifact.ArtifactCoordinatesValidatorRegistry;
import org.carlspring.strongbox.storage.validation.artifact.version.VersionValidationException;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Regex pattern used here is following Canonical Version format specified in PEP 440 (https://www.python.org/dev/peps/pep-0440/)
 * This Validator ensures strict compliance to the Canonical (normalized) form of the version.
 * Full Canonical pattern is ^((\\d+!)?(\\d+(\\.\\d+)*)((a|b|c|rc)\\d+)?(\\.post\\d+)?\\+[a-zA-Z0-9](\\.*[a-zA-Z0-9])*)?)$
 *
 * @author sainalshah
 */
@Component
public class PypiCanonicalVersionValidator
        implements PypiVersionValidator
{

    public static final String EPOCH_FORMAT = "(\\d+!)";
    public static final String FINAL_RELEASE_FORMAT = "(\\d+(\\.\\d+)*)";
    public static final String PRE_RELEASE_FORMAT = "((a|b|c|rc)\\d+)";
    public static final String POST_RELEASE_FORMAT = "(\\.post\\d+)";
    public static final String DEVELOPMENTAL_RELEASE_FORMAT = "(\\.dev\\d+)";

    //must start and end with ASCII character, can have periods in between
    public static final String LOCAL_VERSION_IDENTIFIER_FORMAT = "(\\+[a-zA-Z0-9](\\.*[a-zA-Z0-9])*)";
    public static final String CANONICAL_VERSION_FORMAT = getCanonicalVersionFormat();


    public static final Pattern PRE_RELEASE_PATTERN = Pattern.compile(PypiCanonicalVersionValidator.PRE_RELEASE_FORMAT);
    public static final Pattern POST_RELEASE_PATTERN = Pattern.compile(
            PypiCanonicalVersionValidator.POST_RELEASE_FORMAT);
    public static final Pattern DEVELOPMENTAL_RELEASE_PATTERN = Pattern.compile(
            PypiCanonicalVersionValidator.DEVELOPMENTAL_RELEASE_FORMAT);
    public static final Pattern LOCAL_VERSION_IDENTIFIER_PATTERN = Pattern.compile(
            PypiCanonicalVersionValidator.LOCAL_VERSION_IDENTIFIER_FORMAT);
    public static final Pattern CANONICAL_PATTERN = Pattern.compile(
            PypiCanonicalVersionValidator.CANONICAL_VERSION_FORMAT);

    @Inject
    private ArtifactCoordinatesValidatorRegistry artifactCoordinatesValidatorRegistry;
    private static final Logger logger = LoggerFactory.getLogger(ArtifactCoordinatesValidatorRegistry.class);

    public static final String ALIAS = "pypi-canonical-version-validator";

    public static final String DESCRIPTION = "PyPi canonical version validator";

    @Override
    public String getDescription()
    {
        return DESCRIPTION;
    }

    @Override
    public void validate(Repository repository,
                         ArtifactCoordinates coordinates)
            throws VersionValidationException
    {
        final Matcher matcher = PypiCanonicalVersionValidator.CANONICAL_PATTERN.matcher(coordinates.getVersion());
        if (!matcher.matches())
        {
            throw new VersionValidationException(String.format("Artifact version [%s] should follow the Canonical " +
                                                               "Versioning specification (https://www.python.org/dev/peps/pep-0440/).",
                                                               coordinates.getVersion()));
        }
    }

    @Override
    public String getAlias()
    {
        return ALIAS;
    }

    @PostConstruct
    @Override
    public void register()
    {
        artifactCoordinatesValidatorRegistry.addProvider(ALIAS, this);

        logger.info("Registered artifact coordinates validator '" + getClass().getCanonicalName() + "'" +
                    " with alias '" + ALIAS + "'.");
    }

    public boolean isPreRelease(String version)
    {
        return PypiCanonicalVersionValidator.PRE_RELEASE_PATTERN.matcher(version).matches();
    }

    public boolean isPostRelease(String version)
    {
        return PypiCanonicalVersionValidator.POST_RELEASE_PATTERN.matcher(version).matches();
    }

    public boolean isDevelopmentalRelease(String version)
    {
        return PypiCanonicalVersionValidator.DEVELOPMENTAL_RELEASE_PATTERN.matcher(version).matches();
    }

    public boolean isLocalVersionIdentifierRelease(String version)
    {
        return PypiCanonicalVersionValidator.LOCAL_VERSION_IDENTIFIER_PATTERN.matcher(version).matches();
    }

    public boolean isFinalRelease(String version)
    {
        return !isPreRelease(version) && !isPostRelease(version) && !isDevelopmentalRelease(version) &&
               !isLocalVersionIdentifierRelease(version);
    }

    private static String getCanonicalVersionFormat()
    {
        return "^(" + EPOCH_FORMAT + "?" + FINAL_RELEASE_FORMAT + PRE_RELEASE_FORMAT + "?" + POST_RELEASE_FORMAT + "?" +
               DEVELOPMENTAL_RELEASE_FORMAT + "?" + LOCAL_VERSION_IDENTIFIER_FORMAT + "?)$";
    }

}
