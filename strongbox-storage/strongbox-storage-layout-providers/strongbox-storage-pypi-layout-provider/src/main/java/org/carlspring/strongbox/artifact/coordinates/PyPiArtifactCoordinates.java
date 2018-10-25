package org.carlspring.strongbox.artifact.coordinates;

import javax.persistence.Entity;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.semver.Version;

public class PyPiArtifactCoordinates
        extends AbstractArtifactCoordinates<PyPiArtifactCoordinates, Version>
{


    public static final String pypiVersionRegex = "[0-9].[0-9]";
    public static final String pypiNameRegex = "[a-z0-9]*";
    public static final String buildTagRegex = "-[0-9]?";
    public static final String pythonTagRegex = "[a-z][a-z][0-9][0-9]";
    public static final String abiTagRegex = "[a-z0-9]*";
    public static final String platformTagRegex = "[a-z0-9]*";
    public static final String pypiExtensionRegex = "whl";
    public static final String pypiPackageRegex = pypiNameRegex + "-" + pypiVersionRegex + buildTagRegex  + "-" + pythonTagRegex  + "-" + abiTagRegex  + "-" + platformTagRegex + "-" + pypiExtensionRegex;

    private static final Pattern PYPI_PATH_PATTERN = Pattern.compile(pypiPackageRegex);

    public static final String ID = "id";

    public static final String DISTRIBUTION = "distribution";

    public static final String VERSION = "version";

    public static final String BUILD_TAG = "buildTag";

    public static final String PYTHON_TAG = "pythonTag";

    public static final String ABI_TAG = "abiTag";

    public static final String PLATFORM_TAG = "platformTag";

    public PyPiArtifactCoordinates(String distribution,
                                   String version,
                                   String buildTag,
                                   String pythonTag,
                                   String abiTag,
                                   String platform_tag)
    {
        if (distribution == null || version == null || pythonTag == null || abiTag == null || platform_tag == null)
        {
            throw new IllegalArgumentException("Id, version, pythonTag, abiTag and platformTag must be specified");
        }
        setCoordinate(DISTRIBUTION, distribution);
        setCoordinate(VERSION, version);
        setCoordinate(BUILD_TAG, buildTag);
        setCoordinate(PYTHON_TAG, pythonTag);
        setCoordinate(ABI_TAG, abiTag);
        setCoordinate(PLATFORM_TAG, platform_tag);

    }

    @Override
    public String getId()
    {
        return String.format("%s-%s-%s-%s-%s-%s", getDistribution(), getVersion(), getBuildTag(), getPythonTag(), getAbiTag(), getPlatformTag());
    }

    @Override
    public void setId(String id)
    {

        setCoordinate(ID, id);
    }

    public String getDistribution()
    {
        return getCoordinate(DISTRIBUTION);
    }

    @Override
    public String getVersion()
    {
        return getCoordinate(VERSION);
    }

    @Override
    public void setVersion(String version)
    {
        Version.parse(version);
        setCoordinate(VERSION, version);
    }

    @Override
    public Version getNativeVersion()
    {
        String versionLocal = getVersion();
        if (versionLocal == null)
        {
            return null;
        }
        try
        {
            return Version.parse(versionLocal);
        }
        catch (IllegalArgumentException e)
        {
            return null;
        }
    }

    @Override
    public String toPath()
    {
        return String.format("%s-%s-%s", getDistribution(), getVersion(), getPlatformTag());

    }


    public String getBuildTag()
    {
        return getCoordinate(BUILD_TAG);
    }

    public String getPythonTag()
    {
        return getCoordinate(PYTHON_TAG);
    }

    public String getAbiTag()
    {
        return getCoordinate(ABI_TAG);
    }

    public String getPlatformTag()
    {
        return getCoordinate(PLATFORM_TAG);
    }

    @Override
    public int hashCode()
    {
        int result = getId().hashCode();
        result = 31 * result + getDistribution().hashCode();

        return result;
    }

    @Override
    public Map<String, String> dropVersion()
    {
        Map<String, String> result = getCoordinates();
        result.remove(VERSION);
        return result;
    }

    public static PyPiArtifactCoordinates parse(String path)
    {
        Matcher matcher = PYPI_PATH_PATTERN.matcher(path);

        assert matcher.matches() == true;

        String distribution = matcher.group(1);
        String version = matcher.group(2);
        String buildTag = matcher.group(3);
        String pythonTag = matcher.group(4);
        String abiTag = matcher.group(5);
        String platformTag = matcher.group(6);

        return new PyPiArtifactCoordinates(distribution,
                                           version,
                                           buildTag,
                                           pythonTag,
                                           abiTag,
                                           platformTag);
    }

}

