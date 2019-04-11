package org.carlspring.strongbox.util;

import org.carlspring.strongbox.artifact.coordinates.PypiWheelArtifactCoordinates;

/**
 * Class to handle parsing of PyPi filename string
 * 
 * @author alecg956
 */
public class PypiWheelArtifactCoordinatesUtils
{
    /**
     * If optional build parameter is not found in the filename the empty string is specified for build_tag
     * in the construction of a PypiWheelArtifactCoordinates object
     * 
     * Format of Wheel: {distribution}-{version}(-{build tag})?-{python tag}-{abi tag}-{platform tag}.whl.
     * 
     * @param path: The filename of the PyPi Wheel artifact
     * @return Returns a PypiWheelArtifactCoordinate object with all coordinates in the filename set
     */
    public static PypiWheelArtifactCoordinates parse(String path)
    {
        String[] splitArray = path.split("-");

        // check for invalid file format
        if (splitArray.length != 5 && splitArray.length != 6)
        {
            throw new IllegalArgumentException("Invalid Wheel filename specified");
        }

        String distribution = splitArray[0];
        String version = splitArray[1];
        String build = "";
        String languageImplementationVersion;
        String abi;
        String platform;

        // build tag not included
        if (splitArray.length == 5)
        {
            languageImplementationVersion = splitArray[2];
            abi = splitArray[3];
            platform = splitArray[4].substring(0, splitArray[4].indexOf(".whl"));

        }
        // build tag is included
        else
        {
            build = splitArray[2];            
            languageImplementationVersion = splitArray[3];
            abi = splitArray[4];
            platform = splitArray[5].substring(0, splitArray[5].indexOf(".whl"));
        }

        return new PypiWheelArtifactCoordinates(distribution, version, build, languageImplementationVersion, abi, platform);
    }   
}
