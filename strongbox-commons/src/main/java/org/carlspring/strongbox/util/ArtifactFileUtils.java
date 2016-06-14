package org.carlspring.strongbox.util;

/**
 * @author mtodorov
 */
public class ArtifactFileUtils
{


    private ArtifactFileUtils() 
    {
    }

    public static boolean isArtifactFile(String path)
    {
        return !isMetadataFile(path) && !isChecksum(path);
    }

    public static boolean isMetadataFile(String path)
    {
        return path.contains("/maven-metadata.");
    }

    public static boolean isChecksum(String path)
    {
        return path.endsWith(".md5") || path.endsWith(".sha1");
    }

}
