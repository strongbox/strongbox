package org.carlspring.strongbox.artifact.coordinates;

import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Map;

import org.semver.Version;



/*
 * Represents {@link ArtifactCoordinates} for PyPi repository
 * <p>
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
    
    
    // these are the variables used to store the artifact coordinates for PypiWheels
	public static final String DISTRIBUTION = "distribution";

    public static final String VERSION = "version";

    public static final String BUILD_TAG = "build_tag";

    public static final String LANG_IMPL_VERSION_TAG = "lang_impl_version_tag";

    private static final String ABI_TAG = "abi_tag";
	
    private static final String PLATFORM_TAG = "platform_tag";
    
	public PypiWheelArtifactCoordinates(String distribution,
			String version,
			String build,
			String lang_impl_version,
			String abi,
			String platform)
	{

		// if any of the required artifacts are missing throw an exception
		if (distribution == "" || version == "" || lang_impl_version == "" || abi == "" || platform == "")
		{
			throw new IllegalArgumentException("distribution, version, language_implementation_version_tag, abi_tag, and platform_tag must be specified");
		}
	
		// set each of the coordinates
		setId(distribution);
		setVersion(version);
		setBuild(build);
		setLang(lang_impl_version);
		setAbi(abi);
		setPlatform(platform);
	}
	
	
	// temporary, I think it should go in utils
    public static PypiWheelArtifactCoordinates parse(String path)
    {
        return null;
    }
	
	
	// get distribution coordinate which serves as ID
	@Override
    public String getId()
    {
        return getCoordinate(DISTRIBUTION);
    }

	
	// set distribution coordinate which serves as ID
    @Override
    public void setId(String id)
    {
        setCoordinate(DISTRIBUTION, id);
    }

    
    // get version coordinate
    @Override
    public String getVersion()
    {
        return getCoordinate(VERSION);
    }
    

    // set version coordinate
    @Override
    public void setVersion(String version)
    {
        setCoordinate(VERSION, version);
    }
    
    
    // get build_tag coordinate
    @ArtifactLayoutCoordinate
    public String getBuild()
    {
        return getCoordinate(BUILD_TAG);
    }
    

    // set build_tag coordinate
    public void setBuild(String build)
    {
        setCoordinate(BUILD_TAG, build);
    }
    
    
    // get language implementation and version tag coordinate
    @ArtifactLayoutCoordinate
    public String getLang()
    {
        return getCoordinate(LANG_IMPL_VERSION_TAG);
    }
    
    
    // set language implementation and version tag coordinate
    public void setLang(String lang)
    {
        setCoordinate(LANG_IMPL_VERSION_TAG, lang);
    }
    
    
    // get ABI_tag coordinate
    @ArtifactLayoutCoordinate
    public String getAbi()
    {
        return getCoordinate(ABI_TAG);
    }
    
    
    // set ABI_tag coordinate
    public void setAbi(String abi)
    {
    
        setCoordinate(ABI_TAG, abi);
    }
    
    
    // get platform_tag coordinate
    @ArtifactLayoutCoordinate
    public String getPlatform()
    {
        return getCoordinate(PLATFORM_TAG);
    }
    
    
    // set platform_tag coordinate
    public void setPlatform(String platform)
    {
    
        setCoordinate(PLATFORM_TAG, platform);
    }
    
    
    // reconstruct the path string from the artifact coordinates 
    @Override
    public String toPath()
    {
    	if (getBuild() == "")
    	{
    		return String.format("%s-%s-%s-%s-%s", getId(), getVersion(), getLang(), getAbi(), getPlatform());
    	}
    	
    	return String.format("%s-%s-%s-%s-%s-%s", getId(), getVersion(), getBuild(), getLang(), getAbi(), getPlatform());
        
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
    
    
    // erase the version coordinate from the result set
    @Override
    public Map<String, String> dropVersion()
    {
        Map<String, String> result = getCoordinates();
        result.remove(VERSION);
        return result;
    }
	
}