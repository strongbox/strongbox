package org.carlspring.strongbox.artifact.coordinates;

import org.carlspring.strongbox.artifact.coordinates.versioning.SemanticVersion;
import org.carlspring.strongbox.util.PypiWheelArtifactCoordinatesUtils;

import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

/**
 * This class is an {@link ArtifactCoordinates} implementation for pypi artifacts
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
    extends AbstractArtifactCoordinates<PypiWheelArtifactCoordinates, SemanticVersion>
{

    public static final String LAYOUT_NAME = "PyPi";
    
    public static final String LAYOUT_ALIAS = "pypi";

    public static final String DISTRIBUTION = "distribution";
    
    public static final String VERSION = "version";
    
    public static final String BUILD = "build";
    
    public static final String LANGUAGE_IMPLEMENTATION_VERSION = "languageImplementationVersion";
    
    public static final String ABI = "abi";
    
    public static final String PLATFORM = "platform";

    /**
     * This method takes in all artifact coordinates of a PyPi Wheel filename, with build being 
     * the empty string if it is not included in the filename
     * 
     * @param distribution Uniquely identifying artifact coordinate (required)
     * @param version Packages current version (required)
     * @param build Build_tag parameter (optional)
     * @param languageImplementationVersion Language and Implementation version argument (required)
     * @param abi ABI tag parameter (required)
     * @param platform Platform tag parameter (required)
     */
    public PypiWheelArtifactCoordinates(String distribution,
                                        String version,
                                        String build,
                                        String languageImplementationVersion,
                                        String abi,
                                        String platform)
    {
        // if any of the required arguments are empty, throw an error
        if (StringUtils.isBlank(distribution) || StringUtils.isBlank(version) || StringUtils.isBlank(languageImplementationVersion) || StringUtils.isBlank(abi) || StringUtils.isBlank(platform))
        {
            throw new IllegalArgumentException("The distribution, version, languageImplementationVersion, abi, and platform fields are mandatory.");
        }

        if (!StringUtils.isBlank(build) && !Character.isDigit(build.charAt(0)))
        {
            throw new IllegalArgumentException("Illegal build tag!");
        }

        setId(distribution);
        setVersion(version);
        setBuild(build);
        setLanguageImplementationVersion(languageImplementationVersion);
        setAbi(abi);
        setPlatform(platform);
    }

    /**
     * @param path The filename of the PyPi Wheel package
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
     * @param id DISTRIBUTION coordinate will take this value
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
     * @param version VERSION coordinate takes this value
     */
    @Override
    public void setVersion(String version)
    {
        setCoordinate(VERSION, version);
    }

    /**
     * @return Returns the BUILD coordinate value
     */
    @ArtifactLayoutCoordinate
    public String getBuild()
    {
        return getCoordinate(BUILD);
    }

    /**
     * @param build BUILD coordinate will take this value
     */
    public void setBuild(String build)
    {
        setCoordinate(BUILD, build);
    }

    /**
     * @return Returns the LANGUAGE_IMPLEMENTATION_VERSION coordinate value
     */
    @ArtifactLayoutCoordinate
    public String getLanguageImplementationVersion()
    {
        return getCoordinate(LANGUAGE_IMPLEMENTATION_VERSION);
    }

    /**
     * @param lang LANGUAGE_IMPLEMENTATION_VERSION takes this value
     */
    public void setLanguageImplementationVersion(String lang)
    {
        setCoordinate(LANGUAGE_IMPLEMENTATION_VERSION, lang);
    }

    /**
     * @return Returns the ABI coordinate value
     */
    @ArtifactLayoutCoordinate
    public String getAbi()
    {
        return getCoordinate(ABI);
    }

    /**
     * @param abi ABI coordinate takes this value
     */
    public void setAbi(String abi)
    {
        setCoordinate(ABI, abi);
    }

    /**
     * @return Returns the PLATFORM coordinate value
     */
    @ArtifactLayoutCoordinate
    public String getPlatform()
    {
        return getCoordinate(PLATFORM);
    }

    /**
     * @param platform PLATFORM coordinate takes this value
     */
    public void setPlatform(String platform)
    {
        setCoordinate(PLATFORM, platform);
    }

    /**
     * @return Returns the reconstructed path from the stored coordinate values
     */
    @Override
    public String toPath()
    {
        // if optional BUILD coordinate is empty, don't include it in the reconstruction
        if (StringUtils.isBlank(getBuild()))
        {
            return String.format("%s-%s-%s-%s-%s", getId(), getVersion(), getLanguageImplementationVersion(), getAbi(), getPlatform()) + ".whl";
        }

        return String.format("%s-%s-%s-%s-%s-%s", getId(), getVersion(), getBuild(), getLanguageImplementationVersion(), getAbi(), getPlatform()) + ".whl";
    }

    /**
     * @return Returns the native version of the package
     */
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
