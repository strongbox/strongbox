package org.carlspring.strongbox.artifact.coordinates;

import org.carlspring.strongbox.artifact.coordinates.versioning.SemanticVersion;

import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.net.URI;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.util.Assert;

/**
 * @author carlspring
 */
@Entity
@SuppressWarnings("serial")
@XmlRootElement(name = "pypiArtifactCoordinates")
@XmlAccessorType(XmlAccessType.NONE)
@ArtifactCoordinatesLayout(name = PypiArtifactCoordinates.LAYOUT_NAME, alias = PypiArtifactCoordinates.LAYOUT_ALIAS)
public class PypiArtifactCoordinates
        extends AbstractArtifactCoordinates<PypiArtifactCoordinates, SemanticVersion>
{

    public static final String LAYOUT_NAME = "PyPi";

    public static final String LAYOUT_ALIAS = LAYOUT_NAME;

    public static final String PYPI_VERSION_REGEX = "(\\d+.\\d+.\\d+)";

    public static final String PYPI_NAME_REGEX = "[a-zA-Z0-9][\\w-.]*";

    public static final String PYPI_EXTENSION_REGEX = "tar.gz";

    public static final String PYPI_PACKAGE_PATH_REGEX = "(([^/]+.[^/]+)([/]+))?(.+)-(\\d+.\\d+.\\d+)\\.tar\\.gz";

    public static final String PYPI_PACKAGE_FILE_ONLY_REGEX = "(.*)-(\\d+.\\d+.\\d+).tar\\.gz";

    private static final Pattern PYPI_FILE_ONLY_PATTERN = Pattern.compile(PYPI_PACKAGE_FILE_ONLY_REGEX);

    private static final Pattern PYPI_NAME_PATTERN = Pattern.compile(PYPI_NAME_REGEX);

    private static final Pattern PYPI_VERSION_PATTERN = Pattern.compile(PYPI_VERSION_REGEX);

    private static final Pattern PYPI_PATH_PATTERN = Pattern.compile(PYPI_PACKAGE_PATH_REGEX);

    private static final Pattern PYPI_EXTENSION_PATTERN = Pattern.compile(PYPI_EXTENSION_REGEX);

    private static final String NAME = "name";

    private static final String VERSION = "version";

    private static final String EXTENSION = "extension";


    public PypiArtifactCoordinates()
    {
        resetCoordinates(NAME, VERSION, EXTENSION);
    }

    public PypiArtifactCoordinates(String name,
                                   String version,
                                   String extension)
    {
        setName(name);
        setVersion(version);
        setExtension(extension);
    }

    @ArtifactLayoutCoordinate
    public String getName()
    {
        return getCoordinate(NAME);
    }

    public void setName(String name)
    {
        setCoordinate(NAME, name);
    }

    @Override
    public String getId()
    {
        return getName();
    }

    @Override
    public void setId(String id)
    {
        setName(id);
    }

    @Override
    public String getVersion()
    {
        return getCoordinate(VERSION);
    }

    @Override
    public void setVersion(String version)
    {
        SemanticVersion.parse(version);
        setCoordinate(VERSION, version);
    }

    public void setExtension(String extension)
    {
        setCoordinate(EXTENSION, extension);
    }

    @ArtifactLayoutCoordinate
    public String getExtension()
    {
        return getCoordinate(EXTENSION);
    }
    
    @Override
    public String toPath()
    {
        return String.format("%s/%s/%s", getName(), getVersion(), getArtifactFileName());
    }

    @Override
    public URI toResource()
    {
        return URI.create(String.format("%s/-/%s", getId(), getArtifactFileName()));
    }

    public String getGroup()
    {
        String nameLocal = getName();

        return nameLocal;
    }

    public String getArtifactFileName()
    {
        return String.format("%s-%s.%s", getName(), getVersion(), getExtension());
    }

    @Override
    public SemanticVersion getNativeVersion()
    {
        String versionLocal = getVersion();
        if (versionLocal == null)
        {
            return null;
        }

        try
        {
            return SemanticVersion.parse(versionLocal);
        }
        catch (IllegalArgumentException e)
        {
            return null;
        }
    }

    @Override
    public Map<String, String> dropVersion()
    {
        Map<String, String> result = getCoordinates();
        result.remove(VERSION);

        return result;
    }

    public static PypiArtifactCoordinates parse(String path)
    {
        final String errorMessage = String.format("Illegal artifact path [%s], PyPi artifact paths should be in the form of " +
                                                  "'{artifactName}-{artifactVersion}.tar.gz', or " +
                                                  "'{artifactName}/{artifactVersion}/{artifactName}-{artifactVersion}.tar.gz.",
                                                  path);

        Matcher matcherPath = PYPI_PATH_PATTERN.matcher(path);

        String[] coordinates = path.split("/");

        if (coordinates.length == 3)
        {
            // Is file name only
            String name = coordinates[0];
            String version = coordinates[1];
            String extension = "tar.gz";

            Matcher matcherVersion = PYPI_VERSION_PATTERN.matcher(version);
            if (matcherVersion.matches())
            {
                return new PypiArtifactCoordinates(name, version, extension);
            }

            throw new IllegalArgumentException(errorMessage);
        }
        else
        {
            if (coordinates.length == 1 && matcherPath.matches())
            {
                // Is file name only
                String name = matcherPath.group(4);
                String version = matcherPath.group(5);
                String extension = "tar.gz";

                Matcher matcherVersion = PYPI_VERSION_PATTERN.matcher(version);
                if (matcherVersion.matches())
                {
                    return new PypiArtifactCoordinates(name, version, extension);
                }
            }

            throw new IllegalArgumentException(errorMessage);
        }
    }

    public static PypiArtifactCoordinates of(String packageId,
                                             String version)
    {
        return new PypiArtifactCoordinates(packageId, version, "tar.gz");
    }

}
