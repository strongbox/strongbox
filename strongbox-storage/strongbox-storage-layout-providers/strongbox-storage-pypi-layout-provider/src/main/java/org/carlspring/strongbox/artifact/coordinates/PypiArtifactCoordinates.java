package org.carlspring.strongbox.artifact.coordinates;

import javax.persistence.Entity;

public class PypiArtifactCoordinates
        extends AbstractArtifactCoordinates<PypiArtifactCoordinates, PypiArtifactCoordinates>
{

    public static final String ID = "id";

    public static final String DISTRIBUTION = "distribution";

    public static final String VERSION = "version";

    public static final String BUILD_TAG = "build_tag";

    public static final String PYTHON_TAG = "python_tag";

    public static final String ABI_TAG = "abi_tag";

    public static final String PLATFORM_TAG = "platform_tag";

    public PypiArtifactCoordinates(String distribution,
                                   String version,
                                   String build_tag,
                                   String python_tag,
                                   String abi_tag,
                                   String platform_tag){
        if (id == null || version == null || classifier == null)
        {
            throw new IllegalArgumentException("Id, version and classifier must be specified");
        }
        setCoordinate(DISTRIBUTION, distribution);
        setVersion(VERSION);
        setCoordinate(BUILD_TAG, build_tag);
        setCoordinate(PYTHON_TAG, python_tag);
        setCoordinate(ABI_TAG, abi_tag);
        setCoordinate(PLATFORM_TAG, platform_tag);

    }

    @Override
    public String getId()
    {
        if (getScope() == null)
        {
            return getName();
        }
        return String.format("%s-%s-%s", getDistribution(), getVersion(), getPlatformTag());
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
        result = 31 * result + getVersion().hashCode();
        result = 31 * result + getClassifier().hashCode();

        return result;
    }

    @Override
    public Map<String, String> dropVersion()
    {
        Map<String, String> result = getCoordinates();
        result.remove(VERSION);
        return result;
    }

}

