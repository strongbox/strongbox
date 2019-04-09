package org.carlspring.strongbox.artifact.coordinates;

import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Map;

import org.carlspring.strongbox.util.PypiWheelArtifactCoordinatesUtils;
import org.semver.Version;

/**
 * Represents {@link ArtifactCoordinates} for PyPi repository
 * 
 * Proper path for this coordinates is in the format of: 
 * {distribution}-{version}(-{build tag})?-{python tag}-{abi tag}-{platform tag}.whl.
 * Example: distribution-1.0-1-py27-none-any.whl
 * 
 * @author alecg956
 */

@Entity
@SuppressWarnings("serial")
@XmlRootElement(name = "PypiWheelArtifactCoordinates")
@XmlAccessorType(XmlAccessType.NONE)
@ArtifactCoordinatesLayout(name = PypiWheelArtifactCoordinates.LAYOUT_NAME, alias = PypiWheelArtifactCoordinates.LAYOUT_ALIAS)
public class PypiWheelArtifactCoordinates
extends AbstractArtifactCoordinates<PypiWheelArtifactCoordinates, Version>
{
    public static final String LAYOUT_NAME = "PyPi";
    public static final String LAYOUT_ALIAS = "pypi";

    public static final String DISTRIBUTION = "distribution";
    public static final String VERSION = "version";
    public static final String BUILD_TAG = "build_tag";
    public static final String LANG_IMPL_VERSION_TAG = "lang_impl_version_tag";
    private static final String ABI_TAG = "abi_tag";
    private static final String PLATFORM_TAG = "platform_tag";

    /**
     * This method takes in all artifact coordinates of a PyPi Wheel filename, with build being 
     * the empty string if it is not included in the filename
     * 
     * @param distribution: Uniquely identifying artifact coordinate (required)
     * @param version: Packages current version (required)
     * @param build: Build_tag parameter (optional)
     * @param lang_impl_version: Language and Implementation version argument (required)
     * @param abi: ABI tag parameter (required)
     * @param platform: Platform tag parameter (required)
     * @return bar Returns bar
     */
    public PypiWheelArtifactCoordinates(String distribution,
            String version,
            String build,
            String lang_impl_version,
            String abi,
            String platform)
    {
        // if any of the required arguments are empty, throw an error
        if (distribution == "" || version == "" || lang_impl_version == "" || abi == "" || platform == "")
        {
            throw new IllegalArgumentException("distribution, version, language_implementation_version_tag, abi_tag, and platform_tag must be specified");
        }

        if (build != "" && !Character.isDigit(build.charAt(0)))
        {
            throw new IllegalArgumentException("illegal build tag");
        }

        setId(distribution);
        setVersion(version);
        setBuild(build);
        setLang(lang_impl_version);
        setAbi(abi);
        setPlatform(platform);
    }

    /**
     * @param path: The filename of the PyPi Wheel package
     * @return Returns a PyPiWheelArtifactCoordinates object with all included coordinates set
     */
    public static PypiWheelArtifactCoordinates parse(String path)
    {
        return PypiWheelArtifactCoordinatesUtils.parse(path);
    }

    /**
     * @return Returns distribution coordinate value (serves as the unique ID)
     */
    @Override
    public String getId()
    {
        return getCoordinate(DISTRIBUTION);
    }

    /**
     * @param id: DISTRIBUTION coordinate will take this value
     */
    @Override
    public void setId(String id)
    {
        setCoordinate(DISTRIBUTION, id);
    }

    /**
     * @return Returns the VERSION coordinate value
     */
    @Override
    public String getVersion()
    {
        return getCoordinate(VERSION);
    }

    /**
     * @param version: VERSION coordinate takes this value
     */
    @Override
    public void setVersion(String version)
    {
        setCoordinate(VERSION, version);
    }

    /**
     * @return Returns the BUILD_TAG coordinate value
     */
    @ArtifactLayoutCoordinate
    public String getBuild()
    {
        return getCoordinate(BUILD_TAG);
    }

    /**
     * @param build: BUILD_TAG coordinate will take this value
     */
    public void setBuild(String build)
    {
        setCoordinate(BUILD_TAG, build);
    }

    /**
     * @return Returns the LANG_IMPL_VERSION_TAG coordinate value
     */
    @ArtifactLayoutCoordinate
    public String getLang()
    {
        return getCoordinate(LANG_IMPL_VERSION_TAG);
    }

    /**
     * @param lang: LANG_IMPL_VERSION_TAG takes this value
     */
    public void setLang(String lang)
    {
        setCoordinate(LANG_IMPL_VERSION_TAG, lang);
    }

    /**
     * @return Returns the ABI_TAG coordinate value
     */
    @ArtifactLayoutCoordinate
    public String getAbi()
    {
        return getCoordinate(ABI_TAG);
    }

    /**
     * @param abi: ABI_TAG coordinate takes this value
     */
    public void setAbi(String abi)
    {
        setCoordinate(ABI_TAG, abi);
    }

    /**
     * @return Returns the PLATFORM_TAG coordinate value
     */
    @ArtifactLayoutCoordinate
    public String getPlatform()
    {
        return getCoordinate(PLATFORM_TAG);
    }

    /**
     * @param platform: PLATFORM_TAG coordinate takes this value
     */
    public void setPlatform(String platform)
    {
        setCoordinate(PLATFORM_TAG, platform);
    }

    /**
     * @return Returns the reconstructed path from the stored coordinate values
     */
    @Override
    public String toPath()
    {
        // if optional BUILD_TAG coordinate is empty, don't include it in the reconstruction
        if (getBuild() == "")
        {
            return String.format("%s-%s-%s-%s-%s", getId(), getVersion(), getLang(), getAbi(), getPlatform()) + ".whl";
        }

        return String.format("%s-%s-%s-%s-%s-%s", getId(), getVersion(), getBuild(), getLang(), getAbi(), getPlatform()) + ".whl";
    }

    /**
     * @return Returns the native version of the package
     */
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

    /**
     * @return Returns a map data structure of the coordinates without the VERSION coordinate
     */
    @Override
    public Map<String, String> dropVersion()
    {
        Map<String, String> result = getCoordinates();
        result.remove(VERSION);
        return result;
    }
}
